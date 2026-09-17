package aaces.aploader.transformers;

import aaces.aploader.APAgent;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class EvolveProgressUiTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (!"evolutionUi/EvolveProgressUi".equals(className)) {
            return null;
        }

        APAgent.log("Transforming EvolveProgressUi");

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


                if ("updateButton".equals(name) && "(Z)V".equals(descriptor)) {

                    APAgent.log("Found EvolveProgressUi updateButton");

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isIntf) {

                            if ("getDpCount".equals(name) && "()I".equals(desc)) {

                                APAgent.log("Replacing getDpCount call with APBridge.getDpCountForEvolution()I");

                                // remove the instance of sessionStats/Stats from the stack
                                super.visitInsn(Opcodes.POP);

                                // call the other method
                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "getDpCountForEvolution",
                                        "()I",
                                        false
                                );
                            } else if("getPrice".equals(name) && "()I".equals(desc)) {
                                APAgent.log("Replacing getPrice call in updateButton with APBridge.getEvolutionPrice(Lcomponents/InformationComponent$InformationCompBlueprint;)I");

                                // call the other method
                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "getEvolutionPrice",
                                        "(Lcomponents/InformationComponent$InformationCompBlueprint;)I",
                                        false
                                );
                            } else {
                                // Keep all other method calls untouched
                                super.visitMethodInsn(opcode, owner, name, desc, isIntf);
                            }
                        }
                    };
                }
                if ("addStartButton".equals(name) && "(F)V".equals(descriptor)) {

                    APAgent.log("Found EvolveProgressUi addStartButton"); // I don't actually know what this one is used for?

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isIntf) {

                            if ("getPrice".equals(name) && "()I".equals(desc)) {

                                APAgent.log("Replacing getPrice call in addStartButton with APBridge.getEvolutionPrice(Lcomponents/InformationComponent$InformationCompBlueprint;)I");

                                // call the other method
                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "getEvolutionPrice",
                                        "(Lcomponents/InformationComponent$InformationCompBlueprint;)I",
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