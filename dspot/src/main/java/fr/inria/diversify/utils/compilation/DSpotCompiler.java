package fr.inria.diversify.utils.compilation;

import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import fr.inria.stamp.Main;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import spoon.Launcher;
import spoon.OutputType;
import spoon.SpoonModelBuilder;
import spoon.compiler.builder.*;
import spoon.support.compiler.FileSystemFolder;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static fr.inria.diversify.utils.AmplificationHelper.PATH_SEPARATOR;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/19/17
 */
public class DSpotCompiler extends JDTBasedSpoonCompiler {

	public static final String pathToTmpTestSources = "target/dspot/tmp_test_sources";

	public static DSpotCompiler createDSpotCompiler(InputProgram program, String pathToDependencies) {
		String pathToSources = program.getAbsoluteSourceCodeDir() + PATH_SEPARATOR + program.getAbsoluteTestSourceCodeDir();
		Launcher launcher = getSpoonModelOf(pathToSources, pathToDependencies);
		return new DSpotCompiler(launcher, program, pathToDependencies);
	}

	private DSpotCompiler(Launcher launcher, InputProgram program, String pathToDependencies) {
		super(launcher.getFactory());
		this.dependencies = pathToDependencies;
		this.launcher = launcher;
		this.binaryOutputDirectory = new File(program.getProgramDir() + "/" + program.getTestClassesDir());
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

		if (! new File("target/dspot/dependencies/compare").exists()) {
			DSpotUtils.copyPackageFromResources("fr/inria/diversify/compare/",
					"MethodsHandler", "ObjectLog", "Observation", "Utils");
		}
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

		if (! new File("target/dspot/dependencies/compare").exists()) {
			DSpotUtils.copyPackageFromResources("fr/inria/diversify/compare/",
					"MethodsHandler", "ObjectLog", "Observation", "Utils");
		}
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
