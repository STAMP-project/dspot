package eu.stamp_project.dspot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.Main;
import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.automaticbuilder.maven.DSpotPOMCreator;
import eu.stamp_project.dspot.amplifier.StringLiteralAmplifier;
import eu.stamp_project.dspot.amplifier.value.ValueCreator;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.AssertionGeneratorUtils;
import eu.stamp_project.dspot.input_ampl_distributor.InputAmplDistributor;
import eu.stamp_project.dspot.selector.JacocoCoverageSelector;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotCache;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.RandomHelper;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.compilation.TestCompiler;
import eu.stamp_project.utils.configuration.DSpotConfiguration;
import eu.stamp_project.utils.execution.TestRunner;
import eu.stamp_project.utils.json.ProjectTimeJSON;
import eu.stamp_project.utils.options.AutomaticBuilderEnum;
import eu.stamp_project.utils.options.InputAmplDistributorEnum;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.report.output.Output;
import eu.stamp_project.utils.test_finder.TestFinder;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.factory.Factory;

import java.io.*;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/06/17
 */
public class ProjectJSONTest extends AbstractTestOnSample {

    private InputConfiguration configuration;

    private AutomaticBuilder builder;

    protected final String outputDirectory = "target/dspot/output";

    private DSpotCompiler compiler;

    private Factory factory;

    private TestSelector testSelector;

    private TestCompiler testCompiler;

    private DSpotConfiguration dspotConfiguration;

    @Before
    public void setUp() {
        super.setUp();
        this.configuration = new InputConfiguration();
        this.configuration.setAbsolutePathToProjectRoot(getPathToProjectRoot());
        this.configuration.setOutputDirectory(outputDirectory);
        this.builder = AutomaticBuilderEnum.Maven.getAutomaticBuilder(configuration);
        this.dspotConfiguration = new DSpotConfiguration();
        String dependencies = dspotConfiguration.completeDependencies(configuration, this.builder);
        DSpotUtils.init(false, outputDirectory,
                this.configuration.getFullClassPathWithExtraDependencies(),
                this.getPathToProjectRoot()
        );
        this.compiler = DSpotCompiler.createDSpotCompiler(
                configuration,
                dependencies
        );
        DSpotCache.init(10000);
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.addInputResource(this.getPathToProjectRoot());
        launcher.buildModel();
        this.factory = launcher.getFactory();
        TestFramework.init(this.factory);
        testCompiler = new TestCompiler(0,
                false,
                this.getPathToProjectRoot(),
                this.configuration.getClasspathClassesProject(),
                10000,
                "",
                false
        );
        AssertionGeneratorUtils.init(false);
        DSpotPOMCreator.createNewPom(configuration);
        RandomHelper.setSeedRandom(72L);
        ValueCreator.count = 0;
        this.testSelector = new JacocoCoverageSelector(builder, configuration);
        try {
            FileUtils.forceDelete(new File(outputDirectory));
        } catch (Exception ignored) {

        }
    }

