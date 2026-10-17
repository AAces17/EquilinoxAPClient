package aaces.aploader.transformers;

import aaces.aploader.APAgent;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class MainAppTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) {

        if (!"main/MainApp".equals(className)) {
            return null;
        }

        APAgent.log("Transforming MainApp");

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

                if ("main".equals(name) && "([Ljava/lang/String;)V".equals(descriptor)) {

                    APAgent.log(
                            "Found main"
                    );

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitLdcInsn(Object value) {

                            if (value instanceof String){
                                String strValue = (String) value;
                                if("An error has caused the program to crash, sorry for the inconvenience! Please email the dev at thinmatrix@gmail.com and copy-paste the error message below:".equals(strValue)){
                                    APAgent.log("Found error message, replacing.");
                                    value = "An error has caused the program to crash, sorry for the inconvenience! Please post about this in the EquilinoxAP thread in the AP Discord server. Please include the following error message, and the APEqLog.log file, if you have it.";
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
