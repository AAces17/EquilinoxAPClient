package aaces.aploader.transformers;

import aaces.aploader.APAgent;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class StatsTransformer  implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (!"sessionStats/Stats".equals(className)) {
            return null;
        }

        APAgent.log("Transforming Stats");

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

                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

                if ("update".equals(name) && "()V".equals(descriptor)) {

                    APAgent.log("Modifying Stats.update() method");

                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isIntf) {

                            if (name.equals("getDpChange") && "()I".equals(desc)) {

                                APAgent.log("Replacing call to getDpChange with APBridge.getDpChange(this)");

                                // the stack should already have "this", since the call is this.getDpChange();

                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "getDpChange",
                                        "(LsessionStats/Stats;)I",
                                        false
                                );
                            } else {
                                // Keep all other instructions exactly the same
                                super.visitMethodInsn(opcode, owner, name, desc, isIntf);
                            }
                        }
                        @Override
                        public void visitInsn(int opcode) {

                            // Insert immediately before RETURN
                            if (opcode == Opcodes.RETURN) {

                                mv.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "updateDpPerMinCounter",
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

