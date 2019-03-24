package jayield.advancer.formatter;

import java.lang.invoke.SerializedLambda;

public class SerializedLambdaFormatter implements Formatter {
    @Override
    public <T> boolean accepts(T target) {
        return SerializedLambda.class.equals(target.getClass());
    }

    @Override
    public <T> String format(T target) {
        StringBuilder builder = new StringBuilder();
        SerializedLambda lambda = (SerializedLambda) target;
        builder.append(String.format("\nLambda: %s\n",lambda));
        builder.append(String.format("     Captured Arguments: %d\n", lambda.getCapturedArgCount()));
        for (int i = 0; i < lambda.getCapturedArgCount(); i++) {
            builder.append(String.format("         Captured Argument at index (%d): %s\n", i, lambda.getCapturedArg(i)));
            Class<?> klass = lambda.getCapturedArg(i).getClass();
            builder.append(String.format("             Class: %s\n", klass.getName()));
            Class<?>[] interfaces = klass.getInterfaces();
            if(interfaces.length > 0){
                builder.append("             Interfaces:\n");
                for (int j = 0; j < interfaces.length; j++) {
                    builder.append(String.format("                 %s\n", interfaces[j].getName()));
                }

            }
            builder.append("\n");
        }
        builder.append(String.format("     Signature: %s\n", lambda.getImplMethodSignature()));
        return builder.toString();
    }
}
