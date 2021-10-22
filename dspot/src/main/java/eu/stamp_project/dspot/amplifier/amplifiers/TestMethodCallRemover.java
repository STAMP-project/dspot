package eu.stamp_project.dspot.amplifier.amplifiers;

import eu.stamp_project.dspot.common.configuration.options.CommentEnum;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.test_framework.TestFramework;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.miscellaneous.CloneHelper;
import eu.stamp_project.dspot.common.miscellaneous.Counter;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
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
                            && !TestFramework.get().isAssert(invocation)
                            && !inWhileLoop(invocation)
                            && !containsIteratorNext(invocation);
                }
            });
            return invocations.stream().map(invocation -> apply(method, invocation));
        } else {
            return Stream.empty();
        }
    }

    @Override
    public void reset(CtType testClass) {
        AmplificationHelper.reset();
    }

    private CtMethod<?> apply(CtMethod<?> method, CtInvocation<?> invocation) {
        final CtStatementList ctStatementList = AmplifierHelper.getParent(invocation);
        final int indexOfInvocation = ctStatementList.getStatements().indexOf(invocation) - 1;
        ctStatementList.removeStatement(invocation);
        invocation.delete();
        final CtMethod<?> cloned = CloneHelper.cloneTestMethodForAmp(method, "_remove");
        if (indexOfInvocation == -1) {
            ctStatementList.insertBegin(invocation);
        } else {
            ctStatementList.getStatements().get(indexOfInvocation).insertAfter(invocation);
        }
        Counter.updateInputOf(cloned, 1);
        DSpotUtils.addComment(ctStatementList,"MethodCallRemover: removed call '" + invocation + "'",
                CtComment.CommentType.INLINE, CommentEnum.Amplifier);
        return cloned;
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
