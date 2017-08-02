package fr.inria.diversify.dspot.selector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.selector.json.coverage.TestCaseJSON;
import fr.inria.diversify.dspot.selector.json.coverage.TestClassJSON;
import fr.inria.diversify.dspot.support.Counter;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.stamp.coverage.CoverageResults;
import fr.inria.stamp.coverage.JacocoExecutor;
import org.apache.commons.io.FileUtils;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/07/17
 */
public class JacocoCoverageSelector implements TestSelector {

	private InputProgram program;

	private InputConfiguration configuration;

	private CtType<?> currentClassTestToBeAmplified;

	private List<CtMethod<?>> selectedAmplifiedTest = new ArrayList<>();

	private Map<String, CoverageResults> selectedToBeAmplifiedCoverageResultsMap;

	private CoverageResults initialCoverage;

	@Override
	public void init(InputConfiguration configuration) {
		this.configuration = configuration;
		this.program = configuration.getInputProgram();
		this.selectedAmplifiedTest.clear();
	}

	@Override
	public void reset() {
		//empty
	}

	@Override
	public List<CtMethod<?>> selectToAmplify(List<CtMethod<?>> testsToBeAmplified) {
		if (this.currentClassTestToBeAmplified == null && !testsToBeAmplified.isEmpty()) {
			this.currentClassTestToBeAmplified = testsToBeAmplified.get(0).getDeclaringType();
			final List<String> methodNames = testsToBeAmplified.stream().map(CtNamedElement::getSimpleName).collect(Collectors.toList());
			this.initialCoverage = new JacocoExecutor(this.program, this.configuration).executeJacoco(this.currentClassTestToBeAmplified.getQualifiedName());
			this.selectedToBeAmplifiedCoverageResultsMap = new JacocoExecutor(this.program, this.configuration).executeJacoco(
					this.currentClassTestToBeAmplified, methodNames);
		}
		return testsToBeAmplified;
	}

	@Override
	public List<CtMethod<?>> selectToKeep(List<CtMethod<?>> amplifiedTestToBeKept) {
		if (amplifiedTestToBeKept.isEmpty()) {
			return amplifiedTestToBeKept;
		}
		final List<String> methodNames = amplifiedTestToBeKept.stream().map(CtNamedElement::getSimpleName).collect(Collectors.toList());
		final Map<String, CoverageResults> coverageResultsMap = new JacocoExecutor(this.program, this.configuration).executeJacoco(
				this.currentClassTestToBeAmplified, methodNames);
		final List<CtMethod<?>> methodsKept = amplifiedTestToBeKept.stream()
				.filter(ctMethod ->
						coverageResultsMap.get(ctMethod.getSimpleName()).isBetterThan(
								this.selectedToBeAmplifiedCoverageResultsMap.get(
										getFirstParentThatHasBeenRun(ctMethod).getSimpleName())
						)
				).collect(Collectors.toList());

		this.selectedToBeAmplifiedCoverageResultsMap.putAll(methodsKept.stream()
				.map(CtNamedElement::getSimpleName)
				.collect(
						Collectors.toMap(Function.identity(), coverageResultsMap::get)
				)
		);

		this.selectedAmplifiedTest.addAll(new ArrayList<>(methodsKept));
		return methodsKept;
	}

	private CtMethod<?> getFirstParentThatHasBeenRun(CtMethod<?> test) {
		CtMethod<?> currentParent = AmplificationHelper.getAmpTestToParent().get(test);
		while (AmplificationHelper.getAmpTestToParent().get(currentParent) != null) {
			if (this.selectedToBeAmplifiedCoverageResultsMap.get(currentParent.getSimpleName()) != null) {
				return currentParent;
			} else {
				currentParent = AmplificationHelper.getAmpTestToParent().get(currentParent);
			}
		}
		return currentParent;
	}

	@Override
	public void update() {
		//empty
	}

