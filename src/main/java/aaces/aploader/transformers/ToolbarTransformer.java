package aaces.aploader.transformers;

import aaces.aploader.APAgent;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class ToolbarTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) {

        if (!"toolbar/Toolbar".equals(className)) {
            return null;
        }

        APAgent.log("Transforming Toolbar");

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

                if ("addButtons".equals(name)
                        && "()V".equals(descriptor)) {

                    APAgent.log(
                            "Found Toolbar.addButtons()"
                    );

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitInsn(int opcode) {

                            // Insert immediately before RETURN
                            if (opcode == Opcodes.RETURN) {

                                // Load 'this' (Toolbar) onto the stack
                                super.visitVarInsn(Opcodes.ALOAD, 0);

                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "addAPToolbarButton",
                                        "(Ltoolbar/Toolbar;)V",
                                        false
                                );
                            }

                            super.visitInsn(opcode);
                        }
                    };
                }
                if ("<init>".equals(name)) {

                    APAgent.log(
                            "Found Toolbar initialization"
                    );

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitLdcInsn(Object value) {
                            if (value instanceof Float){
                                float fValue = (Float) value;
                                if(fValue == 645.0F){
                                    value = 645F + 36.7F;
                                }
                            }
                            super.visitLdcInsn(value);

                        }
                    };
                }
                if ("addCounters".equals(name)) {

                    APAgent.log(
                            "Found Toolbar addCounters"
                    );

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitLdcInsn(Object value) {
                            if (value instanceof Float){
                                float fValue = (Float) value;
                                if(Math.abs(fValue - 0.023255814F) < 0.01F){
                                    value = fValue * 645F / (645F + 36.7F);
                                } else if (Math.abs(fValue - 0.28325582) < 0.01F){
                                    value = fValue * 645F / (645F + 36.7F);
                                }
                            }
                            super.visitLdcInsn(value);

                        }
                    };
                }
                if ("addPlantShopButton".equals(name)) {

                    APAgent.log(
                            "Found Toolbar addPlantShopButton"
                    );

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitLdcInsn(Object value) {
                            if (value instanceof Float){
                                float fValue = (Float) value;
                                APAgent.log("Found plant float " + fValue);
                                if(Math.abs(fValue - 0.5432558F) < 0.1F){
                                    value = fValue * 645F / (645F + 36.7F);
                                }
                            }
                            super.visitLdcInsn(value);

                        }
                    };
                }
                if ("addAnimalShopButton".equals(name)) {

                    APAgent.log(
                            "Found Toolbar addAnimalShopButton"
                    );

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitLdcInsn(Object value) {
                            if (value instanceof Float){
                                float fValue = (Float) value;
                                APAgent.log("Found animal float " + fValue);
                                if(Math.abs(fValue - 0.60015506F) < 0.1F){
                                    value = fValue * 645F / (645F + 36.7F);
                                }
                            }
                            super.visitLdcInsn(value);

                        }
                    };
                }
                if ("addTasksButton".equals(name)) {

                    APAgent.log(
                            "Found Toolbar addTasksButton"
                    );

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitLdcInsn(Object value) {
                            if (value instanceof Float){
                                float fValue = (Float) value;
                                if(Math.abs(fValue - 0.6570543F) < 0.01F){
                                    value = fValue * 645F / (645F + 36.7F);
                                }
                            }
                            super.visitLdcInsn(value);

                        }
                    };
                }
                if ("addBiomeButton".equals(name)) {

                    APAgent.log(
                            "Found Toolbar addBiomeButton"
                    );

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitLdcInsn(Object value) {
                            if (value instanceof Float){
                                float fValue = (Float) value;
                                if(Math.abs(fValue - 0.8862017F) < 0.01F){
                                    value = fValue * 645F / (645F + 36.7F);
                                }
                            }
                            super.visitLdcInsn(value);

                        }
                    };
                }
                if ("addEraserButton".equals(name)) {

                    APAgent.log(
                            "Found Toolbar addEraserButton"
                    );

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitLdcInsn(Object value) {
                            if (value instanceof Float){
                                float fValue = (Float) value;
                                if(Math.abs(fValue - 0.9431009F) < 0.01F){
                                    value = fValue * 645F / (645F + 36.7F);
                                }
                            }
                            super.visitLdcInsn(value);

                        }
                    };
                }
                if ("addMusicButton".equals(name)) {

                    APAgent.log(
                            "Found Toolbar addMusicButton"
                    );

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitLdcInsn(Object value) {
                            if (value instanceof Float){
                                float fValue = (Float) value;
                                APAgent.log("Found music float " + fValue);
                                if(Math.abs(fValue - 0.7708528F) < 0.1F){
                                    value = fValue * 645F / (645F + 36.7F);
                                }
                            }
                            super.visitLdcInsn(value);

                        }
                    };
                }
                if ("addHelpButton".equals(name)) {

                    APAgent.log(
                            "Found Toolbar addHelpButton"
                    );

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitLdcInsn(Object value) {
                            if (value instanceof Float){
                                float fValue = (Float) value;
                                if(Math.abs(fValue - 0.82775205F) < 0.01F){
                                    value = fValue * 645F / (645F + 36.7F);
                                }
                            }
                            super.visitLdcInsn(value);

                        }
                    };
                }
                if ("addCheckListButton".equals(name)) {

                    APAgent.log(
                            "Found Toolbar addCheckListButton"
                    );

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitLdcInsn(Object value) {
                            if (value instanceof Float){
                                float fValue = (Float) value;
                                if(Math.abs(fValue - 0.71395355F) < 0.01F){
                                    value = fValue * 645F / (645F + 36.7F);
                                }
                            }
                            super.visitLdcInsn(value);

                        }
                    };
                }
                if ("updateGuiTexturePositions".equals(name)) {

                    APAgent.log(
                            "Found Toolbar updateGuiTexturePositions"
                    );

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitLdcInsn(Object value) {
                            if (value instanceof Float){
                                float fValue = (Float) value;
                                if(Math.abs(fValue - 0.023255814F) < 0.05F){
                                    value = fValue * 645F / (645F + 36.7F);
                                }
                                else if(Math.abs(fValue - 0.28325582F) < 0.05F){
                                    value = fValue * 645F / (645F + 36.7F);
                                }
                                else if(Math.abs(fValue - 0.7124032F) < 0.05F){
                                    value = fValue * 645F / (645F + 36.7F);
                                }
                                else if(Math.abs(fValue - 0.5417054F) < 0.05F){
                                    value = fValue * 645F / (645F + 36.7F);
                                }
                                else if(Math.abs(fValue - 0.8846513F) < 0.05F){
                                    value = fValue * 645F / (645F + 36.7F);
                                }
                            }
                            super.visitLdcInsn(value);

                        }
                    };
                }
                if ("reduce".equals(name)) {

                    APAgent.log(
                            "Found Toolbar reduce"
                    );

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitLdcInsn(Object value) {
                            if (value instanceof Float){
                                float fValue = (Float) value;
                                if(Math.abs(fValue - 0.71395355F) < 0.05F){
                                    value = fValue * 645F / (645F + 36.7F);
                                }
                                else if(Math.abs(fValue - 0.7708528F) < 0.05F){
                                    value = fValue * 645F / (645F + 36.7F);
                                }
                                else if(Math.abs(fValue - 0.82775205F) < 0.05F){
                                    value = fValue * 645F / (645F + 36.7F);
                                }
                                else if(Math.abs(fValue - 0.5432558F) < 0.05F){
                                    value = fValue * 645F / (645F + 36.7F);
                                }
                                else if(Math.abs(fValue - 0.60015506F) < 0.05F){
                                    value = fValue * 645F / (645F + 36.7F);
                                }
                            }
                            super.visitLdcInsn(value);

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