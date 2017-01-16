package fr.inria.diversify.dspot.amplifier;

import fr.inria.diversify.dspot.AmplificationChecker;
import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.support.Counter;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;


public class TestMethodCallRemover implements Amplifier {

    public List<CtMethod> apply(CtMethod method) {
        List<CtMethod> methods = new ArrayList<>();

        if (method.getDeclaringType() != null) {
            //get the list of method calls
            List<CtInvocation> invocations = Query.getElements(method, new TypeFilter(CtInvocation.class));
            //this index serves to replace ith literal is replaced by zero in the ith clone of the method
            int invocation_index = 0;
            for (CtInvocation invocation : invocations) {
                try {
                    if (toRemove(invocation)
                            && !AmplificationChecker.isAssert(invocation)
                            && !inWhileLoop(invocation)
                            && !containsIteratorNext(invocation)) {
                        methods.add(apply(method, invocation_index));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                invocation_index++;
            }
        }
        return AmplificationHelper.updateAmpTestToParent(methods, method);
    }

    public CtMethod applyRandom(CtMethod method) {
        if (method.getDeclaringType() != null) {
            List<CtInvocation> invocations = Query.getElements(method, new TypeFilter(CtInvocation.class));
            if (!invocations.isEmpty()) {
                try {
                    int invocation_index = AmplificationHelper.getRandom().nextInt(invocations.size());
                    return apply(method, invocation_index);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    @Override
    public void reset(CtType testClass) {
        AmplificationHelper.reset();
    }

    private CtMethod apply(CtMethod method, int invocation_index) {
        //clone the method
        CtMethod cloned_method = AmplificationHelper.cloneMethodTest(method, "_remove", 1000);

        //get the lit_indexth literal of the cloned method
        CtInvocation stmt = Query.getElements(cloned_method, new TypeFilter<CtInvocation>(CtInvocation.class)).get(invocation_index);
        CtBlock b = ((CtBlock) stmt.getParent());
        b.removeStatement(stmt);

        Counter.updateInputOf(cloned_method, 1);

        return cloned_method;
    }

    private boolean toRemove(CtInvocation invocation) {
        return invocation.getParent() instanceof CtBlock
                && invocation.getParent(CtTry.class) == null;
    }

    private boolean inWhileLoop(CtStatement stmt) {
        return stmt.getParent(CtWhile.class) != null;
    }

    private boolean containsIteratorNext(CtStatement stmt) {
        return stmt.toString().contains(".next()");
    }
}
