package org.jayield.builders;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * This visitor enhances the visited class with the DummyInterface.
 * It admits that class already has the bar() method required by that interface.
 *
 *
 * @author Miguel Gamboa
 *         created on 17-11-2017
 */
public class EnhanceClassVisitor extends ClassVisitor implements Opcodes{
    final String targetClassname;

    public EnhanceClassVisitor(String targetClassname, ClassWriter cw) {
        super(ASM6, cw);
        this.targetClassname = targetClassname;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // Change the name of the class and add DummyInterface
        super.visit(
                version,
                access,
                targetClassname,
                signature,
                superName,
                new String[] {"DummyInterface"});
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor sourceMethod = super.visitMethod(access, name, desc, signature, exceptions);
        if(name.equals("bar") == false)
            return sourceMethod;
        return new MethodVisitor(ASM6, sourceMethod) {
            @Override
            public void visitCode() {
                mv.visitVarInsn(ILOAD, 1);
                mv.visitVarInsn(ILOAD, 2);
                mv.visitInsn(IMUL);
                mv.visitVarInsn(ISTORE, 3);
                mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                mv.visitLdcInsn("%d times %d = %d");
                mv.visitInsn(ICONST_3);
                mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
                mv.visitInsn(DUP);
                mv.visitInsn(ICONST_0);
                mv.visitVarInsn(ILOAD, 1);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                mv.visitInsn(AASTORE);
                mv.visitInsn(DUP);
                mv.visitInsn(ICONST_1);
                mv.visitVarInsn(ILOAD, 2);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                mv.visitInsn(AASTORE);
                mv.visitInsn(DUP);
                mv.visitInsn(ICONST_2);
                mv.visitVarInsn(ILOAD, 3);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                mv.visitInsn(AASTORE);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "printf", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/io/PrintStream;", false);
                mv.visitInsn(POP);
                super.visitCode();
            }
        };
    }
}
