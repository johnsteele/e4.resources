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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

/**
 * The file-based content handle
 * 
 * @since 4.0
 */
class CachedFileHandle implements ICachedContentHandle {

	private final File cacheFile;
	private final FileHandleFactory factory;

	/**
	 * @param factory
	 *            the file handle factory
	 * @param cacheFile
	 *            the file holding the cached data
	 */
	public CachedFileHandle(FileHandleFactory factory, File cacheFile) {
		this.factory = factory;
		this.cacheFile = cacheFile;
	}

	public boolean exists() {
		return this.factory.checkFileExists(this.cacheFile);
	}

	public void delete() {
		this.factory.tryDelete(this.cacheFile);
	}

	public InputStream openInputStream() throws CoreException {
		try {
			return this.factory.openInputStream(this.cacheFile);
		} catch (FileNotFoundException e) {
			throw new SemanticResourceException(SemanticResourceStatusCode.CACHED_CONTENT_NOT_FOUND, new Path(this.cacheFile
					.getAbsolutePath()), null, e);
		}
	}

}
