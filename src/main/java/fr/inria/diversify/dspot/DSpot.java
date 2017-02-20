package fr.inria.diversify.dspot;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.amplifier.*;
import fr.inria.diversify.dspot.selector.BranchCoverageTestSelector;
import fr.inria.diversify.dspot.selector.TestSelector;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.InitUtils;
import spoon.Launcher;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 08/06/15
 * Time: 17:36
 */
public class DSpot {

    private List<Amplifier> amplifiers;
    private int numberOfIterations;
    private TestSelector testSelector;
    public InputProgram inputProgram;

    private List<String> testResources;

    @Deprecated
    private InputConfiguration inputConfiguration;

    private DSpotCompiler compiler;

    public DSpot(InputConfiguration inputConfiguration) throws InvalidSdkException, Exception {
        this(inputConfiguration, 3, Arrays.asList(
                new TestDataMutator(),
                new TestMethodCallAdder(),
                new TestMethodCallRemover(),
                new StatementAdderOnAssert()),
                new BranchCoverageTestSelector(10));
    }

    public DSpot(InputConfiguration configuration, int numberOfIterations) throws InvalidSdkException, Exception {
        this(configuration, numberOfIterations, Arrays.asList(
                new TestDataMutator(),
                new TestMethodCallAdder(),
                new TestMethodCallRemover(),
                new StatementAdderOnAssert())
        );
    }

    public DSpot(InputConfiguration configuration, TestSelector testSelector) throws InvalidSdkException, Exception {
        this(configuration, 3, Arrays.asList(
                new TestDataMutator(),
                new TestMethodCallAdder(),
                new TestMethodCallRemover(),
                new StatementAdderOnAssert()), testSelector);
    }

    public DSpot(InputConfiguration configuration, List<Amplifier> amplifiers) throws InvalidSdkException, Exception {
        this(configuration, 3, amplifiers);
    }

    public DSpot(InputConfiguration inputConfiguration, int numberOfIterations, List<Amplifier> amplifiers) throws InvalidSdkException, Exception {
        this(inputConfiguration, numberOfIterations, amplifiers, new BranchCoverageTestSelector(10));
    }

    public DSpot(InputConfiguration inputConfiguration, int numberOfIterations, List<Amplifier> amplifiers, TestSelector testSelector) throws InvalidSdkException, Exception {
        this.testResources = new ArrayList<>();
        this.inputConfiguration = inputConfiguration;
        InitUtils.initLogLevel(inputConfiguration);
        inputProgram = InitUtils.initInputProgram(inputConfiguration);
        inputConfiguration.setInputProgram(inputProgram);
        String outputDirectory = inputConfiguration.getProperty("tmpDir") + "/tmp/";
        File tmpDir = new File(inputConfiguration.getProperty("tmpDir"));
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        } else {
            FileUtils.cleanDirectory(tmpDir);
        }
        String mavenLocalRepository = inputConfiguration.getProperty("maven.localRepository", null);
        FileUtils.copyDirectory(new File(inputProgram.getProgramDir()), new File(outputDirectory));

        //Ugly way to support usage of resources with relative path.
        copyResourceOfTargetProjectIntoDspot("testResources");
        copyResourceOfTargetProjectIntoDspot("srcResources");

        inputProgram.setProgramDir(outputDirectory);
        DSpotUtils.compileOriginalProject(this.inputProgram, inputConfiguration, mavenLocalRepository);
        String dependencies = AmplificationHelper.getDependenciesOf(this.inputConfiguration, inputProgram);

        //We need to use separate factory here, because the BranchProcessor will process test also
        //TODO this is used only with the BranchCoverageSelector
        if (testSelector instanceof BranchCoverageTestSelector) {
            Launcher spoonModel = DSpotCompiler.getSpoonModelOf(inputProgram.getAbsoluteSourceCodeDir(), dependencies);
            DSpotUtils.addBranchLogger(inputProgram, spoonModel.getFactory());
            DSpotUtils.copyLoggerPackage(inputProgram);
        }

        File output = new File(inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir());
        FileUtils.cleanDirectory(output);
        boolean status = DSpotCompiler.compile(inputProgram.getAbsoluteSourceCodeDir(), dependencies, output);
        if (!status) {
            throw new RuntimeException("Error during compilation");
        }

        this.compiler = new DSpotCompiler(inputProgram, dependencies);

        this.inputProgram.setFactory(compiler.getLauncher().getFactory());

