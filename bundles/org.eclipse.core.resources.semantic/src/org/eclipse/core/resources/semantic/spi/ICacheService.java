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

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The Cache Service interface
 * <p>
 * Different implementation may handle caching differently, e.g. use file system
 * or memory as cache
 * 
 * @noimplement
 * @noextend
 */
public interface ICacheService {

	/**
	 * Adds the given content to the cache
	 * 
	 * @param path
	 *            the path
	 * @param input
	 *            the content
	 * @param options
	 *            only {@link EFS#APPEND} is supported
	 * @param monitor
	 *            may be null
	 * @throws CoreException
	 *             upon failure
	 */
	public void addContent(IPath path, InputStream input, int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * @param path
	 *            the path
	 * @return the content the content
	 * @throws CoreException
	 *             upon failure
	 */
	public InputStream getContent(IPath path) throws CoreException;

	/**
	 * 
	 * @param path
	 *            the path
	 * @return <code>true</code> if the cache has content for the given path
	 * @throws CoreException
	 *             upon failure
	 */
	public boolean hasContent(IPath path) throws CoreException;

	/**
	 * 
	 * @param path
	 *            the path
	 * @param monitor
	 *            may be null
	 * @throws CoreException
	 *             upon failure
	 */
	public void removeContent(IPath path, IProgressMonitor monitor) throws CoreException;

	/**
	 * removes all content that starts with the specified path
	 * 
	 * @param path
	 *            the path
	 * @param monitor
	 *            may be null
	 * @throws CoreException
	 *             upon failure
	 * @since 0.3
	 */
	public void removeContentRecursive(IPath path, IProgressMonitor monitor) throws CoreException;

	/**
	 * 
	 * @param path
	 *            the path
	 * @param append
	 *            <code>true</code> if content should be appended
	 * @param callback
	 *            the call-back for the cache update
	 * @param monitor
	 *            may be null
	 * @return the stream
	 * @throws CoreException
	 */
	public OutputStream wrapOutputStream(IPath path, boolean append, ICacheUpdateCallback callback, IProgressMonitor monitor)
			throws CoreException;

}
