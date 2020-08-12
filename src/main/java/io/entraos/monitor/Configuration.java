package io.entraos.monitor;

import org.constretto.ConstrettoBuilder;
import org.constretto.ConstrettoConfiguration;
import org.constretto.model.Resource;

public class Configuration {
	
	private static final ConstrettoConfiguration configuration = new ConstrettoBuilder()
            .createPropertiesStore()
			.addResource(Resource.create("classpath:default.properties"))
			.addResource(Resource.create("file:./local_config.properties"))
            .done()
            .getConfiguration();
	
	private Configuration() {}
	
	public static String getString(String key) {
		return configuration.evaluateToString(key);
	}
	
	public static Integer getInt(String key) {
		return configuration.evaluateToInt(key);
	}

	public static Integer getInt(String key, int defaultValue) {
		return configuration.evaluateTo(key, defaultValue);
	}

	public static boolean getBoolean(String key) {
		return configuration.evaluateToBoolean(key);
	}
}