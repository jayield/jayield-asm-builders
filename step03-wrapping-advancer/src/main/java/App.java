import org.objectweb.asm.util.ASMifier;

public class App {

    public static void main(final String args[]) throws Exception {
        String outPath = App.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        ASMifier.main(new String[]{outPath + "/yield/advancers/FilterAdvancer.class"});
    }

}