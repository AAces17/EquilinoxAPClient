package aaces.aploader.transformers;

import aaces.aploader.APAgent;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class ChildStateSubClassTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (!("evolutionUi/ChildState$1".equals(className) || "evolutionUi/ChildState$2".equals(className))) {
            return null;
        }

        APAgent.log("Transforming ChildState$1 and ChildState$2");

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


                if ("getStatusString".equals(name)
                        && "(Lcomponents/InformationComponent$InformationCompBlueprint;I)Ljava/lang/String;".equals(descriptor)) {

                    APAgent.log("Found ChildState$1/2 getStatusString");

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isIntf) {

                            if ("getPrice".equals(name) && "()I".equals(desc)) {

                                APAgent.log("Replacing getPrice call with APBridge.getEvolutionPrice(Lcomponents/InformationComponent$InformationCompBlueprint;)I");

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
