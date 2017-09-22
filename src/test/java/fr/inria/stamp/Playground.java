package fr.inria.stamp;

import org.junit.Test;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.SettingsFactory;
import org.pitest.mutationtest.tooling.EntryPoint;
import org.pitest.testapi.TestGroupConfig;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 20/09/17
 */
public class Playground {

	public class Tacos {

	}

	public class Benjamin {
		public void eat(Tacos tacos) {

		}
		public boolean isHungry() {
			return true;
		}
	}

	@Test
	public void testTacos() throws Exception {
		Tacos tacos = new Tacos();
		Benjamin benjamin = new Benjamin();
		benjamin.eat(tacos);
		assertFalse(benjamin.isHungry());
	}

	@Test
	public void testLaunchPit() throws Exception {
		final ReportOptions data = createReportOptions();
		final SettingsFactory settingsFactory = createSettingFactory(data);
		final EntryPoint e = new EntryPoint();
		e.execute(new File("src/test/resources/test-projects/"), data, settingsFactory, Collections.emptyMap());
	}

	private ReportOptions createReportOptions() {
		final ReportOptions data = new ReportOptions();
		final List<String> classpathList = Arrays.stream(((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs())
				.map(URL::getFile)
				.collect(Collectors.toList());
		classpathList.add("src/test/resources/test-projects/target/classes");
		classpathList.add("src/test/resources/test-projects/target/test-classes/");
		data.setClassPathElements(classpathList);
		data.setDependencyAnalysisMaxDistance(-1);
		data.setTargetClasses(Collections.singletonList(string -> string.startsWith("example.")));
		data.setTargetTests(Collections.singletonList("example.TestSuiteExample"::equals));
		data.setReportDir("target/report-pits/");
		data.setVerbose(true);
		data.setMutators(Collections.singletonList("ALL"));
		data.setSourceDirs(Collections.singletonList(new File("src/test/resources/test-projects/")));
		data.addOutputFormats(Collections.singletonList("CSV"));
		final TestGroupConfig testGroupConfig = new TestGroupConfig(Collections.emptyList(), Collections.emptyList());
		data.setGroupConfig(testGroupConfig);
		data.setExportLineCoverage(true);
		data.setMutationEngine("gregor");
		return data;
	}

	private SettingsFactory createSettingFactory(ReportOptions data) {

		final String classpath = "/home/bdanglot/.m2/repository/junit/junit/4.11/junit-4.11.jar:" +
				"/home/bdanglot/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:" +
				"src/test/resources/test-projects/target/classes:" +
				"src/test/resources/test-projects/target/test-classes/";

		final URL[] urls = Arrays.stream(classpath.split(":"))
				.map(File::new)
				.map(file -> {
					try {
						return file.toURI().toURL();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}).toArray(URL[]::new);
		ClassLoader classLoader = new URLClassLoader(urls);

		return new SettingsFactory(data, new PluginServices(classLoader));
	}
}

