import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import origin.*;
import visitors.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static java.lang.String.format;

public class App  implements Opcodes{

    public static void main(String[] args) throws Exception{
        OriginalImplementation source = new OriginalImplementation();
        String outPath = App.class.getProtectionDomain().getCodeSource().getLocation().getPath();
//        ASMifier.main(new String[]{outPath + "/origin/OriginalImplementation.class"});

        String[] classNames = new String[]{"DummyTarget","DCopyTarget","DummyRenamedImpl","TypeChanged"};

        CustomClassVisitorBuilder copy = new CustomClassVisitorBuilder();
        copy.setInterface("origin/OriginalInterfaceCopy");

        CustomClassVisitorBuilder renamed = new CustomClassVisitorBuilder();
        renamed.setInterface("origin/RenamedMethodInterface");
        renamed.setToMethodDeclaration(cv -> cv.visitMethod(ACC_PUBLIC, "toInt", "()I", null, null));

        CustomClassVisitorBuilder typeChange = new CustomClassVisitorBuilder();
        typeChange.setInterface("origin/BooleanInterface");
        typeChange.setToMethodDeclaration(cv -> cv.visitMethod(ACC_PUBLIC, "toBoolean", "()Z", null, null));



        byte[] dummyTarget = generateClass(classNames[0],outPath, new CustomClassVisitorBuilder());
        byte[] dcopyTarget = generateClass(classNames[1],outPath, copy);
        byte[] drenamedTarget = generateClass(classNames[2],outPath, renamed);
        byte[] dbooleanTarget = generateClass(classNames[3],outPath, typeChange);

        OriginalInterface i = (OriginalInterface) ByteArrayClassLoader
                .load(classNames[0], dummyTarget)
                .newInstance();
        OriginalInterfaceCopy icopy = (OriginalInterfaceCopy) ByteArrayClassLoader
                .load(classNames[1], dcopyTarget)
                .newInstance();
        RenamedMethodInterface irenamed = (RenamedMethodInterface) ByteArrayClassLoader
                .load(classNames[2], drenamedTarget)
                .newInstance();
        BooleanInterface iboolean = (BooleanInterface) ByteArrayClassLoader
                .load(classNames[3], dbooleanTarget)
                .newInstance();

        log(source.getClass(),"toNumber", String.valueOf(source.toNumber()));
        log(i.getClass(),"toNumber", String.valueOf(i.toNumber()));
        log(icopy.getClass(),"toNumber", String.valueOf(icopy.toNumber()));
        log(irenamed.getClass(),"toInt", String.valueOf(icopy.toNumber()));
        log(iboolean.getClass(),"toBoolean", String.valueOf(iboolean.toBoolean()));
    }

    private static byte[] generateClass(String targetClassName, String outPath, CustomClassVisitorBuilder builder) throws IOException, InvalidStateException {
        ClassReader cr = new ClassReader(OriginalImplementation.class.getName());
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        builder.setClassVisitor(cw);
        builder.setTargetName(targetClassName);
        cr.accept(builder.build(),0);
        FileOutputStream fos = new FileOutputStream(outPath + "./" + targetClassName + ".class");
        byte[] targetBytes = cw.toByteArray();
        fos.write(targetBytes);
        fos.close();
        return targetBytes;
    }

    private static String getInterfaces(Class<?> clazz) {
        return Arrays.stream(clazz.getInterfaces()).map(Class::getName)
                .reduce("", (acc, curr)-> acc + " " + curr);
    }

    private static String getMethods(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods()).map(Method::getName)
                .reduce("", (acc, curr)-> acc + " " + curr);
    }

    private static void log(Class<?> clazz, String invoked, String value){
        System.out.println(format("class: %s, \ninterfaces: %s, \nmethods: %s, \n%s:%s\n",
                clazz.getName(),
                getInterfaces(clazz),
                getMethods(clazz),
                invoked,
                value));
    }

}
