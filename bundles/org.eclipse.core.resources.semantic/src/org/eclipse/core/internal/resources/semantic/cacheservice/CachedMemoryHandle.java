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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;

/**
 * The memory-based content handle
 * 
 * @since 4.0
 */
class CachedMemoryHandle implements ICachedContentHandle {

	private final MemoryCache myCache;
	private final String myPath;
	private byte[] myContent = new byte[0];

	boolean exists = false;
	long time = 0l;

	/**
	 * @param path
	 *            the path
	 * @param cache
	 *            the memory cache
	 */
	public CachedMemoryHandle(String path, MemoryCache cache) {
		this.myPath = path;
		this.myCache = cache;
	}

	public boolean exists() {
		return this.exists;
	}

	public long lastModified() {
		return this.time;
	}

	public void setLastModified(long timestamp) {
		this.time = timestamp;
	}

	public void delete() {
		this.exists = false;
		this.myCache.removeStore(this.myPath);
	}

	public InputStream openInputStream() throws CoreException {
		return new ByteArrayInputStream(this.myContent);
	}

	/**
	 * Updates the memory cache with data
	 * 
	 * @param contents
	 *            the content
	 * @param append
	 *            if <code>true</code>, the content should be appended,
	 *            otherwise it will be replaced
	 */
	public void setContents(byte[] contents, boolean append) {
		this.exists = true;
		if (append && this.myContent.length > 0) {
			byte[] oldContent = this.myContent;
			this.myContent = new byte[oldContent.length + contents.length];
			System.arraycopy(oldContent, 0, this.myContent, 0, oldContent.length);
			System.arraycopy(contents, 0, this.myContent, oldContent.length, contents.length);
		} else {
			this.myContent = contents;
		}
	}
}
