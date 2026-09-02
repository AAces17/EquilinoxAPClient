package aaces.aploader.transformers;

import aaces.aploader.APAgent;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class SavesTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) {

        if (!"saves/Saves".equals(className)) {
            return null;
        }

        APAgent.log("Transforming Saves");

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

                if ("<clinit>".equals(name)) {

                    APAgent.log(
                            "Found  Saves initialization"
                    );

                    return new MethodVisitor(
                            Opcodes.ASM9,
                            mv
                    ) {

                        @Override
                        public void visitLdcInsn(Object value) {

                            // Change the folder/file name format that the game searches for.
                            // This should stop the mod from impacting existing save files, and it should stop
                            // the game from trying to load an archipelago save when the mod isn't loaded. (which
                            // would probably cause problems that I don't even want to try and predict, lol)
                            if (value instanceof String){
                                String strValue = (String) value;
                                if("Equilinox_0_Saves".equals(strValue)){
                                    value = "Equilinox_AP_Saves";
                                } else if ("Equilinox_0".equals(strValue)){
                                    value = "Equilinox_AP";
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