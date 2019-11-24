package eu.stamp_project.utils.configuration;

import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.automaticbuilder.maven.DSpotPOMCreator;
import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.dspot.assertiongenerator.AssertionGenerator;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.AssertionGeneratorUtils;
import eu.stamp_project.dspot.input_ampl_distributor.InputAmplDistributor;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.*;
import eu.stamp_project.utils.collector.Collector;
import eu.stamp_project.utils.collector.CollectorFactory;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.compilation.TestCompiler;
import eu.stamp_project.utils.options.AmplifierEnum;
import eu.stamp_project.utils.options.check.Checker;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.report.GlobalReport;
import eu.stamp_project.utils.report.error.ErrorReportImpl;
import eu.stamp_project.utils.report.output.Output;
import eu.stamp_project.utils.report.output.OutputReportImpl;
import eu.stamp_project.utils.report.output.selector.TestSelectorReportImpl;
import eu.stamp_project.utils.smtp.EmailSender;
import eu.stamp_project.utils.test_finder.TestFinder;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import static eu.stamp_project.utils.AmplificationHelper.PATH_SEPARATOR;

/**
 * Created by Andrew Bwogi
 * abwogi@kth.se
 * on 31/10/19
 */
public class DSpotConfiguration {

    private int nbIteration;
    private InputConfiguration inputConfiguration;
    private boolean verbose;
    private List<CtType<?>> testClassesToBeAmplified;
    private List<String> testMethodsToBeAmplifiedNames;
    private TestSelector testSelector;
    private InputAmplDistributor inputAmplDistributor;
    private Output output;
    private Collector collector;
    private boolean collectData;
    private DSpotCompiler compiler;
    private AutomaticBuilder automaticBuilder;
    private TestFinder testFinder;
    private long startTime;
    private AssertionGenerator assertionGenerator;
    private TestCompiler testCompiler;
    public static final GlobalReport GLOBAL_REPORT =
            new GlobalReport(new OutputReportImpl(), new ErrorReportImpl(), new TestSelectorReportImpl());
    private final Logger LOGGER = LoggerFactory.getLogger(DSpot.class);
    private double delta;

    public DSpotConfiguration(InputConfiguration inputConfiguration) {
        this.inputConfiguration = inputConfiguration;
        verbose = inputConfiguration.isVerbose();
        startTime = System.currentTimeMillis();
        testFinder = new TestFinder(
                Arrays.stream(inputConfiguration.getExcludedClasses().split(",")).collect(Collectors.toList()),
                Arrays.stream(inputConfiguration.getExcludedTestCases().split(",")).collect(Collectors.toList())
        );
        automaticBuilder = inputConfiguration.getBuilderEnum().getAutomaticBuilder(inputConfiguration);
        final String dependencies = completeDependencies(inputConfiguration, automaticBuilder);
        compiler = DSpotCompiler.createDSpotCompiler(
                inputConfiguration,
                dependencies
        );
        inputConfiguration.setFactory(compiler.getLauncher().getFactory());
        initHelpers(inputConfiguration);
        testCompiler = new TestCompiler(
                inputConfiguration.getNumberParallelExecutionProcessors(),
                inputConfiguration.shouldExecuteTestsInParallel(),
                inputConfiguration.getAbsolutePathToProjectRoot(),
                inputConfiguration.getClasspathClassesProject(),
                inputConfiguration.getTimeOutInMs(),
                inputConfiguration.getPreGoalsTestExecution(),
                inputConfiguration.shouldUseMavenToExecuteTest()
        );
        final EmailSender emailSender = new EmailSender(
                inputConfiguration.getSmtpUsername(),
                inputConfiguration.getSmtpPassword(),
                inputConfiguration.getSmtpHost(),
                inputConfiguration.getSmtpPort(),
                inputConfiguration.isSmtpAuth(),
                inputConfiguration.getSmtpTls()
        );
        collector = CollectorFactory.build(inputConfiguration, emailSender);
        collector.reportInitInformation(
                inputConfiguration.getAmplifiers(),
                inputConfiguration.getSelector(),
                inputConfiguration.getNbIteration(),
                inputConfiguration.isGregorMode(),
                !inputConfiguration.isGregorMode(),
                inputConfiguration.getNumberParallelExecutionProcessors()
        );
        testClassesToBeAmplified = testFinder.findTestClasses(inputConfiguration.getTestClasses());
        testMethodsToBeAmplifiedNames = inputConfiguration.getTestCases();
        testSelector = inputConfiguration.getSelector().buildSelector(automaticBuilder, inputConfiguration);
        final List<Amplifier> amplifiers = inputConfiguration
                .getAmplifiers()
                .stream()
                .map(AmplifierEnum::getAmplifier)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        inputAmplDistributor = inputConfiguration
                .getInputAmplDistributor()
                .getInputAmplDistributor(inputConfiguration.getMaxTestAmplified(), amplifiers);
        output = new Output(
                inputConfiguration.getAbsolutePathToProjectRoot(),
                inputConfiguration.getOutputDirectory(),
                collector

        );
        assertionGenerator = new AssertionGenerator(inputConfiguration.getDelta(), compiler, testCompiler);
        Checker.postChecking(inputConfiguration);
        collectData = true;
        delta = inputConfiguration.getDelta();
        nbIteration = inputConfiguration.getNbIteration();
    }

