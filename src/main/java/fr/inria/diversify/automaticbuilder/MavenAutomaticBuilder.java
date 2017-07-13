package fr.inria.diversify.automaticbuilder;

import fr.inria.diversify.buildSystem.maven.MavenInvoker;
import fr.inria.diversify.dspot.DSpotUtils;
import fr.inria.diversify.mutant.pit.PitResult;
import fr.inria.diversify.mutant.pit.PitResultParser;
import fr.inria.diversify.runner.InputConfiguration;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.PrintStreamHandler;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static fr.inria.diversify.mutant.pit.MavenPitCommandAndOptions.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/07/17.
 */
public class MavenAutomaticBuilder implements AutomaticBuilder {

    private InputConfiguration configuration;

    private String mavenHome;

    private static final String FILE_SEPARATOR = "/";

    private static final String NAME_FILE_CLASSPATH = "cp";

    private static final String POM_FILE = "pom.xml";

    public MavenAutomaticBuilder(@Deprecated InputConfiguration configuration) {
        this.mavenHome = DSpotUtils.buildMavenHome(configuration);
        this.configuration = configuration;
    }

    @Override
    public void compile(String pathToRootOfProject) {
        this.runGoals(pathToRootOfProject,"clean", "test", "-DskipTests");
    }

    @Override
    public String buildClasspath(String pathToRootOfProject) {
        try {
            final File classpathFile = new File(pathToRootOfProject + FILE_SEPARATOR + NAME_FILE_CLASSPATH);
            if (!classpathFile.exists()) {
                this.runGoals(pathToRootOfProject, "dependency:build-classpath", "-Dmdep.outputFile=" + NAME_FILE_CLASSPATH);
            }
            try (BufferedReader buffer = new BufferedReader(new FileReader(classpathFile))) {
                return buffer.lines().collect(Collectors.joining());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<PitResult> runPit(String pathToRootOfProject, CtType<?> testClass) {
        try {
            String[] phases = new String[]{PRE_GOAL_PIT, //
                    CMD_PIT_MUTATION_COVERAGE, //
                    OPT_WITH_HISTORY, //
                    OPT_TARGET_CLASSES + configuration.getProperty("filter"), //
                    OPT_VALUE_REPORT_DIR, //
                    OPT_VALUE_FORMAT, //
                    OPT_VALUE_TIMEOUT, //
                    OPT_VALUE_MEMORY, //
                    OPT_TARGET_TESTS + ctTypeToFullQualifiedName(testClass), //
                    configuration.getProperty(PROPERTY_ADDITIONAL_CP_ELEMENTS) != null ?
                            OPT_ADDITIONAL_CP_ELEMENTS + configuration.getProperty(PROPERTY_ADDITIONAL_CP_ELEMENTS) :
                            "", //
                    descartesMode ? OPT_MUTATION_ENGINE :
                            OPT_MUTATORS + (evosuiteMode ?
                                    Arrays.stream(VALUE_MUTATORS_EVOSUITE).collect(Collectors.joining(","))
                                    : VALUE_MUTATORS_ALL), //
                    configuration.getProperty(PROPERTY_EXCLUDED_CLASSES) != null ?
                            OPT_EXCLUDED_CLASSES + configuration.getProperty(PROPERTY_EXCLUDED_CLASSES) :
                            ""//
            };
            this.runGoals(pathToRootOfProject, phases);
            if (!new File(pathToRootOfProject + "/target/pit-reports").exists()) {
                return null;
            }
            final File[] files = new File(pathToRootOfProject + "/target/pit-reports").listFiles();
            if (files == null) {
                return null;
            }
            File directoryReportPit = files[0];
            if (!directoryReportPit.exists()) {
                return null;
            }
            File fileResults = new File(directoryReportPit.getPath() + "/mutations.csv");
            return PitResultParser.parse(fileResults);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Will convert a CtType into a list of test classes full qualified names
     * in case of abstract test classes, otherwise returns only the full qualified name
     **/
    private String ctTypeToFullQualifiedName(CtType<?> testClass) {
        if (testClass.getModifiers().contains(ModifierKind.ABSTRACT)) {
            CtTypeReference<?> referenceOfSuperClass = testClass.getReference();
            return testClass.getFactory().Class().getAll()
                    .stream()
                    .filter(ctType -> referenceOfSuperClass.equals(ctType.getSuperclass()))
                    .map(CtType::getQualifiedName)
                    .collect(Collectors.joining(","));
        } else {
            return testClass.getQualifiedName();
        }
    }

    @Override
    public List<PitResult> runPit(String pathToRootOfProject) {
        try {
            String[] phases = new String[]{PRE_GOAL_PIT, //
                    CMD_PIT_MUTATION_COVERAGE, //
                    OPT_WITH_HISTORY, //
                    OPT_TARGET_CLASSES + configuration.getProperty("filter"), //
                    OPT_VALUE_REPORT_DIR, //
                    OPT_VALUE_FORMAT, //
                    OPT_VALUE_TIMEOUT, //
                    OPT_VALUE_MEMORY, //
                    descartesMode ? OPT_MUTATION_ENGINE : OPT_MUTATORS + (evosuiteMode ?
                            Arrays.stream(VALUE_MUTATORS_EVOSUITE).collect(Collectors.joining(","))
                            : VALUE_MUTATORS_ALL), //
                    configuration.getProperty(PROPERTY_ADDITIONAL_CP_ELEMENTS) != null ?
                            OPT_ADDITIONAL_CP_ELEMENTS + configuration.getProperty(PROPERTY_ADDITIONAL_CP_ELEMENTS) :
                            "", //
                    configuration.getProperty(PROPERTY_EXCLUDED_CLASSES) != null ?
                            OPT_EXCLUDED_CLASSES + configuration.getProperty(PROPERTY_EXCLUDED_CLASSES) :
                            ""//
            };
            this.runGoals(pathToRootOfProject, phases);
            File directoryReportPit = new File(pathToRootOfProject + "/target/pit-reports").listFiles()[0];
            return PitResultParser.parse(new File(directoryReportPit.getPath() + "/mutations.csv"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void runGoals(String pathToRootOfProject, String... goals) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setGoals(Arrays.asList(goals));
        request.setPomFile(new File(pathToRootOfProject + FILE_SEPARATOR + POM_FILE));
        request.setJavaHome(new File(System.getProperty("java.home")));
        MavenInvoker invoker = new MavenInvoker();
        invoker.setMavenHome(new File(this.mavenHome));
        PrintStream stream = null;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            stream = new PrintStream(os);
            PrintStreamHandler psh = new PrintStreamHandler(stream, true);
            invoker.setOutputHandler(psh);
            invoker.setErrorHandler(psh);
            invoker.execute(request);
            System.out.println(os.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (stream != null)
                stream.close();
        }
    }
}