	@Override
	public void report() {
		final String nl = System.getProperty("line.separator");
		StringBuilder report = new StringBuilder();
		report.append(nl).append("======= REPORT =======").append(nl);
		report.append("Initial instruction coverage: ").append(this.initialCoverage.instructionsCovered)
				.append(" / ").append(this.initialCoverage.instructionsTotal).append(nl)
				.append(String.format("%.2f", 100.0D * ((double) this.initialCoverage.instructionsCovered /
						(double) this.initialCoverage.instructionsTotal))).append("%").append(nl);
		report.append("Amplification results with ").append(this.selectedAmplifiedTest.size())
				.append(" amplified tests.").append(nl);

		final CtType<?> clone = this.currentClassTestToBeAmplified.clone();
		this.currentClassTestToBeAmplified.getPackage().addType(clone);
		this.selectedAmplifiedTest.forEach(clone::addMethod);
		try {
			FileUtils.deleteDirectory(new File("tmpDir/tmpSrc_test"));
		} catch (IOException ignored) {
			//ignored
		}
		DSpotUtils.printJavaFileWithComment(clone, new File("tmpDir/tmpSrc_test"));

		final String fileSeparator = System.getProperty("file.separator");
		final String classpath = new AutomaticBuilderFactory()
				.getAutomaticBuilder(this.configuration)
				.buildClasspath(this.program.getProgramDir())
				+ System.getProperty("path.separator") +
				this.program.getProgramDir() + fileSeparator + this.program.getClassesDir();

		DSpotCompiler.compile("tmpDir/tmpSrc_test", classpath,
				new File(this.program.getProgramDir() + fileSeparator + this.program.getTestClassesDir()));

		final CoverageResults coverageResults =
				new JacocoExecutor(this.program, this.configuration).executeJacoco(this.currentClassTestToBeAmplified.getQualifiedName());

		report.append("Amplified instruction coverage: ").append(coverageResults.instructionsCovered)
				.append(" / ").append(coverageResults.instructionsTotal).append(nl)
				.append(String.format("%.2f", 100.0D * ((double) coverageResults.instructionsCovered /
						(double) coverageResults.instructionsTotal))).append("%").append(nl);

		System.out.println(report.toString());

		File reportDir = new File(this.configuration.getOutputDirectory());
		if (!reportDir.exists()) {
			reportDir.mkdir();
		}

		try (FileWriter writer = new FileWriter(this.configuration.getOutputDirectory() +
				fileSeparator + this.currentClassTestToBeAmplified.getQualifiedName()
				+ "_jacoco_instr_coverage_report.txt", false)) {
			writer.write(report.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		jsonReport(coverageResults);

		this.currentClassTestToBeAmplified = null;
	}

	private void jsonReport(CoverageResults coverageResults) {
		TestClassJSON testClassJSON;
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		final File file = new File(this.configuration.getOutputDirectory() + "/" +
				this.currentClassTestToBeAmplified.getQualifiedName() + "_jacoco_instr_coverage.json");
		if (file.exists()) {
			try {
				testClassJSON = gson.fromJson(new FileReader(file), TestClassJSON.class);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		} else {
			testClassJSON = new TestClassJSON(this.currentClassTestToBeAmplified.getQualifiedName(),
					this.currentClassTestToBeAmplified.getMethods().size(),
					this.initialCoverage.instructionsCovered, this.initialCoverage.instructionsTotal,
					coverageResults.instructionsCovered, coverageResults.instructionsTotal
			);
		}
		this.selectedAmplifiedTest.forEach(ctMethod ->
				new TestCaseJSON(ctMethod.getSimpleName(),
						Counter.getInputOfSinceOrigin(ctMethod),
						Counter.getAssertionOfSinceOrigin(ctMethod),
						this.selectedToBeAmplifiedCoverageResultsMap.get(ctMethod.getSimpleName()).instructionsCovered,
						this.selectedToBeAmplifiedCoverageResultsMap.get(ctMethod.getSimpleName()).instructionsTotal
				)
		);
		try (FileWriter writer = new FileWriter(file, false)) {
			writer.write(gson.toJson(testClassJSON));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override //TODO factorize with pit selector
	public int getNbAmplifiedTestCase() {
		return this.selectedAmplifiedTest.size();
	}

	@Override //TODO factorize with pit selector
	public CtType buildClassForSelection(CtType original, List<CtMethod<?>> methods) {
		CtType clone = original.clone();
		original.getPackage().addType(clone);
		methods.forEach(clone::addMethod);
		return clone;
	}
}
