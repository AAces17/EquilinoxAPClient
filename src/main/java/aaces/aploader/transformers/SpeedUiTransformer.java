package aaces.aploader.transformers;

import aaces.aploader.APAgent;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class SpeedUiTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (!"bottomBar/SpeedUi".equals(className)) {
            return null;
        }

        APAgent.log("Transforming SpeedUi");

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

                if ("init".equals(name) && "()V".equals(descriptor)) {

                    APAgent.log("Modifying SpeedUi.init() method");

                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isIntf) {

                            if (name.equals("addButton") && "()V".equals(desc)) {

                                APAgent.log("Replacing call to SpeedUi.addButton with APBridge.addSpeedToggleButton(this)");

                                // the stack should already have "this", since the call is this.addButton();

                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "addSpeedToggleButton",
                                        "(LbottomBar/SpeedUi;)V",
                                        false
                                );
                            } else {
                                // Keep all other instructions exactly the same
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

