package aaces.aploader.transformers;

import aaces.aploader.APAgent;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class GameMenuBackgroundTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) {

        if (!"gameMenu/GameMenuBackground".equals(className)) {
            return null;
        }

        APAgent.log("[AP] Transforming GameMenuBackground");

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

                if ("quit".equals(name)
                        && "()V".equals(descriptor)) {

                    APAgent.log(
                            "[AP] Found GameMenuBackground.quit()"
                    );

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitInsn(int opcode) {

                            // Insert immediately before RETURN
                            if (opcode == Opcodes.RETURN) {
                                mv.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "onApplicationQuit",
                                        "()V",
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
