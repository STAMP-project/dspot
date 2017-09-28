package fr.inria.stamp.input;

import spoon.reflect.factory.Factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static fr.inria.stamp.input.ProjectProperties.ADDITIONAL_CLASSPATH_ELEMENT;
import static fr.inria.stamp.input.ProjectProperties.CLASSES_FOLDER;
import static fr.inria.stamp.input.ProjectProperties.EXCLUDED;
import static fr.inria.stamp.input.ProjectProperties.MAVEN_HOME;
import static fr.inria.stamp.input.ProjectProperties.OUTPUT;
import static fr.inria.stamp.input.ProjectProperties.PROJECT_ROOT;
import static fr.inria.stamp.input.ProjectProperties.SOURCE_FOLDER;
import static fr.inria.stamp.input.ProjectProperties.TARGET_MODULE;
import static fr.inria.stamp.input.ProjectProperties.TARGET_PACKAGE;
import static fr.inria.stamp.input.ProjectProperties.TEST_CLASSES_FOLDER;
import static fr.inria.stamp.input.ProjectProperties.TEST_FOLDER;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/07/17
 */
public class Project {

	private final static String FILE_SEPARATOR = "/";

	private Properties properties;

	private Factory factory;

	public Project(String pathToProperty) {
		this.properties = new Properties();
		this.initDefaultProperties();
		try {
			this.properties.load(new FileInputStream(new File(pathToProperty)));
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	private void initDefaultProperties() {
		this.properties.setProperty(SOURCE_FOLDER, "src/main/java");
		this.properties.setProperty(TEST_FOLDER, "src/test/java");
		this.properties.setProperty(CLASSES_FOLDER, "target/classes");
		this.properties.setProperty(TEST_CLASSES_FOLDER, "target/test-classes");
		this.properties.setProperty(OUTPUT, "dspot-report");
		this.properties.setProperty(MAVEN_HOME, findMavenHome());
	}

	private static String findMavenHome() {
		return System.getenv().get("MAVEN_HOME") != null ? System.getenv().get("MAVEN_HOME") :
						System.getenv().get("M2_HOME") != null ? System.getenv().get("M2_HOME") :
								new File("/usr/share/maven/").exists() ? "/usr/share/maven/" : "/usr/share/maven3/";
	}

	public Factory getFactory() {
		return factory;
	}

	public void setFactory(Factory factory) {
		this.factory = factory;
	}

	public void setProjectRoot(String projectRoot) {
		this.properties.setProperty(PROJECT_ROOT, projectRoot);
	}

	public String getProjectRoot() {
		return this.properties.getProperty(PROJECT_ROOT);
	}

	public String getTargetModule() {
		return this.properties.getProperty(TARGET_MODULE);
	}

	public String getPathToTargetModule() {
		return this.getProjectRoot() + FILE_SEPARATOR + this.getTargetModule();
	}

	public String getSourceFolder() {
		return this.properties.getProperty(SOURCE_FOLDER);
	}

	public String getPathToSource() {
		return this.getPathToTargetModule() + FILE_SEPARATOR + this.getSourceFolder();
	}

	public String getTestSourceFolder() {
		return this.properties.getProperty(TEST_FOLDER);
	}

	public String getPathToTest() {
		return this.getPathToTargetModule() + FILE_SEPARATOR + this.getTestSourceFolder();
	}

	public String getClassesFolder() {
		return this.properties.getProperty(CLASSES_FOLDER);
	}

	public String getPathToClassesFolder() {
		return this.getProjectRoot() + FILE_SEPARATOR + this.getClassesFolder();
	}

	public String getTestClassesFolder() {
		return this.properties.getProperty(TEST_CLASSES_FOLDER);
	}

	public String getPathToTestClassesFolder() {
		return this.getProjectRoot() + FILE_SEPARATOR + this.getTestClassesFolder();
	}

	public String getTargetPackage() {
		return this.properties.getProperty(TARGET_PACKAGE);
	}

	public String getOutput() {
		return this.properties.getProperty(OUTPUT);
	}

	public String getMavenHome() {
		return this.properties.getProperty(MAVEN_HOME);
	}

	public String getExcludedClass() {
		return this.properties.getProperty(EXCLUDED);
	}

	public String getAdditionalClassPathElements() {
		return this.properties.getProperty(ADDITIONAL_CLASSPATH_ELEMENT);
	}

	@Override
	public String toString() {
		return "Project{" +
				"properties=" + properties +
				'}';
	}
}
