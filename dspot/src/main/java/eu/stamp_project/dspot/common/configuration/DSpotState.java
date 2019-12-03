package eu.stamp_project.dspot.common.configuration;

import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.dspot.assertiongenerator.AssertionGenerator;
import eu.stamp_project.dspot.amplifier.InputAmplDistributor;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.dspot.common.collector.Collector;
import eu.stamp_project.dspot.common.compilation.DSpotCompiler;
import eu.stamp_project.dspot.common.compilation.TestCompiler;
import eu.stamp_project.dspot.common.report.GlobalReport;
import eu.stamp_project.dspot.common.report.error.ErrorReportImpl;
import eu.stamp_project.dspot.common.report.output.Output;
import eu.stamp_project.dspot.common.report.output.OutputReportImpl;
import eu.stamp_project.dspot.common.report.output.selector.TestSelectorReportImpl;
import eu.stamp_project.dspot.common.configuration.test_finder.TestFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.util.Collections;
import java.util.List;

/**
 * Created by Andrew Bwogi
 * abwogi@kth.se
 * on 31/10/19
 */
public class DSpotState {

    private int nbIteration;
    private UserInput userInput;
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
    public static boolean verbose = false;
    private final Logger LOGGER = LoggerFactory.getLogger(DSpot.class);
    private double delta;

    public DSpotState() {
        userInput = new UserInput();
        testMethodsToBeAmplifiedNames = Collections.emptyList();
        collectData = false;
    }

    /**
     * Optimization: an object that holds a dictionary
     * with large number of cloned CtMethods is not required anymore.
     * it is cleared before iterating again for next test class.
     */
    public void clearData(){
        this.assertionGenerator = new AssertionGenerator(delta, this.compiler, this.testCompiler);
    }

    public int getNbIteration() {
        return nbIteration;
    }

    public void setNbIteration(int nbIteration) {
        this.nbIteration = nbIteration;
    }

    public UserInput getUserInput() {
        return userInput;
    }

    public void setUserInput(UserInput userInput) {
        this.userInput = userInput;
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

    public void setTestMethodsToBeAmplifiedNames(List<String> testMethodsToBeAmplifiedNames) {
        this.testMethodsToBeAmplifiedNames = testMethodsToBeAmplifiedNames;
    }

    public TestSelector getTestSelector() {
        return testSelector;
    }

    public void setTestSelector(TestSelector testSelector) {
        this.testSelector = testSelector;
    }

    public InputAmplDistributor getInputAmplDistributor() {
        return inputAmplDistributor;
    }

    public void setInputAmplDistributor(InputAmplDistributor inputAmplDistributor) {
        this.inputAmplDistributor = inputAmplDistributor;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    public Collector getCollector() {
        return collector;
    }

    public void setCollector(Collector collector) {
        this.collector = collector;
    }

    public boolean isCollectData() {
        return collectData;
    }

    public void setCollectData(boolean collectData) {
        this.collectData = collectData;
    }

    public DSpotCompiler getCompiler() {
        return compiler;
    }

    public void setCompiler(DSpotCompiler compiler) {
        this.compiler = compiler;
    }

    public AutomaticBuilder getAutomaticBuilder() {
        return automaticBuilder;
    }

    public void setAutomaticBuilder(AutomaticBuilder automaticBuilder) {
        this.automaticBuilder = automaticBuilder;
    }

    public TestFinder getTestFinder() {
        return testFinder;
    }

    public void setTestFinder(TestFinder testFinder) {
        this.testFinder = testFinder;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public AssertionGenerator getAssertionGenerator() {
        return assertionGenerator;
    }

    public void setAssertionGenerator(AssertionGenerator assertionGenerator) {
        this.assertionGenerator = assertionGenerator;
    }

    public TestCompiler getTestCompiler() {
        return testCompiler;
    }

    public void setTestCompiler(TestCompiler testCompiler) {
        this.testCompiler = testCompiler;
    }

    public Logger getLogger() {
        return LOGGER;
    }

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public boolean shouldGenerateAmplifiedTestClass() {
        return userInput.shouldGenerateAmplifiedTestClass();
    }

    public GlobalReport getGlobalReport() {
        return GLOBAL_REPORT;
    }
}
