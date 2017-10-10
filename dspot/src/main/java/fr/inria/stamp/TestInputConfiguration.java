package fr.inria.stamp;

import java.io.IOException;

import fr.inria.diversify.runner.InputConfiguration;

public class TestInputConfiguration extends InputConfiguration {

	public TestInputConfiguration(String pathToConfigurationFile) throws IOException {
		super(pathToConfigurationFile);
	}

	@Override
	public String getProperty(String key) {
		System.out.println(key + ": " + super.getProperty(key));
		return super.getProperty(key);
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		System.out.println(key + ": " + super.getProperty(key));
		return super.getProperty(key, defaultValue);
	}

}
