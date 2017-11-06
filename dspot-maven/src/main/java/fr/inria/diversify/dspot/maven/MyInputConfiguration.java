package fr.inria.diversify.dspot.maven;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import fr.inria.diversify.utils.sosiefier.InputConfiguration;

public class MyInputConfiguration extends InputConfiguration {

	private File project;
	private File srcDir;
	private File testDir;
	private File srcResourcesDir;
	private File testResourcesDir;
	private File classesDir;
	private File testClassesDir;
	private File tempDir;
	private String filter;
	private File mavenHome;

	public MyInputConfiguration(File project, File srcDir, File testDir, File classesDir, File testClassesDir,
			File tempDir, String filter, File mavenHome) throws IOException {
		super();
		this.project = project;
		this.srcDir = srcDir;
		this.testDir = testDir;
		this.srcResourcesDir = null;
		this.testResourcesDir = null;
		this.classesDir = classesDir;
		this.testClassesDir = testClassesDir;
		this.tempDir = tempDir;
		this.filter = filter;
		this.mavenHome = mavenHome;
		getProperties().setProperty("project", project.getAbsolutePath());
		getProperties().setProperty("src", getRelativePath(srcDir));
		getProperties().setProperty("testSrc", getRelativePath(testDir));
//		getProperties().setProperty("testResources", "pippoTestR");
//		getProperties().setProperty("srcResources", "pippoSrcR");
		getProperties().setProperty("maven.home", mavenHome.getAbsolutePath());
		getProperties().setProperty("classes", getRelativePath(classesDir));
		getProperties().setProperty("tmpDir", getRelativePath(testClassesDir));
		// getProperties().setProperty("clojure", "false");
		// getProperties().setProperty("javaVersion", "5");
		// getProperties().setProperty("transformation.type", "all");
		// getProperties().setProperty("transformation.size", "1");
		// getProperties().setProperty("stat", "false");
		// getProperties().setProperty("sosie", "false");
	}

	@Override
	public String getProjectPath() {
		return this.project.toString();
	}

	@Override
	public Properties getProperties() {
		return super.getProperties();
	}

	@Override
	public String getProperty(String key) {
		System.out.println("getProperty(" + key + ") = " + super.getProperty(key));
		return super.getProperty(key);
	}

	@Override
	public String getClassesDir() {
		return classesDir.getAbsolutePath();
	}

	public File getTestClassesDir() {
		return testClassesDir;
	}

	@Override
	public String getTempDir() {
		return tempDir.getAbsolutePath();
	}

	
	private String getRelativePath(File path) {
		String projectAbsolutePath = project.getAbsolutePath();
		return path.getAbsolutePath().replace(projectAbsolutePath, "");
	}
}
