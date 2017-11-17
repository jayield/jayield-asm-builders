import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.jayield.builders.EnhanceClassVisitor;
import org.objectweb.asm.*;

/**
 * This demo copies from DummySource.class to DummyTarget.class and
 * adds DummyInterface to DummyTarget.
 * Finally, it will create an instance of the new class DummyTarget and
 * invokes its bar() method through interface DummyInterface.
 */
public class App implements Opcodes{

    static final String TARGET_CLASSNAME = "DummyTarget";

    public static void main(final String args[]) throws Exception {
        String sourceClassName = DummySource.class.getName() + ".class";
        InputStream is = App.class.getResourceAsStream(sourceClassName);

        /*
         * Copy from DummySource.class to DummyTarget.class
         */
        ClassReader cr = new ClassReader(is); // This is DummySource.class
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES); // This is the destination DummyTarget.class
        cr.accept(new EnhanceClassVisitor(TARGET_CLASSNAME, cw) , 0);

        /*
         * Write output bytecodes to  ./DummyTarget.class
         */
        String outPath = App.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        FileOutputStream fos = new FileOutputStream(outPath + "./" + TARGET_CLASSNAME + ".class");
        byte[] targetBytes = cw.toByteArray();
        fos.write(targetBytes);
        fos.close();
        DummyInterface i = (DummyInterface) ByteArrayClassLoader
                .load(TARGET_CLASSNAME, targetBytes)
                .newInstance();
        System.out.println(i.getClass());
        i.bar(7, 9);
    }
}
