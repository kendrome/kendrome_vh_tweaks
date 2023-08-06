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
                'class': 'iskallia.vault.gear.tooltip.VaultGearTooltipItem',
            },
            'transformer': function(methodNode) {
                ASMAPI.log('INFO', 'Attemtpting to patch VaultGearTooltipItem...');

                // Apply transformations.

                return methodNode;
            }
        }
    }
}