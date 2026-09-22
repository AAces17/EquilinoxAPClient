package aaces.aploader.transformers;

import aaces.aploader.APAgent;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class TextureManagerTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (!"textures/TextureManager".equals(className)) {
            return null;
        }

        APAgent.log("Transforming TextureManager");

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

                if ("decodeTextureFile".equals(name) && "(Lutils/MyFile;)Ltextures/TextureData;".equals(descriptor)) {

                    APAgent.log("Modifying TextureManager.decodeTextureFile() method");

                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isIntf) {

                            if (name.equals("getInputStream")) {

                                APAgent.log("Intercepted file.getInputStream() -> Replacing with APBridge.getFileInputStream(MyFile file)");

                                // the stack should already have "file", since the call is file.getInputStream();

                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "aaces/aploader/APBridge",
                                        "getFileInputStream",
                                        "(Lutils/MyFile;)Ljava/io/InputStream;",
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