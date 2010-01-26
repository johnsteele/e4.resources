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

import java.io.File;

import org.eclipse.core.internal.resources.semantic.spi.SemanticResourcesSpiPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * This is a persistent cache implementation
 * 
 * @since 4.0
 * @noextend This class is not intended to be extended by clients.
 * 
 */
public class SemanticFileCache {

	private static final String CACHE_DIR_NAME = ".cache";//$NON-NLS-1$

	/**
	 * Thread safety for lazy instantiation of the cache
	 */
	private static final Object creationLock = new Object();

	/**
	 * The singleton file cache instance.
	 */
	private static SemanticFileCache instance = null;

	private File cacheDir;

	/**
	 * Public method to obtain the singleton file cache instance, creating the
	 * cache lazily if necessary.
	 * 
	 * @return The file cache instance
	 * @throws CoreException
	 *             in case of failure
	 */
	public static SemanticFileCache getCache() throws CoreException {
		synchronized (SemanticFileCache.creationLock) {
			if (SemanticFileCache.instance == null)
				SemanticFileCache.instance = new SemanticFileCache();
			return SemanticFileCache.instance;
		}
	}

	/**
	 * @return the directory for caching in the local file system
	 */
	public File getCacheDir() {
		return this.cacheDir;
	}

	/**
	 * Creates a new file cache.
	 * 
	 * @throws CoreException
	 *             If the file cache could not be created
	 */
	private SemanticFileCache() throws CoreException {
		IPath location = SemanticResourcesSpiPlugin.getCacheLocation();
		File cacheParent = new File(location.toFile(), SemanticFileCache.CACHE_DIR_NAME);
		cacheParent.mkdirs();
		this.cacheDir = cacheParent;
	}

}
