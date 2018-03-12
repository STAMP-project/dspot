package fr.inria.diversify.automaticbuilder;

import fr.inria.diversify.dspot.selector.PitMutantScoreSelector;
import fr.inria.diversify.mutant.descartes.DescartesChecker;
import fr.inria.diversify.mutant.descartes.DescartesInjector;
import fr.inria.diversify.mutant.pit.MavenPitCommandAndOptions;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.stamp.Main;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.io.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

import static fr.inria.diversify.mutant.pit.MavenPitCommandAndOptions.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/07/17.
 */
public class MavenAutomaticBuilder implements AutomaticBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(MavenAutomaticBuilder.class);

	private InputConfiguration configuration;

	private String mavenHome;

	private String classpath;

	private String contentOfOriginalPom;

	private static final String FILE_SEPARATOR = "/";

	private static final String POM_FILE = "pom.xml";

	MavenAutomaticBuilder(InputConfiguration configuration) {
		this.mavenHome = DSpotUtils.buildMavenHome(configuration);
		this.configuration = configuration;
		final String pathToPom = this.configuration.getInputProgram().getProgramDir() + "/" + POM_FILE;
		if (PitMutantScoreSelector.descartesMode &&
				DescartesChecker.shouldInjectDescartes(pathToPom)) {
			try (final BufferedReader buffer = new BufferedReader(new FileReader(pathToPom))) {
				this.contentOfOriginalPom = buffer.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
			} catch (Exception ignored) {

			}
			DescartesInjector.injectDescartesIntoPom(pathToPom);
		} else {
			this.contentOfOriginalPom = null;
		}
	}

	@Override
	public void compile(String pathToRootOfProject) {
		this.runGoals(pathToRootOfProject, "clean",
				"test",
				"-DskipTests",
				"dependency:build-classpath",
				"-Dmdep.outputFile=" + "target/dspot/classpath"
		);
		final File classpathFile = new File(pathToRootOfProject + "/target/dspot/classpath");
		try (BufferedReader buffer = new BufferedReader(new FileReader(classpathFile))) {
			this.classpath = buffer.lines().collect(Collectors.joining());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String buildClasspath(String pathToRootOfProject) {
		if (this.classpath == null) {
			try {
				final File classpathFile = new File(pathToRootOfProject + "/target/dspot/classpath");
				if (!classpathFile.exists()) {
					this.runGoals(pathToRootOfProject, "dependency:build-classpath", "-Dmdep.outputFile=" + "target/dspot/classpath");
				}
				try (BufferedReader buffer = new BufferedReader(new FileReader(classpathFile))) {
					this.classpath = buffer.lines().collect(Collectors.joining());
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return this.classpath;
	}

	@Override
	public void reset() {
		if (contentOfOriginalPom != null) {
			final String pathToPom = this.configuration.getInputProgram().getProgramDir() + "/" + POM_FILE;
			try (FileWriter writer = new FileWriter(pathToPom)) {
				writer.write(this.contentOfOriginalPom);
				this.contentOfOriginalPom =  null;
			} catch (Exception ignored) {

			}
		}
	}

	@Override
	public void runPit(String pathToRootOfProject, CtType<?>... testClasses) {
		try {
			org.apache.commons.io.FileUtils.deleteDirectory(new File(pathToRootOfProject + "/target/pit-reports"));
		} catch (Exception ignored) {

		}
		try {
			String[] phases = new String[]{CMD_PIT_MUTATION_COVERAGE + ":" +
							PitMutantScoreSelector.pitVersion + ":" + GOAL_PIT_MUTATION_COVERAGE, //
					OPT_WITH_HISTORY, //
					OPT_TARGET_CLASSES + configuration.getProperty("filter"), //
					OPT_VALUE_REPORT_DIR, //
					OPT_VALUE_FORMAT, //
					OPT_VALUE_TIMEOUT, //
					OPT_VALUE_MEMORY, //
					OPT_TARGET_TESTS + Arrays.stream(testClasses).map(DSpotUtils::ctTypeToFullQualifiedName).collect(Collectors.joining(",")), //
					OPT_ADDITIONAL_CP_ELEMENTS + "target/dspot/dependencies/" +
							(configuration.getProperty(PROPERTY_ADDITIONAL_CP_ELEMENTS) != null ?
									"," + configuration.getProperty(PROPERTY_ADDITIONAL_CP_ELEMENTS) : "") , //
					PitMutantScoreSelector.descartesMode ? OPT_MUTATION_ENGINE_DESCARTES : OPT_MUTATION_ENGINE_DEFAULT,
					PitMutantScoreSelector.descartesMode ? "" : OPT_MUTATORS + VALUE_MUTATORS_ALL, //
					configuration.getProperty(PROPERTY_EXCLUDED_CLASSES) != null ?
							OPT_EXCLUDED_CLASSES + configuration.getProperty(PROPERTY_EXCLUDED_CLASSES) :
							""//
			};
			if (this.runGoals(pathToRootOfProject, phases) != 0) {
				throw new RuntimeException("Maven build failed! Enable verbose mode for more information (--verbose)");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Will convert a CtType into a list of test classes full qualified names
	 * in case of abstract test classes, otherwise returns only the full qualified name
	 **/

	@Override
	public void runPit(String pathToRootOfProject) {
		try {
			org.apache.commons.io.FileUtils.deleteDirectory(new File(pathToRootOfProject + "/target/pit-reports"));
		} catch (Exception ignored) {

		}
		try {
			String[] phases = new String[]{CMD_PIT_MUTATION_COVERAGE + ":" + PitMutantScoreSelector.pitVersion + ":" + GOAL_PIT_MUTATION_COVERAGE, //
					OPT_WITH_HISTORY, //
					OPT_TARGET_CLASSES + configuration.getProperty("filter"), //
					OPT_VALUE_REPORT_DIR, //
					OPT_VALUE_FORMAT, //
					OPT_VALUE_TIMEOUT, //
					OPT_VALUE_MEMORY, //
					PitMutantScoreSelector.descartesMode ? OPT_MUTATION_ENGINE_DESCARTES : OPT_MUTATION_ENGINE_DEFAULT,
					PitMutantScoreSelector.descartesMode? "" : OPT_MUTATORS + VALUE_MUTATORS_ALL, //
					OPT_ADDITIONAL_CP_ELEMENTS + "target/dspot/dependencies/" +
							(configuration.getProperty(PROPERTY_ADDITIONAL_CP_ELEMENTS) != null ?
									"," + configuration.getProperty(PROPERTY_ADDITIONAL_CP_ELEMENTS) : "") , //
					configuration.getProperty(PROPERTY_EXCLUDED_CLASSES) != null ?
							OPT_EXCLUDED_CLASSES + configuration.getProperty(PROPERTY_EXCLUDED_CLASSES) :
							""//
			};
			if (this.runGoals(pathToRootOfProject, phases) != 0) {
				throw new RuntimeException("Maven build failed! Enable verbose mode for more information (--verbose)");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private int runGoals(String pathToRootOfProject, String... goals) {
		InvocationRequest request = new DefaultInvocationRequest();
		request.setGoals(Arrays.asList(goals));
		request.setPomFile(new File(pathToRootOfProject + FILE_SEPARATOR + POM_FILE));
		request.setJavaHome(new File(System.getProperty("java.home")));

		Properties properties = new Properties();
		properties.setProperty("enforcer.skip", "true");
		properties.setProperty("checkstyle.skip", "true");
		properties.setProperty("cobertura.skip", "true");
		properties.setProperty("skipITs", "true");
		properties.setProperty("rat.skip", "true");
		properties.setProperty("license.skip", "true");
		properties.setProperty("findbugs.skip", "true");
		properties.setProperty("gpg.skip", "true");
		request.setProperties(properties);

		Invoker invoker = new DefaultInvoker();
		invoker.setMavenHome(new File(this.mavenHome));
		LOGGER.info(String.format("run maven %s", Arrays.stream(goals).collect(Collectors.joining(" "))));
		if (Main.verbose) {
			invoker.setOutputHandler(System.out::println);
			invoker.setErrorHandler(System.err::println);
		} else {
			invoker.setOutputHandler(null);
			invoker.setErrorHandler(null);
		}
		try {
			return invoker.execute(request).getExitCode();
		} catch (MavenInvocationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getOutputDirectoryPit() {
		return MavenPitCommandAndOptions.OUTPUT_DIRECTORY_PIT;
	}
}
