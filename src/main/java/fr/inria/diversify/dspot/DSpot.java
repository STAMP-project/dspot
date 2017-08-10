package fr.inria.diversify.dspot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.amplifier.*;
import fr.inria.diversify.dspot.selector.BranchCoverageTestSelector;
import fr.inria.diversify.dspot.selector.TestSelector;
import fr.inria.diversify.dspot.support.json.ClassTimeJSON;
import fr.inria.diversify.dspot.support.Counter;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.dspot.support.json.ProjectTimeJSON;
import fr.inria.diversify.mutant.descartes.DescartesChecker;
import fr.inria.diversify.mutant.descartes.DescartesInjector;
import fr.inria.diversify.mutant.pit.MavenPitCommandAndOptions;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.InitUtils;
import fr.inria.diversify.util.Log;
import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import spoon.Launcher;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;

import java.io.*;
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

	private InputConfiguration inputConfiguration;

	private DSpotCompiler compiler;

	private ProjectTimeJSON projectTimeJSON;

	public DSpot(InputConfiguration inputConfiguration) throws InvalidSdkException, Exception {
		this(inputConfiguration, 3, Arrays.asList(
				new TestDataMutator(),
				new TestMethodCallAdder(),
				new TestMethodCallRemover(),
				new StatementAdderOnAssert(),
				new StatementAdd(inputConfiguration.getProperty("filter"))),
				new BranchCoverageTestSelector(10));
	}

	public DSpot(InputConfiguration configuration, int numberOfIterations) throws InvalidSdkException, Exception {
		this(configuration, numberOfIterations, Arrays.asList(
				new TestDataMutator(),
				new TestMethodCallAdder(),
				new TestMethodCallRemover(),
				new StatementAdderOnAssert(),
				new StatementAdd(configuration.getProperty("filter")))
		);
	}

	public DSpot(InputConfiguration configuration, TestSelector testSelector) throws InvalidSdkException, Exception {
		this(configuration, 3, Arrays.asList(
				new TestDataMutator(),
				new TestMethodCallAdder(),
				new TestMethodCallRemover(),
				new StatementAdderOnAssert(),
				new StatementAdd(configuration.getProperty("filter"))), testSelector);
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
        final String[] splittedPath = inputProgram.getProgramDir().split("/");

        File tmpDir = new File(inputConfiguration.getProperty("tmpDir"));
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        } else {
            FileUtils.cleanDirectory(tmpDir);
        }

        FileUtils.copyDirectory(new File(inputProgram.getProgramDir()), new File(inputConfiguration.getProperty("tmpDir") + "/tmp"));

        //Ugly way to support usage of resources with relative path
        copyResourcesOfTargetProjectIntoDspot("testResources");
        copyResourcesOfTargetProjectIntoDspot("srcResources");
        //copyParentPomIfExist(outputDirectory);
        final String outputDirectory = inputConfiguration.getProperty("tmpDir") + "/tmp/" +
                (inputConfiguration.getProperty("targetModule") == null ? "" : inputConfiguration.getProperty("targetModule"));
        inputProgram.setProgramDir(outputDirectory);

        if (MavenPitCommandAndOptions.descartesMode &&
                DescartesChecker.shouldInjectDescartes(inputProgram.getProgramDir() + "/pom.xml")) {
            DescartesInjector.injectDescartesIntoPom(inputProgram.getProgramDir() + "/pom.xml");
        }

        AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(inputConfiguration);

        String dependencies = builder.buildClasspath(this.inputProgram.getProgramDir());
        File output = new File(inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir());
        File outputTest = new File(inputProgram.getProgramDir() + "/" + inputProgram.getTestClassesDir());
        try {
            FileUtils.cleanDirectory(output);
            FileUtils.cleanDirectory(outputTest);
        } catch (IllegalArgumentException ignored) {
            //the target directory does not exist, do not need to clean it
        }
        boolean status = DSpotCompiler.compile(inputProgram.getAbsoluteSourceCodeDir(), dependencies, output);
        boolean statusTest = DSpotCompiler.compile(inputProgram.getAbsoluteTestSourceCodeDir(),
                output.getAbsolutePath() + System.getProperty("path.separator") + dependencies, outputTest);

        if (! (status && statusTest)) {
            throw new RuntimeException("Error during compilation");
        }
        //We need to use separate factory here, because the BranchProcessor will process test also
        //TODO this is used only with the BranchCoverageSelector
        if (testSelector instanceof BranchCoverageTestSelector) {
            Launcher spoonModel = DSpotCompiler.getSpoonModelOf(inputProgram.getAbsoluteSourceCodeDir(), dependencies);
            DSpotUtils.addBranchLogger(inputProgram, spoonModel.getFactory());
            DSpotUtils.copyLoggerPackage(inputProgram);
            FileUtils.cleanDirectory(output);
            status = DSpotCompiler.compile(inputProgram.getAbsoluteSourceCodeDir(), dependencies, output);
            statusTest = DSpotCompiler.compile(inputProgram.getAbsoluteTestSourceCodeDir(),
                    output.getAbsolutePath() + System.getProperty("path.separator") + dependencies, outputTest);
            if (! (status && statusTest)) {
                throw new RuntimeException("Error during compilation");
            }
        }

        this.compiler = new DSpotCompiler(inputProgram, dependencies);
        this.inputProgram.setFactory(compiler.getLauncher().getFactory());
        this.amplifiers = new ArrayList<>(amplifiers);
        this.numberOfIterations = numberOfIterations;
        this.testSelector = testSelector;
        this.testSelector.init(this.inputConfiguration);
        final File projectJsonFile = new File(this.inputConfiguration.getOutputDirectory() +
                "/" + splittedPath[splittedPath.length - 1] + ".json");
        if (projectJsonFile.exists()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            this.projectTimeJSON = gson.fromJson(new FileReader(projectJsonFile), ProjectTimeJSON.class);
        } else {
            this.projectTimeJSON = new ProjectTimeJSON(splittedPath[splittedPath.length - 1]);
        }
    }

    private void copyResourcesOfTargetProjectIntoDspot(String key) {
        final String resources = inputConfiguration.getProperty(key);
        if (resources != null) {
            String[] pathFiles = resources.split(System.getProperty("path.separator"));
            Arrays.stream(pathFiles).forEach(this::copyResourceOfTargetProjectIntoDspot);
        }
    }

    private void copyResourceOfTargetProjectIntoDspot(String path) {
        try {
            final File resourcesDirectory = new File(inputProgram.getProgramDir() + "/" + path);
            final File[] resources = resourcesDirectory.listFiles();
            if (resources != null) {
                this.testResources.addAll(Arrays.stream(resources)
                        .map(this::relativePathFromListFile)
                        .collect(Collectors.toList()));
                FileUtils.copyDirectory(resourcesDirectory,
                        new File(path));
            }
        } catch (FileAlreadyExistsException ignored) {
            //ignored
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String relativePathFromListFile(File f) {
        return f.getPath().substring((inputProgram.getProgramDir() + "/").length(), f.getPath().length());
    }

    public void addAmplifier(Amplifier amplifier) {
        this.amplifiers.add(amplifier);
    }

    public List<CtType> amplifyAllTests() throws InterruptedException, IOException, ClassNotFoundException {
        final List<CtType> amplifiedTest = inputProgram.getFactory().Class().getAll().stream()
                .filter(ctClass -> !ctClass.getModifiers().contains(ModifierKind.ABSTRACT))
                .filter(ctClass ->
                        ctClass.getMethods().stream()
                                .anyMatch(AmplificationChecker::isTest))
                .map(this::amplifyTest)
                .collect(Collectors.toList());
        writeTimeJson();
        return amplifiedTest;
    }

    private void writeTimeJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File file = new File(this.inputConfiguration.getOutputDirectory() +
                "/" + this.projectTimeJSON.projectName + ".json");
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(gson.toJson(this.projectTimeJSON));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CtType amplifyTest(String fullName) throws InterruptedException, IOException, ClassNotFoundException {
        CtType<Object> clone = this.compiler.getLauncher().getFactory().Type().get(fullName).clone();
        clone.setParent(this.compiler.getLauncher().getFactory().Type().get(fullName).getParent());
        return amplifyTest(clone);
    }

    public CtType amplifyTest(CtType test) {
	    return this.amplifyTest(test, AmplificationHelper.getAllTest(test));
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
            Counter.reset();
            Amplification testAmplification = new Amplification(this.inputConfiguration, this.amplifiers, this.testSelector, this.compiler);
            long time = System.currentTimeMillis();
            testAmplification.amplification(test, methods, numberOfIterations);
            final long elapsedTime = System.currentTimeMillis() - time;
            Log.debug("elapsedTime {}", elapsedTime);
            this.projectTimeJSON.add(new ClassTimeJSON(test.getQualifiedName(), elapsedTime));
            testSelector.report();
            final File outputDirectory = new File(inputConfiguration.getOutputDirectory());
            CtType<?> amplification = AmplificationHelper.createAmplifiedTest(testSelector.getAmplifiedTestCases(), test);
            Log.info("Print {} with {} amplified test cases in {}",  amplification.getSimpleName() ,
                    testSelector.getAmplifiedTestCases().size(), this.inputConfiguration.getOutputDirectory());
            DSpotUtils.printAmplifiedTestClass(amplification, outputDirectory);
            FileUtils.cleanDirectory(compiler.getSourceOutputDirectory());
            FileUtils.cleanDirectory(compiler.getBinaryOutputDirectory());
            writeTimeJson();
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