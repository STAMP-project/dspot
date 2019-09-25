package eu.stamp_project.dspot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.Main;
import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.dspot.input_ampl_distributor.InputAmplDistributor;
import eu.stamp_project.dspot.selector.PitMutantScoreSelector;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.options.InputAmplDistributorEnum;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.Counter;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.json.ClassTimeJSON;
import eu.stamp_project.utils.json.ProjectTimeJSON;
import eu.stamp_project.utils.test_finder.TestFinder;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 08/06/15
 * Time: 17:36
 */
public class DSpot {

    private static final Logger LOGGER = LoggerFactory.getLogger(DSpot.class);

    private List<Amplifier> amplifiers;

    private int numberOfIterations;

    private TestSelector testSelector;

    private InputAmplDistributor inputAmplDistributor;

    private DSpotCompiler compiler;

    private ProjectTimeJSON projectTimeJSON;

    private TestFinder testFinder;

    public DSpot() {
        this(3, Collections.emptyList(), new PitMutantScoreSelector(), InputAmplDistributorEnum.RandomInputAmplDistributor);
    }

    public DSpot(int numberOfIterations) {
        this(numberOfIterations, Collections.emptyList(), new PitMutantScoreSelector(), InputAmplDistributorEnum.RandomInputAmplDistributor);
    }

    public DSpot(TestSelector testSelector) {
        this(3, Collections.emptyList(), testSelector, InputAmplDistributorEnum.RandomInputAmplDistributor);
    }

    public DSpot(int iteration, TestSelector testSelector) throws Exception {
        this(iteration, Collections.emptyList(), testSelector, InputAmplDistributorEnum.RandomInputAmplDistributor);
    }

    public DSpot(List<Amplifier> amplifiers) {
        this(3, amplifiers, new PitMutantScoreSelector(), InputAmplDistributorEnum.RandomInputAmplDistributor);
    }

    public DSpot(int numberOfIterations, List<Amplifier> amplifiers) {
        this(numberOfIterations, amplifiers, new PitMutantScoreSelector(), InputAmplDistributorEnum.RandomInputAmplDistributor);
    }

    public DSpot(int numberOfIterations, List<Amplifier> amplifiers, TestSelector testSelector) throws Exception {
        this(numberOfIterations, amplifiers, testSelector, InputAmplDistributorEnum.RandomInputAmplDistributor);
    }

    public DSpot(int numberOfIterations,
                 List<Amplifier> amplifiers,
                 TestSelector testSelector,
                 InputAmplDistributorEnum inputAmplDistributor) {
        this.testFinder = new TestFinder(
                Arrays.stream(InputConfiguration.get().getExcludedClasses().split(",")).collect(Collectors.toList()),
                Arrays.stream(InputConfiguration.get().getExcludedTestCases().split(",")).collect(Collectors.toList())
        );
        String dependencies = InputConfiguration.get().getDependencies();
        this.compiler = DSpotCompiler.createDSpotCompiler(InputConfiguration.get(), dependencies);
        InputConfiguration.get().setFactory(this.compiler.getLauncher().getFactory());
        this.amplifiers = new ArrayList<>(amplifiers);
        this.numberOfIterations = numberOfIterations;
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
        this.inputAmplDistributor = inputAmplDistributor.getInputAmplDistributor(this.amplifiers);
    }

    /**
     * Amplify all the test methods of all the test classes that DSpot can find.
     * A class is considered as a test class if it contains at least one test method.
     * A method is considred as test method if it matches {@link eu.stamp_project.test_framework.TestFrameworkSupport#isTest(CtMethod)}
     *
     * @return a list of amplified test classes with amplified test methods.
     */
    public List<CtType<?>> amplifyAllTests() {
        return this._amplifyTestClasses(
                TestFramework.getAllTestClasses().stream()
                .filter(this.testFinder.isNotExcluded)
                .collect(Collectors.toList())
        );
    }

    /**
     * Amplify the given test methods of the given test classes.
     *
     * @param testClassToBeAmplified the test class to be amplified. It can be a java regex.
     * @return a list of amplified test classes with amplified test methods.
     */
    public List<CtType<?>> amplifyTestClass(String testClassToBeAmplified) {
        return this.amplifyTestClassesTestMethods(Collections.singletonList(testClassToBeAmplified), Collections.emptyList());
    }

    /**
     * Amplify the given test methods of the given test classes.
     *
     * @param testClassToBeAmplified the test class to be amplified. It can be a java regex.
     * @param testMethod             the test method to be amplified. It can be a java regex.
     * @return a list of amplified test classes with amplified test methods.
     */
    public List<CtType<?>> amplifyTestClassTestMethod(String testClassToBeAmplified, String testMethod) {
        return this.amplifyTestClassesTestMethods(Collections.singletonList(testClassToBeAmplified), Collections.singletonList(testMethod));
    }

