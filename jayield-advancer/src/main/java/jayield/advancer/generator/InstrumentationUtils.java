package jayield.advancer.generator;

import jayield.Yield;
import jayield.traversable.Traversable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.ASMifier;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static jayield.advancer.generator.Constants.YIELD_METHOD_DESCRIPTION;
import static jayield.advancer.generator.Constants.YIELD_METHOD_NAME;

public class InstrumentationUtils implements Opcodes {

    public static final char VOID = 'V';
    public static final char BOOLEAN = 'Z';
    public static final char BYTE = 'B';
    public static final char CHAR = 'C';
    public static final char SHORT = 'S';
    public static final char INTEGER = 'I';
    public static final char FLOAT = 'F';
    public static final char LONG = 'J';
    public static final char DOUBLE = 'D';
    public static final char ARRAY = '[';
    public static final char OBJECT = 'L';
    public static final char OBJECT_DELIMITER = ';';
    public static final char METHOD_PARAMETERS_START = '(';
    public static final char METHOD_PARAMETERS_END = ')';
    public static final char PAKAGE_DELIMITER = '.';
    public static final char PATH_DELIMITER = '/';

    private static final List<Integer> LOAD_CODES = asList(
            ALOAD,
            FLOAD,
            LLOAD,
            ILOAD,
            AALOAD,
            BALOAD,
            CALOAD,
            DALOAD,
            DLOAD,
            FALOAD,
            IALOAD,
            LALOAD,
            SALOAD
    );
    private static final List<Integer> STORE_CODES = asList(
            ASTORE,
            FSTORE,
            LSTORE,
            ISTORE,
            AASTORE,
            BASTORE,
            CASTORE,
            DASTORE,
            DSTORE,
            FASTORE,
            IASTORE,
            LASTORE,
            SASTORE
    );
    public static final String DEBUG_FLAG = "-debug";

    public static String getClassPath(Class clazz) {
        return packageToPath(clazz.getName());
    }

    public static int getLoadCode(String desc) {
        switch (desc.charAt(0)) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case SHORT:
            case INTEGER:
                return ILOAD;
            case FLOAT:
                return FLOAD;
            case LONG:
                return LLOAD;
            case DOUBLE:
                return DLOAD;
            case ARRAY:
            case OBJECT:
                return ALOAD;
        }
        return 0;
    }

    public static String getArrayTypeDescriptor(String type) {
        return format("%s%s", ARRAY, type);
    }

    public static String getArrayTypeDescriptor(char type) {
        return format("%s%c", ARRAY, type);
    }

    public static String getTypeDescriptor(Class clazz) {
        return getTypeDescriptor(getClassPath(clazz));
    }

    public static String getTypeDescriptor(String clazz) {
        return format("%s%s%s", OBJECT, clazz, OBJECT_DELIMITER);
    }

    public static String getMethodDescriptor(char returnType, String... parameters) {
        return getMethodDescriptor(format("%c", returnType), parameters);
    }

    public static String getMethodDescriptor(String returnType, String... parameters) {
        return Stream.of(parameters)
                     .reduce((prev, curr) -> format("%s%s", prev, curr))
                     .map(parameterTypes -> format("%s%s%s%s",
                                                   METHOD_PARAMETERS_START,
                                                   parameterTypes,
                                                   METHOD_PARAMETERS_END,
                                                   returnType))
                     .orElse(format("%s%s%s",
                                    METHOD_PARAMETERS_START,
                                    METHOD_PARAMETERS_END,
                                    returnType));
    }

    public static String getMethodDescriptor(Class returnType, Class... parameters) {
        String returnTypePath = getTypeDescriptor(returnType);
        String[] parameterTypes = Stream.of(parameters)
                                        .map(InstrumentationUtils::getTypeDescriptor)
                                        .collect(Collectors.toList())
                                        .toArray(new String[parameters.length]);
        return getMethodDescriptor(returnTypePath, parameterTypes);
    }

    public static String packageToPath(String source) {
        return source.replace(PAKAGE_DELIMITER, PATH_DELIMITER);
    }

    public static String insertTypeInDescriptor(String desc, String type) {
        int lastTypeStart = getLastTypeStart(desc);
        String start = desc.substring(0, lastTypeStart);
        String middle = getFullyQualifiedClassDescription(type);
        String end = desc.substring(lastTypeStart);
        return format("%s%s%s", start, middle, end);
    }

    private static String getFullyQualifiedClassDescription(String type) {
        return format("%s%s%s", OBJECT, type, OBJECT_DELIMITER);
    }

    public static int getLastTypeStart(String desc) {
        int lastType = 0;
        int aux = 0;
        int until = desc.lastIndexOf(METHOD_PARAMETERS_END);
        while ((aux = desc.indexOf(OBJECT, aux + 1)) != -1 && aux < until) {
            if (desc.charAt(aux - 1) == PATH_DELIMITER)
                return lastType;
            lastType = aux;
        }
        return lastType;
    }

    public static boolean isStoreOpcode(int opcode) {
        return STORE_CODES.contains(opcode);
    }

    public static boolean isLoadOpcode(int opcode) {
        return LOAD_CODES.contains(opcode);
    }

    public static void printASM(String filename) throws Exception {
        ASMifier.main(new String[]{DEBUG_FLAG, filename});
    }

    public static String getOutputPath() {
        return Traversable.class.getProtectionDomain()
                                .getCodeSource()
                                .getLocation()
                                .getPath();
    }

    public static void debugASM(boolean enabled, String filename) throws Exception {
        if (enabled) {
            printASM(filename);
        }
    }

    public static boolean isYield(int opcode, String owner, String name, String desc, boolean isInterface) {
        return opcode == INVOKEINTERFACE &&
                owner.equals(getClassPath(Yield.class)) &&
                name.equals(YIELD_METHOD_NAME) &&
                desc.equals(YIELD_METHOD_DESCRIPTION) &&
                isInterface;
    }


}
