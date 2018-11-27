package eu.stamp_project.automaticbuilder.maven;

import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.utils.AmplificationHelper;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 01/11/18
 */
public class DSpotPOMCreatorTest {

    @After
    public void tearDown() throws Exception {
        try {
            FileUtils.forceDelete(new File(InputConfiguration.get().getAbsolutePathToProjectRoot() + DSpotPOMCreator.DSPOT_POM_FILE));
        } catch (Exception ignored) {
            // ignored
        }
    }

    @Test
    public void test() throws Exception {

        /*
            Test the copy and injection of the profile to run different foals in the origial pom.xml
         */

        InputConfiguration.initialize("src/test/resources/test-projects/test-projects.properties");
        DSpotPOMCreator.createNewPom();

        try (BufferedReader reader = new BufferedReader(new FileReader(InputConfiguration.get().getAbsolutePathToProjectRoot() + DSpotPOMCreator.DSPOT_POM_FILE))) {
            final String content = reader.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
            assertTrue(content + AmplificationHelper.LINE_SEPARATOR +
                            "should be ends with " + AmplificationHelper.LINE_SEPARATOR +
                            ENDS_WITH,
                    content.endsWith(ENDS_WITH));
        }
    }

    private static final String ENDS_WITH = "<profiles><profile><id>id-descartes-for-dspot</id><build><plugins><plugin><groupId>org.pitest</groupId><artifactId>pitest-maven</artifactId><version>1.4.0</version><configuration><mutationEngine>descartes</mutationEngine><outputFormats><value>CSV</value><value>XML</value></outputFormats><targetClasses>example.*</targetClasses><reportsDirectory>target/pit-reports</reportsDirectory><timeoutConstant>10000</timeoutConstant><jvmArgs><value>-Xmx2048m</value><value>-Xms1024m</value></jvmArgs></configuration><dependencies><dependency><groupId>eu.stamp-project</groupId><artifactId>descartes</artifactId><version>1.2.4</version></dependency></dependencies></plugin></plugins></build></profile></profiles></project>";

    @Test
    public void testOnPOMWithProfiles() throws Exception {

        /*
            Test the copy and the injection of the profile on a pom with an existing profiles tag
         */

        InputConfiguration.initialize("src/test/resources/sample/sample.properties");
        DSpotPOMCreator.createNewPom();

        try (BufferedReader reader = new BufferedReader(new FileReader(InputConfiguration.get().getAbsolutePathToProjectRoot() + DSpotPOMCreator.DSPOT_POM_FILE))) {
            final String content = reader.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
            assertTrue(content + AmplificationHelper.LINE_SEPARATOR +
                            "should be ends with " + AmplificationHelper.LINE_SEPARATOR +
                            PROFILE_ENDS_WITH,
                    content.endsWith(PROFILE_ENDS_WITH));
        }
    }

    private static final String PROFILE_ENDS_WITH = "<profiles>" + AmplificationHelper.LINE_SEPARATOR +
            "        <profile>" + AmplificationHelper.LINE_SEPARATOR +
            "            <id>test-resources</id>" + AmplificationHelper.LINE_SEPARATOR +
            "        </profile>" + AmplificationHelper.LINE_SEPARATOR +
            "    <profile><id>id-descartes-for-dspot</id><build><plugins><plugin><groupId>org.pitest</groupId><artifactId>pitest-maven</artifactId><version>1.4.0</version><configuration><mutationEngine>descartes</mutationEngine><outputFormats><value>CSV</value><value>XML</value></outputFormats><targetClasses>fr.inria.sample.*</targetClasses><reportsDirectory>target/pit-reports</reportsDirectory><timeoutConstant>10000</timeoutConstant><jvmArgs><value>-Xmx2048m</value><value>-Xms1024m</value><value>-Dis.admin.user=admin</value><value>-Dis.admin.passwd=$2pRSid#</value></jvmArgs><excludedTestClasses><value>fr.inria.filter.failing.*</value></excludedTestClasses></configuration><dependencies><dependency><groupId>eu.stamp-project</groupId><artifactId>descartes</artifactId><version>1.2.4</version></dependency></dependencies></plugin></plugins></build></profile></profiles>" + AmplificationHelper.LINE_SEPARATOR +
            "" + AmplificationHelper.LINE_SEPARATOR +
            "</project>";

}
