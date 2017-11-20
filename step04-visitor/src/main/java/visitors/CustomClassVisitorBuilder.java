package visitors;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.function.Function;

public class CustomClassVisitorBuilder {

    private ClassVisitor cv;
    private String targetName;
    private String[] interfaces;
    private Function<MethodVisitor, MethodVisitor> toMethodInstrumenter;
    private Function<ClassVisitor, MethodVisitor> toMethodDeclaration;

    public CustomClassVisitorBuilder() {}

    public CustomClassVisitor build() throws InvalidStateException {
        if(cv == null || targetName == null)
            throw new InvalidStateException();
        CustomClassVisitor target =  new CustomClassVisitor(cv,targetName);
        if(interfaces != null) {
            target.setInterfaces(interfaces);
        }
        if(toMethodInstrumenter != null) {
            target.setToMethodIntrumenter(toMethodInstrumenter);
        }
        if(toMethodDeclaration != null) {
            target.setToMethodDeclaration(toMethodDeclaration);
        }
        return target;
    }

    public void setClassVisitor(ClassVisitor cv) {
        this.cv = cv;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public void setInterfaces(String[] interfaces) {
        this.interfaces = interfaces;
    }

    public void setInterface(String interfaceName) {
        this.interfaces = new String[]{interfaceName};
    }

    public void setToMethodInstrumenter(Function<MethodVisitor, MethodVisitor> toMethodInstrumenter) {
        this.toMethodInstrumenter = toMethodInstrumenter;
    }

    public void setToMethodDeclaration(Function<ClassVisitor, MethodVisitor> toMethodDeclaration) {
        this.toMethodDeclaration = toMethodDeclaration;
    }
}
