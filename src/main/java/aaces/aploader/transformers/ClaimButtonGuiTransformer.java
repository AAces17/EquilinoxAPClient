package aaces.aploader.transformers;

import aaces.aploader.APAgent;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class ClaimButtonGuiTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (!"taskUi/ClaimButtonGui".equals(className)) {
            return null;
        }

        APAgent.log("Transforming ClaimButtonGui");

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

                // Target the private void complete() method inside ClaimButtonGui
                if ("complete".equals(name) && "()V".equals(descriptor)) {

                    APAgent.log("Modifying ClaimButtonGui.complete() method");

                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isIntf) {

                            // Match the task.complete() method invocation
                            // We check if the method name is "complete" and its owner is the Task class
                            if (name.equals("complete") && "()V".equals(desc) && owner.contains("Task")) {

                                APAgent.log("Intercepted task.complete() -> Replacing with APBridge.onTaskComplete(task)");

                                // STACK MANAGEMENT:
                                // Right before this original INVOKEVIRTUAL instruction, the stack already contains
                                // the 'Task' object instance (loaded by the preceding GETFIELD instruction).
                                // Instead of calling the original complete() method which consumes the Task object,
                                // we pass that exact Task object reference directly into our static method.

                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "onTaskComplete",
                                        "(Ltasks/Task;)V",
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
