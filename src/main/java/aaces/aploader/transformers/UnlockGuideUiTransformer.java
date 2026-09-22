package aaces.aploader.transformers;

import aaces.aploader.APAgent;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class UnlockGuideUiTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) {

        if (!"unlockGuide/UnlockGuideUi".equals(className)) {
            return null;
        }

        APAgent.log("Transforming UnlockGuideUi");

        ClassReader reader = new ClassReader(classfileBuffer);

        ClassWriter writer = new ClassWriter(
                reader,
                ClassWriter.COMPUTE_MAXS
        );

        ClassVisitor visitor = new ClassVisitor(
                Opcodes.ASM9,
                writer
        ) {

            @Override
            public MethodVisitor visitMethod(
                    int access,
                    String name,
                    String descriptor,
                    String signature,
                    String[] exceptions) {

                MethodVisitor mv = super.visitMethod(
                        access,
                        name,
                        descriptor,
                        signature,
                        exceptions
                );

                if ("openUnlockGuidePanel".equals(name)
                        && "(Lshopping/BlueprintShopItem;)V".equals(descriptor)) {

                    APAgent.log(
                            "Found UnlockGuideUi.openUnlockGuidePanel()"
                    );

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitInsn(int opcode) {

                            // Insert immediately before RETURN
                            if (opcode == Opcodes.RETURN) {

                                // Load 'extraInfoGui' onto the stack
                                // (the first locally defined variable, living at slot 1)
                                super.visitVarInsn(Opcodes.ALOAD, 1);

                                // Load 'item' onto the stack
                                // (stored in slot 0 bc it is the first argument to a static method)
                                super.visitVarInsn(Opcodes.ALOAD, 0);

                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "reactToLockedClick",
                                        "(LextraInfoGui/ExtraInfoGui;Lshopping/BlueprintShopItem;)V",
                                        false
                                );
                            }

                            super.visitInsn(opcode);
                        }
                    };
                }

                return mv;
            }
        };

        reader.accept(visitor, 0);

        return writer.toByteArray();
    }
}
