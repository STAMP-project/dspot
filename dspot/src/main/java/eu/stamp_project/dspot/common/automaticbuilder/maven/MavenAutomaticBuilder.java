package eu.stamp_project.dspot.common.automaticbuilder.maven;

import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.common.configuration.DSpotState;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/07/17.
 */
public class MavenAutomaticBuilder implements AutomaticBuilder {

    public static final String CMD_PIT_MUTATION_COVERAGE = "org.pitest:pitest-maven:mutationCoverage";

    public static final String OPT_TARGET_TESTS = "-DtargetTests=";

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenAutomaticBuilder.class);

    private String classpath = null;

    private String absolutePathToTopProjectRoot;

    private String absolutePathToProjectRoot;

    private String mavenHome;

    private boolean shouldExecuteTestsInParallel;

    public void setAbsolutePathToProjectRoot(String absolutePathToProjectRoot) {
        this.absolutePathToProjectRoot = absolutePathToProjectRoot;
    }

    public MavenAutomaticBuilder(UserInput configuration) {
        this.shouldExecuteTestsInParallel = configuration.shouldExecuteTestsInParallel();
        DSpotPOMCreator.createNewPom(configuration);
        this.absolutePathToProjectRoot = configuration.getAbsolutePathToProjectRoot();
        this.absolutePathToTopProjectRoot = configuration.getAbsolutePathToTopProjectRoot();
        this.mavenHome = buildMavenHome(configuration);
    }

    @Override
    public String compileAndBuildClasspath() {
        if (this.classpath == null) {
            this.computeClasspath(
                    "clean",
                    "test",
                    "-DskipTests",
                    "dependency:build-classpath",
                    "-Dmdep.outputFile=" + "target/dspot/classpath"
            );
            final File classpathFile = new File(this.absolutePathToProjectRoot + "/target/dspot/classpath");
            try (BufferedReader buffer = new BufferedReader(new FileReader(classpathFile))) {
                this.classpath = buffer.lines().collect(Collectors.joining());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return this.classpath;
    }

    private int computeClasspath(String... goals) {
        final String pomPathname;
        if (shouldExecuteTestsInParallel) {
            // TODO
            DSpotPOMCreator.createNewPomForComputingClassPathWithParallelExecution(false, null);
            pomPathname = this.absolutePathToTopProjectRoot
                    + DSpotPOMCreator.getParallelPOMName();
            LOGGER.info("Using {} to run maven.", pomPathname);
            return _runGoals(true, pomPathname, goals);
        } else {
            pomPathname = this.absolutePathToTopProjectRoot + DSpotPOMCreator.POM_FILE;
            LOGGER.info("Using {} to run maven.", pomPathname);
            return _runGoals(false, pomPathname, goals);
        }
    }


    @Override
    public void compile() {
        this.runGoals(false,
                "clean",
                "test",
                "-DskipTests"
        );
    }

    @Override
    public String buildClasspath() {
        if (this.classpath == null) {
            try {
                final File classpathFile = new File(this.absolutePathToProjectRoot + "/target/dspot/classpath");
                if (!classpathFile.exists()) {
                    this.runGoals(false,
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
        // empty
    }

    @Override
    public void runPit(CtType<?>... testClasses) {
        try {
            FileUtils.deleteDirectory(new File(this.absolutePathToProjectRoot + "/target/pit-reports"));
        } catch (Exception ignored) {

        }
        try {
            String[] goals = new String[]{
                    CMD_PIT_MUTATION_COVERAGE, //
                    testClasses.length > 0 ?
                            OPT_TARGET_TESTS + Arrays.stream(testClasses)
                                    .map(DSpotUtils::ctTypeToFullQualifiedName)
                                    .collect(Collectors.joining(",")) :
                            "" //
            };
            if (this.runGoals(true, goals) != 0) {
                throw new RuntimeException("Maven build failed! Enable verbose mode for more information (--verbose)");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void runPit() {
        this.runPit(new CtType<?>[0]);
    }

    private int runGoals(boolean specificPom, String... goals) {
        final String pomPathname = this.absolutePathToProjectRoot + "/" + (
                specificPom ? DSpotPOMCreator.getPOMName() : DSpotPOMCreator.POM_FILE);
        LOGGER.info("Using {} to run maven.", pomPathname);
        return _runGoals(specificPom, pomPathname, goals);
    }

    private int _runGoals(boolean specificPom, final String pomPathname, String... goals) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setGoals(Arrays.asList(goals));
        request.setPomFile(new File(pomPathname));
        request.setJavaHome(new File(System.getProperty("java.home")));
        if (specificPom) {
            request.setProfiles(Collections.singletonList(DSpotPOMCreator.PROFILE_ID));
        }

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
        LOGGER.info("Using {} for maven home", mavenHome);
        invoker.setMavenHome(new File(mavenHome));
        LOGGER.info(String.format("run maven: %s/bin/mvn %s", mavenHome, String.join(" ", goals)));
        if (DSpotState.verbose) {
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
        return DSpotPOMCreator.REPORT_DIRECTORY_VALUE;
    }

    private String buildMavenHome(UserInput configuration) {
        String mavenHome = null;
        if (configuration != null ) {
            if (configuration.getMavenHome() != null && !configuration.getMavenHome().isEmpty()) {
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
