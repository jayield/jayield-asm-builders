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


    public static String getNewLambdaDesc(String desc, String type) {
        int lastTypeStart = getLastTypeStart(desc);
        return desc.substring(0 , lastTypeStart) + String.format("L%s;", type) + desc.substring(lastTypeStart);
    }

    public static int getLastTypeStart(String desc) {
        int lastType = 0;
        int aux = 0;
        int until = desc.lastIndexOf(')');
        while((aux = desc.indexOf('L', aux + 1)) != -1 && aux < until){
            if(desc.charAt(aux - 1) == '/')
                return lastType;
            lastType = aux;
        }
        return lastType;
    }
}
