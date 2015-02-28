package com.lotaris.minirox.client;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.lotaris.rox.common.model.RoxPayload;
import com.lotaris.rox.common.model.RoxTestResult;
import com.lotaris.rox.core.serializer.json.JsonSerializer;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URISyntaxException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Send a payload to MiniROX agent
 * 
 * @author Laurent Prevost, laurent.prevost@lotaris.com
 */
public class MiniRoxConnector implements Serializable {
	public static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MiniRoxConnector.class);

	private final Socket socket;
	
	private static final MiniRoxConnector instance = new MiniRoxConnector();
	
	/**
	 * Constructor
	 * 
	 * @param miniRoxUrl The miniROX URL
	 */
	private MiniRoxConnector() {
		this.socket = createConnectedSocket(MiniRoxConfiguration.getInstance().getMiniroxUrl());
	}
	
	/**
	 * @return Instance of a connector to MiniROX
	 */
	public static MiniRoxConnector getInstance() {
		return instance;
	}
	
	/**
	 * @return True if mini ROX is connected
	 */
	private boolean isStarted() {
		return socket != null && socket.connected();
	}
	
	/**
	 * Send a starting notification to Mini ROX
	 * 
	 * @param projectName The project name
	 * @param projectVersion The project version
	 * @param category The category
	 */
	public void notifyStart(String projectName, String projectVersion, String category) {
		try {
			if (isStarted()) {
				JSONObject startNotification = new JSONObject().
					put("project", new JSONObject().
						put("name", projectName).
						put("version", projectVersion)
					).
					put("category", category);
				
				socket.emit("run:start", startNotification);
			}
			else {
				LOGGER.warn("Minirox is not available to send the start notification");
			}
		}
		catch (Exception e) {
			LOGGER.info("Unable to send the start notification to MINI ROX. Cause: {}",  e.getMessage());
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Exception: ", e);
			}
		}
	}
	
	/**
	 * Send a test result notification to MiniROX Server Agent
	 * 
	 * @param result The test result to get the data
	 * @param projectName The default project name if none provided by the test result
	 * @param projectVersion The default project version if none provided by the test result
	 * @param category The default category if none provided by the test result
	 */
	public void notifyTestResult(RoxTestResult result, String projectName, String projectVersion, String category) {
		try {
			if (isStarted()) {
				JSONObject testResult = new JSONObject().
					put("k", result.getKey()).
					put("n", result.getName()).
					put("j", projectName).
					put("v", projectVersion).
					put("p", result.isPassed()).
					put("d", result.getDuration()).
					put("f", result.getFlags()).
					put("m", result.getMessage()).
					put("c", (result.getCategory() != null && !result.getCategory().isEmpty() ? result.getCategory() : category)).
					put("g", new JSONArray(result.getTags())).
					put("t", new JSONArray(result.getTickets())).
					put("a", new JSONObject(result.getData()));

				socket.emit("run:test:result", testResult);
			}
		}
		catch (Exception e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Unable to send the test result notification to MINI ROX. Cause: {}", e.getMessage());
				LOGGER.debug("Exception: ", e);
			}
		}
	}
	
	/**
	 * Send a ending notification to Mini ROX
	 * 
	 * @param projectName The project name
	 * @param projectVersion The project version
	 * @param category The category
	 * @param duration The duration of the test run
	 */
	public void notifyEnd(String projectName, String projectVersion, String category, long duration) {
		try {
			if (isStarted()) {
				JSONObject endNotification = new JSONObject().
					put("project", new JSONObject().
						put("name", projectName).
						put("version", projectVersion)
					).
					put("category", category).
					put("duration", duration);
				
				socket.emit("run:end", endNotification);
			}
			else {
				LOGGER.warn("Minirox is not available to send the end notification");
			}
		}
		catch (Exception e) {
			LOGGER.info("Unable to send the end notification to MINI ROX. Cause: {}", e.getMessage());
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Exception: ", e);
			}
		}
	}
	
	/**
	 * Send a payload to the MINI ROX. This is a best effort
	 * and when the request failed, there is no crash
	 * 
	 * @param payload The payload to send
	 */
	public void send(RoxPayload payload) {		
		try {
			if (isStarted()) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				new JsonSerializer().serializePayload(new OutputStreamWriter(baos), payload, false);

				socket.emit("payload", new String(baos.toByteArray()));
			}
			else {
				LOGGER.warn("Minirox is not available to send the test results");
			}
		}
		catch (Exception e) {
			LOGGER.warn("Unable to send the result to MINI ROX. Cause: {}", e.getMessage());
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Exception: ", e);
			}
		}
	}
	
	/**
	 * Try to get a filter list from MINI ROX
	 * 
	 * @return The list of filters or null if there is none or MINI ROX is not accessible
	 */
	public String[] getFilters() {
		try {
			if (isStarted()) {
				final MiniRoxFilterAcknowledger acknowledger = new MiniRoxFilterAcknowledger();
				
				// Be sure that the emit/ack is synchronous to get the filters before the test are run
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							LOGGER.trace("Retrieve filters");
							socket.emit("filters:get", acknowledger);
						}
						catch (Exception e) {
							LOGGER.warn("Unable to get the filters: {}", e.getMessage());
							synchronized (acknowledger) {
								acknowledger.notify();
							}
						}
					}
				}).start();

				synchronized (acknowledger) {
					acknowledger.wait();
				}

				if (!acknowledger.isFilters()) {
					for (String filter : acknowledger.getFilters()) {
						LOGGER.info("Filter element: {}", filter);
					}
				}

				return acknowledger.getFilters();
			}
		}
		catch (Exception e) {
			LOGGER.warn("Unable to retrieve the filters from MINI ROX. Cause: {}", e.getMessage());
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Exception: ", e);
			}
		}
		
		return null;
	}
	
	/**
	 * Create a new connection to MiniROX
	 * 
	 * @param miniRoxUrl The miniROX URL
	 * @return The socket connected to MiniROX or null if the connection is not possible
	 */
	private Socket createConnectedSocket(final String miniRoxUrl) {
		try {
			final Socket initSocket = IO.socket(miniRoxUrl);

			final MiniRoxCallback callback = new MiniRoxCallback();
			
			initSocket.on(Socket.EVENT_CONNECT, callback);
			initSocket.on(Socket.EVENT_CONNECT_ERROR, callback);
			initSocket.on(Socket.EVENT_CONNECT_TIMEOUT, callback);
			initSocket.on(Socket.EVENT_CONNECT_ERROR, callback);
			initSocket.on(Socket.EVENT_DISCONNECT, callback);
			initSocket.on(Socket.EVENT_ERROR, callback);
			
			// Be sure that the emit/ack is synchronous to get the filters before the test are run
			LOGGER.trace("Connection to minirox"); // Unable to use the logger into the run method
			new Thread(new Runnable() {
				@Override
				public void run() {
					initSocket.connect();
				}
			}).start();
			
			synchronized (callback) {
				callback.wait();
			}

			if (!initSocket.connected()) {
				LOGGER.warn("Minirox is not available");
				return null;
			}
			
			return initSocket;
		}
		catch (URISyntaxException | InterruptedException e) {
			LOGGER.warn("Unknown error", e);
		}
		
		return null;
	}
}
