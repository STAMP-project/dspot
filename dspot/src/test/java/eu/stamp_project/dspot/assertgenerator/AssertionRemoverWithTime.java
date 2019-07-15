package eu.stamp_project.dspot.assertgenerator;

import eu.stamp_project.dspot.assertgenerator.components.AssertionRemover;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtMethod;

import java.util.List;
import java.util.Map;

public class AssertionRemoverWithTime extends AssertionRemover {

        public long timeGetVariableAssertedPerTestMethod;
        public long timeRemoveAssertionMethod;
        public long timeRemoveAssertionInvocation;

        public AssertionRemoverWithTime() {
            this.reset();
        }

        void reset() {
            this.timeGetVariableAssertedPerTestMethod = 0L;
            this.timeRemoveAssertionMethod = 0L;
            this.timeRemoveAssertionInvocation = 0L;
        }

        @Override
        public Map<CtMethod<?>, List<CtLocalVariable<?>>> getVariableAssertedPerTestMethod() {
            final long time = System.currentTimeMillis();
            final Map<CtMethod<?>, List<CtLocalVariable<?>>> variableAssertedPerTestMethod = super.getVariableAssertedPerTestMethod();
            this.timeGetVariableAssertedPerTestMethod += System.currentTimeMillis() - time;
            return variableAssertedPerTestMethod;
        }

        @Override
        public CtMethod<?> removeAssertion(CtMethod<?> testMethod) {
            final long time = System.currentTimeMillis();
            final CtMethod<?> ctMethod = super.removeAssertion(testMethod);
            this.timeRemoveAssertionMethod += System.currentTimeMillis() - time;
            return ctMethod;
        }

        @Override
        public List<CtLocalVariable<?>> removeAssertion(CtInvocation<?> invocation) {
            final long time = System.currentTimeMillis();
            final List<CtLocalVariable<?>> ctLocalVariables = super.removeAssertion(invocation);
            this.timeRemoveAssertionInvocation += System.currentTimeMillis() - time;
            return ctLocalVariables;
        }
    }
