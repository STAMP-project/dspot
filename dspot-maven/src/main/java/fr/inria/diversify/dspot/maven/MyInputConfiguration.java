package fr.inria.diversify.dspot.maven;

import java.io.IOException;

import fr.inria.diversify.runner.InputConfiguration;

public class MyInputConfiguration extends InputConfiguration {

	private String projectPath;
	private String tmpDirPath;

	public String getTmpDirPath() {
		return tmpDirPath;
	}

	public MyInputConfiguration(String propertiesPath, String projectPath, String tmpDirPath) throws IOException {
		super(propertiesPath);
		this.projectPath = ".";
		this.tmpDirPath = tmpDirPath;
	}

	@Override
	public String getProjectPath() {
		return projectPath;
	}

	@Override
	public String getRootPath() {
		return projectPath;
	}

	@Override
	public String getProperty(String key) {
		String toReturn = super.getProperty(key);
		if (key.equals("project")) {
			toReturn = getProjectPath();
		} 
		if (key.equals("tmpDir")) {
			toReturn = getTmpDirPath();
		} 
		
		return toReturn;
	}

}
