/*******************************************************************************
 * Copyright (c) 2009 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eduard Bartsch (SAP AG) - initial API and implementation
 *    Mathias Kinzler (SAP AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources.semantic.cacheservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * An internal implementation of a memory cache.
 * 
 * @since 4.0
 * @noextend This class is not intended to be extended by clients.
 * 
 */
class MemoryCache {

	private final static MemoryCache INSTANCE = new MemoryCache();

	private Map<String, CachedMemoryHandle> cacheMap = new HashMap<String, CachedMemoryHandle>();

	/**
	 * @return the instance
	 */
	public static MemoryCache getInstance() {
		return MemoryCache.INSTANCE;
	}

	private MemoryCache() {
		// singleton
	}

	/**
	 * 
	 * @param path
	 *            the path
	 * @return a memory cache store, either a new one or a cached one
	 */
	public CachedMemoryHandle getOrCreateMemoryStore(String path) {
		synchronized (this.cacheMap) {

			CachedMemoryHandle mstore = this.cacheMap.get(path);
			if (mstore != null) {
				return mstore;
			}
			mstore = new CachedMemoryHandle(path, this);
			this.cacheMap.put(path, mstore);
			return mstore;
		}
	}

	/**
	 * Removes a memory cache store from the cache.
	 * 
	 * @param path
	 *            the path
	 */
	public void removeStore(String path) {
		this.cacheMap.remove(path);
	}

	public void removeStoresRecursively(String path) {
		ArrayList<String> keysToBeRemoved = new ArrayList<String>();

		for (String storePath : this.cacheMap.keySet()) {
			if (storePath.startsWith(path)) {
				keysToBeRemoved.add(storePath);
			}
		}

		for (String string : keysToBeRemoved) {
			this.cacheMap.remove(string);
		}
	}

}
