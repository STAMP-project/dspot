package eu.stamp_project.automaticbuilder;

import eu.stamp_project.mutant.descartes.DescartesChecker;
import eu.stamp_project.mutant.descartes.DescartesInjector;
import eu.stamp_project.mutant.pit.MavenPitCommandAndOptions;
import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static eu.stamp_project.mutant.pit.MavenPitCommandAndOptions.CMD_PIT_MUTATION_COVERAGE;
import static eu.stamp_project.mutant.pit.MavenPitCommandAndOptions.GOAL_PIT_MUTATION_COVERAGE;
import static eu.stamp_project.mutant.pit.MavenPitCommandAndOptions.OPT_ADDITIONAL_CP_ELEMENTS;
import static eu.stamp_project.mutant.pit.MavenPitCommandAndOptions.OPT_EXCLUDED_CLASSES;
import static eu.stamp_project.mutant.pit.MavenPitCommandAndOptions.OPT_MUTATION_ENGINE_DEFAULT;
import static eu.stamp_project.mutant.pit.MavenPitCommandAndOptions.OPT_MUTATION_ENGINE_DESCARTES;
import static eu.stamp_project.mutant.pit.MavenPitCommandAndOptions.OPT_MUTATORS;
import static eu.stamp_project.mutant.pit.MavenPitCommandAndOptions.OPT_TARGET_CLASSES;
import static eu.stamp_project.mutant.pit.MavenPitCommandAndOptions.OPT_TARGET_TESTS;
import static eu.stamp_project.mutant.pit.MavenPitCommandAndOptions.OPT_VALUE_FORMAT;
import static eu.stamp_project.mutant.pit.MavenPitCommandAndOptions.OPT_VALUE_MEMORY;
import static eu.stamp_project.mutant.pit.MavenPitCommandAndOptions.OPT_VALUE_REPORT_DIR;
import static eu.stamp_project.mutant.pit.MavenPitCommandAndOptions.OPT_VALUE_TIMEOUT;
import static eu.stamp_project.mutant.pit.MavenPitCommandAndOptions.OPT_WITH_HISTORY;
import static eu.stamp_project.mutant.pit.MavenPitCommandAndOptions.VALUE_MUTATORS_ALL;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/07/17.
 */
public class MavenAutomaticBuilder implements AutomaticBuilder {

