package eu.stamp_project.automaticbuilder.gradle;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.utils.AmplificationHelper;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 25/10/18
 */
public class GradleInjectorTest extends AbstractTest {

    private static final String expectedEnd = "apply plugin: 'info.solidsoft.pitest'" + AmplificationHelper.LINE_SEPARATOR +
            "" + AmplificationHelper.LINE_SEPARATOR +
            "pitest {" + AmplificationHelper.LINE_SEPARATOR +
            "    targetClasses = ['fr.inria.sample.*']" + AmplificationHelper.LINE_SEPARATOR +
            "    enableDefaultIncrementalAnalysis = true" + AmplificationHelper.LINE_SEPARATOR +
            "    reportDir = 'build/pit-reports'" + AmplificationHelper.LINE_SEPARATOR +
            "    outputFormats = ['CSV','HTML']" + AmplificationHelper.LINE_SEPARATOR +
            "    pitestVersion = '1.4.0'" + AmplificationHelper.LINE_SEPARATOR +
            "    jvmArgs = ['-Xmx2048m','-Xms1024m','-Dis.admin.user=admin','-Dis.admin.passwd=$2pRSid#']" + AmplificationHelper.LINE_SEPARATOR +
            "    targetTests = ['']" + AmplificationHelper.LINE_SEPARATOR +
            "    mutationEngine = 'descartes'" + AmplificationHelper.LINE_SEPARATOR +
            "    excludedClasses = ['fr.inria.filter.failing.*']" + AmplificationHelper.LINE_SEPARATOR +
            "}" + AmplificationHelper.LINE_SEPARATOR;

    @Test
    public void testOnGradleFromActiveon() {

        /*
            test the injection of what we need in the build.gradle
         */

        final GradleInjector gradleInjector = new GradleInjector("src/test/resources/build.gradle");
        final String pitTask = gradleInjector.getPitTask();
        assertTrue(pitTask, pitTask.startsWith(expectedStarts));
        assertTrue(pitTask, pitTask.endsWith(expectedEnd));
    }

    private static final String expectedStarts = "buildscript {" + AmplificationHelper.LINE_SEPARATOR +
            "    configurations.maybeCreate('pitest')" + AmplificationHelper.LINE_SEPARATOR +
            "" + AmplificationHelper.LINE_SEPARATOR +
            "    dependencies {" + AmplificationHelper.LINE_SEPARATOR +
            "       classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.3.0'" + AmplificationHelper.LINE_SEPARATOR +
            "       pitest 'eu.stamp-project:descartes:1.2.4'" + AmplificationHelper.LINE_SEPARATOR +
            "    }" + AmplificationHelper.LINE_SEPARATOR;

    @Test
    public void testOnGradleFromActiveonWithoutMavenRepositoryPluginsGradle() {
        /*
            test the injection of what we need in the build.gradle when we removed the
                maven {
                    url "https://plugins.gradle.org/m2/"
                }
         */
        final GradleInjector gradleInjector = new GradleInjector("src/test/resources/no_repository_build.gradle");
        final String pitTask = gradleInjector.getPitTask();
        assertTrue(pitTask, pitTask.startsWith(expectedStarts));
        assertTrue(pitTask, pitTask.endsWith(expectedEnd));
    }

    @Test
    public void testOnGradleFromActiveonWithoutBuildscript() {
        /*
            test the injection of what we need in the build.gradle when we removed the
                buildscript {
                    ...
                }
         */
        final GradleInjector gradleInjector = new GradleInjector("src/test/resources/no_buildscript_build.gradle");
        final String pitTask = gradleInjector.getPitTask();
        assertTrue(pitTask, pitTask.endsWith(expectedEnd));
    }

    private static final String shouldContains = "buildscript { " + AmplificationHelper.LINE_SEPARATOR +
            "    repositories { " + AmplificationHelper.LINE_SEPARATOR +
            "        maven { " + AmplificationHelper.LINE_SEPARATOR +
            "             url \"https://plugins.gradle.org/m2/\" " + AmplificationHelper.LINE_SEPARATOR +
            "        } " + AmplificationHelper.LINE_SEPARATOR +
            "    } " + AmplificationHelper.LINE_SEPARATOR +
            AmplificationHelper.LINE_SEPARATOR +
            "    configurations.maybeCreate('pitest') " + AmplificationHelper.LINE_SEPARATOR +
            AmplificationHelper.LINE_SEPARATOR +
            "    dependencies { " + AmplificationHelper.LINE_SEPARATOR +
            "       classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.3.0' " + AmplificationHelper.LINE_SEPARATOR +
            "       pitest 'eu.stamp-project:descartes:1.2.4' " + AmplificationHelper.LINE_SEPARATOR +
            "    } " + AmplificationHelper.LINE_SEPARATOR +
            "}" + AmplificationHelper.LINE_SEPARATOR + AmplificationHelper.LINE_SEPARATOR + AmplificationHelper.LINE_SEPARATOR;

    @Test
    public void testOnGradleFromActiveonWithoutRepositories() {
        /*
            test the injection of what we need in the build.gradle when we removed the
                buildscript {
                    ...
                }
         */
        final GradleInjector gradleInjector = new GradleInjector("src/test/resources/no_repositories_build.gradle");
        final String pitTask = gradleInjector.getPitTask();
        assertTrue(pitTask, pitTask.startsWith(expectedStarts));
        assertTrue(pitTask, pitTask.endsWith(expectedEnd));
    }
}
