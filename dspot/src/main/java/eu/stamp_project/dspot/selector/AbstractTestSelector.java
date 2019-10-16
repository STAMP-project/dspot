package eu.stamp_project.dspot.selector;

import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.utils.execution.TestRunner;
import eu.stamp_project.utils.program.InputConfiguration;

import static eu.stamp_project.utils.AmplificationHelper.PATH_SEPARATOR;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 04/10/19
 */
public abstract class AbstractTestSelector implements TestSelector {

    protected AutomaticBuilder automaticBuilder;

    protected TestRunner testRunner;

    protected String targetClasses;

    protected String classpath;

    protected String pathToTestClasses;

    protected String outputDirectory;

    public AbstractTestSelector(AutomaticBuilder automaticBuilder,
                                InputConfiguration configuration) {
        this.outputDirectory = configuration.getOutputDirectory();
        this.testRunner = new TestRunner(configuration.getAbsolutePathToProjectRoot(), configuration.getPreGoalsTestExecution(), configuration.shouldUseMavenToExecuteTest());
        this.automaticBuilder = automaticBuilder;
        String classpath = automaticBuilder.buildClasspath();
        if (!configuration.getAdditionalClasspathElements().isEmpty()) {
            classpath += PATH_SEPARATOR + configuration.getProcessedAddtionalClasspathElements();
        }
        this.targetClasses = configuration.getClasspathClassesProject();
        this.classpath = classpath;
        this.pathToTestClasses = configuration.getPathToTestClasses();
    }
}
