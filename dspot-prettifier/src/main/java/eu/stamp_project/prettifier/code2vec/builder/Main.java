package eu.stamp_project.prettifier.code2vec.builder;

import eu.stamp_project.utils.DSpotUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/12/18
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static final String ROOT_PATH_DATA = "dspot-prettifier/data/files/";

    public static void main(String[] args) {
        // init values
        final Output output = new Output();
        final Cloner cloner = new Cloner();
        final Report report = new Report(cloner);
        final List<String> userAndProjectList = Reader.readAll();
        for (String userAndProject : userAndProjectList) {
            if (report.containsProject(userAndProject)) {
                LOGGER.info("{} is already in the database: {}",
                        userAndProject,
                        report.getNumbersOfMethodsPerSet(userAndProject).toString()
                );
                LOGGER.info("skipping...");
                continue;
            }
            DSpotUtils.printProgress(userAndProjectList.indexOf(userAndProject), userAndProjectList.size());
            final Path path;
            try {
                path = cloner.clone(userAndProject);
            } catch (Exception e) {
                e.printStackTrace();
                report.addToGivenMap(report.getErrorDuringCloning(), userAndProject, e);
                continue;
            }
            try {
                final List<CtType<?>> testClasses =
                        TestMethodsExtractor.extractAllTestMethodsForGivenProject(path.toString());
                if (!testClasses.isEmpty()) {
                    final int numberOfTestMethods = testClasses.stream()
                            .map(CtType::getMethods)
                            .mapToInt(Set::size)
                            .sum();
                    final NumbersOfMethodsPerSet numbersOfMethodsPerSet = output.output(testClasses, numberOfTestMethods);
                    report.addNumberOfMethodsPerSetPerProject(userAndProject, numbersOfMethodsPerSet);
                    LOGGER.info("{}: {}", userAndProject, numbersOfMethodsPerSet.toString());
                } else {
                    LOGGER.warn("{}: no test could be found!", userAndProject);
                }
                cloner.output(); // add current sha to shaPerProject file
                report.report(); // tmp output
            } catch (Exception e) {
                e.printStackTrace();
                report.addToGivenMap(report.getErrorDuringMethodExtraction(), userAndProject, e);
                continue;
            }
        }
        report.report();
    }

}
