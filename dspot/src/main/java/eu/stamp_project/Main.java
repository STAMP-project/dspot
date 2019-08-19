package eu.stamp_project;

import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.utils.options.JSAPOptions;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.RandomHelper;
import eu.stamp_project.utils.report.GlobalReport;
import eu.stamp_project.utils.report.error.ErrorReportImpl;
import eu.stamp_project.utils.report.output.OutputReportImpl;
import eu.stamp_project.utils.report.output.selector.TestSelectorReportImpl;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Benjamin DANGLOT benjamin.danglot@inria.fr on 2/9/17
 */
public class Main {

	public static final GlobalReport GLOBAL_REPORT =
			new GlobalReport(new OutputReportImpl(), new ErrorReportImpl(), new TestSelectorReportImpl());

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		try {
			FileUtils.forceDelete(new File("target/dspot/"));
		} catch (Exception ignored) {

		}
		JSAPOptions.parse(args);
		run();
	}

	public static void run() {
		DSpot dspot = new DSpot(
				InputConfiguration.get().getNbIteration(),
				InputConfiguration.get().getAmplifiers(),
				InputConfiguration.get().getSelector(),
				InputConfiguration.get().getBudgetizer()
		);
		RandomHelper.setSeedRandom(InputConfiguration.get().getSeed());
		createOutputDirectories();
		final long startTime = System.currentTimeMillis();
		final List<CtType<?>> amplifiedTestClasses;
		if (InputConfiguration.get().getTestClasses().isEmpty() || "all".equals(InputConfiguration.get().getTestClasses().get(0))) {
			amplifiedTestClasses = dspot.amplifyAllTests();
		} else {
			amplifiedTestClasses = dspot.amplifyTestClassesTestMethods(InputConfiguration.get().getTestClasses(), InputConfiguration.get().getTestCases());
		}
		LOGGER.info("Amplification {}.", amplifiedTestClasses.isEmpty() ? "failed" : "succeed");
		final long elapsedTime = System.currentTimeMillis() - startTime;
		LOGGER.info("Elapsed time {} ms", elapsedTime);
		// global report handling
		Main.GLOBAL_REPORT.output();
		Main.GLOBAL_REPORT.reset();
		// Send info collected.
		InputConfiguration.getInformationCollector().sendInfo();
	}

	public static void createOutputDirectories() {
		final File outputDirectory = new File(InputConfiguration.get().getOutputDirectory());
		try {
			if (InputConfiguration.get().shouldClean() && outputDirectory.exists()) {
				FileUtils.forceDelete(outputDirectory);
			}
			if (!outputDirectory.exists()) {
				FileUtils.forceMkdir(outputDirectory);
			}
		} catch (IOException ignored) {
			// ignored
		}
	}

}