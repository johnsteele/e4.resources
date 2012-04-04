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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

class TemporaryMemoryHandle implements ITemporaryContentHandle {

	private IPath path;
	private final boolean appendMode;
	private final ByteArrayOutputStream bos;

	public TemporaryMemoryHandle(IPath path, boolean append, ByteArrayOutputStream bos) {
		this.bos = bos;
		this.appendMode = append;
		this.path = path;
	}

	/**
	 * @throws CoreException
	 */
	public void commit() throws CoreException {
		CachedMemoryHandle handle = MemoryCache.getInstance().getOrCreateMemoryStore(this.path.toString());
		byte[] content = this.bos.toByteArray();
		handle.setContents(content, this.appendMode);
	}

	public InputStream closeAndGetContents() {
		Util.safeClose(this.bos);
		byte[] content = this.bos.toByteArray();
		return new ByteArrayInputStream(content);
	}

	public IPath getKey() {
		return this.path;
	}

	public void flush() throws IOException {
		this.bos.flush();

	}

	public long getAppendPosition() {
		return 0;
	}

	public void setContents(InputStream input, IProgressMonitor monitor) throws CoreException {
		Util.transferStreams(input, this.bos, monitor);

	}

	/**
	 * @throws IOException
	 */
	public void write(byte[] b, int off, int len) throws IOException {
		this.bos.write(b, off, len);

	}

	public void write(byte[] b) throws IOException {
		this.bos.write(b);

	}

	/**
	 * @throws IOException
	 */
	public void write(int b) throws IOException {
		this.bos.write(b);
	}

	public void rollback() {
		// do nothing
	}

}
