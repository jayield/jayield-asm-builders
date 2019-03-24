package jayield.advancer;

import static jayield.advancer.generator.Generator.getSerializedLambda;
import static jayield.advancer.generator.InstrumentationUtils.getYieldIndex;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.invoke.SerializedLambda;

import org.jayield.Query;
import org.jayield.Traverser;
import org.junit.Test;

import jayield.advancer.generator.visitor.info.extractor.Info;
import jayield.advancer.generator.visitor.info.extractor.InfoExtractorVisitor;

public class YieldIndexTests {

    @Test
    public void testSimpleTraverser() {
        Traverser<Integer> t = yield -> yield.ret(1);
        int expected = 0;

        assertExpectedIndex(t, expected);
    }

    @Test
    public void testWithCapturedContext() {
        int capturedContext = 1;
        Traverser<Integer> t = yield -> yield.ret(capturedContext);
        int expected = 1;

        assertExpectedIndex(t, expected);
    }

    @Test
    public void testWithCapturedContextAndLocalVariables() {
        int capturedContext = 1;
        Traverser<Integer> t = yield -> {
            for (int i = 0; i < 3; i++) {
                yield.ret(capturedContext);
            }
        };
        int expected = 1;

        assertExpectedIndex(t, expected);
    }

    @Test
    public void testWithLocalVariable() {
        Traverser<Integer> t = yield -> {
            for (int i = 0; i < 3; i++) {
                yield.ret(i);
            }
        };
        int expected = 0;

        assertExpectedIndex(t, expected);
    }

    @Test
    public void testWithMultipleLocalVariables() {
        Traverser<Integer> t = yield -> {
            int ret = 10;
            int mul = -1;
            for (int i = 0; i < 3; i++) {
                yield.ret(ret * mul);
            }
        };
        int expected = 0;

        assertExpectedIndex(t, expected);
    }

    @Test
    public void testWithMultipleCapturedContexts() {
        int capturedContext1 = 1;
        int capturedContext2 = 1;
        int capturedContext3 = 1;
        Traverser<Integer> t = yield -> {
            yield.ret(capturedContext1);
            yield.ret(capturedContext2);
            yield.ret(capturedContext3);
        };
        int expected = 3;

        assertExpectedIndex(t, expected);
    }

    @Test
    public void testWithCapturedThis() {
        Traverser<Integer> t = Query.of(1, 2, 3).map(v -> v * v).getTraverser();
        int expected = 2;

        assertExpectedIndex(t, expected);

    }

    @Test
    public void testWithCapturedThisAndContext() {
        Traverser<Integer> t = Query.of(1, 2, 3).skip(1).getTraverser();
        int expected = 2;
        assertExpectedIndex(t, expected);

    }

    private void assertExpectedIndex(Traverser<Integer> t, int expected) {
        try {
            SerializedLambda lambda = getSerializedLambda(t);
            String name = lambda.getImplMethodName();
            Info info = InfoExtractorVisitor.extractInfo(lambda).get(name);

            assertEquals(expected, getYieldIndex(lambda, info));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
