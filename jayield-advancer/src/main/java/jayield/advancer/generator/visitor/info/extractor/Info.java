package jayield.advancer.generator.visitor.info.extractor;

import jayield.advancer.generator.visitor.info.extractor.local.variable.LocalVariable;

import java.util.List;
import java.util.Map;

public class Info {

    private Map<Integer, LocalVariable> localVariables;
    private Integer states;
    private List<String> ramifications;
    private String descriptor;

    public Map<Integer, LocalVariable> getLocalVariables() {
        return localVariables;
    }

    public void setLocalVariables(Map<Integer, LocalVariable> localVariables) {
        this.localVariables = localVariables;
    }

    public Integer getStates() {
        return states;
    }

    public void setStates(Integer states) {
        this.states = states;
    }

    public List<String> getRamifications() {
        return ramifications;
    }

    public void setRamifications(List<String> ramifications) {
        this.ramifications = ramifications;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public String getDescriptor() {
        return descriptor;
    }
}