    public DSpotConfiguration() {
        inputConfiguration = new InputConfiguration();
        testMethodsToBeAmplifiedNames = Collections.emptyList();
        collectData = false;
    }

    public void initHelpers(InputConfiguration configuration){
        TestFramework.init(configuration.getFactory());
        AmplificationHelper.init(
                configuration.getTimeOutInMs(),
                configuration.shouldGenerateAmplifiedTestClass(),
                configuration.shouldKeepOriginalTestMethods()
        );
        RandomHelper.setSeedRandom(configuration.getSeed());
        createOutputDirectories(configuration);
        DSpotCache.init(configuration.getCacheSize());
        DSpotUtils.init(
                configuration.withComment(),
                configuration.getOutputDirectory(),
                configuration.getFullClassPathWithExtraDependencies(),
                configuration.getAbsolutePathToProjectRoot()
        );
        initSystemProperties(configuration.getSystemProperties());
        AssertionGeneratorUtils.init(configuration.shouldAllowPathInAssertion());
        CloneHelper.init(configuration.shouldExecuteTestsInParallel());
    }

    private void initSystemProperties(String systemProperties) {
        if (!systemProperties.isEmpty()) {
            Arrays.stream(systemProperties.split(","))
                    .forEach(systemProperty -> {
                        String[] keyValueInArray = systemProperty.split("=");
                        System.getProperties().put(keyValueInArray[0], keyValueInArray[1]);
                    });
        }
    }

    public String completeDependencies(InputConfiguration configuration, AutomaticBuilder automaticBuilder) {
        String dependencies = configuration.getDependencies();
        final String additionalClasspathElements = configuration.getAdditionalClasspathElements();
        final String absolutePathToProjectRoot = configuration.getAbsolutePathToProjectRoot();
        if (dependencies.isEmpty()) {
            dependencies = automaticBuilder.compileAndBuildClasspath();
            configuration.setDependencies(dependencies);
        }
        // TODO checks this. Since we support different Test Support, we may not need to add artificially junit in the classpath
        if (!dependencies.contains("junit" + File.separator + "junit" + File.separator + "4")) {
            dependencies = Test.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getFile() +
                    AmplificationHelper.PATH_SEPARATOR + dependencies;
        }
        if (!additionalClasspathElements.isEmpty()) {
            String pathToAdditionalClasspathElements = additionalClasspathElements;
            if (!Paths.get(additionalClasspathElements).isAbsolute()) {
                pathToAdditionalClasspathElements =
                        DSpotUtils.shouldAddSeparator.apply(absolutePathToProjectRoot + additionalClasspathElements);
            }
            dependencies += PATH_SEPARATOR + pathToAdditionalClasspathElements;
        }
        return dependencies;
    }

