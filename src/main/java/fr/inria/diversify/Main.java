package fr.inria.diversify;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.DSpotUtils;
import fr.inria.diversify.dspot.amplifier.TestDataMutator;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.util.Log;

import java.io.File;
import java.util.Collections;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/4/17
 */
public class Main {

    public static void main(String[] args) throws Exception, InvalidSdkException {
        if (args.length == 0) {
            System.out.println(
                    new DSpot(new InputConfiguration("src/test/resources/test-projects/test-projects.properties"), 1, Collections.singletonList(new TestDataMutator()))
                            .amplifyTest("example.TestSuiteExample"));
        } else {
            if (!new File(args[0]).exists()) {
                Log.error("Could not find {}", args[0]);
                System.exit(-1);
            } else {
                InputConfiguration configuration = new InputConfiguration(args[0]);
                DSpot dspot = new DSpot(configuration, 1, Collections.singletonList(new TestDataMutator()));
                final File outputDirectory = new File(configuration.getOutputDirectory());
                dspot.amplifyAllTests().forEach(test -> {
                    try {
                        DSpotUtils.printJavaFileWithComment(test, outputDirectory);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
        System.exit(0);
    }

}
