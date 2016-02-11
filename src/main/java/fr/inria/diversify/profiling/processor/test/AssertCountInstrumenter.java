package fr.inria.diversify.profiling.processor.test;

import fr.inria.diversify.util.Log;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtCodeSnippetStatementImpl;

public class AssertCountInstrumenter extends TestProcessor {
    protected int staticCount = 0;

    @Override
    public void process(CtMethod element) {

        CtCodeSnippetStatement snippetStatement = new CtCodeSnippetStatementImpl();
        String snippet =  getLogName() + ".assertCount(\"" + element.getSignature() + "\")";
        snippetStatement.setValue(snippet);

        Query.getElements(element, new TypeFilter<CtInvocation>(CtInvocation.class))
                .stream()
                .filter(call -> isAssert(call))
                .forEach(call -> {
                    staticCount++;
                    try {
                    call.insertBefore(snippetStatement);

                } catch (Exception e) {}
                });

        Log.debug("intru assert in test: {}, {}", element.getSignature(), staticCount);

    }

    protected boolean isAssert(CtInvocation invocation) {
        try {
            Class cl = invocation.getExecutable().getDeclaringType().getActualClass();

            return isAssertInstance(cl);
        } catch (Exception e) {
            return false;
        }

    }

    protected boolean isAssertInstance(Class cl) {
        if (cl.equals(org.junit.Assert.class) || cl.equals(junit.framework.Assert.class))
            return true;
        Class superCl = cl.getSuperclass();
        if(superCl != null) {
            return isAssertInstance(superCl);
        }
        return false;
    }
}