    private static final String MESSAGE_WARN_PIT_NO_FILTER = "You gave an empty filter. To use PIT, it is recommend to specify a filter, at least, the top package of your program, otherwise, PIT may take a long time or could not be run.";

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenAutomaticBuilder.class);

    private String classpath;

    private String contentOfOriginalPom;

    private static final String FILE_SEPARATOR = File.separator;

    public static final String POM_FILE = "pom.xml";

    private void initializeForDescartes() {
        final String pathToPom = InputConfiguration.get().getAbsolutePathToProjectRoot() + FILE_SEPARATOR + POM_FILE;
        if (DescartesChecker.shouldInjectDescartes(pathToPom)) {
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
    public String compileAndBuildClasspath() {
        this.runGoals(InputConfiguration.get().getAbsolutePathToProjectRoot(), "clean",
                "test",
                "-DskipTests",
                "dependency:build-classpath",
                "-Dmdep.outputFile=" + "target/dspot/classpath"
        );
        final File classpathFile = new File(InputConfiguration.get().getAbsolutePathToProjectRoot() + "/target/dspot/classpath");
        try (BufferedReader buffer = new BufferedReader(new FileReader(classpathFile))) {
            this.classpath = buffer.lines().collect(Collectors.joining());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this.classpath;
    }

    @Override
    public void compile() {
        this.runGoals(InputConfiguration.get().getAbsolutePathToProjectRoot(), "clean",
                "test",
                "-DskipTests",
                "dependency:build-classpath",
                "-Dmdep.outputFile=" + "target/dspot/classpath"
        );
        final File classpathFile = new File(InputConfiguration.get().getAbsolutePathToProjectRoot() + "/target/dspot/classpath");
        try (BufferedReader buffer = new BufferedReader(new FileReader(classpathFile))) {
            this.classpath = buffer.lines().collect(Collectors.joining());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String buildClasspath() {
        if (this.classpath == null) {
            try {
                final File classpathFile = new File(InputConfiguration.get().getAbsolutePathToProjectRoot() + "/target/dspot/classpath");
                if (!classpathFile.exists()) {
                    this.runGoals(
                            InputConfiguration.get().getAbsolutePathToProjectRoot(),
                            "dependency:build-classpath",
                            "-Dmdep.outputFile=" + "target/dspot/classpath"
                    );
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
            final String pathToPom = InputConfiguration.get().getAbsolutePathToProjectRoot() + FILE_SEPARATOR + POM_FILE;
            try (FileWriter writer = new FileWriter(pathToPom)) {
                writer.write(this.contentOfOriginalPom);
                this.contentOfOriginalPom = null;
            } catch (Exception ignored) {

            }
        }
    }

    @Override
    public void runPit(String pathToRootOfProject, CtType<?>... testClasses) {
        initializeForDescartes();
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(new File(pathToRootOfProject + "/target/pit-reports"));
        } catch (Exception ignored) {

        }
        try {

            if (InputConfiguration.get().getFilter().isEmpty()) {
                initializeFilterInputConfiguration();
            }

            String[] phases = new String[]{CMD_PIT_MUTATION_COVERAGE + ":" +
                    InputConfiguration.get().getPitVersion() + ":" + GOAL_PIT_MUTATION_COVERAGE, //
                    OPT_WITH_HISTORY, //
                    OPT_TARGET_CLASSES + InputConfiguration.get().getFilter(), //
                    OPT_VALUE_REPORT_DIR, //
                    OPT_VALUE_FORMAT, //
                    OPT_VALUE_TIMEOUT, //
                    OPT_VALUE_MEMORY, //
                    OPT_TARGET_TESTS + Arrays.stream(testClasses).map(DSpotUtils::ctTypeToFullQualifiedName).collect(Collectors.joining(",")), //
                    OPT_ADDITIONAL_CP_ELEMENTS + DSpotUtils.getAbsolutePathToDSpotDependencies() +
                            (!InputConfiguration.get().getAdditionalClasspathElements().isEmpty() ?
                                    "," + InputConfiguration.get().getAdditionalClasspathElements() : ""), //
                    InputConfiguration.get().isDescartesMode() ? OPT_MUTATION_ENGINE_DESCARTES : OPT_MUTATION_ENGINE_DEFAULT,
                    InputConfiguration.get().isDescartesMode() ? "" : OPT_MUTATORS + VALUE_MUTATORS_ALL, //
                    !InputConfiguration.get().getExcludedClasses().isEmpty() ?
                            OPT_EXCLUDED_CLASSES + InputConfiguration.get().getExcludedClasses() :
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

        if (InputConfiguration.get().getFilter().isEmpty()) {
            initializeFilterInputConfiguration();
        }

        try {
            String[] phases = new String[]{CMD_PIT_MUTATION_COVERAGE + ":" + InputConfiguration.get().getPitVersion() + ":" + GOAL_PIT_MUTATION_COVERAGE, //
                    OPT_WITH_HISTORY, //
                    OPT_TARGET_CLASSES + InputConfiguration.get().getFilter(), //
                    OPT_VALUE_REPORT_DIR, //
                    OPT_VALUE_FORMAT, //
                    OPT_VALUE_TIMEOUT, //
                    OPT_VALUE_MEMORY, //
                    InputConfiguration.get().isDescartesMode() ? OPT_MUTATION_ENGINE_DESCARTES : OPT_MUTATION_ENGINE_DEFAULT,
                    InputConfiguration.get().isDescartesMode() ? "" : OPT_MUTATORS + VALUE_MUTATORS_ALL, //
                    OPT_ADDITIONAL_CP_ELEMENTS + DSpotUtils.getAbsolutePathToDSpotDependencies() +
                            (!InputConfiguration.get().getAdditionalClasspathElements().isEmpty() ?
                                    "," + InputConfiguration.get().getAdditionalClasspathElements() : ""), //
                    !InputConfiguration.get().getExcludedClasses().isEmpty() ?
                            OPT_EXCLUDED_CLASSES + InputConfiguration.get().getExcludedClasses() :
                            ""//
            };
            if (this.runGoals(pathToRootOfProject, phases) != 0) {
                throw new RuntimeException("Maven build failed! Enable verbose mode for more information (--verbose)");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeFilterInputConfiguration() {
        LOGGER.warn(MESSAGE_WARN_PIT_NO_FILTER);
        LOGGER.warn("Trying to compute the top package of the project...");
        CtPackage currentPackage = InputConfiguration.get().getFactory().Package().getRootPackage();
        while (currentPackage.getPackages().size() == 1) {
            currentPackage = (CtPackage) currentPackage.getPackages().toArray()[0];
        }
        InputConfiguration.get().setFilter(currentPackage.getQualifiedName());
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
        invoker.setMavenHome(new File(this.buildMavenHome()));
        LOGGER.info(String.format("run maven %s", Arrays.stream(goals).collect(Collectors.joining(" "))));
        if (InputConfiguration.get().isVerbose()) {
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

    private String buildMavenHome() {
        InputConfiguration configuration = InputConfiguration.get();
        String mavenHome = null;
        if (configuration != null) {
            if (!configuration.getMavenHome().isEmpty()) {
                mavenHome = configuration.getMavenHome();
            } else {
                mavenHome = getMavenHome(envVariable -> System.getenv().get(envVariable) != null,
                        envVariable -> System.getenv().get(envVariable),
                        "MAVEN_HOME", "M2_HOME");
                if (mavenHome == null) {
                    mavenHome = getMavenHome(path -> new File(path).exists(),
                            Function.identity(),
                            "/usr/share/maven/", "/usr/local/maven-3.3.9/", "/usr/share/maven3/");
                    if (mavenHome == null) {
                        throw new RuntimeException("Maven home not found, please set properly MAVEN_HOME or M2_HOME.");
                    }
                }
                // update the value inside the input configuration
                configuration.setMavenHome(mavenHome);
            }
        }
        return mavenHome;
    }

    private String getMavenHome(Predicate<String> conditional,
                                Function<String, String> getFunction,
                                String... possibleValues) {
        String mavenHome = null;
        final Optional<String> potentialMavenHome = Arrays.stream(possibleValues).filter(conditional).findFirst();
        if (potentialMavenHome.isPresent()) {
            mavenHome = getFunction.apply(potentialMavenHome.get());
        }
        return mavenHome;
    }
}
