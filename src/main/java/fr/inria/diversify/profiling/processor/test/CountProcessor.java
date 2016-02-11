package fr.inria.diversify.profiling.processor.test;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Simon on 03/02/15.
 */
public class CountProcessor extends TestProcessor {
    protected int testCount;
    protected Map<String, Integer> assertCount;
    protected int monitoringPointCount;

    public CountProcessor() {
        assertCount = new HashMap<>();
    }

    public boolean isToBeProcessed(CtMethod candidate) {
        return true;
    }

    @Override
    public void process(CtMethod method) {
        List<CtInvocation> invocations = Query.getElements(method, new TypeFilter(CtInvocation.class));
        testCount++;
        for(CtInvocation invocation : invocations) {
            if(isAssert(invocation)) {
                String testName = method.getDeclaringType().getQualifiedName() + "#" + method.getSimpleName();
                if(!assertCount.containsKey(testName)) {
                    assertCount.put(testName, 1);
                } else {
                    assertCount.put(testName, assertCount.get(testName) + 1);
                }

            }
            if(isMonitoringPoint(invocation)) {
                monitoringPointCount++;
            }
        }
    }


    protected boolean isMonitoringPoint(CtInvocation invocation) {
        return invocation.toString().contains(".logAssertArgument(");
    }

    public int getTestCount() {
        return testCount;
    }

    public int getAssertCount() {
        return assertCount.values().stream()
                .mapToInt(i -> i)
                .sum();
    }

    public int getMonitoringPointCount() {
        return monitoringPointCount;
    }

    public Map<String, Integer> getAssertPerTest() {
        return assertCount;
    }
}
