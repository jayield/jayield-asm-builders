package jayield.lite.codegen;

public class GeneratorUtils {

    public static String classNameToPath(Class<?> clazz) {
        return clazz.getName().replace('.', '/');
    }
}
