package com.lotaris.minirox.client;

import io.socket.IOAcknowledge;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Acknowledger to get the filters from the MiniROX Agent
 * 
 * @author Laurent Prevost, laurent.prevost@lotaris.com
 */
public class MiniRoxFilterAcknowledger implements IOAcknowledge {
	private static final Logger LOGGER = LoggerFactory.getLogger(MiniRoxFilterAcknowledger.class);

	/**
	 * List of filters collectable from the MINI ROX server
	 */
	private List<String> filters = new ArrayList<>();
	
	@Override
	public void ack(Object... os) {
		try {
			JSONArray jsonFilters = ((JSONObject) os[0]).getJSONArray("filters");

			for (int i = 0; i < jsonFilters.length(); i++) {
				filters.add(jsonFilters.getString(i));
			}
		}
		catch (Exception e) {
			LOGGER.info("Unable to parse the filters");
		}
		
		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * @return Retrieve the filters or null if none is defined
	 */
	public String[] getFilters() {
		if (!filters.isEmpty()) {
			return filters.toArray(new String[filters.size()]);
		}
		else {
			return null;
		}
	}
}
