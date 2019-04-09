package eu.stamp_project.utils.execution;

import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.listener.pit.AbstractPitResult;
import eu.stamp_project.utils.program.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/04/19
 */
public class PitRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PitRunner.class);

    public static List<?  extends AbstractPitResult> runPit(final String targetTest) {
        // TODO implement the choice between API and automatic builder
        LOGGER.info("Running PIT:");
        LOGGER.info("Classpath: {}", InputConfiguration.get().getFullClassPath());
        LOGGER.info("Path to project root: {}", InputConfiguration.get().getAbsolutePathToProjectRoot());
        LOGGER.info("Target classes: {}", InputConfiguration.get().getFilter());
        LOGGER.info("Target tests: {}", targetTest);
        return EntryPoint.runPit(
                InputConfiguration.get().getFullClassPath(),
                InputConfiguration.get().getAbsolutePathToProjectRoot(),
                InputConfiguration.get().getFilter(),
                targetTest
        );
    }

}
