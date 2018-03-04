package jayield.lite.codegen.visitors.clazz;

import jayield.lite.codegen.visitors.method.RamificationsMethodVisitor;
import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.invoke.SerializedLambda;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class RamificationCalculatorVisitor extends ClassVisitor implements Opcodes {


    private final Map<String, List<String>> ramifications;
    private final SerializedLambda traversable;

    public RamificationCalculatorVisitor(ClassWriter writer, SerializedLambda traversable) {
        super(ASM6, writer);
        this.traversable = traversable;
        this.ramifications = new HashMap<String, List<String>>();
    }

    public static List<String> getRamifications(SerializedLambda traversable) {
        try {
            ClassReader reader = new ClassReader(traversable.getImplClass());
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            RamificationCalculatorVisitor visitor = new RamificationCalculatorVisitor(writer, traversable);
            reader.accept(visitor, 0);

            return visitor.getRamifications(traversable.getImplMethodName());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<String> getRamifications(String name) {
        List<String> ramifications = new ArrayList<>();
        if (this.ramifications.containsKey(name)) {
            List<String> currentRamifications = this.ramifications.get(name);
            ramifications.addAll(currentRamifications);
            ramifications.addAll(currentRamifications.stream()
                    .map(this::getRamifications)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()));
        }
        return ramifications;
    }

    public void addRamification(String name, String ramification) {
        if (this.ramifications.containsKey(name)) {
            this.ramifications.get(name).add(ramification);
        } else {
            List<String> ramifications = new ArrayList<>();
            ramifications.add(ramification);
            this.ramifications.put(name, ramifications);
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new RamificationsMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), this, name, traversable.getCapturingClass());
    }
}
