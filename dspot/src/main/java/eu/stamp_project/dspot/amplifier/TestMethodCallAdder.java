package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.utils.AmplificationChecker;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.Counter;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.stream.Stream;


public class TestMethodCallAdder implements Amplifier {

    public Stream<CtMethod<?>> apply(CtMethod<?> method) {
        if (method.getDeclaringType() != null) {
            final List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
                @Override
                public boolean matches(CtInvocation<?> element) {
                    return AmplificationChecker.canBeAdded(element) && !AmplificationChecker.isAssert(element);
                }
            });
            return invocations.stream().map(invocation -> apply(method, invocations.indexOf(invocation)));
        } else {
            return Stream.empty();
        }
    }

    @Override
    public void reset(CtType<?> testClass) {
        AmplificationHelper.reset();
    }

    private CtMethod<?> apply(CtMethod<?> method, int invocation_index) {
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
