package jayield.advancer;


import jayield.advancer.generator.Generator;
import jayield.advancer.generator.classloader.ByteArrayClassLoader;
import jayield.advancer.generator.visitor.ownership.ChangeOwnersMethodVisitor;
import org.jayield.Query;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import static java.lang.String.format;
import static jayield.advancer.generator.InstrumentationUtils.debugASM;
import static jayield.advancer.generator.InstrumentationUtils.getOutputPath;
import static jayield.advancer.generator.InstrumentationUtils.packageToPath;
import static org.junit.Assert.assertNotNull;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.ASM6;

public class VisitorTests {

    @Test
    public void testClassGenerationWithExternalLibField() throws Exception {
        String originalName = DummyClass.class.getName();
        String finalName = originalName + "Generated";
        ClassReader reader = new ClassReader(originalName);
        ClassWriter writer = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
        ClassVisitor visitor = new ClassVisitor(ASM6, writer) {
            @Override
            public void visit(int version,
                              int access,
                              String name,
                              String signature,
                              String superName,
                              String[] interfaces) {
                super.visit(version, access, packageToPath(finalName), signature, superName, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access,
                                             String name,
                                             String descriptor,
                                             String signature,
                                             String[] exceptions) {
                return new ChangeOwnersMethodVisitor(super.visitMethod(access, name, descriptor, signature, exceptions),
                                                     packageToPath(originalName), packageToPath(finalName));
            }
        };

        reader.accept(visitor, 0);
        String filename = format("%s./%s___%s.class",
                                 getOutputPath(),
                                 "testClassGenerationWithExternalLibField",
                                 originalName);
        byte[] targetBytes = writer.toByteArray();
        Generator.writeClassToFile(filename, targetBytes);
        debugASM(true, filename);
        System.out.println(ByteArrayClassLoader.load(finalName, targetBytes).newInstance());
    }

    @Test
    public void testGenerateAdvancerFromQuery() {
        Advancer<Integer> from = Advancer.from(Query.of(1));
        assertNotNull(from);
    }

    @Test
    public void testGenerateAdvancerFromQueryWithContext() {
        Query<Integer> query = Query.of().then(q -> yield -> {
            yield.ret(1);
            yield.ret(2);
            yield.ret(3);
        });
        Advancer<Integer> from = Advancer.from(query);

        assertNotNull(from);
    }

    @Test
    public void testGenerateAdvancerFromQueryWithExternalLibContext() {
        Query<Query> query = Query.of().then(q -> yield -> {
            yield.ret(q);
            yield.ret(q);
            yield.ret(q);
        });
        Advancer<Query> from = Advancer.from(query);

        assertNotNull(from);
    }

    @Test
    public void testGenerateAdvancerFromTraverser() {
        Advancer<Integer> from = Advancer.from(yield -> {
            yield.ret(1);
            yield.ret(2);
            yield.ret(3);
        });

        assertNotNull(from);
    }

    @Test
    public void testGenerateAdvancerFromTraverserWithContext() {
        Advancer<Integer> from = Advancer.from(yield -> {
            for (int i = 0; i < 3; i++) {
                yield.ret(i);
            }
        });

        assertNotNull(from);
    }

    @Test
    public void testGenerateAdvancerFromTraverserWithExternalLibContext() {
        Query context = Query.of(1);
        Advancer<Query> from = Advancer.from(yield -> {
            for (int i = 0; i < 3; i++) {
                yield.ret(context);
            }
        });

        assertNotNull(from);
    }


}