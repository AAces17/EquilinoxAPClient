package aaces.aploader.transformers;

import aaces.aploader.APAgent;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class GameMenuGuiTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (!"gameMenu/GameMenuGui".equals(className)) {
            return null;
        }

        APAgent.log("Transforming GameMenuGui");

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


                if ("<init>".equals(name) && "(LgameMenu/GameMenuBackground;)V".equals(descriptor)) {

                    APAgent.log("Found GameMenuGui constructor");

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isIntf) {

                            if ("addVersion".equals(name) && "()V".equals(desc)) {

                                APAgent.log("Replacing GameMenuGui.addVersion() call with APBridge.addVersionText(this)");

                                //load 'this' to the stack
                                super.visitVarInsn(Opcodes.ALOAD, 0);

                                // call the other method
                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "addVersionText",
                                        "(LgameMenu/GameMenuGui;)V",
                                        false
                                );
                            } else {
                                // Keep all other method calls untouched
                                super.visitMethodInsn(opcode, owner, name, desc, isIntf);
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
