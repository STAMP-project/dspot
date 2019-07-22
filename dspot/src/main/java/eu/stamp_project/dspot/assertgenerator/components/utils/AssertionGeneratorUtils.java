package eu.stamp_project.dspot.assertgenerator.components.utils;

import eu.stamp_project.utils.program.InputConfiguration;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtTypeReference;

import java.io.File;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 3/3/17
 */
public class AssertionGeneratorUtils {

    public static boolean canGenerateAnAssertionFor(String candidate) {
        return !containsObjectReferences(candidate) &&
                (InputConfiguration.get().shouldAllowPathInAssertion() || !containsAPath(candidate));
    }

    public static boolean isVoidReturn(CtInvocation invocation) {
        return (invocation.getType().equals(invocation.getFactory().Type().voidType()) ||
                invocation.getType().equals(invocation.getFactory().Type().voidPrimitiveType()));
    }

    public static CtTypeReference getCorrectTypeOfInvocation(CtInvocation<?> invocation) {
        final CtTypeReference type = invocation.getType().clone();
        type.getActualTypeArguments().removeIf(CtTypeReference::isGenerics);
        return type;
    }

    public final static String METADATA_WAS_IN_ASSERTION = "Was-Asserted";

    public final static String METADATA_ASSERT_AMPLIFICATION = "A-Amplification";

    public static CtStatement getTopStatement(CtElement start) {
        CtElement topStatement = start;
        while (!(topStatement.getParent() instanceof CtStatementList)) {
            topStatement = topStatement.getParent();
        }
        return (CtStatement) topStatement;
    }

    public static boolean containsAPath(String candidate) {
        if (candidate == null) {
            return false;
        }
        if (new File(candidate).exists()) {
            return true;
        }

        String[] split = candidate.split(" ");
        final Pattern pattern = Pattern.compile(".*((.*/)+).*");
        for (String s : split) {
            if (s.length() < 4096 &&
                    pattern.matcher(s).matches()) {
                return true;
            }
        }

        try {
            new URL(candidate);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean containsObjectReferences(String candidate) {
        return candidate != null &&
                Pattern.compile("(\\w+\\.)*\\w@[a-f0-9]+").matcher(candidate).find();
    }
}
