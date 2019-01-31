package eu.stamp_project.diff_test_selection;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 25/07/18
 */
public class CloverExecutor {

    private String mavenHome;

    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private static final String POM_FILE = "pom.xml";

    /**
     * This class will execute, though maven goals, the instrumentation of Clover and the test of the project
     */
    void instrumentAndRunTest(String pathToRootOfProject) {
        setMavenHome();
        runGoals(
                pathToRootOfProject,
                "clean",
                "org.openclover:clover-maven-plugin:4.2.0:setup",
                "test",
                ""
        );
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
        properties.setProperty("jacoco.skip", "true");
        request.setProperties(properties);

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(mavenHome));
        invoker.setOutputHandler(System.out::println);
        invoker.setErrorHandler(System.err::println);
        try {
            return invoker.execute(request).getExitCode();
        } catch (MavenInvocationException e) {
            throw new RuntimeException(e);
        }
    }

    private void setMavenHome() {
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
    }

    private  String getMavenHome(Predicate<String> conditional,
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
