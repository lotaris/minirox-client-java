package com.lotaris.minirox.client;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIOException;
import org.json.JSONObject;

/**
 * Empty implementation of IOCallback to use in MiniROX Client
 * 
 * @author Laurent Prevost, laurent.prevost@lotaris.com
 */
public class MiniRoxCallback implements IOCallback {
//	private static final Logger LOGGER = LoggerFactory.getLogger(MiniRoxCallback.class);
	
	@Override public void onMessage(JSONObject json, IOAcknowledge ack) {}
	@Override public void onMessage(String data, IOAcknowledge ack) {}
	@Override public void onError(SocketIOException socketIOException) {
		synchronized(this) {
			this.notify();
		}
	}
	@Override public void onDisconnect() {
		synchronized(this) {
			this.notify();
		}
	}
	@Override public void onConnect() {
		synchronized(this) {
			this.notify();
		}
	}
	@Override public void on(String event, IOAcknowledge ack, Object... args) {}
}
