package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.InitUtils;
import fr.inria.stamp.test.launcher.TestLauncher;
import fr.inria.stamp.test.listener.TestListener;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/08/17
 */
public class RegressionSelector extends TakeAllSelector {

	private String pathToChangedVersionOfProgram;

	public RegressionSelector(String configurationPath, String pathToFolder) throws IOException, InterruptedException {
		InputConfiguration inputConfiguration = new InputConfiguration(configurationPath);
		InitUtils.initLogLevel(inputConfiguration);
		InputProgram inputProgram = InitUtils.initInputProgram(inputConfiguration);
		inputConfiguration.setInputProgram(inputProgram);
		inputProgram.setProgramDir(pathToFolder);
		String dependencies = AutomaticBuilderFactory.getAutomaticBuilder(inputConfiguration)
				.buildClasspath(inputProgram.getProgramDir());
		File output = new File(inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir());
		try {
			FileUtils.cleanDirectory(output);
		} catch (IllegalArgumentException ignored) {
			//the target directory does not exist, do not need to clean it
		}
		DSpotCompiler.compile(inputProgram.getAbsoluteSourceCodeDir(), dependencies, output);
		this.pathToChangedVersionOfProgram = inputProgram.getProgramDir();
	}

	@Override
	public List<CtMethod<?>> selectToKeep(List<CtMethod<?>> amplifiedTestToBeKept) {

		final String classpath = AutomaticBuilderFactory.getAutomaticBuilder(this.configuration)
				.buildClasspath(this.program.getProgramDir()) +
				System.getProperty("path.separator") +
				this.pathToChangedVersionOfProgram + "/" + this.program.getClassesDir() +
				System.getProperty("path.separator") +
				this.program.getProgramDir() + "/" + this.program.getTestClassesDir();

		final TestListener results = TestLauncher.run(this.configuration,
				classpath,
				this.currentClassTestToBeAmplified,
				amplifiedTestToBeKept.stream()
						.map(CtNamedElement::getSimpleName)
						.collect(Collectors.toList())
		);


		if (!results.getFailingTests().isEmpty()) {
			final List<String> failingTestName = results.getFailingTests()
					.stream()
					.map(Failure::getDescription)
					.map(Description::getMethodName)
					.collect(Collectors.toList());
			amplifiedTestToBeKept
					.stream()
					.filter(ctMethod -> failingTestName.contains(ctMethod.getSimpleName()))
					.forEach(this.selectedAmplifiedTest::add);
			System.out.println(this.selectedAmplifiedTest);
			throw new RuntimeException();
		}
		return super.selectToKeep(amplifiedTestToBeKept);
	}
}
