package fr.inria.diversify.dspot.value;

import fr.inria.diversify.dspot.dynamic.logger.KeyWord;
import fr.inria.diversify.log.LogParser;
import spoon.reflect.factory.Factory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Simon
 * Date: 31/08/16
 * Time: 14:32
 */
public class MethodCallReader extends LogParser<Set<MethodCall>> {
    protected MethodCall currentMethodCall;
    protected ObjectWithSetter currentObjectValue = null;
    protected Factory factory;
    protected ValueFactory valueFactory;


    public MethodCallReader(Factory factory, ValueFactory valueFactory) {
        this.valueFactory = valueFactory;
        result = new HashSet<>();
        this.factory = factory;
        currentMethodCall = new MethodCall(factory);
    }
    @Override
    public void readLogLine(String logLine) {
        String[] split = logLine.split(";");
        switch (split[0]) {
            case KeyWord.methodCallPrimitiveParameter:
                if(currentObjectValue != null) {
                    currentMethodCall.addParameter(currentObjectValue);
                    currentObjectValue = null;
                }
                PrimitiveValue primitive;
                ValueType type = valueFactory.getValueType(split[1]);
                if(split.length != 3) {
                    primitive = valueFactory.getPrimitiveValue(type, "");
                } else {
                    primitive = valueFactory.getPrimitiveValue(type, split[2]);
                }
                currentMethodCall.addParameter(primitive);
                break;
            case KeyWord.methodCallObjectParameter:
                if(currentObjectValue != null) {
                    currentMethodCall.addParameter(currentObjectValue);
                }
                currentObjectValue = new ObjectWithSetter(split[1], factory, valueFactory);
                break;
            case KeyWord.ObjectParameterField:
                if(currentObjectValue != null) {
                    type = valueFactory.getValueType(split[2]);
                    if(split.length != 4) {
                            primitive = valueFactory.getPrimitiveValue(type, "");
                        } else {
                            primitive = valueFactory.getPrimitiveValue(type, split[3]);
                        }
                    currentObjectValue.addValue(split[1], primitive);
                }
                break;

            case KeyWord.methodCallMethod:
                if(currentObjectValue != null) {
                    currentMethodCall.addParameter(currentObjectValue);
                    currentObjectValue = null;
                }
                currentMethodCall.setMethod(split[1]);
                break;

            case KeyWord.methodCallReceiverType:
                currentMethodCall.setTarget(split[1]);
                result.add(currentMethodCall);
                currentMethodCall = new MethodCall(factory);
                break;
        }
    }

    @Override
    public void init(File dir) throws IOException {

    }

    @Override
    public void newLogFile(File file) {

    }
}
