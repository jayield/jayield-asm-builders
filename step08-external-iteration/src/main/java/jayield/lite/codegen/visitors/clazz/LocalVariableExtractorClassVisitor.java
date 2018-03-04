package jayield.lite.codegen.visitors.clazz;

import jayield.lite.codegen.visitors.method.LocalVariable;
import jayield.lite.codegen.visitors.method.LocalVariableExtractorMethodVisitor;
import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.invoke.SerializedLambda;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalVariableExtractorClassVisitor extends ClassVisitor implements Opcodes {

    private final String finalName;
    private final List<String> ramifications;
    private Map<String, LocalVariableExtractorMethodVisitor> mvMap;

    public LocalVariableExtractorClassVisitor(ClassVisitor cv, String finalName, List<String> ramifications) {
        super(ASM6, cv);
        this.finalName = finalName;
        this.ramifications = ramifications;
        mvMap = new HashMap<>();
    }

    public Map<Integer, LocalVariable> getLocalVariables(String name) {
        return this.mvMap.get(name).getLocalVariables();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, finalName + "LocalVariableExtractor", signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (!name.equals(finalName) && !ramifications.contains(name)) {
            return null;
        }
        LocalVariableExtractorMethodVisitor mv = new LocalVariableExtractorMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions));
        this.mvMap.put(name, mv);
        return mv;
    }

    public static  Map<String, Map<Integer, LocalVariable>> getLocalVariables(SerializedLambda traversable, List<String> ramifications) {
        try {
            ClassReader reader = new ClassReader(traversable.getImplClass());
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            LocalVariableExtractorClassVisitor visitor = new LocalVariableExtractorClassVisitor(writer, traversable.getImplMethodName(), ramifications);
            reader.accept(visitor, 0);

            HashMap<String, Map<Integer, LocalVariable>> localVariablesMap = new HashMap<>();
            localVariablesMap.put(traversable.getImplMethodName(), visitor.getLocalVariables(traversable.getImplMethodName()));
            ramifications.forEach(ramification -> localVariablesMap.put(ramification, visitor.getLocalVariables(ramification)));
            return localVariablesMap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
