package com.lotaris.minirox.client;

import com.lotaris.rox.common.model.RoxTestResult;
import java.io.Serializable;

/**
 * MiniROX listener to gather information and forward to the MiniROX agent
 * 
 * @author Laurent Prevost, laurent.prevost@lotaris.com
 */
public class MiniRoxListener implements Serializable {
	public static final long serialVersionUID = 1L;
	
	/**
	 * The connector to communicate with MiniROX agent
	 */
	private static final MiniRoxConnector connector = MiniRoxConnector.getInstance();
	
	/**
	 * Notify that a test run started
	 * 
	 * @param project Project name
	 * @param version Project version
	 * @param category Category of tests
	 */
	public void testRunStart(String project, String version, String category) {
		connector.notifyStart(project, version, category);
	}
	
	/**
	 * Notify that a test run ended
	 * 
	 * @param project Project name
	 * @param version Project version
	 * @param category Category of tests
	 * @param duration Duration of the whole test run
	 */
	public void testRunEnd(String project, String version, String category, Long duration) {
		connector.notifyEnd(project, version, category, duration);
	}

	/**
	 * Notify a test result
	 * 
	 * @param test The test data
	 * @param project The project name
	 * @param version The project version
	 * @param category The catory of tests
	 */
	public void testResult(RoxTestResult test, String project, String version, String category) {
		connector.notifyTestResult(test, project, version, category);
	}
}
