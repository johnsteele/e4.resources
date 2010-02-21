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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

class TemporaryFileHandle implements ITemporaryContentHandle {

	private final File file;
	private final File cacheFile;
	private final IPath path;
	private final boolean appendMode;
	private final long appendPosition;
	private final OutputStream outputStream;
	private final FileHandleFactory factory;

	/**
	 * The "normal" instance
	 * 
	 * @param factory
	 * @param path
	 * @param file
	 * @param cacheFile
	 * @param outputStream
	 */
	public TemporaryFileHandle(FileHandleFactory factory, IPath path, File file, File cacheFile, OutputStream outputStream) {
		this.factory = factory;
		this.path = path;
		this.file = file;
		this.cacheFile = cacheFile;
		this.appendMode = false;
		this.appendPosition = -1l;
		this.outputStream = outputStream;
	}

	/**
	 * The "appending" instance
	 * 
	 * @param factory
	 * @param path
	 * @param cacheFile
	 * @param outputStream
	 */
	public TemporaryFileHandle(FileHandleFactory factory, IPath path, File cacheFile, OutputStream outputStream) {
		this.factory = factory;
		this.path = path;
		this.file = cacheFile;
		this.cacheFile = cacheFile;
		this.appendMode = true;
		this.appendPosition = this.file.length();
		this.outputStream = outputStream;
	}

	public File getFile() {
		return this.file;
	}

	public void commit(long timestamp) throws CoreException {

		try {
			this.outputStream.flush();
			this.outputStream.close();
		} catch (IOException e) {
			// TODO 0.1: cleanup when append mode
			if (!this.appendMode) {
				// in append mode, we write to the cache directly, so we can't
				// delete

				// delete temporary content
				this.factory.tryDelete(this.file);
			}

			throw new SemanticResourceException(SemanticResourceStatusCode.FILECACHE_ERROR_WRITING_CONTENT, new Path(this.file
					.getAbsolutePath()), MessageFormat.format(Messages.TemporaryFileHandle_OsCloseErrorOnCommit_XMSG, this.file
					.getAbsolutePath()), e);
		}

		if (!this.appendMode) {
			this.factory.doRename(this.getFile(), this.cacheFile);
		}

		this.factory.setLastModified(cacheFile, timestamp);
	}

	public IPath getKey() {
		return this.path;
	}

	public long getAppendPosition() {
		return this.appendPosition;
	}

	public void flush() throws IOException {
		this.outputStream.flush();
	}

	public void write(byte[] b, int off, int len) throws IOException {
		this.outputStream.write(b, off, len);
	}

	public void write(byte[] b) throws IOException {
		this.outputStream.write(b);
	}

	public void write(int b) throws IOException {
		this.outputStream.write(b);
	}

	public void setContents(InputStream input, IProgressMonitor monitor) throws CoreException {
		Util.transferStreams(input, this.outputStream, monitor);
	}

}
