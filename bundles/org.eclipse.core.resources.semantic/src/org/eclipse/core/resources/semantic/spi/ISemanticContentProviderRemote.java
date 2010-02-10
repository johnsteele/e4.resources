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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * This interface must be implemented by content providers that support remote
 * operations.
 * 
 * @since 4.0
 * 
 */
public interface ISemanticContentProviderRemote {

	/**
	 * Validates remote deletion.
	 * 
	 * @param semanticFileStore
	 *            the file store
	 * @param shell
	 *            the shell, or <code>null</code> for headless validation
	 * @return the result of the validation
	 */
	public IStatus validateRemoteDelete(ISemanticFileStore semanticFileStore, Object shell);

	/**
	 * Validates remote creation.
	 * 
	 * @param parentStore
	 *            the parent of the resource to be created
	 * @param childName
	 *            the child name
	 * @param shell
	 *            the shell, or <code>null</code> for headless validation
	 * @return the result of the validation
	 */
	public IStatus validateRemoteCreate(ISemanticFileStore parentStore, String childName, Object shell);

	/**
	 * Creates a file resource in the remote directory and adds it's name to the
	 * child list of this resource.
	 * <p>
	 * Depending on the content provider, either an {@link InputStream} or a
	 * provider-specific "context" object or both must be provided by the
	 * caller.
	 * 
	 * @param parentStore
	 *            the parent resource handle
	 * @param name
	 *            the name of the new resource
	 * @param source
	 *            an input stream containing the content of the new resource
	 *            (optional)
	 * @param context
	 *            a specific object containing the information needed for
	 *            creating the resource (optional)
	 * @param monitor
	 *            may be null
	 * @throws CoreException
	 *             in case of failure
	 */
	public void createFileRemotely(ISemanticFileStore parentStore, String name, InputStream source, Object context, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * Creates a resource in the remote directory and adds it's name to the
	 * child list of this resource.
	 * <p>
	 * The actual type (file or folder) of the resource is determined by the
	 * content provider.
	 * 
	 * @param parentStore
	 *            the parent resource handle
	 * @param name
	 *            the name of the new resource
	 * @param context
	 *            a specific object containing the information needed for
	 *            creating the resource (optional)
	 * @param monitor
	 *            may be null
	 * @throws CoreException
	 *             in case of failure
	 */
	public void createResourceRemotely(ISemanticFileStore parentStore, String name, Object context, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * Deletes the resource remotely and removes it from the workspace.
	 * <p>
	 * Content providers are responsible to clean up behind the deleted resource
	 * and it's children.
	 * 
	 * @param semanticFileStore
	 *            the resource handle
	 * @param monitor
	 *            may be null
	 * @see ISemanticContentProvider#removeResource(ISemanticFileStore,
	 *      IProgressMonitor)
	 * @throws CoreException
	 *             in case of failure
	 */
	public void deleteRemotely(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException;

}
