package fr.inria.diversify.dspot.amplifier.value;

import fr.inria.diversify.Utils;
import fr.inria.diversify.dspot.AbstractTest;
import fr.inria.diversify.utils.AmplificationHelper;
import org.junit.Test;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.HashSet;
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
        assertEquals("java.util.Collections.emptyList()" ,
                CollectionCreator.generateCollection(
                        typeList,
                        "List",
                        String.class)
                        .toString()
        );
        assertEquals("java.util.Collections.singletonList(new fr.inria.statementadd.ClassParameterAmplify(1224731715))" ,
                CollectionCreator.generateCollection(
                        typeList,
                        "List",
                        String.class)
                        .toString()
        );

        final CtTypeReference typeCollectionObject = ((CtParameter)Utils.findMethod(
                "fr.inria.statementadd.ClassTarget",
                "getSizeOf")
                .getParameters()
                .get(0)
        ).getType();
        assertEquals("java.util.Collections.emptyList()" ,
                CollectionCreator.generateCollection(
                        typeCollectionObject,
                        "List",
                        String.class)
                        .toString()
        );
        assertEquals("java.util.Collections.emptyList()" ,
                CollectionCreator.generateCollection(
                        typeCollectionObject,
                        "List",
                        String.class)
                        .toString()
        );


        final CtTypeReference typeSet = ((CtParameter) Utils.findMethod(
                "fr.inria.statementadd.ClassTarget",
                "getSizeOfTypedCollection")
                .getParameters()
                .get(0)
        ).getType();

        assertEquals("java.util.Collections.emptySet()" ,
                CollectionCreator.generateCollection(
                        typeSet,
                        "Set",
                        String.class)
                        .toString()
        );

        assertEquals("java.util.Collections.singleton(new fr.inria.statementadd.ClassParameterAmplify(-1355944483))" ,
                CollectionCreator.generateCollection(
                        typeSet,
                        "Set",
                        String.class)
                        .toString()
        );


    }
}
