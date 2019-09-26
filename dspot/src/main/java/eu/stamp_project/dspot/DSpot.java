package eu.stamp_project.dspot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.Main;
import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.dspot.input_ampl_distributor.InputAmplDistributor;
import eu.stamp_project.dspot.selector.PitMutantScoreSelector;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.utils.compilation.TestCompiler;
import eu.stamp_project.utils.options.InputAmplDistributorEnum;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.Counter;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.json.ClassTimeJSON;
import eu.stamp_project.utils.json.ProjectTimeJSON;
import eu.stamp_project.utils.report.error.Error;
import eu.stamp_project.utils.test_finder.TestFinder;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static eu.stamp_project.utils.report.error.ErrorEnum.*;

/**
 * User: Simon
 * Date: 08/06/15
 * Time: 17:36
 */
public class DSpot {

    private static final Logger LOGGER = LoggerFactory.getLogger(DSpot.class);

    private DSpotCompiler compiler;

    private TestSelector testSelector;

    private ProjectTimeJSON projectTimeJSON;

    private int globalNumberOfSelectedAmplification;

    private int numberOfIteration;

    public DSpot(TestSelector testSelector, DSpotCompiler compiler, int numberOfIteration) {
        this.numberOfIteration = numberOfIteration;
        this.compiler = compiler;
        this.testSelector = testSelector;
        String splitter = File.separator.equals("/") ? "/" : "\\\\";
        final String[] splittedPath = InputConfiguration.get().getAbsolutePathToProjectRoot().split(splitter);
        final File projectJsonFile = new File(InputConfiguration.get().getOutputDirectory() +
                File.separator + splittedPath[splittedPath.length - 1] + ".json");
        if (projectJsonFile.exists()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try {
                this.projectTimeJSON = gson.fromJson(new FileReader(projectJsonFile), ProjectTimeJSON.class);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            this.projectTimeJSON = new ProjectTimeJSON(splittedPath[splittedPath.length - 1]);
        }
        this.globalNumberOfSelectedAmplification = 0;
    }

    public CtType<?> amplify(Amplification testAmplification, CtType<?> testClassToBeAmplified, List<CtMethod<?>> testMethodsToBeAmplified) {
        Counter.reset();
        if (InputConfiguration.get().shouldGenerateAmplifiedTestClass()) {
            testClassToBeAmplified = AmplificationHelper.renameTestClassUnderAmplification(testClassToBeAmplified);
        }
        long time = System.currentTimeMillis();
        this.loopAmplification(testAmplification, testClassToBeAmplified, testMethodsToBeAmplified);
        final long elapsedTime = System.currentTimeMillis() - time;
        LOGGER.info("elapsedTime {}", elapsedTime);
        this.projectTimeJSON.add(new ClassTimeJSON(testClassToBeAmplified.getQualifiedName(), elapsedTime));
        final CtType clone = testClassToBeAmplified.clone();
        testClassToBeAmplified.getPackage().addType(clone);
        final CtType<?> amplification = AmplificationHelper.createAmplifiedTest(testSelector.getAmplifiedTestCases(), clone);
        final File outputDirectory = new File(InputConfiguration.get().getOutputDirectory());
        //Optimization: this object is not required anymore
        //and holds a dictionary with large number of cloned CtMethods.
        testAmplification = null;
        //Optimization: this.testSelector.getAmplifiedTestCases() also holds a large number of cloned CtMethods,
        //but it is clear before iterating again for next test class
        LOGGER.debug("OPTIMIZATION: GC invoked");
        System.gc(); //Optimization: cleaning up heap before printing the amplified class
        if (!testSelector.getAmplifiedTestCases().isEmpty()) {
            Main.GLOBAL_REPORT.addNumberAmplifiedTestMethodsToTotal(testSelector.getAmplifiedTestCases().size());
            Main.GLOBAL_REPORT.addPrintedTestClasses(
                    String.format("Print %s with %d amplified test cases in %s",
                    amplification.getQualifiedName() + ".java",
                    testSelector.getAmplifiedTestCases().size(),
                    InputConfiguration.get().getOutputDirectory())
            );
            // we try to compile the newly generated amplified test class (.java)
            // if this fail, we re-print the java test class without imports
            DSpotUtils.printAndCompileToCheck(amplification, outputDirectory);
        } else {
            LOGGER.warn("DSpot could not obtain any amplified test method.");
            LOGGER.warn("You can customize the following options: --amplifiers, --test-criterion, --iteration, --inputAmplDistributor etc, and retry with a new configuration.");
        }
        //TODO if something bad happened, the call to TestSelector#report() might throw an exception.
        //For now, I wrap it in a try/catch, but we might think of a better way to handle this.
        try {
            Main.GLOBAL_REPORT.addTestSelectorReportForTestClass(testClassToBeAmplified, testSelector.report());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Something bad happened during the report fot test-criterion.");
            LOGGER.error("Dspot might not have output correctly!");
        }
        writeTimeJson();
        InputConfiguration.get().getBuilder().reset();
        return amplification;
    }

    private void loopAmplification(Amplification testAmplification,
                                   CtType<?> testClassToBeAmplified,
                                   List<CtMethod<?>> testMethodsToBeAmplified) {

        if(testMethodsToBeAmplified.isEmpty()) {
            LOGGER.warn("No test provided for amplification in class {}", testClassToBeAmplified.getQualifiedName());
            return;
        }

        LOGGER.info("Amplification of {} ({} test(s))", testClassToBeAmplified.getQualifiedName(), testMethodsToBeAmplified.size());
        LOGGER.info("Assertion amplification of {} ({} test(s))", testClassToBeAmplified.getQualifiedName(), testMethodsToBeAmplified.size());

        // here, we base the execution mode to the first test method given.
        // the user should provide whether JUnit3/4 OR JUnit5 but not both at the same time.
        // TODO DSpot could be able to switch from one to another version of JUnit, but I believe that the ROI is not worth it.
        final boolean jUnit5 = TestFramework.isJUnit5(testMethodsToBeAmplified.get(0));
        EntryPoint.jUnit5Mode = jUnit5;
        InputConfiguration.get().setJUnit5(jUnit5);
        if (!this.testSelector.init()) {
            return;
        }
        final List<CtMethod<?>> passingTests;
        try {
            passingTests =
                    TestCompiler.compileRunAndDiscardUncompilableAndFailingTestMethods(
                            testClassToBeAmplified,
                            testMethodsToBeAmplified,
                            this.compiler,
                            InputConfiguration.get()
                    );
        } catch (Exception | java.lang.Error e) {
            Main.GLOBAL_REPORT.addError(new Error(ERROR_EXEC_TEST_BEFORE_AMPLIFICATION, e));
            return;
        }
        final List<CtMethod<?>> selectedToBeAmplified;
        try {
            // set up the selector with tests to amplify
            selectedToBeAmplified = this.testSelector.selectToAmplify(testClassToBeAmplified, passingTests);
        } catch (Exception | java.lang.Error e) {
            Main.GLOBAL_REPORT.addError(new Error(ERROR_PRE_SELECTION, e));
            return;
        }

        // generate tests with additional assertions
        final List<CtMethod<?>> assertionAmplifiedTestMethods = testAmplification.assertionsAmplification(testClassToBeAmplified, selectedToBeAmplified);
        final List<CtMethod<?>> amplifiedTestMethodsToKeep;
        try {
            // keep tests that improve the test suite
            amplifiedTestMethodsToKeep = this.testSelector.selectToKeep(assertionAmplifiedTestMethods);
        } catch (Exception | java.lang.Error e) {
            Main.GLOBAL_REPORT.addError(new Error(ERROR_SELECTION, e));
            return;
        }
        this.globalNumberOfSelectedAmplification += amplifiedTestMethodsToKeep.size();
        LOGGER.info("{} amplified test methods has been selected to be kept. (global: {})", amplifiedTestMethodsToKeep.size(), this.globalNumberOfSelectedAmplification);
        // in case there is no amplifier, we can leave
//        if (this.amplifiers.isEmpty()) {
//            return;
//        }

        // generate tests with input modification and associated new assertions
        LOGGER.info("Applying Input-amplification and Assertion-amplification test by test.");
        for (int i = 0; i < testMethodsToBeAmplified.size(); i++) {
            CtMethod test = testMethodsToBeAmplified.get(i);
            LOGGER.info("Amplification of {}, ({}/{})", test.getSimpleName(), i + 1, testMethodsToBeAmplified.size());
            int numberOfAmplifiedTestMethod = amplificationIteration(testAmplification, testClassToBeAmplified, test);
            this.globalNumberOfSelectedAmplification += numberOfAmplifiedTestMethod;
            LOGGER.info("{} amplified test methods has been selected to be kept. (global: {})", amplifiedTestMethodsToKeep.size(), this.globalNumberOfSelectedAmplification);
        }
    }

    private int amplificationIteration(Amplification testAmplification, CtType<?> testClassToBeAmplified, CtMethod test) {
        // tmp list for current test methods to be amplified
        // this list must be a implementation that support remove / clear methods
        List<CtMethod<?>> currentTestList = new ArrayList<>();
        currentTestList.add(test);
        int numberOfAmplifiedTestMethod = 0;
        // output
        final List<CtMethod<?>> amplifiedTests = new ArrayList<>();
        for (int i = 0; i < this.numberOfIteration ; i++) {
            LOGGER.info("iteration {} / {}", i, this.numberOfIteration);
            currentTestList = testAmplification.amplification(testClassToBeAmplified, currentTestList, i);
            numberOfAmplifiedTestMethod += currentTestList.size();
        }
        return numberOfAmplifiedTestMethod;
    }

    private void writeTimeJson() {
        final File file1 = new File(InputConfiguration.get().getOutputDirectory());
        if (!file1.exists()) {
            file1.mkdir();
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File file = new File(InputConfiguration.get().getOutputDirectory() +
                "/" + this.projectTimeJSON.projectName + ".json");
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(gson.toJson(this.projectTimeJSON));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
