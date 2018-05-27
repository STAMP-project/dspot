package eu.stamp_project.utils.compilation;

import eu.stamp_project.Main;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.sosiefier.InputConfiguration;
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

	public static final String pathToTmpTestSources = "target/dspot/tmp_test_sources";

	public static DSpotCompiler createDSpotCompiler(InputConfiguration configuration, String pathToDependencies) {
		String pathToSources = configuration.getInputProgram().getAbsoluteSourceCodeDir() + PATH_SEPARATOR +
				configuration.getInputProgram().getAbsoluteTestSourceCodeDir();
		Launcher launcher = getSpoonModelOf(pathToSources, pathToDependencies);
		return new DSpotCompiler(launcher, configuration, pathToDependencies);
	}

	private DSpotCompiler(Launcher launcher, InputConfiguration configuration, String pathToDependencies) {
		super(launcher.getFactory());
		this.dependencies = pathToDependencies;
		this.launcher = launcher;
		this.binaryOutputDirectory = new File(configuration.getAbsolutePathToProjectRoot() + "/" +
				configuration.getInputProgram().getTestClassesDir());
		this.sourceOutputDirectory = new File(pathToTmpTestSources);
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

	public List<CategorizedProblem> compileAndGetProbs(String pathToAdditionalDependencies) {
		this.compile(pathToAdditionalDependencies);
		return getProblems();
	}

	public static Launcher getSpoonModelOf(String pathToSources, String pathToDependencies) {
		Launcher launcher = new Launcher();
		launcher.getEnvironment().setNoClasspath(true);
		launcher.getEnvironment().setCommentEnabled(true);
		launcher.getEnvironment().setOutputType(OutputType.CLASSES);
		DSpotUtils.copyPackageFromResources();
		String[] sourcesArray = (pathToSources + PATH_SEPARATOR + "target/dspot/dependencies/").split(PATH_SEPARATOR);
		Arrays.stream(sourcesArray).forEach(launcher::addInputResource);
		if (!pathToDependencies.isEmpty()) {
			String[] dependenciesArray = pathToDependencies.split(PATH_SEPARATOR);
			launcher.getModelBuilder().setSourceClasspath(dependenciesArray);
		}
		launcher.buildModel();
		return launcher;
	}

	public static boolean compile(String pathToSources, String dependencies, File binaryOutputDirectory) {
		Launcher launcher = new Launcher();
		if (Main.verbose) {
			launcher.getEnvironment().setLevel("DEBUG");
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
