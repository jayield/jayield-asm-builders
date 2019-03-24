package jayield.advancer.formatter;

public interface Formatter {

    <T> boolean accepts(T target);

    <T> String format(T target);
}