    @Test
    public void test() throws Exception {

        final File file = new File(outputDirectory + "/sample.json");
        if (file.exists()) {
            file.delete();
        }

        final InputAmplDistributor inputAmplDistributor =
                InputAmplDistributorEnum.RandomInputAmplDistributor.getInputAmplDistributor(200, Collections.emptyList());
        DSpotConfiguration dspotConfiguration = new DSpotConfiguration();
        dspotConfiguration.getInputConfiguration().setDelta(0.1f);
        dspotConfiguration.setTestFinder(TestFinder.get());
        dspotConfiguration.setCompiler(compiler);
        dspotConfiguration.setTestSelector(testSelector);
        dspotConfiguration.setInputAmplDistributor(inputAmplDistributor);
        dspotConfiguration.setOutput(Output.get(configuration));
        dspotConfiguration.getInputConfiguration().setNbIteration(1);
        dspotConfiguration.getInputConfiguration().setGenerateAmplifiedTestClass(false);
        dspotConfiguration.setAutomaticBuilder(builder);
        dspotConfiguration.setTestCompiler(testCompiler);
        dspotConfiguration.setTestClassesToBeAmplified(Collections.singletonList(findClass("fr.inria.amp.TestJavaPoet")));
        DSpot dspot = new DSpot(dspotConfiguration);
        final CtClass<?> clone = findClass("fr.inria.amp.TestJavaPoet").clone();
        dspot.run();
        ProjectTimeJSON projectJson = getProjectJson(file);
        assertTrue(projectJson.classTimes.
                stream()
                .anyMatch(classTimeJSON ->
                        "fr.inria.amp.TestJavaPoet".equals(classTimeJSON.fullQualifiedName)
                )
        );
        assertEquals(1, projectJson.classTimes.size());
        assertEquals("sample", projectJson.projectName);

        dspotConfiguration.setTestClassesToBeAmplified(Collections.singletonList(findClass("fr.inria.mutation.ClassUnderTestTest")));
        dspot.run();
        projectJson = getProjectJson(file);
        assertTrue(projectJson.classTimes.stream().anyMatch(classTimeJSON -> classTimeJSON.fullQualifiedName.equals("fr.inria.amp.TestJavaPoet")));
        assertTrue(projectJson.classTimes.stream().anyMatch(classTimeJSON -> classTimeJSON.fullQualifiedName.equals("fr.inria.mutation.ClassUnderTestTest")));
        assertEquals(2, projectJson.classTimes.size());
        assertEquals("sample", projectJson.projectName);

        /* we reinitialize the factory to remove the amplified test class */

        final CtClass<?> amplifiedClassToBeRemoved = findClass("fr.inria.amp.TestJavaPoet");
        final CtPackage aPackage = amplifiedClassToBeRemoved.getPackage();
        aPackage.removeType(amplifiedClassToBeRemoved);
        aPackage.addType(clone);

        dspotConfiguration.setTestClassesToBeAmplified(Collections.singletonList(findClass("fr.inria.amp.TestJavaPoet")));
        dspot.run();
        projectJson = getProjectJson(file);
        assertTrue(projectJson.classTimes.stream().anyMatch(classTimeJSON -> classTimeJSON.fullQualifiedName.equals("fr.inria.amp.TestJavaPoet")));
        assertTrue(projectJson.classTimes.stream().anyMatch(classTimeJSON -> classTimeJSON.fullQualifiedName.equals("fr.inria.mutation.ClassUnderTestTest")));
        assertEquals(2, projectJson.classTimes.size());
        assertEquals("sample", projectJson.projectName);
    }

    private static ProjectTimeJSON getProjectJson(File fileProjectJSON) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            return gson.fromJson(new FileReader(fileProjectJSON), ProjectTimeJSON.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //we cannot predict the time of the computation of dspot, we split the output string into two parts: head and tail.

    private static final String[] expectedFirstProjectJSON = new String[]{
            "{" + AmplificationHelper.LINE_SEPARATOR +
                    "  \"classTimes\": [" + AmplificationHelper.LINE_SEPARATOR +
                    "    {" + AmplificationHelper.LINE_SEPARATOR +
                    "      \"fullQualifiedName\": \"fr.inria.amp.TestJavaPoet\"," + AmplificationHelper.LINE_SEPARATOR +
                    "      \"timeInMs\": ",
            AmplificationHelper.LINE_SEPARATOR +
                    "    }" + AmplificationHelper.LINE_SEPARATOR +
                    "  ]," + AmplificationHelper.LINE_SEPARATOR +
                    "  \"projectName\": \"sample\"" + AmplificationHelper.LINE_SEPARATOR +
                    "}",
            "    }," + AmplificationHelper.LINE_SEPARATOR +
                    "    {" + AmplificationHelper.LINE_SEPARATOR +
                    "      \"fullQualifiedName\": \"fr.inria.mutation.ClassUnderTestTest\"," + AmplificationHelper.LINE_SEPARATOR +
                    "      \"timeInMs\": ",
            "    }" + AmplificationHelper.LINE_SEPARATOR +
                    "  ]," + AmplificationHelper.LINE_SEPARATOR +
                    "  \"projectName\": \"sample\"" + AmplificationHelper.LINE_SEPARATOR +
                    "}"
    };
}
