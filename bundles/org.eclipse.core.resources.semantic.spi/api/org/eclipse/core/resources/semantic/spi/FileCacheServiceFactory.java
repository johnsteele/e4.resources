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
package org.eclipse.core.resources.semantic.spi;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.internal.resources.semantic.cacheservice.CacheService;
import org.eclipse.core.internal.resources.semantic.cacheservice.FileHandleFactory;
import org.eclipse.core.runtime.CoreException;

/**
 * A cache service factory that uses the local file system for caching
 * 
 */
public class FileCacheServiceFactory implements ICacheServiceFactory {

	private static final Lock rl = new ReentrantLock();
	private static ICacheService service;

	public ICacheService getCacheService() throws CoreException {
		try {
			rl.lock();
			if (service == null) {
				service = new CacheService(new FileHandleFactory(SemanticFileCache.getCache().getCacheDir()));
			}
			return service;
		} finally {
			rl.unlock();
		}
	}

}
