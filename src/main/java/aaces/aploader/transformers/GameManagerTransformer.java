package aaces.aploader.transformers;

import aaces.aploader.APAgent;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class GameManagerTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) {

        if (!"gameManaging/GameManager".equals(className)) {
            return null;
        }

        APAgent.log("Transforming GameManager");

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

                if ("update".equals(name)
                        && "()V".equals(descriptor)) {

                    APAgent.log(
                            "Found GameManager.update()"
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
                                        "gameManagerUpdate",
                                        "()V",
                                        false
                                );
                            }

                            super.visitInsn(opcode);
                        }
                    };
                }

                if ("init".equals(name)
                        && "()V".equals(descriptor)) {

                    APAgent.log(
                            "Found GameManager.init()"
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

                            // Remove the call to loadInitialSession(), and replace it with our init
                            if (opcode == Opcodes.INVOKEVIRTUAL &&
                                    "session/SessionManager".equals(owner) &&
                                    "loadInitialSession".equals(name) &&
                                    "()V".equals(descriptor)) {



                                mv.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "gameManagerInit",
                                        "(Lsession/SessionManager;)V",
                                        false
                                );

                            } else {
                                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                            }


                        }
                    };
                }

                if ("cleanUp".equals(name)
                        && "()V".equals(descriptor)){

                    APAgent.log(
                            "Found GameManager.cleanUp()"
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