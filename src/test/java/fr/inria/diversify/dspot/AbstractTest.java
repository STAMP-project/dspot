package fr.inria.diversify.dspot;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.util.FileUtils;
import org.junit.AfterClass;

import java.io.FileNotFoundException;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/23/16
 */
public abstract class AbstractTest {

    @AfterClass
    public static void tearDown() throws InvalidSdkException, Exception {
        try {
            FileUtils.forceDelete(Utils.getCompiler().getBinaryOutputDirectory());
        } catch (FileNotFoundException | IllegalArgumentException ignored) {}
        try {
            FileUtils.forceDelete(Utils.getCompiler().getSourceOutputDirectory());
        } catch (FileNotFoundException | IllegalArgumentException ignored) {}
        Utils.reset();
    }

}
