/**
 * @author Miguel Gamboa
 *         created on 17-11-2017
 */
class ByteArrayClassLoader extends ClassLoader {
    static Class<?> load(String name, byte[] ba) {
        return new ByteArrayClassLoader().getClassFromByteArray(name, ba);
    }
    /**
     * We use a long method name to avoid mistaken override.
     */
    protected Class getClassFromByteArray(String name, byte[] ba) {
        return super.defineClass(name, ba, 0, ba.length);
    }
}
