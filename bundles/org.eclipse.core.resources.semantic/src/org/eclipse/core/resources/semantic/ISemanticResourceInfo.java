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
package org.eclipse.core.resources.semantic;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * Semantic Resource Information.
 * <p>
 * In order to allow for performance optimization, instances of this interface
 * are always constructed using a bit mask specifying the requested attributes
 * (see {@link ISemanticResource#fetchResourceInfo(int, IProgressMonitor)}.
 * <p>
 * If a given attribute was not requested when obtaining an instance of this
 * interface, a {@link RuntimeException} will be thrown when trying to read the
 * attribute value through the corresponding getter method.
 * 
 * @since 4.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISemanticResourceInfo {

	/**
	 * Returns the read-only flag.
	 * <p>
	 * Note the particular semantics of this flag within the team paradigm (see
	 * {@link ISemanticFile#validateEdit(Object)}).
	 * <p>
	 * 
	 * @return <code>true</code> if the resource is read-only
	 * @see ISemanticFileSystem#RESOURCE_INFO_READ_ONLY
	 */
	public boolean isReadOnly();

	/**
	 * Returns the local-only flag.
	 * <p>
	 * The Semantic File System API does not provide methods to make local-only
	 * resources repository-controlled ("open for add"). Such methods may be
	 * offered in future releases.
	 * <p>
	 * Local-only resources have no corresponding remote repository
	 * representation, i.e. they can not be synchronized.
	 * <p>
	 * For {@link ISemanticFolder} and {@link ISemanticProject} instances, this
	 * always returns <code>true</code>
	 * 
	 * @return <code>true</code> if the resource is local-only
	 * @see ISemanticFileSystem#RESOURCE_INFO_LOCAL_ONLY
	 */
	public boolean isLocalOnly();

	/**
	 * Checks whether locking is supported for this resource.
	 * <p>
	 * If locking is not supported,
	 * {@link ISemanticResource#lockResource(int, IProgressMonitor)} and
	 * {@link ISemanticResource#unlockResource(int, IProgressMonitor)} will
	 * return {@link IStatus#CANCEL}.
	 * 
	 * 
	 * @return <code>true</code> if locking is supported
	 * @throws CoreException
	 *             in case of failure
	 * @see ISemanticFileSystem#RESOURCE_INFO_LOCKING_SUPPORTED
	 */
	public boolean isLockingSupported() throws CoreException;

	/**
	 * Returns the lock-flag.
	 * <p>
	 * See {@link ISemanticResource#lockResource(int, IProgressMonitor)},
	 * {@link ISemanticResource#unlockResource(int, IProgressMonitor)};
	 * 
	 * @return <code>true</code> if the resource is locked, otherwise
	 *         <code>false</code>, even if locking is not supported
	 * @see #isLockingSupported()
	 * @see ISemanticFileSystem#RESOURCE_INFO_LOCKED
	 */
	public boolean isLocked();

	/**
	 * Returns an URI string that identifies the resource remotely.
	 * <p>
	 * This functionality is optional. If the responsible content provider does
	 * not implement <code>IRestSemanticContentProvider</code>, a
	 * {@link CoreException} with status code
	 * {@link SemanticResourceStatusCode#METHOD_NOT_SUPPORTED} will be thrown
	 * 
	 * @return URI string or <code>null</code> if this value was not set
	 * 
	 * @see ISemanticFileSystem#RESOURCE_INFO_URI_STRING
	 */
	public String getRemoteURIString();

	/**
	 * Remote existence check.
	 * <p>
	 * 
	 * @return <code>true</code> if the resource exists remotely
	 * 
	 * @see ISemanticFileSystem#RESOURCE_INFO_EXISTS_REMOTELY
	 */
	public boolean existsRemotely();

	/**
	 * 
	 * @return content type or <code>null</code>
	 * @throws CoreException
	 * @see ISemanticFileSystem#RESOURCE_INFO_CONTENT_TYPE
	 */
	public String getContentType() throws CoreException;
}
