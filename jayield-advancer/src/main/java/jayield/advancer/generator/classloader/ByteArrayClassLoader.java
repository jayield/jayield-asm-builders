package jayield.advancer.generator.classloader;


public class ByteArrayClassLoader extends ClassLoader {
    public static Class<?> load(String name, byte[] ba) {
        ByteArrayClassLoader cl = new ByteArrayClassLoader();
        try {
            cl.loadClass("org.jayield.boxes.BoolBox");
            cl.loadClass("jayield.advancer.generator.wrapper.AbstractAdvance");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return cl.getClassFromByteArray(name, ba);
    }

    /**
     * We use a long method name to avoid mistaken override.
     */
    protected Class getClassFromByteArray(String name, byte[] ba) {
        return super.defineClass(name, ba, 0, ba.length);
    }
}
