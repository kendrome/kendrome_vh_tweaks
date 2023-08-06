var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

// function initializeCoreMod() {
//     return {
//         'patch_vault_item_tooltip': {
//             'target': {
//                 'type': 'METHOD',
//                 'class': 'iskallia.vault.gear.tooltip.VaultGearTooltipItem',
//                 'methodName': 'createTooltip',
//                 'methodDesc': '(Lnet/minecraft/world/item/ItemStack;Liskallia/vault/gear/tooltip/GearTooltip;)Ljava/util/List'
//             },
//             'transformer': function(method) {
//                 ASMAPI.log('INFO', 'Patching VaultGearTooltipItem');
//
//                 var insertion = null;
//                 var instructions = method.instructions;
//                 var i;
//                 for (i = instructions.size() -1; i > 0; i--) {
//                     var instruction = instructions.get(i);
//                     if (instruction.getOpcode() == Opcodes.RETURN) {
//                         insertion = instruction;
//                     }
//                 }
//
//                 var insn = new InsnList();
//                 insn.add(new InsnNode(Opcodes.POP));
//                 insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
//                 insn.add(new VarInsnNode(Opcodes.ALOAD, 4));
//                 insn.add(ASMAPI.buildMethodCall(
//                     "com/kendrome/kendrome_vh_tweaks/GearComparison",
//                     "ShowComparison",
//                     "(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;)V",
//                     ASMAPI.MethodType.STATIC));
//                 instructions.insertBefore(insertion, insn);
//
//                 return method;
//             }
//         }
//     }
// }

function initializeCoreMod() {
    return {
        'patch_vault_item_tooltip': {
            'target': {
                'type': 'CLASS',
                'name': 'iskallia.vault.gear.tooltip.VaultGearTooltipItem',
            },
            'transformer': function(classNode) {
                ASMAPI.log('INFO', 'Attempting to patch VaultGearTooltipItem...');

                var methodNode = null;
                for (var m = 0; m < classNode.methods.size(); ++m) {
                    var mn = classNode.methods.get(m);
                    if (mn.name == 'createTooltip' && mn.desc == '(Lnet/minecraft/world/item/ItemStack;Liskallia/vault/gear/tooltip/GearTooltip;)Ljava/util/List;') {
                        methodNode = mn;
                        break;
                    }
                }

                if (methodNode == null) {
                    ASMAPI.log('ERROR', 'Failed to find VaultGearTooltipItem#createTooltip');
                    throw new Error('Failed to find VaultGearTooltipItem#createTooltip method?!');
                }

                ASMAPI.log('INFO', 'Successfully found VaultGearTooltipItem#createTooltip');

                var insnNode = null;
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

                var insnList = new InsnList();
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 4));
                insnList.add(ASMAPI.buildMethodCall(
                    "com/kendrome/kendrome_vh_tweaks/GearComparison",
                    "ShowComparison",
                    "(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;)V",
                    ASMAPI.MethodType.STATIC));
                instructions.insertBefore(aload, insnList);

                return classNode;
            }
        }
    }
}