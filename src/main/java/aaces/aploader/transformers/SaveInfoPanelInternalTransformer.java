package aaces.aploader.transformers;

import aaces.aploader.APAgent;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class SaveInfoPanelInternalTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) {

        if (!"loadWorldScreen/SaveInfoPanel$3$1".equals(className)) {
            return null;
        }

        APAgent.log("Transforming SaveInfoPanel$3$1");

        ClassReader reader = new ClassReader(classfileBuffer);

        ClassWriter writer = new ClassWriter(
                reader,
                ClassWriter.COMPUTE_FRAMES
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

                if ("isLoaded".equals(name)
                        && "()Z".equals(descriptor)) {

                    APAgent.log(
                            "Found internal .isLoaded()"
                    );

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitMethodInsn(int opcode,
                                                    String owner,
                                                    String name,
                                                    String descriptor,
                                                    boolean isInterface) {

                            // Stop the auto-save that happens the first time a world is loaded
                            if ("session/SessionManager".equals(owner) &&
                                    "loadSaveSlot".equals(name) &&
                                    "(I)V".equals(descriptor)) {

                                // There should be an instance of SessionManager and an int on the stack
                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "loadSaveSlot",
                                        "(Lsession/SessionManager;I)V",
                                        false
                                );

                            } else {
                                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                            }


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