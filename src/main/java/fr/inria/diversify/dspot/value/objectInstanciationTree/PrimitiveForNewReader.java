package fr.inria.diversify.dspot.value.objectInstanciationTree;

import fr.inria.diversify.logger.KeyWord;
import fr.inria.diversify.dspot.value.PrimitiveValue;
import fr.inria.diversify.dspot.value.ValueFactory;
import fr.inria.diversify.dspot.value.ValueType;
import fr.inria.diversify.log.LogParser;
import fr.inria.diversify.processor.ProcessorUtil;
import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 29/08/16
 * Time: 13:58
 */
public class PrimitiveForNewReader extends LogParser<Map<String, ValueType>> {
    protected InputProgram inputProgram;
    protected ValueFactory valueFactory;


    public PrimitiveForNewReader(InputProgram inputProgram, ValueFactory valueFactory) {
        this.inputProgram = inputProgram;
        this.valueFactory = valueFactory;
        this.result = new HashMap<>();
    }

    @Override
    public void readLogLine(String line) {
        if (line.startsWith(KeyWord.primitiveKeyWord)) {
            String[] tmp = line.split(KeyWord.simpleSeparator);
            Integer mthId = Integer.parseInt(tmp[1]);
            Integer constructorId = Integer.parseInt(tmp[2]);
            ObjectInstantiation objectInstantiation = findConstructorCall(mthId, constructorId);

            if(objectInstantiation != null) {
                Integer paramIndex = Integer.parseInt(tmp[3]);
                String key = ids.get(constructorId) + "_" + paramIndex;
                if(!result.containsKey(key)) {
                    CtTypeReference paramType = (CtTypeReference) objectInstantiation.getParameters().get(paramIndex);
                    result.put(key, valueFactory.getValueType(paramType));
                }
                ValueType pi = result.get(key);
                PrimitiveValue value;
                if(tmp.length != 4) {
                    value = valueFactory.getPrimitiveValue(pi, tmp[4]);
                } else {
                    value = valueFactory.getPrimitiveValue(pi, "");
                }
                objectInstantiation.addParameterValue(value, paramIndex);
            }
        }
    }

    @Override
    public void init(File file) throws IOException {}

    @Override
    public void newLogFile(File file) {}

    Map<Integer, ObjectInstantiation> idToConstructor = new HashMap<>();
    Map<Integer, List<CtExecutableReference>> mthIdToconstructorCalls = new HashMap<>();

    protected ObjectInstantiation findConstructorCall(int mthId, int constructorId) {
        if (!idToConstructor.containsKey(constructorId)) {
            if (!mthIdToconstructorCalls.containsKey(mthId)) {
                String mthName = ids.get(mthId);
                List<CtExecutable> mths = getMethodOrcConstrutors();
                CtExecutable mth = mths.stream()
                        .filter(m -> mthName.contains(m.getSimpleName()))
                        .filter(m -> mthName.startsWith(m.getReference().getDeclaringType().getQualifiedName()))
                        .filter(m -> ProcessorUtil.methodString(m).equals(mthName))
                        .findAny()
                        .orElse(null);
                if(mth != null) {
                    List<CtConstructorCall> constructorCalls = Query.getElements(mth, new TypeFilter<CtConstructorCall>(CtConstructorCall.class));
                    mthIdToconstructorCalls.put(mthId, constructorCalls.stream()
                            .map(cc -> cc.getExecutable())
                            .collect(Collectors.toList()));
                } else {
                    mthIdToconstructorCalls.put(mthId, null);
                }
            }
            String constructorName = ids.get(constructorId);
            List<CtExecutableReference> constructorCalls = mthIdToconstructorCalls.get(mthId);
            if(constructorCalls != null) {
                CtExecutableReference constructor = constructorCalls.stream()
                        .filter(cc -> cc.toString().equals(constructorName))
                        .findAny()
                        .get();

                idToConstructor.put(constructorId, valueFactory.getObjectInstantiation(constructor));
            } else {
                idToConstructor.put(constructorId, null);
            }
        }
        return idToConstructor.get(constructorId);
    }

    protected List<CtExecutable> methodOrcConstrutors;
    protected List<CtExecutable>  getMethodOrcConstrutors() {
        if(methodOrcConstrutors == null) {
            methodOrcConstrutors = new ArrayList<>();
            for(CtType type : inputProgram.getFactory().Class().getAll(true)) {
                try {
                    methodOrcConstrutors.addAll(type.getMethods());
                    methodOrcConstrutors.addAll(((CtClass) type).getConstructors());
                } catch (Exception e) {}
            }
        }
        return methodOrcConstrutors;
    }
}
