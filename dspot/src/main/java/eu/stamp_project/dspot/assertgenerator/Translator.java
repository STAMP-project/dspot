package eu.stamp_project.dspot.assertgenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 17/07/18
 *
 * This class aims at translating string to spoon node.
 * In fact, values provided by the observations are strings.
 * Here, we want to keep the semantic of the instruction
 *
 */
public class Translator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Translator.class);

    private final Factory factory;

    public Translator(Factory factory) {
        this.factory = factory;
    }

    /**
     * Translate the given string into a spoon node.
     * WARRANTY, this method has been developed to support only the assertion generation done by DSpot.
     * You can't use it unless the given string is in a good shape:
     *  - simple variable read
     *  - chained invocation: ((cast)((cast) o).getX())).getY()
     *  - invocation of isEmpty from Collection: ((cast) o).getX().isEmpty()
     * @param stringToBeTranslated
     * @return a spoon represented by the given String. This node is either a CtInvocation, either a VariableRead.
     */
    public CtExpression<?> translate(String stringToBeTranslated) {
        if (!stringToBeTranslated.contains("()")) { // this is not an invocation, it is a invocation.
            final CtVariableReference<?> variable = factory.createLocalVariableReference();
            variable.setSimpleName(stringToBeTranslated);
            return factory.createVariableRead(variable, false);
        } else {
            return buildInvocationFromString(stringToBeTranslated);
        }
    }

    public CtExpression<?> buildInvocationFromString(String invocationAsString) {
        final CtExpression<?> invocation = this.buildInvocationFromString(invocationAsString, null);
        if (invocation instanceof CtInvocation<?> &&
                "isEmpty".equals(((CtInvocation) invocation).getExecutable().getSimpleName())) {
            return invocation;
        }
        if (invocationAsString.endsWith("isEmpty()")) {
            final CtType<?> listCtType = factory.Type()
                    .get(java.util.List.class);
            final CtMethod<?> isEmpty = listCtType.getMethodsByName("isEmpty").get(0);
            return factory.createInvocation(
                    invocation,
                    isEmpty.getReference()
            );
        } else {
            return invocation;
        }
    }

    private CtExpression<?> buildInvocationFromString(String invocationAsString, CtInvocation<?> subInvocation) {
        CtInvocation invocation = factory.createInvocation();
        int end = invocationAsString.indexOf("()");
        int start = findMatchingIndex(invocationAsString, '.', end);
        final String executableName = invocationAsString.substring(start + 1, end);
        if (subInvocation == null) {
            end = start - 1; // i.e. the closing parenthesis
            start = findMatchingIndex(invocationAsString, ')', end);
            if (start == -1) {
                return buildTargetFromString(invocationAsString);
            }
            final CtLocalVariableReference<?> localVariableReference = factory.createLocalVariableReference();
            localVariableReference.setSimpleName(invocationAsString.substring(start + 1, end));
            final CtVariableAccess<?> variableRead = factory.createVariableRead(localVariableReference, false);
            invocation.setTarget(variableRead);
            end = start;
        } else {
            invocation.setTarget(subInvocation);
            end = start - 1;
        }
        start = findMatchingIndex(invocationAsString, '(', end);
        String fullQualifiedName = invocationAsString.substring(start + 1, end);
        CtType<?> ctType = getCtType(fullQualifiedName, factory);
        // handling inner types
        while(ctType == null) {
            final int lastIndexOf = fullQualifiedName.lastIndexOf(".");
            fullQualifiedName = fullQualifiedName.substring(0, lastIndexOf) + "$" + fullQualifiedName.substring(lastIndexOf + 1, fullQualifiedName.length());
            ctType = getCtType(fullQualifiedName, factory);
        }
        final CtTypeReference<?> reference = ctType.getReference();
        invocation.getTarget().addTypeCast(reference);
        // we are sure that there is only one, since we use only getters
        final Optional<CtMethod<?>> candidate = getAllMethods(ctType)
                .stream()
                .filter(ctMethod -> ctMethod.getSimpleName().equals(executableName))
                .findFirst();
        if (!candidate.isPresent()) {
            LOGGER.error("Could not find {} in {}.", executableName, ctType.getQualifiedName());
        }
        final CtMethod<?> getter = candidate.get();
        final CtExecutableReference<?> referenceToGetter = getter.getReference();
        invocation.setExecutable(referenceToGetter);
        if (start != 1) {
            final String substringToBeRemove = invocationAsString.substring(start - 2, invocationAsString.indexOf("()") + 2);
            invocationAsString = invocationAsString.replace(substringToBeRemove, "");
            return buildInvocationFromString(invocationAsString, invocation);
        } else {
            return invocation;
        }
    }

    // TODO this a hotfix, and must be reworked. The issue has been reported in Spoon see: https://github.com/INRIA/spoon/issues/2378
    @Deprecated
    private Set<CtMethod<?>> getAllMethods(CtType<?> type) {
        try {
            return type.getAllMethods();
        } catch (StackOverflowError e) {
            LOGGER.error("Overflow!");
            final Set<CtMethod<?>> methods = new HashSet<>(type.getMethods());
            CtTypeReference<?> currentSuperClass = type.getSuperclass();
            while (currentSuperClass != null) {
                methods.addAll(currentSuperClass.getTypeDeclaration().getMethods());
                currentSuperClass = currentSuperClass.getSuperclass();
            }
            return methods;
        }
    }

    private CtExpression<?> buildTargetFromString(String invocationAsString) {
        final CtLocalVariableReference<?> localVariableReference = factory.createLocalVariableReference();
        localVariableReference.setSimpleName(invocationAsString.split("\\.")[0]);
        return factory.createVariableRead(localVariableReference, false);
    }

    private CtType<?> getCtType(String fullQualifiedName, Factory factory) {
        CtType<?> ctType = factory.Type().get(fullQualifiedName);
        if (ctType == null) {
            try {
                ctType = factory.Type().get(
                        factory.getEnvironment().getInputClassLoader().loadClass(fullQualifiedName)
                );
            } catch (ClassNotFoundException ignored) {

            }
        }
        return ctType;
    }

    private int findMatchingIndex(String stringToBeMatched, char charToBeMatched, int start) {
        --start;
        while (start != -1 && stringToBeMatched.charAt(start) != charToBeMatched) {
               --start;
        }
        return start;
    }

}
