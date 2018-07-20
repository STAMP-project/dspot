package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.utils.AmplificationChecker;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.Counter;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.stream.Stream;


public class TestMethodCallRemover implements Amplifier {

    public Stream<CtMethod<?>> amplify(CtMethod<?> method, int iteration) {
        if (method.getDeclaringType() != null) {
            final List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
                @Override
                public boolean matches(CtInvocation<?> invocation) {
                    return toRemove(invocation)
                            && !AmplificationChecker.isAssert(invocation)
                            && !inWhileLoop(invocation)
                            && !containsIteratorNext(invocation);
                }
            });
            return invocations.stream().map(invocation -> apply(method, invocations.indexOf(invocation)));
        } else {
            return Stream.empty();
        }
    }

    @Override
    public void reset(CtType testClass) {
        AmplificationHelper.reset();
    }

    private CtMethod<?> apply(CtMethod<?> method, int invocation_index) {
        //clone the method
        CtMethod<?> cloned_method = AmplificationHelper.cloneTestMethodForAmp(method, "_remove");
        //get the lit_indexth literal of the cloned method
        CtInvocation stmt = Query.getElements(cloned_method, new TypeFilter<>(CtInvocation.class)).get(invocation_index);
        CtBlock b = ((CtBlock) stmt.getParent());
//        DSpotUtils.addComment(b, "removed " + stmt.toString() + " at line " + stmt.getPosition().getLine(), CtComment.CommentType.INLINE);
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
