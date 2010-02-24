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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A temporary content handle for use by the cache service.
 * <p>
 * This is instantiated when content is to be written to the cache. The
 * {@link OutputStream}-like write methods below are used to store the content
 * temporarily. Eventually, the cache service will call the {@link #commit()}
 * method which must (logically) copy the content into a
 * {@link ICachedContentHandle}.
 * 
 */
public interface ITemporaryContentHandle {

	/**
	 * 
	 * @return the key for this handle
	 */
	public IPath getKey();

	/**
	 * Commits temporary content into cache
	 * 
	 * @throws CoreException
	 * 
	 */
	public void commit() throws CoreException;

	/**
	 * 
	 * @return the length of the content before appending to it; returns -1 if
	 *         append mode was <code>false</code>
	 */
	public long getAppendPosition();

	/**
	 * Flushes the underlying data stream
	 * 
	 * @throws IOException
	 * @see OutputStream#flush()
	 */
	public void flush() throws IOException;

	/**
	 * Writes data to the underlying stream
	 * 
	 * @param b
	 *            data
	 * @param off
	 *            offset
	 * @param len
	 *            length
	 * @throws IOException
	 *             upon failure
	 * @see OutputStream#write(byte[], int, int)
	 */
	public void write(byte[] b, int off, int len) throws IOException;

	/**
	 * Writes data to the underlying stream
	 * 
	 * @param b
	 *            data
	 * @throws IOException
	 *             upon failure
	 * @see OutputStream#write(byte[])
	 */
	public void write(byte[] b) throws IOException;

	/**
	 * Writes data to the underlying stream
	 * 
	 * @param b
	 *            data
	 * @throws IOException
	 *             upon failure
	 * @see OutputStream#write(int)
	 */
	public void write(int b) throws IOException;

	/**
	 * Used by the cache service when content is added to the cache
	 * 
	 * @param input
	 *            the content
	 * @param monitor
	 *            may be <code>null</code>
	 * @throws CoreException
	 *             upon failure
	 */
	public void setContents(InputStream input, IProgressMonitor monitor) throws CoreException;
}
