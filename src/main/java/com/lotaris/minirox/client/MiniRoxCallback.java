package com.lotaris.minirox.client;

import com.github.nkzawa.emitter.Emitter;

/**
 * Empty implementation of IOCallback to use in MiniROX Client
 * 
 * @author Laurent Prevost, laurent.prevost@lotaris.com
 */
public class MiniRoxCallback implements Emitter.Listener {
	@Override
	public void call(Object... args) {
		synchronized (this) {
			this.notify();
		}
	}
}
