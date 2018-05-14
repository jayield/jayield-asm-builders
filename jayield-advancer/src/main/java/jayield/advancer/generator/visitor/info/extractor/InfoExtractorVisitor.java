package jayield.advancer.generator.visitor.info.extractor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.lang.invoke.SerializedLambda;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.objectweb.asm.ClassReader.SKIP_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

public class InfoExtractorVisitor extends ClassVisitor implements Opcodes {


    private final Map<String, Info> infoMap;
    private final String owner;

    InfoExtractorVisitor(ClassWriter writer, String owner) {
        super(ASM6, writer);
        this.owner = owner;
        this.infoMap = new HashMap<>();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version,
                    access,
                    format("%s%s%s", owner, this.getClass().getName(), new Date().toInstant().toString()),
                    signature,
                    superName,
                    interfaces);
    }

    public static Map<String, Info> extractInfo(SerializedLambda traversable) {
        try {
            ClassReader reader = new ClassReader(traversable.getImplClass());
            ClassWriter writer = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
            InfoExtractorVisitor visitor = new InfoExtractorVisitor(writer, traversable.getCapturingClass());
            reader.accept(visitor, SKIP_FRAMES);

            return visitor.fillMapWithRelevantInfo(traversable.getImplMethodName(), new HashMap<>());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Info> fillMapWithRelevantInfo(String name, Map<String, Info> map) {
        if (infoMap.containsKey(name) && !map.containsKey(name)) {
            Info info = infoMap.get(name);
            map.put(name, info);
            info.getRamifications().forEach(ramification -> fillMapWithRelevantInfo(ramification, map));
        }
        return map;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        Info info = new Info();
        info.setDescriptor(desc);
        this.infoMap.put(name, info);
        return new InfoMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), owner, info);
    }
}
