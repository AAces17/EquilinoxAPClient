package aaces.aploader.transformers;

import aaces.aploader.APAgent;
import aaces.aploader.APBridge;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class SessionTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) {

        if (!"session/Session".equals(className)) {
            return null;
        }

        APAgent.log("Transforming Session");

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

                if ("save".equals(name)
                        && "()Z".equals(descriptor)) {

                    APAgent.log(
                            "Found Session.save()"
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

                            // Insert immediately before writer.close()
                            if (opcode == Opcodes.INVOKEVIRTUAL
                                    && "utils/BinaryWriter".equals(owner)
                                    && "close".equals(name)
                                    && "()V".equals(descriptor)) {

                                APAgent.log("Found writer.close(), injecting APSave call");

                                // The stack has [writer] -> DUP turns it into [writer, writer]
                                super.visitInsn(Opcodes.DUP);

                                // Invoke APBridge.saveAPState(writer); this consumes one of the "writer"s on the stack
                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "saveAPState",
                                        "(Lutils/BinaryWriter;)V",
                                        false
                                );
                            }

                            // Continue with the call to writer.close(), which consumes the other writer on the stack
                            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                        }
                    };
                } else if ("loadSession".equals(name)
                        && "(Lsession/Session;)V".equals(descriptor)) {

                    APAgent.log(
                            "Found Session.loadSession(Session session)"
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

                            // Insert immediately before reader.close()
                            if (opcode == Opcodes.INVOKEVIRTUAL
                                    && "utils/BinaryReader".equals(owner)
                                    && "close".equals(name)
                                    && "()V".equals(descriptor)) {

                                APAgent.log("Found reader.close(), injecting APLoad call");

                                // The stack has [reader] -> DUP turns it into [reader, reader]
                                super.visitInsn(Opcodes.DUP);

                                // Invoke APBridge.loadAPState(reader); consumes one of the "reader"s on the stack
                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "loadAPState",
                                        "(Lutils/BinaryReader;)V",
                                        false
                                );
                            }

                            // Continue with the call to reader.close();
                            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                        }
                    };
                } else if ("update".equals(name)
                        && "()V".equals(descriptor)) {

                    APAgent.log(
                            "Found Session.update()"
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
                            //APAgent.log("Inspecting call: " + owner + "." + name + descriptor + " (Opcode: " + opcode + ")");
                            // wrap the call to updateAutoSave with a checker.
                            if ("updateAutoSave".equals(name)
                                    && "()V".equals(descriptor)) {

                                APAgent.log("Found Session.updateAutoSave(), wrapping");

                                // Consume the 'this' off the stack with this call
                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "safeUpdateAutoSave",
                                        "(Lsession/Session;)V",
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

