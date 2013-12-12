package com.lotaris.minirox.client;

import java.io.Serializable;

/**
 * MiniROX filter to gather filters from MiniROX Agent
 * 
 * @author Laurent Prevost, laurent.prevost@lotaris.com
 */
public class MiniRoxFilter implements Serializable {
	public static final long serialVersionUID = 1L;
	
	/**
	 * The connector to communicate with MiniROX agent
	 */
	private static final MiniRoxConnector connector = MiniRoxConnector.getInstance();
	
	/**
	 * @return The filters defined in the MiniROX Agent
	 */
	public String[] getFilters() {
		return connector.getFilters();
	}
}
