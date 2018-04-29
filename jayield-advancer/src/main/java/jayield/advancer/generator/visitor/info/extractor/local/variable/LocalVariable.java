package jayield.advancer.generator.visitor.info.extractor.local.variable;

import org.objectweb.asm.Label;

public class LocalVariable {

    private final String name;
    private final String desc;
    private final String signature;
    private final Label start;
    private final Label end;
    private final int index;

    public LocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        this.name = name;
        this.desc = desc;
        this.signature = signature;
        this.start = start;
        this.end = end;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getSignature() {
        return signature;
    }

    public Label getStart() {
        return start;
    }

    public Label getEnd() {
        return end;
    }

    public int getIndex() {
        return index;
    }
}
