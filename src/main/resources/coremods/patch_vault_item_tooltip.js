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

                // Create a new InsnList to hold the instructions we want to insert
                var insnList = new InsnList();

                // Add instructions to the InsnList to load onto the stack
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 1)); // Loads the first argument (ItemStack)
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 4)); // Loads the fourth argument (GearTooltip)

                // Add the method call to the GearComparison.ShowComparison() method
                insnList.add(ASMAPI.buildMethodCall(
                    "com/kendrome/kendrome_vh_tweaks/GearComparison",
                    "ShowComparison",
                    "(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;)V",
                    ASMAPI.MethodType.STATIC));

                // Insert the new instructions before the ALOAD instruction
                instructions.insertBefore(aload, insnList);

                // Return the modified classNode
                return classNode;
            }
        }
    }
}