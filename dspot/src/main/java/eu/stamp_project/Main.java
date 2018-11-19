package eu.stamp_project;

import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.dspot.amplifier.TestDataMutator;
import eu.stamp_project.dspot.selector.JacocoCoverageSelector;
import eu.stamp_project.utils.options.BudgetizerEnum;
import eu.stamp_project.utils.options.JSAPOptions;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.RandomHelper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by Benjamin DANGLOT benjamin.danglot@inria.fr on 2/9/17
 */
public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		try {
			FileUtils.forceDelete(new File("target/dspot/"));
		} catch (Exception ignored) {

		}
		final InputConfiguration configuration = JSAPOptions.parse(args);
		if (configuration == null) {
			Main.runExample();
		} else {
			run(configuration);
		}

		// global report handling
		InputConfiguration.get().getReport().output();
	}

	public static void run(InputConfiguration configuration) throws Exception {
		DSpot dspot = new DSpot(
				configuration.getNbIteration(),
				configuration.getAmplifiers(),
				configuration.getSelector(),
				configuration.getBudgetizer()
		);
		RandomHelper.setSeedRandom(configuration.getSeed());
		createOutputDirectories(configuration);
		final long startTime = System.currentTimeMillis();
		final List<CtType<?>> amplifiedTestClasses;
		if (configuration.getTestClasses().isEmpty() || "all".equals(configuration.getTestClasses().get(0))) {
			amplifiedTestClasses = dspot.amplifyAllTests();
		} else {
			amplifiedTestClasses = dspot.amplifyTestClassesTestMethods(configuration.getTestClasses(), configuration.getTestCases());
		}
		LOGGER.info("Amplification {}.", amplifiedTestClasses.isEmpty() ? "failed" : "succeed");
		final long elapsedTime = System.currentTimeMillis() - startTime;
		LOGGER.info("Elapsed time {} ms", elapsedTime);
	}

	public static void createOutputDirectories(InputConfiguration inputConfiguration) {
		final File outputDirectory = new File(inputConfiguration.getOutputDirectory());
		try {
			if (inputConfiguration.shouldClean() && outputDirectory.exists()) {
				FileUtils.forceDelete(outputDirectory);
			}
			if (!outputDirectory.exists()) {
				FileUtils.forceMkdir(outputDirectory);
			}
		} catch (IOException ignored) {
			// ignored
		}
	}

	static void runExample() {
		try {
			InputConfiguration.initialize("src/test/resources/test-projects/test-projects.properties");
			DSpot dSpot = new DSpot(1,
					Collections.singletonList(new TestDataMutator()),
					new JacocoCoverageSelector(),
					BudgetizerEnum.NoBudgetizer
			);
			dSpot.amplifyTestClassesTestMethods(Collections.singletonList("example.TestSuiteExample"), Collections.emptyList());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}