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

import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The REST (<u>Re</u>presentional <u>S</u>tate <u>T</u>ransfer) content
 * provider interface.
 * <p>
 * Content provider implementing this interface store {@link URI}s along with
 * their (semantic) files. These URIs can then be used to map (semantic) file
 * operations to URI operations.
 * 
 * @since 4.0
 * 
 * @noimplement this should not be implemented directly, instead the abstract
 *              base class RESTContentProvider should be extended
 * 
 */
public interface ISemanticContentProviderREST {

	/**
	 * Adds a file and assigns a {@link URI} to it.
	 * <p>
	 * 
	 * @param parentStore
	 *            the parent resource handle
	 * @param name
	 *            the name
	 * @param uri
	 *            the URI
	 * @param monitor
	 *            may be null
	 * @throws CoreException
	 *             in case of failure
	 */
	public void addFileFromRemoteByURI(ISemanticFileStore parentStore, String name, URI uri, IProgressMonitor monitor) throws CoreException;

	/**
	 * Adds a folder and assigns a {@link URI} to it.
	 * <p>
	 * 
	 * @param parentStore
	 *            the parent resource handle
	 * @param name
	 *            the name
	 * @param uri
	 *            the URI
	 * @param monitor
	 *            may be null
	 * @throws CoreException
	 *             in case of failure
	 */
	public void addFolderFromRemoteByURI(ISemanticFileStore parentStore, String name, URI uri, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * Retrieves a URI from a file or folder.
	 * 
	 * @param semanticFileStore
	 *            the resource handle
	 * @return the URI, or <code>null</code>, for example if no URI was assigned
	 * 
	 * @throws CoreException
	 *             in case of failure
	 */
	public String getURIString(ISemanticFileStore semanticFileStore) throws CoreException;

	/**
	 * Sets a URI on file or folder.
	 * 
	 * @param semanticFileStore
	 *            the resource handle
	 * @param uri
	 *            new URI or <code>null</code> if URI should be removed
	 * @param monitor
	 * @throws CoreException
	 */
	public void setURIString(ISemanticFileStore semanticFileStore, URI uri, IProgressMonitor monitor) throws CoreException;

}
