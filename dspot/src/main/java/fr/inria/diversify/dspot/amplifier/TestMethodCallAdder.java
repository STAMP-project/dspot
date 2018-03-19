package fr.inria.diversify.dspot.amplifier;

import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.Counter;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;



public class TestMethodCallAdder implements Amplifier {

    public List<CtMethod> apply(CtMethod method) {
        List<CtMethod> methods = new ArrayList<>();

        if (method.getDeclaringType() != null) {
            //get the list of method calls
            List<CtInvocation> invocations = Query.getElements(method, new TypeFilter(CtInvocation.class));
            //this index serves to replace ith literal is replaced by zero in the ith clone of the method
            int invocation_index = 0;
            for (CtInvocation invocation : invocations) {
                try {
                    if (AmplificationChecker.canBeAdded(invocation) && !AmplificationChecker.isAssert(invocation)) {
                        methods.add(apply(method, invocation_index));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                invocation_index++;
            }
        }
        return methods;
    }

    @Override
    public void reset(CtType testClass) {
        AmplificationHelper.reset();
    }

    private CtMethod apply(CtMethod method, int invocation_index) {
        CtMethod<?> cloned_method = AmplificationHelper.cloneTestMethodForAmp(method, "_add");
        //add the cloned method in the same class as the original method
        //get the lit_indexth literal of the cloned method
        CtInvocation stmt = Query.getElements(cloned_method, new TypeFilter<>(CtInvocation.class)).get(invocation_index);
        CtInvocation cloneStmt = stmt.clone();
        final CtStatement parent = getParent(stmt);
        parent.insertBefore(cloneStmt);
        cloneStmt.setParent(parent.getParent(CtBlock.class));
        Counter.updateInputOf(cloned_method, 1);
//        DSpotUtils.addComment(cloneStmt, "MethodCallAdder", CtComment.CommentType.INLINE);
        return cloned_method;
    }

    private CtStatement getParent(CtInvocation invocationToBeCloned) {
        CtElement parent = invocationToBeCloned;
        while (!(parent.getParent() instanceof CtBlock)){
            parent = parent.getParent();
        }
        return (CtStatement) parent;
    }


}
