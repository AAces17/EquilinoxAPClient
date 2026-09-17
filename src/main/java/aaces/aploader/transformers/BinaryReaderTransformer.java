package aaces.aploader.transformers;

import aaces.aploader.APAgent;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class BinaryReaderTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (!"utils/BinaryReader".equals(className)) {
            return null;
        }

        APAgent.log("Transforming BinaryReader");

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


                if ("readInt".equals(name)) {

                    APAgent.log("Found BinaryReader readInt");

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {


                        @Override
                        public void visitInsn(int opcode) {

                            // Insert immediately before RETURN
                            if (opcode == Opcodes.IRETURN) {

                                super.visitInsn(Opcodes.DUP);

                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "printRead",
                                        "(I)V",
                                        false
                                );
                            }


                            super.visitInsn(opcode);
                        }

                    };
                }
                if ("readFloat".equals(name)) {

                    APAgent.log("Found BinaryReader readFloat");

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {


                        @Override
                        public void visitInsn(int opcode) {

                            // Insert immediately before RETURN
                            if (opcode == Opcodes.FRETURN) {

                                super.visitInsn(Opcodes.DUP);

                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "printRead",
                                        "(F)V",
                                        false
                                );
                            }


                            super.visitInsn(opcode);
                        }

                    };
                }
                if ("readBoolean".equals(name)) {

                    APAgent.log("Found BinaryReader readBoolean");

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {


                        @Override
                        public void visitInsn(int opcode) {

                            // Insert immediately before RETURN
                            if (opcode == Opcodes.IRETURN) {

                                super.visitInsn(Opcodes.DUP);

                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "printRead",
                                        "(Z)V",
                                        false
                                );
                            }


                            super.visitInsn(opcode);
                        }

                    };
                }
                if ("readString".equals(name)) {

                    APAgent.log("Found BinaryReader readString");

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {


                        @Override
                        public void visitInsn(int opcode) {

                            // Insert immediately before RETURN
                            if (opcode == Opcodes.ARETURN) {

                                super.visitInsn(Opcodes.DUP);

                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "printRead",
                                        "(Ljava/lang/String;)V",
                                        false
                                );
                            }


                            super.visitInsn(opcode);
                        }

                    };
                }
                if ("readLong".equals(name)) {

                    APAgent.log("Found BinaryReader readLong");

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {


                        @Override
                        public void visitInsn(int opcode) {

                            // Insert immediately before RETURN
                            if (opcode == Opcodes.LRETURN) {

                                super.visitInsn(Opcodes.DUP2);

                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "printRead",
                                        "(J)V",
                                        false
                                );
                            }


                            super.visitInsn(opcode);
                        }

                    };
                }
                if ("readShort".equals(name)) {

                    APAgent.log("Found BinaryReader readShort");

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {


                        @Override
                        public void visitInsn(int opcode) {

                            // Insert immediately before RETURN
                            if (opcode == Opcodes.IRETURN) {

                                super.visitInsn(Opcodes.DUP);

                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "printRead",
                                        "(S)V",
                                        false
                                );
                            }


                            super.visitInsn(opcode);
                        }

                    };
                }
                if ("readVector".equals(name)) {

                    APAgent.log("Found BinaryReader readVector");

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {


                        @Override
                        public void visitInsn(int opcode) {

                            // Insert immediately before RETURN
                            if (opcode == Opcodes.ARETURN) {

                                super.visitInsn(Opcodes.DUP);

                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "printRead",
                                        "(Lorg/lwjgl/util/vector/Vector3f;)V",
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
