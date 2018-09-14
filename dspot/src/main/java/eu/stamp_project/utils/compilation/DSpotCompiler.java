package eu.stamp_project.utils.compilation;

import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.program.InputConfiguration;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
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
import java.util.Arrays;
import java.util.List;

import static eu.stamp_project.utils.AmplificationHelper.PATH_SEPARATOR;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/19/17
 */
public class DSpotCompiler extends JDTBasedSpoonCompiler {

	public static DSpotCompiler createDSpotCompiler(InputConfiguration configuration, String pathToDependencies) {
		String pathToSources = configuration.getAbsolutePathToSourceCode()
				+ PATH_SEPARATOR +
				configuration.getAbsolutePathToTestSourceCode();
		Launcher launcher = getSpoonModelOf(pathToSources, pathToDependencies);
		return new DSpotCompiler(launcher, configuration, pathToDependencies);
	}

	private DSpotCompiler(Launcher launcher, InputConfiguration configuration, String pathToDependencies) {
		super(launcher.getFactory());
		this.dependencies = pathToDependencies;
		this.launcher = launcher;
		this.binaryOutputDirectory = new File(configuration.getAbsolutePathToTestClasses());
		this.sourceOutputDirectory = new File(PATH_TO_AMPLIFIED_TEST_SRC);
		if (!this.sourceOutputDirectory.exists()) {
			this.sourceOutputDirectory.mkdir();
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
		String[] classpath = (this.dependencies + PATH_SEPARATOR + pathToAdditionalDependencies).split(PATH_SEPARATOR);
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

		compiler.compile(finalArgs);
		environment = compiler.getEnvironment();

		return compiler.globalErrorsCount == 0;
	}

	public static Launcher getSpoonModelOf(String pathToSources, String pathToDependencies) {
		Launcher launcher = new Launcher();
		launcher.getEnvironment().setNoClasspath(true);
		launcher.getEnvironment().setCommentEnabled(true);
		launcher.getEnvironment().setOutputType(OutputType.CLASSES);
		DSpotUtils.copyPackageFromResources();
		String[] sourcesArray = (pathToSources + PATH_SEPARATOR + DSpotUtils.getAbsolutePathToDSpotDependencies()).split(PATH_SEPARATOR);
		Arrays.stream(sourcesArray).forEach(launcher::addInputResource);
		if (!pathToDependencies.isEmpty()) {
			String[] dependenciesArray = pathToDependencies.split(PATH_SEPARATOR);
			launcher.getModelBuilder().setSourceClasspath(dependenciesArray);
		}
		launcher.buildModel();
		return launcher;
	}

	public static boolean compile(InputConfiguration configuration, String pathToSources, String dependencies, File binaryOutputDirectory) {
		Launcher launcher = new Launcher();
		if (configuration.isVerbose()) {
			launcher.getEnvironment().setLevel("INFO");
		}
		launcher.getEnvironment().setNoClasspath(true);
		launcher.getEnvironment().setCommentEnabled(true);
		launcher.getEnvironment().setOutputType(OutputType.CLASSES);
		DSpotUtils.copyPackageFromResources();
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
	 * @param pathToAdditionalDependencies
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
	public static final String PATH_TO_AMPLIFIED_TEST_SRC = new File(InputConfiguration.get().getAbsolutePathToProjectRoot(), "target/dspot/tmp_test_sources").getAbsolutePath();

	private Launcher launcher;

	private File binaryOutputDirectory;

	private String dependencies;

	private File sourceOutputDirectory;

	public File getBinaryOutputDirectory() {
		return binaryOutputDirectory;
	}

	public File getSourceOutputDirectory() {
		return sourceOutputDirectory;
	}

	public String getDependencies() {
		return dependencies;
	}

	public Launcher getLauncher() {
		return launcher;
	}

}
