package aaces.aploader.transformers;

import aaces.aploader.APAgent;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class TaskInfoFrameUiTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (!"taskUi/TaskInfoFrameUi".equals(className)) {
            return null;
        }

        APAgent.log("Transforming TaskInfoFrameUi");

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

                    APAgent.log("Modifying TaskInfoFrameUi.init() method");

                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isIntf) {

                            if (name.equals("addRewardsPanel") && "()V".equals(desc)) {

                                APAgent.log("Intercepted addRewardsPanel() -> Replacing with APBridge.addRewardsPanel(this)");

                                // the stack should already have "this", since the call is this.addRewardsPanel();

                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "rewardsPanelIntercept",
                                        "(LtaskUi/TaskInfoFrameUi;)V",
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
