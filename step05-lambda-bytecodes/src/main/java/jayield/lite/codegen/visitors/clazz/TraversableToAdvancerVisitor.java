package jayield.lite.codegen.visitors.clazz;

import jayield.lite.codegen.LambdaToAdvancerMethodGenerator;
import jayield.lite.codegen.visitors.method.AdvancerMethodVisitor;
import jayield.lite.codegen.visitors.method.InvokeDynamicMethodVisitor;
import jayield.lite.codegen.wrappers.LambdaToAdvancer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.invoke.SerializedLambda;

import static jayield.lite.codegen.GeneratorUtils.classNameToPath;


public class TraversableToAdvancerVisitor extends ClassVisitor implements Opcodes {

    private final String finalName;
    private final String sourceName;
    private final String[] interfaces;
    private final LambdaToAdvancerMethodGenerator advanverMethodGenerator;


    public TraversableToAdvancerVisitor(ClassVisitor cv, String finalName, String sourceName, SerializedLambda source) {
        super(ASM6, cv);
        this.sourceName = sourceName;
        this.cv = cv;
        this.finalName = finalName;
        this.interfaces = new String [] {classNameToPath(LambdaToAdvancer.class)};
        this.advanverMethodGenerator = new LambdaToAdvancerMethodGenerator(source,this,finalName);

    }

    // Change class Name
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, finalName, signature, superName, this.interfaces);
        advanverMethodGenerator.generateGetAdvancerMethod();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (!name.equals(finalName))
            return new InvokeDynamicMethodVisitor(
                    super.visitMethod(access, name, desc, signature, exceptions),
                    sourceName,
                    finalName);
        return new AdvancerMethodVisitor(super.visitMethod(ACC_PUBLIC + ACC_STATIC,
                name,
                desc.replace(";)V", ";)Z"),
                signature,
                exceptions),
                sourceName,
                finalName);
    }

}

