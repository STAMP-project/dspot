package fr.inria.diversify.dspot.processor;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;


public class TestMethodCallAdder extends AbstractAmp {

    public List<CtMethod> apply(CtMethod method) {
        List<CtMethod> methods = new ArrayList<>();

        if (method.getDeclaringType() != null) {
            //get the list of method calls
            List<CtInvocation> invocations = Query.getElements(method, new TypeFilter(CtInvocation.class));
            //this index serves to replace ith literal is replaced by zero in the ith clone of the method
            int invocation_index = 0;
            for (CtInvocation invocation : invocations) {
                try {
                    if (toAdd(invocation) && !isAssert(invocation)) {
                        methods.add(apply(method, invocation_index));
                    }
                } catch (Exception e) {
                }
                invocation_index++;
            }
        }
        return filterAmpTest(methods, method);
    }

    public CtMethod applyRandom(CtMethod method) {
        if (method.getDeclaringType() != null) {
            List<CtInvocation> invocations = Query.getElements(method, new TypeFilter(CtInvocation.class));

            while (!invocations.isEmpty()) {
                try {
                    int invocation_index = getRandom().nextInt(invocations.size());
                    return apply(method, invocation_index);
                } catch (Exception e) {
                }
            }
        }
        return null;
    }

    protected CtMethod apply(CtMethod method, int invocation_index) {
        CtMethod cloned_method = cloneMethodTest(method, "_add", 1000);
        //add the cloned method in the same class as the original method
        //get the lit_indexth literal of the cloned method
        CtInvocation stmt = Query.getElements(cloned_method, new TypeFilter<CtInvocation>(CtInvocation.class)).get(invocation_index);
        CtInvocation cloneStmt = method.getFactory().Core().clone(stmt);
        cloneStmt.setParent(stmt.getParent());
        stmt.insertBefore(cloneStmt);

        return cloned_method;
    }

    public boolean toAdd(CtInvocation invocation) {
        return !invocation.toString().startsWith("super(")
                && invocation.getParent() instanceof CtBlock;
    }
}