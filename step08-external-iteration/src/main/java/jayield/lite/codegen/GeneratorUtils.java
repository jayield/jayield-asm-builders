package jayield.lite.codegen;

import jdk.internal.org.objectweb.asm.Type;
import org.objectweb.asm.Opcodes;

import java.util.stream.Stream;

public class GeneratorUtils implements Opcodes {

    public static String classNameToPath(Class<?> clazz) {
        return clazz.getName()
                    .replace('.', '/');
    }

    public static int getLoadCode(String desc) {
        switch (desc.charAt(0)) {
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

    public static boolean isStoreOpcode(int opcode) {
        switch (opcode) {
            case ASTORE:
            case FSTORE:
            case LSTORE:
            case ISTORE:
            case AASTORE:
            case BASTORE:
            case CASTORE:
            case DASTORE:
            case DSTORE:
            case FASTORE:
            case IASTORE:
            case LASTORE:
            case SASTORE:
                return true;
            default:
                return false;
        }
    }

    public static boolean isLoadOpcode(int opcode) {
        switch (opcode) {
            case ALOAD:
            case FLOAD:
            case LLOAD:
            case ILOAD:
            case AALOAD:
            case BALOAD:
            case CALOAD:
            case DALOAD:
            case DLOAD:
            case FALOAD:
            case IALOAD:
            case LALOAD:
            case SALOAD:
                return true;
            default:
                return false;
        }
    }



}
