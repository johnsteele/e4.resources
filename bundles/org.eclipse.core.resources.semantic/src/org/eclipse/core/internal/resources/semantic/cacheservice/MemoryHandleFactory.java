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

import java.io.ByteArrayOutputStream;

import org.eclipse.core.resources.semantic.spi.ICacheService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * The memory-based content handle factory
 * 
 * @since 4.0
 */
public class MemoryHandleFactory implements IContentHandleFactory {

	public ICachedContentHandle createCacheContentHandle(ICacheService service, IPath path) {

		return MemoryCache.getInstance().getOrCreateMemoryStore(path.toString());
	}

	/**
	 * @throws CoreException
	 */
	public ITemporaryContentHandle createTemporaryHandle(ICacheService service, IPath path, boolean append) throws CoreException {

		return new TemporaryMemoryHandle(path, append, new ByteArrayOutputStream());
	}

	public void removeContentRecursive(CacheService cacheService, IPath path) {
		MemoryCache.getInstance().removeStore(path.toString());
		MemoryCache.getInstance().removeStoresRecursively(path.addTrailingSeparator().toString());
	}

}
