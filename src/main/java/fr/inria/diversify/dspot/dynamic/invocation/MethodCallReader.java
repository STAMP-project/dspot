package fr.inria.diversify.dspot.dynamic.invocation;

import fr.inria.diversify.log.LogParser;
import fr.inria.diversify.dspot.dynamic.logger.KeyWord;
import spoon.reflect.factory.Factory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * User: Simon
 * Date: 31/08/16
 * Time: 14:32
 */
public class MethodCallReader extends LogParser<Set<MethodCall>> {
    protected MethodCall currentMethodCall;
    protected ObjectValue currentObjectValue = null;
    protected Factory factory;


    public MethodCallReader(Factory factory) {
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
                if(split.length != 3) {
                    currentMethodCall.addParameter(new PrimitiveValue(split[1], "", factory));
                } else {
                    currentMethodCall.addParameter(new PrimitiveValue(split[1], split[2], factory));
                }
                break;
            case KeyWord.methodCallObjectParameter:
                if(currentObjectValue != null) {
                    currentMethodCall.addParameter(currentObjectValue);
                }
                currentObjectValue = new ObjectValue(split[1], factory);
                break;
            case KeyWord.ObjectParameterField:
                if(currentObjectValue != null) {
                    PrimitiveValue primitive;
                    if(split.length != 4) {
                        primitive = new PrimitiveValue(split[2], "", factory);
                    } else {
                        primitive = new PrimitiveValue(split[2], split[3], factory);
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