        this.amplifiers = new ArrayList<>(amplifiers);
        this.numberOfIterations = numberOfIterations;
        this.testSelector = testSelector;
        this.testSelector.init(this.inputConfiguration);
    }

    private void copyResourceOfTargetProjectIntoDspot(String key) {
        if (inputConfiguration.getProperty(key) != null) {
            try {
                final File resourcesDirectory = new File(inputProgram.getProgramDir() + "/" + inputConfiguration.getProperty(key));
                final File[] resources = resourcesDirectory.listFiles();
                if (resources != null) {
                    this.testResources.addAll(Arrays.stream(resources)
                            .map(this::relativePathFromListFile)
                            .collect(Collectors.toList()));
                    FileUtils.copyDirectory(resourcesDirectory,
                            new File(inputConfiguration.getProperty(key)));
                }
            } catch (FileAlreadyExistsException ignored) {
                //ignored
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String relativePathFromListFile(File f) {
        return f.getPath().substring((inputProgram.getProgramDir() + "/").length(), f.getPath().length());
    }


    public void addAmplifier(Amplifier amplifier) {
        this.amplifiers.add(amplifier);
    }

    public List<CtType> amplifyAllTests() throws InterruptedException, IOException, ClassNotFoundException {
        return inputProgram.getFactory().Class().getAll().stream()
                .filter(ctClass -> !ctClass.getModifiers().contains(ModifierKind.ABSTRACT))
                .filter(ctClass ->
                        ctClass.getMethods().stream()
                                .filter(method ->
                                        AmplificationChecker.isTest(method, inputProgram.getRelativeTestSourceCodeDir()))
                                .count() > 0)
                .map(this::amplifyTest)
                .collect(Collectors.toList());
    }

    public CtType amplifyTest(String fullName) throws InterruptedException, IOException, ClassNotFoundException {
        CtType<Object> clone = this.compiler.getLauncher().getFactory().Type().get(fullName).clone();
        clone.setParent(this.compiler.getLauncher().getFactory().Type().get(fullName).getParent());
        return amplifyTest(clone);
    }

    public CtType amplifyTest(CtType test) {
        try {
            Amplification testAmplification = new Amplification(this.inputProgram, this.amplifiers, this.testSelector, this.compiler);
            CtType amplification = testAmplification.amplification(test, numberOfIterations);
            testSelector.report();
            final File outputDirectory = new File(inputConfiguration.getOutputDirectory());
            System.out.println("Print " + amplification.getSimpleName() + " with " + testSelector.getNbAmplifiedTestCase() + " amplified test cases in " + this.inputConfiguration.getOutputDirectory());
            DSpotUtils.printAmplifiedTestClass(amplification, outputDirectory);
            FileUtils.cleanDirectory(compiler.getSourceOutputDirectory());
            FileUtils.cleanDirectory(compiler.getBinaryOutputDirectory());
            return amplification;
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public CtType amplifyTest(String fullName, List<String> methods) throws InterruptedException, IOException, ClassNotFoundException {
        CtType<Object> clone = this.compiler.getLauncher().getFactory().Type().get(fullName).clone();
        clone.setParent(this.compiler.getLauncher().getFactory().Type().get(fullName).getParent());
        return amplifyTest(clone, methods.stream()
                .map(methodName -> clone.getMethodsByName(methodName).get(0))
                .collect(Collectors.toList()));
    }

    public CtType amplifyTest(CtType test, List<CtMethod<?>> methods) {
        try {
            Amplification testAmplification = new Amplification(this.inputProgram, this.amplifiers, this.testSelector, this.compiler);
            CtType amplification = testAmplification.amplification(test, methods, numberOfIterations);
            testSelector.report();
            final File outputDirectory = new File(inputConfiguration.getOutputDirectory());
            System.out.println("Print " + amplification.getSimpleName() + " with " + testSelector.getNbAmplifiedTestCase() + " amplified test cases in " + this.inputConfiguration.getOutputDirectory());
            DSpotUtils.printAmplifiedTestClass(amplification, outputDirectory);
            FileUtils.cleanDirectory(compiler.getSourceOutputDirectory());
            FileUtils.cleanDirectory(compiler.getBinaryOutputDirectory());
            DSpotUtils.compileOriginalProject(this.inputProgram, inputConfiguration, inputConfiguration.getProperty("maven.localRepository", null));
            return amplification;
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public InputProgram getInputProgram() {
        return inputProgram;
    }

    public void cleanResources() {
        if (this.testResources != null) {
            this.testResources.stream()
                    .map(File::new)
                    .filter(File::exists)
                    .forEach(file -> {
                        try {
                            FileUtils.forceDelete(file);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }
}
