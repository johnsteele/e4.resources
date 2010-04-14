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

import org.eclipse.core.resources.semantic.spi.ICacheService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * The content handle factory interface used by the cache service to abstract
 * the cache service from the actual cache storage.
 * <p>
 * Cache updates are performed atomically in order to achieve proper
 * synchronization: new content is first written into a temporary handle; after
 * completion of the write operation, the the temporary handle is "committed"
 * into the cache by (logically) moving it from a temporary handle to a cached
 * handle with the same path.
 * <p>
 * Synchronization is achieved by a global write lock on the cache which exists
 * during the "commit" operation.
 * <p>
 * 
 */
public interface IContentHandleFactory {
	/**
	 * Creates a temporary content handle
	 * 
	 * @param service
	 *            the cache service
	 * @param path
	 *            the path
	 * @param append
	 *            if <code>true</code>, write operations should append the
	 *            cached content, otherwise it should be replaced
	 * @return the handle
	 * @throws CoreException
	 *             in case of failure
	 */
	ITemporaryContentHandle createTemporaryHandle(ICacheService service, IPath path, boolean append) throws CoreException;

	/**
	 * Creates a cached content handle
	 * 
	 * @param service
	 *            the cache service
	 * @param path
	 *            the path
	 * @return the handle
	 * @throws CoreException
	 *             upon failure
	 */
	ICachedContentHandle createCacheContentHandle(ICacheService service, IPath path) throws CoreException;

	/**
	 * Removes all content underneath the specified path
	 * 
	 * @param cacheService
	 *            the cache service
	 * @param path
	 *            the path
	 * @throws CoreException
	 *             upon failure
	 */
	void removeContentRecursive(CacheService cacheService, IPath path) throws CoreException;
}
