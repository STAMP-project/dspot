package eu.stamp_project.dspot.amplifier;

import org.apache.commons.compress.utils.Sets;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import java.util.Collections;
import java.util.Set;

public class BooleanLiteralAmplifier extends AbstractLiteralAmplifier<Boolean> {

    @Override
    protected Set<CtExpression<Boolean>> amplify(CtExpression<Boolean> original, CtMethod<?> testMethod) {
        final Factory factory = testMethod.getFactory();
        if (((CtLiteral<Boolean>)original).getValue() == null){
            return Sets.newHashSet(
                    factory.createLiteral(true),
                    factory.createLiteral(false)
            );
        }
        return Collections.singleton(factory.createLiteral(!((CtLiteral<Boolean>)original).getValue()));
    }

    @Override
    protected String getSuffix() {
        return "litBool";
    }

    @Override
    protected Class<?> getTargetedClass() {
        return Boolean.class;
    }
}
