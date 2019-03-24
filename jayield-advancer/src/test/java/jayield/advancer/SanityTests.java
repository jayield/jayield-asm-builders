package jayield.advancer;

import jayield.advancer.generator.Generator;
import jdk.internal.org.objectweb.asm.Type;
import org.jayield.Query;
import org.jayield.Traverser;
import org.jayield.Yield;
import org.junit.Test;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SanityTests {

    @Test
    public void testQuerySerialization() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Traverser<Integer> source = Query.of(1, 2, 3).getTraverser();
        SerializedLambda target = Generator.getSerializedLambda(source);

        assertEquals(1, target.getCapturedArgCount());
        assertTrue(target.getCapturedArg(0) instanceof Integer[]);
        assertEquals("([Ljava/lang/Object;Lorg/jayield/Yield;)V", target.getImplMethodSignature());

    }

    @Test
    public void testSimpleAdvancer() {
        boolean[] first = new boolean[]{true};
        String sanityCheck = "Sanity";
        Advancer<Object> target = yield -> {
            if (first[0]) {
                yield.ret(sanityCheck);
                first[0] = false;
                return true;
            }
            return false;
        };

        Yield<Object> yieldAssertion = value -> assertEquals(value, sanityCheck);
        assertTrue(target.tryAdvance(yieldAssertion));
        assertFalse(target.tryAdvance(yieldAssertion));
    }

    @Test
    public void testTraverserSerialization() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String captArg0 = "captArg0";
        Integer captArg1 = 1;
        int captArg2 = 2;
        Traverser<Integer> source = yield -> {
            yield.ret(captArg0.length());
            yield.ret(captArg1);
            yield.ret(captArg2);
            yield.ret(0);
        };
        SerializedLambda target = Generator.getSerializedLambda(source);

        assertEquals(3, target.getCapturedArgCount());
        assertTrue(target.getCapturedArg(0) instanceof String);
        assertTrue(target.getCapturedArg(1) instanceof Integer);
        assertTrue(target.getCapturedArg(2) instanceof Integer);

        assertEquals("(Ljava/lang/String;Ljava/lang/Integer;ILorg/jayield/Yield;)V",
                     target.getImplMethodSignature());

    }
}