    /**
     * Amplify the given test methods of the given test classes.
     *
     * @param testClassToBeAmplified the test class to be amplified. It can be a java regex.
     * @param testMethods            the list of test methods to be amplified. This list can be a list of java regex.
     * @return a list of amplified test classes with amplified test methods.
     */
    public List<CtType<?>> amplifyTestClassTestMethods(String testClassToBeAmplified, List<String> testMethods) {
        return this.amplifyTestClassesTestMethods(Collections.singletonList(testClassToBeAmplified), testMethods);
    }

    /**
     * Amplify the given test methods of the given test classes.
     *
     * @param testClassesToBeAmplified the list of test classes to be amplified. This list can be a list of java regex.
     * @return a list of amplified test classes with amplified test methods.
     */
    public List<CtType<?>> amplifyTestClasses(List<String> testClassesToBeAmplified) {
        return this.amplifyTestClassesTestMethods(testClassesToBeAmplified, Collections.emptyList());
    }

    /**
     * Amplify the given test methods of the given test classes.
     *
     * @param testClassesToBeAmplified the list of test classes to be amplified. This list can be a list of java regex.
     * @param testMethod               the test method to be amplified. This can be a java regex.
     * @return a list of amplified test classes with amplified test methods.
     */
    @Deprecated
    public List<CtType<?>> amplifyTestClassesTestMethod(List<String> testClassesToBeAmplified, String testMethod) {
        return this.amplifyTestClassesTestMethods(testClassesToBeAmplified, Collections.singletonList(testMethod));
    }

    /**
     * Amplify the given test methods of the given test classes.
     *
     * @param testClassesToBeAmplified the list of test classes to be amplified. This list can be a list of java regex.
     * @param testMethods              the list of test methods to be amplified. This list can be a list of java regex.
     * @return a list of amplified test classes with amplified test methods.
     */
    public List<CtType<?>> amplifyTestClassesTestMethods(List<String> testClassesToBeAmplified, List<String> testMethods) {
        final List<CtType<?>> testClasses = this.testFinder.findTestClasses(testClassesToBeAmplified);
        return testClasses.stream()
                .map(ctType ->
                    this._amplify(ctType, this.testFinder.findTestMethods(ctType, testMethods))
                ).collect(Collectors.toList());
    }

    private List<CtType<?>> _amplifyTestClasses(List<CtType<?>> testClassesToBeAmplified) {
        return testClassesToBeAmplified.stream()
                .map(this::_amplifyTestClass)
                .collect(Collectors.toList());
    }

    private CtType<?> _amplifyTestClass(CtType<?> test) {
        return this._amplify(test, TestFramework.getAllTest(test));
    }

    protected CtType<?> _amplify(CtType<?> test, List<CtMethod<?>> methods) {
        Counter.reset();
        Amplification testAmplification = new Amplification(this.compiler, this.amplifiers, this.testSelector, this.inputAmplDistributor);
        long time = System.currentTimeMillis();
        testAmplification.amplification(test, methods, numberOfIterations);
        final long elapsedTime = System.currentTimeMillis() - time;
        LOGGER.info("elapsedTime {}", elapsedTime);
        this.projectTimeJSON.add(new ClassTimeJSON(test.getQualifiedName(), elapsedTime));
        final CtType clone = test.clone();
        test.getPackage().addType(clone);
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
            Main.GLOBAL_REPORT.addTestSelectorReportForTestClass(test, testSelector.report());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Something bad happened during the report fot test-criterion.");
            LOGGER.error("Dspot might not have output correctly!");
        }
        /* Cleaning modified source directory by DSpot */
        try {
            FileUtils.cleanDirectory(compiler.getSourceOutputDirectory());
        } catch (Exception exception) {
            exception.printStackTrace();
            LOGGER.warn("Something went wrong when trying to cleaning temporary sources directory: {}", compiler.getSourceOutputDirectory());
        }
        /* Cleaning binary generated by Dspot */
        try {
            String pathToDotClass = compiler.getBinaryOutputDirectory().getAbsolutePath() + "/" +
                    test.getQualifiedName().replaceAll("\\.", "/") + ".class";
            FileUtils.forceDelete(new File(pathToDotClass));
        } catch (IOException ignored) {
            //ignored
        }
        writeTimeJson();
        InputConfiguration.get().getBuilder().reset();
        return amplification;
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
