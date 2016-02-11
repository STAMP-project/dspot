package fr.inria.diversify.profiling.processor.test;

import fr.inria.diversify.util.Log;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Simon on 17/12/14.
 */
public class GuavaProcessor extends AbstractProcessor<CtClass> {
    String testDirectory;
    public static Set<CtClass> ampclasses = new HashSet<>();


    public GuavaProcessor(String testDirectory) {
        this.testDirectory = testDirectory;
    }


    public boolean isToBeProcessed(CtClass candidate) {
        return candidate.getPosition().getFile().getAbsolutePath().contains(testDirectory);
    }

    @Override
    public void process(CtClass element) {
        Map<CtMethod, List<CtMethod>> map = new HashMap<>();
        for(Object o : element.getMethods()) {
            CtMethod mth = (CtMethod) o;
            List<CtMethod> amp = findAllAmp(mth);
            if(!amp.isEmpty()) {
                map.put(mth, amp);
            }

        }
        int count = 0;
        if(!map.isEmpty()) {
            Log.debug("GuavaProcessor: {}", element.getSimpleName());
        }
        while (!map.isEmpty() && count < 151) {
            CtClass ampClass = this.getFactory().Core().clone(element);
            ampClass.setSimpleName("Test_"+count+"_"+element.getSimpleName());
            count++;
            element.getPackage().addType(ampClass);
            ampClass.setParent(element.getPackage());
            ampclasses.add(ampClass);
            for(Object o : Query.getElements(ampClass, new TypeFilter(CtFieldAccess.class))) {
                CtFieldAccess access = (CtFieldAccess) o;
                if(access.getVariable().getType().getSimpleName().equals(element.getSimpleName())) {
                    CtCodeSnippetExpression newAccess = this.getFactory().Core().createCodeSnippetExpression();
                    newAccess.setValue(ampClass.getQualifiedName() + "." + access.getVariable().getSimpleName());
                    access.replace(newAccess);
                }
            }
            for(CtMethod originalMethod : map.keySet()) {
                try {
                    if (!map.get(originalMethod).isEmpty()) {
                        CtMethod ampMethod = this.getFactory().Core().clone(map.get(originalMethod).remove(0));

                        CtMethod toRemove = ampClass.getMethod(originalMethod.getSimpleName());
                        ampMethod.setSimpleName(originalMethod.getSimpleName());

                        ampClass.addMethod(ampMethod);

                        for (CtMethod mth : TestProcessor.mutatedMethod) {
                            CtMethod rm = ampClass.getMethod(mth.getSimpleName());
                            if(rm != null) {
                                ampClass.removeMethod(rm);
                            }
                        }
                        if(toRemove != null) {
                            ampClass.removeMethod(toRemove);
                        }
                        ampClass.addMethod(ampMethod);
                    }
                } catch (Exception e) {
                    Log.debug("");
                }
            }
            List<CtMethod> toRemove = map.keySet().stream()
                    .filter(mth -> map.get(mth).isEmpty())
                    .collect(Collectors.toList());

            toRemove.stream()
                .forEach(mth -> map.remove(mth));
        }
    }

    protected List<CtMethod> findAllAmp(CtMethod method) {
        if(TestProcessor.mutatedMethod.contains(method)) {
            return new ArrayList<>();
        }
        CtType<?> type = method.getDeclaringType();
        String methodName = method.getSimpleName();

        try {
            return TestProcessor.mutatedMethod.stream()
                    .filter(mth -> mth.getDeclaringType().equals(type))
                    .filter(mth -> mth.getSimpleName().startsWith(methodName))
                    .filter(mth -> mth.getParameters().equals(method.getParameters()))
                    .collect(Collectors.toList());
        }  catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
