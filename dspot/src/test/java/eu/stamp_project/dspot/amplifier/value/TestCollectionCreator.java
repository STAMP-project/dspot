package eu.stamp_project.dspot.amplifier.value;

import eu.stamp_project.Utils;
import eu.stamp_project.AbstractTest;
import org.junit.Test;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/10/17
 */
public class TestCollectionCreator extends AbstractTest {

    @Test
    public void testCreateCollection() throws Exception {
        final CtTypeReference typeList = Utils.findMethod("fr.inria.statementadd.ClassTarget", "getList").getType();
        assertEquals("java.util.Collections.<fr.inria.statementadd.ClassParameterAmplify>emptyList()" ,
                CollectionCreator.generateCollection(
                        typeList,
                        "List",
                        List.class)
                        .toString()
        );
        assertEquals("java.util.Collections.singletonList(new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(-538589801)), 562520686), 1085381857)))" ,
                CollectionCreator.generateCollection(
                        typeList,
                        "List",
                        List.class)
                        .toString()
        );

        assertEquals("java.util.Collections.<java.lang.Object>emptyList()" ,
                CollectionCreator.generateCollection(
                        Utils.getFactory().Type().get(Object.class).getReference(),
                        "List",
                        List.class)
                        .toString()
        );

        final CtTypeReference typeSet = ((CtParameter) Utils.findMethod(
                "fr.inria.statementadd.ClassTarget",
                "getSizeOfTypedCollection")
                .getParameters()
                .get(0)
        ).getType();

        assertEquals("java.util.Collections.<fr.inria.statementadd.ClassParameterAmplify>emptySet()" ,
                CollectionCreator.generateCollection(
                        typeSet,
                        "Set",
                        Set.class)
                        .toString()
        );

        // This done because of the randomness
        CollectionCreator.generateCollection(
                typeSet,
                "Set",
                Set.class)
                .toString();

        assertEquals("java.util.Collections.<fr.inria.statementadd.ClassParameterAmplify>emptySet()" ,
                CollectionCreator.generateCollection(
                        typeSet,
                        "Set",
                        Set.class)
                        .toString()
        );


    }
}
