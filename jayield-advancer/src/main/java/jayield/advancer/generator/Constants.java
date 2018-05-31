package jayield.advancer.generator;

import org.jayield.Query;
import org.jayield.Traverser;
import org.jayield.Yield;
import jayield.advancer.Advancer;
import jayield.advancer.generator.wrapper.YieldWrapper;
import org.jayield.boxes.BoolBox;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.PrintStream;
import java.lang.invoke.SerializedLambda;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

import static java.lang.String.valueOf;
import static jayield.advancer.generator.InstrumentationUtils.BOOLEAN;
import static jayield.advancer.generator.InstrumentationUtils.BYTE;
import static jayield.advancer.generator.InstrumentationUtils.CHAR;
import static jayield.advancer.generator.InstrumentationUtils.SHORT;
import static jayield.advancer.generator.InstrumentationUtils.VOID;
import static jayield.advancer.generator.InstrumentationUtils.getArrayTypeDescriptor;
import static jayield.advancer.generator.InstrumentationUtils.getClassPath;
import static jayield.advancer.generator.InstrumentationUtils.getMethodDescriptor;
import static jayield.advancer.generator.InstrumentationUtils.getTypeDescriptor;

public class Constants implements Opcodes {

    public static final String PRINT_STREAM_DESCRIPTOR = getTypeDescriptor(PrintStream.class);
    public static final String TRAVERSER_DESCRIPTOR = getTypeDescriptor(Traverser.class);
    public static final String ITERATOR_DESCRIPTOR = getTypeDescriptor(Iterator.class);
    public static final String INT_DESCRIPTOR = valueOf(InstrumentationUtils.INTEGER);
    public static final String BOOL_BOX_DESCRIPTOR = getTypeDescriptor(BoolBox.class);
    public static final String INTEGER_DESCRIPTOR = getTypeDescriptor(Integer.class);
    public static final String OBJECT_DESCRIPTOR = getTypeDescriptor(Object.class);
    public static final String STRING_DESCRIPTOR = getTypeDescriptor(String.class);
    public static final String QUERY_DESCRIPTOR = getTypeDescriptor(Query.class);
    public static final String YIELD_DESCRIPTOR = getTypeDescriptor(Yield.class);

    public static final String SERIALIZED_LAMBDA = getClassPath(SerializedLambda.class);
    public static final String JAVA_IO_PRINT_STREAM = getClassPath(PrintStream.class);
    public static final String YIELD_WRAPPER = getClassPath(YieldWrapper.class);
    public static final String JAVA_LANG_INTEGER = getClassPath(Integer.class);
    public static final String JAVA_LANG_SYSTEM = getClassPath(System.class);
    public static final String JAVA_LANG_OBJECT = getClassPath(Object.class);
    public static final String ITERATOR_CLASS = getClassPath(Iterator.class);
    public static final String JAVA_LANG_STRING = getClassPath(String.class);
    public static final String BOOL_BOX = getClassPath(BoolBox.class);
    public static final String YIELD = getClassPath(Yield.class);

    public static final String INT_ARRAY_DESCRIPTOR = getArrayTypeDescriptor(InstrumentationUtils.INTEGER);
    public static final String OBJECT_ARRAY_DESCRIPTOR = getArrayTypeDescriptor(OBJECT_DESCRIPTOR);

    public static final String ITERATOR_FROM_TRAVERSER = getMethodDescriptor(ITERATOR_DESCRIPTOR, TRAVERSER_DESCRIPTOR);
    public static final String ITERATOR_FROM_QUERY = getMethodDescriptor(ITERATOR_DESCRIPTOR, QUERY_DESCRIPTOR);
    public static final String INT_BOX_METHOD_DESCRIPTOR = getMethodDescriptor(INTEGER_DESCRIPTOR,
                                                                               valueOf(InstrumentationUtils.INTEGER));
    public static final String INITIALIZE_METHOD_DESCRIPTOR = getMethodDescriptor(Advancer.class,
                                                                                  SerializedLambda.class);
    public static final String GET_CAPTURED_ARG_METHOD_SIGNATURE = getMethodDescriptor(OBJECT_DESCRIPTOR,
                                                                                       INT_DESCRIPTOR);

    public static final String TRY_ADVANCE_METHOD_DESC = getMethodDescriptor(BOOLEAN, YIELD_DESCRIPTOR);
    public static final String YIELD_METHOD_DESCRIPTION = getMethodDescriptor(VOID, OBJECT_DESCRIPTOR);
    public static final String ADVANCE_METHOD_DESC = getMethodDescriptor(VOID, YIELD_DESCRIPTOR);
    public static final String PRINTLN_DESCRIPTOR = getMethodDescriptor(VOID, STRING_DESCRIPTOR);
    public static final String FORMAT_METHOD_DESCRIPTOR = getMethodDescriptor(STRING_DESCRIPTOR,
                                                                              STRING_DESCRIPTOR,
                                                                              OBJECT_ARRAY_DESCRIPTOR);
    public static final String BOOLEAN_SUPPLIER = getMethodDescriptor(BOOLEAN);


    public static final String GET_CAPTURED_ARG_METHOD_NAME = "getCapturedArg";
    public static final String TRY_ADVANCE_METHOD_NAME = "tryAdvance";
    public static final String STATE_TEMPLATE_STRING = "state: %d";
    public static final String CONSTRUCTOR_METHOD_NAME = "<init>";
    public static final String TRAVERSE_METHOD_NAME = "traverse";
    public static final String STATE_FIELD_NAME = "$state";
    public static final String HAS_ELEMENT = "hasElement";
    public static final String YIELD_METHOD_NAME = "ret";
    public static final String INITIALIZE = "initialize";
    public static final String VALID_VALUE = "validValue";
    public static final String ITERATOR = "iterator";
    public static final String IS_FALSE = "isFalse";
    public static final String HAS_NEXT = "hasNext";
    public static final String VALUE_OF = "valueOf";
    public static final String ADVANCE = "advance";
    public static final String WRAPPER = "wrapper";
    public static final String PRINTLN = "println";
    public static final String IS_TRUE = "isTrue";
    public static final String FORMAT = "format";
    public static final String RESET = "reset";
    public static final String THIS = "this";
    public static final String AUX = "aux";
    public static final String OUT = "out";


    public static final boolean DEBUG = false;

    public static final Map<Character, Consumer<MethodVisitor>> PRIMITIVE_TYPE_MAPPER;

    static {
        PRIMITIVE_TYPE_MAPPER = new HashMap<>();
        putMapper(Boolean.class, "booleanValue", BOOLEAN);
        putMapper(Byte.class, "byteValue", BYTE);
        putMapper(Character.class, "charValue", CHAR);
        putMapper(Short.class, "shortValue", SHORT);
        putMapper(Integer.class, "intValue", InstrumentationUtils.INTEGER);
        putMapper(Float.class, "floatValue", InstrumentationUtils.FLOAT);
        putMapper(Long.class, "longValue", InstrumentationUtils.LONG);
        putMapper(Double.class, "doubleValue", InstrumentationUtils.DOUBLE);
    }

    private static void putMapper(Class boxer, String valueOf, char type) {
        String clazz = getClassPath(boxer);
        String methodDescriptor = getMethodDescriptor(type);
        PRIMITIVE_TYPE_MAPPER.put(type, mv -> {
            mv.visitTypeInsn(CHECKCAST, clazz);
            mv.visitMethodInsn(INVOKEVIRTUAL, clazz, valueOf, methodDescriptor, false);
        });
    }

}
