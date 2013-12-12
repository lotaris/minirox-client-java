package com.lotaris.minirox.client;

import com.lotaris.rox.common.config.ServerListConfiguration;
import com.lotaris.rox.common.config.YamlConfigurationFile;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for MiniROX clients. 
 * 
 * <strong>Example:</strong>
 * <pre>
 *		{
 *      "enabled": true,
 *      "url": "http://localhost:1337"
 *    }
 * </pre>
 * 
 * @author Laurent Prevost, laurent.prevost@lotaris.com
 */
public class MiniRoxConfiguration {
	private static final Logger LOGGER = LoggerFactory.getLogger(MiniRoxConfiguration.class);
	
	/**
	 * Base configuration that should be present in the home directory
	 */
	private static final String BASE_CONFIG_PATH = ".rox/minirox.yml";
		
	/**
	 * Root node name of the tree configuration
	 */
	private static final String P_ROOT_NODE_NAME = "minirox";
	
	private static final String P_MINIROX_ENABLE = P_ROOT_NODE_NAME + ".enable";
	private static final String P_MINIROX_URL		 = P_ROOT_NODE_NAME + ".url";
		
	/**
	 * Not thread safe, not critical
	 */
	private static MiniRoxConfiguration instance;
	
	/**
	 * Configuration
	 */
	protected CompositeConfiguration config;
	
	/**
	 * Define if MiniROX is enabled
	 */
	private boolean enabled = true;
	
	/**
	 * Constructor
	 */
	protected MiniRoxConfiguration() {
		config = new CompositeConfiguration();
				
		try {
			config.addConfiguration(new YamlConfigurationFile(BASE_CONFIG_PATH, P_ROOT_NODE_NAME, new ServerListConfiguration()));
		}
		catch (ConfigurationException ce) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Unable to load the ROX configuration.", ce);
			}
			else {
				LOGGER.warn("Unable to load the ROX configuration due to: {}", ce.getMessage());
			}
		}
		
		if (config.getNumberOfConfigurations() > 0 && config.containsKey(P_MINIROX_URL)) {
			enabled = config.getBoolean(P_MINIROX_ENABLE, enabled);
		}
		else {
			enabled = false;
		}
	}
	
	/**
	 * @return The configuration instance
	 */
	public static MiniRoxConfiguration getInstance() {
		if (instance == null) {
			instance = new MiniRoxConfiguration();
		}
		
		return instance;
	}
	
	/**
	 * Enforce the fact that a parameter is mandatory
	 * @param name The name of the parameter
	 * @return The value found
	 * @throws RuntimeException When a mandatory parameter is missing
	 */
	private String getMandatory(String name) {
		if (!config.containsKey(name)) {
			throw new RuntimeException(name + " parameter is missing.");
		}
		else {
			return config.getString(name);
		}
		
	}
	
	/**
	 * @return In case of missing mandatory configuration, MiniROX will be disabled to allow a smooth failure
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * @return The URL to contact MiniROX
	 */
	public String getMiniroxUrl() {
		return getMandatory(P_MINIROX_URL);
	}	
}