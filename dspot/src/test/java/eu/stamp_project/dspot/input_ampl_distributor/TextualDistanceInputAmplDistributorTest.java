package eu.stamp_project.dspot.input_ampl_distributor;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.utils.options.InputConfiguration;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/08/18
 */
public class TextualDistanceInputAmplDistributorTest extends AbstractTest {

    @After
    public void tearDown() throws Exception {
        InputConfiguration.get().setMaxTestAmplified(200);
    }

    // TODO
    @Ignore
    @Test
    public void testReduction() throws Exception {

        /*
            test that the reduction, using hashcode is correct.
            The method should return a list with different test
         */

        InputConfiguration.get().setMaxTestAmplified(2);

        final CtMethod methodString = Utils.findMethod("fr.inria.amp.LiteralMutation", "methodString");
        // very different
        final CtMethod methodInteger = Utils.findMethod("fr.inria.amp.LiteralMutation", "methodInteger");

        List<CtMethod<?>> methods = new ArrayList<>();
        methods.add(methodString);
        methods.add(methodString);
        methods.add(methodString);
        methods.add(methodString);
        methods.add(methodString);
        methods.add(methodString);
        methods.add(methodString);
        methods.add(methodString);
        final CtMethod clone = methodString.clone();
        final CtLiteral originalLiteral = clone.getElements(new TypeFilter<>(CtLiteral.class)).get(0);
        originalLiteral.replace(Utils.getFactory().createLiteral(originalLiteral.getValue() + "a"));
        methods.add(clone);
        methods.add(clone);
        methods.add(clone);
        methods.add(methodInteger);

        final List<CtMethod<?>> reduce = new TextualDistanceInputAmplDistributor().reduce(methods);
        assertEquals(2, reduce.size());

    }

}
