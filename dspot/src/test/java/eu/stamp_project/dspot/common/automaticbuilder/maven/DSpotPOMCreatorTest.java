package eu.stamp_project.dspot.common.automaticbuilder.maven;

import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 01/11/18
 */
public class DSpotPOMCreatorTest {

    @Test
    public void test() throws Exception {

        /*
            Test the copy and injection of the profile to run different foals in the origial pom.xml
         */

        UserInput configuration = new UserInput();
        configuration.setFilter("example.*");
        configuration.setAbsolutePathToProjectRoot("src/test/resources/test-projects/");
        configuration.setJVMArgs("-Xmx2048m,-Xms1024m");
        DSpotPOMCreator.createNewPom(configuration);

        try (BufferedReader reader = new BufferedReader(new FileReader(configuration.getAbsolutePathToProjectRoot() + DSpotPOMCreator.getPOMName()))) {
            final String content = reader.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
            assertTrue(content + AmplificationHelper.LINE_SEPARATOR +
                            "must contain " + AmplificationHelper.LINE_SEPARATOR +
                            MUST_CONTAIN,
                    content.contains(MUST_CONTAIN));
            assertTrue(content + AmplificationHelper.LINE_SEPARATOR +
                            "should be ends with " + AmplificationHelper.LINE_SEPARATOR +
                            ENDS_WITH,
                    content.endsWith(ENDS_WITH));
        }
    }

    private static final String MUST_CONTAIN = "<plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-compiler-plugin</artifactId><executions><execution><id>default-testCompile</id><phase>none</phase></execution></executions></plugin><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-surefire-plugin</artifactId><configuration><additionalClasspathElements><additionalClasspathElement>target/dspot/dependencies/</additionalClasspathElement></additionalClasspathElements></configuration></plugin></plugins>";

    private static final String ENDS_WITH = "<profiles><profile><id>id-descartes-for-dspot</id><build><plugins><plugin><groupId>org.pitest</groupId><artifactId>pitest-maven</artifactId><version>1.4.0</version><configuration><mutationEngine>descartes</mutationEngine><outputFormats><value>CSV</value><value>XML</value></outputFormats><targetClasses>example.*</targetClasses><reportsDirectory>target/pit-reports/</reportsDirectory><timeoutConstant>10000</timeoutConstant><jvmArgs><value>-Xmx2048m</value><value>-Xms1024m</value></jvmArgs></configuration><dependencies><dependency><groupId>eu.stamp-project</groupId><artifactId>descartes</artifactId><version>1.2.4</version></dependency></dependencies></plugin></plugins></build></profile></profiles></project>";

    @Test
    public void testOnPOMWithProfiles() throws Exception {

        /*
            Test the copy and the injection of the profile on a pom with an existing profiles tag
         */

        // TODO system properties are not used in DSpotPOMCreator

        UserInput configuration = new UserInput();
        configuration.setFilter("fr.inria.sample.*");
        configuration.setAbsolutePathToProjectRoot("src/test/resources/sample/");
        configuration.setJVMArgs("-Xmx2048m,-Xms1024m");
        configuration.setSystemProperties("-Dis.admin.user=admin,-Dis.admin.passwd=$2pRSid#");
        configuration.setExcludedClasses("fr.inria.filter.failing.*");
        DSpotPOMCreator.createNewPom(configuration);

        try (BufferedReader reader = new BufferedReader(new FileReader(configuration.getAbsolutePathToProjectRoot() + DSpotPOMCreator.getPOMName()))) {
            final String content = reader.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
            assertTrue(content + AmplificationHelper.LINE_SEPARATOR +
                            "should be ends with " + AmplificationHelper.LINE_SEPARATOR +
                            PROFILE_ENDS_WITH,
                    content.replaceAll(" ", "").endsWith(PROFILE_ENDS_WITH.replaceAll(" ", "")));
        }
    }

    private static final String PROFILE_ENDS_WITH =
            "            <id>test-resources</id>" + AmplificationHelper.LINE_SEPARATOR +
            "        </profile>" + AmplificationHelper.LINE_SEPARATOR +
            "    <profile><id>id-descartes-for-dspot</id><build><plugins><plugin><groupId>org.pitest</groupId><artifactId>pitest-maven</artifactId><version>1.4.0</version><configuration><mutationEngine>descartes</mutationEngine><outputFormats><value>CSV</value><value>XML</value></outputFormats><targetClasses>fr.inria.sample.*</targetClasses><reportsDirectory>target/pit-reports/</reportsDirectory><timeoutConstant>10000</timeoutConstant><jvmArgs><value>-Xmx2048m</value><value>-Xms1024m</value></jvmArgs><excludedTestClasses><value>fr.inria.filter.failing.*</value></excludedTestClasses></configuration><dependencies><dependency><groupId>eu.stamp-project</groupId><artifactId>descartes</artifactId><version>1.2.4</version></dependency></dependencies></plugin></plugins></build></profile></profiles>" + AmplificationHelper.LINE_SEPARATOR +
            "" + AmplificationHelper.LINE_SEPARATOR +
            "</project>";

}
