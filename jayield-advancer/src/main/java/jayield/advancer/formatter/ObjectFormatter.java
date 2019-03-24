package jayield.advancer.formatter;

import static java.util.Arrays.asList;

import java.util.List;

public class ObjectFormatter {

    private static List<Formatter> FORMATTERS;

    static {
        FORMATTERS = asList(
                new SerializedLambdaFormatter()
        );
    }

    public static <T> String format(T target) {
        return FORMATTERS.stream()
                         .filter(f -> f.accepts(target))
                         .findAny()
                         .map(f -> f.format(target))
                         .orElse(String.format("%s (No Formatter Found)", target));
    }
}