    public void createOutputDirectories(InputConfiguration configuration) {
        final File outputDirectory = new File(configuration.getOutputDirectory());
        try {
            if (configuration.shouldClean() && outputDirectory.exists()) {
                FileUtils.forceDelete(outputDirectory);
            }
            if (!outputDirectory.exists()) {
                FileUtils.forceMkdir(outputDirectory);
            }
        } catch (IOException ignored) {
            // ignored
        }
    }

    public void report(List<CtType<?>> amplifiedTestClasses) {
        LOGGER.info("Amplification {}.", amplifiedTestClasses.isEmpty() ? "failed" : "succeed");
        final long elapsedTime = System.currentTimeMillis() - startTime;
        LOGGER.info("Elapsed time {} ms", elapsedTime);
        GLOBAL_REPORT.output(inputConfiguration.getOutputDirectory());
        DSpotCache.reset();
        GLOBAL_REPORT.reset();
        AmplificationHelper.reset();
        DSpotPOMCreator.delete();
        if(collectData) {
            collector.sendInfo();
        }
    }

    /**
     * Optimization: an object that holds a dictionary
     * with large number of cloned CtMethods is not required anymore.
     * it is cleared before iterating again for next test class.
     */
    public void clearData(){
        this.assertionGenerator = new AssertionGenerator(delta, this.compiler, this.testCompiler);
    }

    public AssertionGenerator getAssertionGenerator() {
        return assertionGenerator;
    }

    public void setTestCompiler(TestCompiler testCompiler) {
        this.testCompiler = testCompiler;
    }

    public TestCompiler getTestCompiler() {
        return testCompiler;
    }

    public List<CtType<?>> getTestClassesToBeAmplified() {
        return testClassesToBeAmplified;
    }

    public void setTestClassesToBeAmplified(List<CtType<?>> testClassesToBeAmplified) {
        this.testClassesToBeAmplified = testClassesToBeAmplified;
    }

    public List<String> getTestMethodsToBeAmplifiedNames() {
        return testMethodsToBeAmplifiedNames;
    }

    public TestSelector getTestSelector() {
        return testSelector;
    }

    public InputAmplDistributor getInputAmplDistributor() {
        return inputAmplDistributor;
    }

    public Output getOutput() {
        return output;
    }

    public DSpotCompiler getCompiler() {
        return compiler;
    }

    public AutomaticBuilder getAutomaticBuilder() {
        return automaticBuilder;
    }

    public TestFinder getTestFinder() {
        return testFinder;
    }

    public Logger getLogger() {
        return LOGGER;
    }

    public GlobalReport getGlobalReport() {
        return GLOBAL_REPORT;
    }

    public void setTestSelector(TestSelector testSelector) {
        this.testSelector = testSelector;
    }

    public void setInputAmplDistributor(InputAmplDistributor inputAmplDistributor) {
        this.inputAmplDistributor = inputAmplDistributor;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    public void setCollector(Collector collector) {
        this.collector = collector;
    }

    public void setCompiler(DSpotCompiler compiler) {
        this.compiler = compiler;
    }

    public void setAutomaticBuilder(AutomaticBuilder automaticBuilder) {
        this.automaticBuilder = automaticBuilder;
    }

    public void setTestFinder(TestFinder testFinder) {
        this.testFinder = testFinder;
    }

    public void setAssertionGenerator(AssertionGenerator assertionGenerator) {
        this.assertionGenerator = assertionGenerator;
    }

    public void setCollectData(boolean collectData){
        this.collectData = collectData;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public int getNbIteration() {
        return nbIteration;
    }

    public void setNbIteration(int nbIteration) {
        this.nbIteration = nbIteration;
    }

    public boolean shouldGenerateAmplifiedTestClass() {
        return inputConfiguration.shouldGenerateAmplifiedTestClass();
    }
}
