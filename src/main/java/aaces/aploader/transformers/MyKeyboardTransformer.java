package aaces.aploader.transformers;

import aaces.aploader.APAgent;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class MyKeyboardTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (!"toolbox/MyKeyboard".equals(className)) {
            return null;
        }

        APAgent.log("Transforming MyKeyboard");

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

                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

                // Target private int getValidAsciiValue(char) method to allow special characters
                if ("getValidAsciiValue".equals(name) && "(C)I".equals(descriptor)) {

                    APAgent.log("Modifying getValidAsciiValue(char) to directly return 'c'");

                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitCode() {
                            super.visitCode();

                            // Load the 'char' argument onto the stack.
                            // Slot 0 is 'this', Slot 1 is the first argument 'c'.
                            super.visitVarInsn(Opcodes.ILOAD, 1);

                            // Return the int/char value from the top of the stack
                            super.visitInsn(Opcodes.IRETURN);
                        }

                        // All of these overrides prevent the original method from executing (some of them may not
                        // be necessary tbh)
                        @Override
                        public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
                        }

                        @Override
                        public void visitLineNumber(int line, org.objectweb.asm.Label start) {
                        }

                        @Override
                        public void visitLocalVariable(String name, String descriptor, String signature, org.objectweb.asm.Label start, org.objectweb.asm.Label end, int index) {
                        }

                        @Override
                        public void visitParameter(String name, int access) {
                        }

                        @Override
                        public void visitLabel(org.objectweb.asm.Label label) {
                        }

                        @Override
                        public void visitInsn(int opcode) {
                        }

                        @Override
                        public void visitVarInsn(int opcode, int var) {
                        }

                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isIntf) {
                        }

                        @Override
                        public void visitJumpInsn(int opcode, org.objectweb.asm.Label label) {
                        }

                        @Override
                        public void visitMaxs(int maxStack, int maxLocals) {
                            // Explicitly define the new strict stack limits for our custom body.
                            // We need 1 slot on the stack (for 'c') and 2 local variables (slot 0='this', slot 1='c').
                            super.visitMaxs(1, 2);
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
