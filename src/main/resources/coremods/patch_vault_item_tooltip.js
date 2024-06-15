var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

function initializeCoreMod() {
    return {
        'patch_vault_item_tooltip': {
            'target': {
                'type': 'CLASS',
                'name': 'iskallia.vault.gear.tooltip.VaultGearTooltipItem',
            },
            'transformer': function(classNode) {
                ASMAPI.log('INFO', 'Attempting to patch VaultGearTooltipItem...');

                // The method that represents the createTooltip method
                var methodNode = null;

                // Find the createTooltip method with the correct name and descriptor
                for (var m = 0; m < classNode.methods.size(); ++m) {
                    var mn = classNode.methods.get(m);
                    if (mn.name == 'createTooltip' && mn.desc == '(Lnet/minecraft/world/item/ItemStack;Liskallia/vault/gear/tooltip/GearTooltip;)Ljava/util/List;') {
                        methodNode = mn;
                        break;
                    }
                }

                if (methodNode == null) {
                    ASMAPI.log('ERROR', 'Failed to find VaultGearTooltipItem#createTooltip');
                    throw new Error('Failed to find VaultGearTooltipItem#createTooltip?!');
                }
                ASMAPI.log('INFO', 'Successfully found VaultGearTooltipItem#createTooltip');

                var insnNode;
                var instructions = methodNode.instructions;
                var returnInstructionIndex;

                for (returnInstructionIndex = instructions.size() - 1; returnInstructionIndex > 0; returnInstructionIndex--) {
                    var instruction = instructions.get(returnInstructionIndex);
                    if (instruction.getOpcode() == Opcodes.ARETURN) {
                        insnNode = instruction;
                        break;
                    }
                }

                if (insnNode == null) {
                    ASMAPI.log('ERROR', 'Failed to find VaultGearTooltipItem#createTooltip return');
                    throw new Error('Failed to find VaultGearTooltipItem#createTooltip return?!');
                }
                ASMAPI.log('INFO', 'Successfully found VaultGearTooltipItem#createTooltip ARETURN at index ' + returnInstructionIndex);

                var aloadInstructionIndex = returnInstructionIndex - 1;
                var aload = instructions.get(aloadInstructionIndex);
                if(aload.getOpcode() != Opcodes.ALOAD) {
                    ASMAPI.log('ERROR', 'Failed to find VaultGearTooltipItem#createTooltip ALOAD');
                    throw new Error('Failed to find VaultGearTooltipItem#createTooltip ALOAD?!');
                }
                ASMAPI.log('INFO', 'Successfully found VaultGearTooltipItem#createTooltip ALOAD at index ' + aloadInstructionIndex);



                //Find the variables we need to pass
                var itemStackNode = null, toolTipNode = null;
                for (var p1 in methodNode.localVariables) {
                    var node = methodNode.localVariables[p1];
                    switch(node.name) {
                        case "tooltip":
                            toolTipNode = new VarInsnNode(Opcodes.ALOAD, node.index);
                            break;
                        case "stack":
                            itemStackNode = new VarInsnNode(Opcodes.ALOAD, node.index);
                            break;
                    }
                    ///Debug info
                    //ASMAPI.log('INFO','p1: ' + node.index + " " + node.name + ' ' + node.desc + " " + node.signature);
                }

                if(itemStackNode === null) {
                    ASMAPI.log('ERROR', 'Failed to find VaultGearTooltipItem#createTooltip itemStack variable');
                    return classNode;
                }
                if(toolTipNode === null) {
                    ASMAPI.log('ERROR', 'Failed to find VaultGearTooltipItem#createTooltip toolTip variable');
                    return classNode;
                }

                // Create a new InsnList to hold the instructions we want to insert
                var insnList = new InsnList();
                insnList.add(itemStackNode);
                insnList.add(toolTipNode);



                ASMAPI.log('INFO', '')

                // Add the method call to the KendromeVhTweaks#tweakTooltips() method
                insnList.add(ASMAPI.buildMethodCall(
                    "com/kendrome/kendrome_vh_tweaks/KendromeVhTweaks",
                    "tweakTooltips",
                    "(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;)V",
                    ASMAPI.MethodType.STATIC));

                // Insert the new instructions before the ALOAD instruction
                instructions.insertBefore(aload, insnList);

                ASMAPI.log('INFO', 'Successfully patched VaultGearTooltipItem#createTooltip');

                // Return the modified classNode
                return classNode;
            }
        }
    }
}