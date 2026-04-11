package net.mika.mikamods.access;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

public class AccessWidenerTransformer implements IClassTransformer {

    private final AccessWidener aw;

    public AccessWidenerTransformer() {
        this.aw = AccessWidenerHolder.INSTANCE;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;

        String internalName = transformedName.replace('.', '/');
        AccessWidener.ClassAccess cls = aw.classes.get(internalName);

        if (cls == null) return basicClass;

        ClassReader cr = new ClassReader(basicClass);
        ClassWriter cw = new ClassWriter(0);

        ClassVisitor cv = new ClassVisitor(Opcodes.ASM6, cw) {

            @Override
            public void visit(int version, int access, String name, String sig, String superName, String[] interfaces) {

                if (cls.accessible) {
                    access &= ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED);
                    access |= Opcodes.ACC_PUBLIC;
                }

                if (cls.extendable) {
                    access &= ~Opcodes.ACC_FINAL;
                }

                super.visit(version, access, name, sig, superName, interfaces);
            }

            @Override
            public FieldVisitor visitField(int access, String name, String desc, String sig, Object value) {

                AccessWidener.MemberAccess acc = cls.fields.get(name);

                if (acc != null) {
                    if (acc.accessible) {
                        access &= ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED);
                        access |= Opcodes.ACC_PUBLIC;
                    }

                    if (acc.mutable) {
                        access &= ~Opcodes.ACC_FINAL;
                    }
                }

                return super.visitField(access, name, desc, sig, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] exceptions) {

                AccessWidener.MemberAccess acc = cls.methods.get(name + desc);

                if (acc != null && acc.accessible) {
                    access &= ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED);
                    access |= Opcodes.ACC_PUBLIC;
                }

                return super.visitMethod(access, name, desc, sig, exceptions);
            }
        };

        cr.accept(cv, 0);
        return cw.toByteArray();
    }
}