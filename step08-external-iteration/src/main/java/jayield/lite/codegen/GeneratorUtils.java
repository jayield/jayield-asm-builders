package jayield.lite.codegen;

import org.objectweb.asm.Opcodes;

public class GeneratorUtils implements Opcodes{

    public static String classNameToPath(Class<?> clazz) {
        return clazz.getName().replace('.', '/');
    }

    public static int getLoadCode(String desc) {
        switch (desc.charAt(0)){
            case 'Z':
            case 'B':
            case 'C':
            case 'S':
            case 'I':
                return ILOAD;
            case 'F':
                return FLOAD;
            case 'J':
                return LLOAD;
            case 'D':
                return DLOAD;
            case '[':
            case 'L':
                return ALOAD;
        }
        return 0;
    }
}
