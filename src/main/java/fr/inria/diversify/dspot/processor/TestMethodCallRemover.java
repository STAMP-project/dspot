package fr.inria.diversify.dspot.processor;

import fr.inria.diversify.profiling.coverage.Coverage;
import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;


public class TestMethodCallRemover extends AbstractAmp {

	protected boolean toRemove(CtInvocation invocation) {
		return invocation.getParent() instanceof CtBlock
				&& invocation.getParent(CtTry.class) == null;
	}

	protected boolean inWhileLoop(CtStatement stmt) {
		return stmt.getParent(CtWhile.class) != null;
	}

	protected boolean containsIteratorNext(CtStatement stmt) {
		return stmt.toString().contains(".next()");
	}

    public List<CtMethod> apply(CtMethod method) {
        List<CtMethod> methods = new ArrayList<>();

        if (method.getDeclaringType() != null) {
            //get the list of method calls
            List<CtInvocation> invocations = Query.getElements(method, new TypeFilter(CtInvocation.class));
            //this index serves to replace ith literal is replaced by zero in the ith clone of the method
            int invocation_index = 0;
            for(CtInvocation invocation : invocations){
                try{
                    if(toRemove(invocation)
                            && !isAssert(invocation)
                            && !inWhileLoop(invocation)
                            && !containsIteratorNext(invocation)) {
                     methods.add(apply(method, invocation_index));
                    }
                } catch(Exception e){}
                invocation_index++;
            }
        }
        return filterAmpTest(methods, method);
    }

    public CtMethod applyRandom(CtMethod method) {
        if (method.getDeclaringType() != null) {
            List<CtInvocation> invocations = Query.getElements(method, new TypeFilter(CtInvocation.class));

            while(!invocations.isEmpty()) {
                try {
                    int invocation_index = getRandom().nextInt(invocations.size());
                    return apply(method, invocation_index);
                } catch (Exception e) {}
            }
        }
        return null;
    }

    protected CtMethod apply(CtMethod method, int invocation_index) {
        //clone the method
        CtMethod cloned_method = cloneMethodTest(method, "_remove",1000);

            //get the lit_indexth literal of the cloned method
            CtInvocation stmt = Query.getElements(cloned_method, new TypeFilter<CtInvocation>(CtInvocation.class)).get(invocation_index);
            CtBlock b = ((CtBlock) stmt.getParent());
            b.removeStatement(stmt);

            return cloned_method;
    }

    public void reset(InputProgram inputProgram, Coverage coverage, CtClass testClass) {
        super.reset(inputProgram, coverage, testClass);
    }
}
