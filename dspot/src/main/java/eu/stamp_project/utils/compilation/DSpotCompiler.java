package eu.stamp_project.utils.compilation;

import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.options.InputConfiguration;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
import spoon.OutputType;
import spoon.SpoonModelBuilder;
import spoon.compiler.builder.AdvancedOptions;
import spoon.compiler.builder.AnnotationProcessingOptions;
import spoon.compiler.builder.ClasspathOptions;
import spoon.compiler.builder.ComplianceOptions;
import spoon.compiler.builder.JDTBuilderImpl;
import spoon.compiler.builder.SourceOptions;
import spoon.support.compiler.FileSystemFolder;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static eu.stamp_project.utils.AmplificationHelper.PATH_SEPARATOR;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/19/17
 */
public class DSpotCompiler extends JDTBasedSpoonCompiler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DSpotCompiler.class);

	public static DSpotCompiler createDSpotCompiler() {
		String pathToSources = InputConfiguration.get().getAbsolutePathToSourceCode()
				+ PATH_SEPARATOR +
				InputConfiguration.get().getAbsolutePathToTestSourceCode();
		Launcher launcher = getSpoonModelOf(pathToSources);
		return new DSpotCompiler(launcher);
	}

	private DSpotCompiler(Launcher launcher) {
		super(launcher.getFactory());
		this.launcher = launcher;
		this.binaryOutputDirectory = new File(InputConfiguration.get().getAbsolutePathToTestClasses());
		this.sourceOutputDirectory = new File(getPathToAmplifiedTestSrc());
		if (!this.sourceOutputDirectory.exists()) {
			this.sourceOutputDirectory.mkdir();
		} else {
			try {
				FileUtils.deleteDirectory(this.sourceOutputDirectory);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public boolean compile(String pathToAdditionalDependencies) {
		if (this.factory == null) {
			this.factory = this.launcher.getFactory();
		}
		javaCompliance = factory.getEnvironment().getComplianceLevel();
		DSpotJDTBatchCompiler compiler = new DSpotJDTBatchCompiler(this, null);//environment);
		final SourceOptions sourcesOptions = new SourceOptions();
		sourcesOptions.sources((new FileSystemFolder(this.sourceOutputDirectory).getAllJavaFiles()));

		this.reportProblems(this.factory.getEnvironment());

		String[] sourcesArray = this.sourceOutputDirectory.getAbsolutePath().split(PATH_SEPARATOR);
		String[] classpath = (InputConfiguration.get().getDependencies() + PATH_SEPARATOR + pathToAdditionalDependencies).split(PATH_SEPARATOR);
		String[] finalClasspath = new String[sourcesArray.length + classpath.length];
		System.arraycopy(sourcesArray, 0, finalClasspath, 0, sourcesArray.length);
		System.arraycopy(classpath, 0, finalClasspath, sourcesArray.length, classpath.length);

		final ClasspathOptions classpathOptions = new ClasspathOptions()
				.encoding(getEnvironment().getEncoding().displayName())
				.classpath(finalClasspath)
				.binaries(getBinaryOutputDirectory());

		final String[] args = new JDTBuilderImpl() //
				.classpathOptions(classpathOptions) //
				.complianceOptions(new ComplianceOptions().compliance(javaCompliance)) //
				.advancedOptions(new AdvancedOptions().preserveUnusedVars().continueExecution().enableJavadoc()) //
				.annotationProcessingOptions(new AnnotationProcessingOptions().compileProcessors()) //
				.sources(sourcesOptions) //
				.build();

		final String[] finalArgs = new String[args.length + 1];
		finalArgs[0] = "-proceedOnError";
		System.arraycopy(args, 0, finalArgs, 1, args.length);

		LOGGER.info("Compiling with {}", String.join(" ", finalArgs));

		compiler.compile(finalArgs);
		environment = compiler.getEnvironment();

		return compiler.globalErrorsCount == 0;
	}

	public static Launcher getSpoonModelOf(String pathToSources) {
		Launcher launcher = new Launcher();
		launcher.getEnvironment().setNoClasspath(true);
		launcher.getEnvironment().setCommentEnabled(true);
		launcher.getEnvironment().setOutputType(OutputType.CLASSES);
		DSpotUtils.copyPackageFromResources();
		String[] sourcesArray = (pathToSources + PATH_SEPARATOR + DSpotUtils.getAbsolutePathToDSpotDependencies()).split(PATH_SEPARATOR);
		Arrays.stream(sourcesArray).forEach(launcher::addInputResource);
		if (!InputConfiguration.get().getDependencies().isEmpty()) {
			String[] dependenciesArray = InputConfiguration.get().getDependencies().split(PATH_SEPARATOR);
			launcher.getModelBuilder().setSourceClasspath(dependenciesArray);
		}
		launcher.buildModel();
		return launcher;
	}

	public static boolean compile(String pathToSources, String dependencies, File binaryOutputDirectory) {
		Launcher launcher = new Launcher();
		if (InputConfiguration.get().isVerbose()) {
			launcher.getEnvironment().setLevel("INFO");
		}
		launcher.getEnvironment().setNoClasspath(true);
		launcher.getEnvironment().setCommentEnabled(true);
		launcher.getEnvironment().setOutputType(OutputType.CLASSES);
		DSpotUtils.copyPackageFromResources(); // sources of problem. TODO check if we need to copy the resources each time we compile
		String[] sourcesArray = (pathToSources + PATH_SEPARATOR).split(PATH_SEPARATOR);
		Arrays.stream(sourcesArray).forEach(launcher::addInputResource);
		String[] dependenciesArray = dependencies.split(PATH_SEPARATOR);
		launcher.getModelBuilder().setSourceClasspath(dependenciesArray);
		launcher.buildModel();

		SpoonModelBuilder modelBuilder = launcher.getModelBuilder();
		modelBuilder.setBinaryOutputDirectory(binaryOutputDirectory);
		return modelBuilder.compile(SpoonModelBuilder.InputType.CTTYPES);
	}

	/**
	 * this method call {@link #compile(String)} and return the potential problems of the compilation.
	 * @param pathToAdditionalDependencies additional dependencies such as external jars. Paths mut be separated by the path separator, e.g. : on Linux
	 * @return a list that contains compilation problems
	 */
	public List<CategorizedProblem> compileAndReturnProblems(String pathToAdditionalDependencies) {
		this.compile(pathToAdditionalDependencies);
		return getProblems();
	}

	/**
	 * This constants represent the path of the .java of the amplified test classes.
	 * This .java contains amplified test methods at different step of the process of DSpot.
	 * The {@link DSpotCompiler} use this path to compile the amplified test class.
	 */

	private static final String PATH_TO_AMPLIFIED_TEST_SRC = "target/dspot/tmp_test_sources";

	public static String getPathToAmplifiedTestSrc() {
		return InputConfiguration.get().getAbsolutePathToProjectRoot() + PATH_TO_AMPLIFIED_TEST_SRC;
	}

	private Launcher launcher;

	private File binaryOutputDirectory;

	private File sourceOutputDirectory;

	public File getBinaryOutputDirectory() {
		return binaryOutputDirectory;
	}

	public File getSourceOutputDirectory() {
		return sourceOutputDirectory;
	}

	public Launcher getLauncher() {
		return launcher;
	}

}
