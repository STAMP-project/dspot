package eu.stamp_project.utils.execution;

import eu.stamp_project.Main;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.listener.pit.AbstractParser;
import eu.stamp_project.testrunner.listener.pit.AbstractPitResult;
import eu.stamp_project.testrunner.listener.pit.PitCSVResultParser;
import eu.stamp_project.testrunner.listener.pit.PitXMLResultParser;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.report.error.Error;
import eu.stamp_project.utils.report.error.ErrorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/04/19
 */
public class PitRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PitRunner.class);

    private AbstractParser<?> parser;

    public PitRunner() {
        this(eu.stamp_project.testrunner.listener.pit.AbstractParser.OutputFormat.XML);
    }

    public PitRunner(eu.stamp_project.testrunner.listener.pit.AbstractParser.OutputFormat format) {
        switch (format) {
            case XML:
                parser = new PitXMLResultParser();
                break;
            case CSV:
                parser = new PitCSVResultParser();
                break;
        }
    }

    private static final Function<CtType<?>[], String> ctTypeToFullQualifiedName = types ->
            Arrays.stream(types)
                .map(DSpotUtils::ctTypeToFullQualifiedName)
                .collect(Collectors.joining(","));

    public List<?  extends AbstractPitResult> runPit(CtType<?>... testClasses) {
        LOGGER.info("Running PIT:");
        LOGGER.info("Classpath: {}", InputConfiguration.get().getFullClassPath());
        LOGGER.info("Path to project root: {}", InputConfiguration.get().getAbsolutePathToProjectRoot());
        LOGGER.info("Target classes: {}", InputConfiguration.get().getPitFilterClassesToKeep());
        String targetTestNames = testClasses.length == 0 ? // when no test class is specified, we reuse the value of ConstantsProperties#PIT_FILTER_CLASSES_TO_KEEP
                InputConfiguration.get().getPitFilterClassesToKeep() : ctTypeToFullQualifiedName.apply(testClasses);
        LOGGER.info("Target tests: {}", targetTestNames);
        try {
            if (InputConfiguration.get().shouldUseMavenToExecuteTest()) {
                InputConfiguration.get().getBuilder().runPit(testClasses);
                return this.parser.parseAndDelete(
                        InputConfiguration.get().getAbsolutePathToProjectRoot() + InputConfiguration.get().getBuilder().getOutputDirectoryPit()
                );
            } else {
                return EntryPoint.runPit(
                        InputConfiguration.get().getFullClassPath(),
                        InputConfiguration.get().getAbsolutePathToProjectRoot(),
                        InputConfiguration.get().getPitFilterClassesToKeep(),
                        targetTestNames
                );
            }
        } catch (Throwable e) {
            LOGGER.error(ErrorEnum.ERROR_ORIGINAL_MUTATION_SCORE.getMessage());
            Main.GLOBAL_REPORT.addError(new Error(ErrorEnum.ERROR_ORIGINAL_MUTATION_SCORE, e));
            return null;
        }
    }

}
