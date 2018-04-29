package jayield.advancer.generator.visitor.captured.context;

public interface CapturedContextHandler {

    int getThisVar();

    int getActualVar(int var);

    boolean isLocalVariable(int var);

    void handleNonLocalVariable(int opcode, int var);
}
