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

import org.eclipse.core.internal.resources.semantic.util.ISemanticFileSystemLog;
import org.eclipse.core.internal.resources.semantic.util.ISemanticFileSystemTrace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The Semantic File System interface.
 * 
 * @since 4.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISemanticFileSystem {

	/**
	 * The Semantic File System Scheme
	 */
	public static final String SCHEME = "semanticfs"; //$NON-NLS-1$
	/**
	 * The Semantic File System Repository Provider to be used in
	 * <code>RepositoryProvider.map()</code>
	 */
	public static final String SFS_REPOSITORY_PROVIDER = "org.eclipse.core.internal.resources.semantic.DelegatingRepositoryProvider"; //$NON-NLS-1$

	/**
	 * The "default" option
	 */
	public static final int NONE = 0;

	/**
	 * Suppress automatic refresh.
	 * <p>
	 * If this option is specified, no local refresh will be tried after
	 * modifications in the Semantic File System.
	 * <p>
	 * This may be useful for performance optimization or if the standard logic
	 * is not suitable, for example because of scheduling rule conflicts.
	 */
	public static final int SUPPRESS_REFRESH = 1 << 1;

	/**
	 * Option flag constant (value 1 &lt;&lt;0) indicating that content should
	 * be appended to a resource.
	 */
	public static final int CONTENT_APPEND = 1 << 0;

	/**
	 * Internal option; clients should not use this
	 */
	public static final int INTERNAL_DELETE_PROJECT = 1 << 16;

	/**
	 * Resource info: isLocked
	 */
	public static final int RESOURCE_INFO_LOCKED = 1 << 1;

	/**
	 * Resource info: isLockingSupported
	 */
	public static final int RESOURCE_INFO_LOCKING_SUPPORTED = 1 << 2;

	/**
	 * Resource info: isLocalOnly
	 */
	public static final int RESOURCE_INFO_LOCAL_ONLY = 1 << 3;

	/**
	 * Resource info: isReadOnly
	 */
	public static final int RESOURCE_INFO_READ_ONLY = 1 << 4;
	/**
	 * Remote Resource info: existsRemotely
	 */
	public static final int RESOURCE_INFO_EXISTS_REMOTELY = 1 << 5;
	/**
	 * Resource info: uriString
	 */
	public static final int RESOURCE_INFO_URI_STRING = 1 << 6;

	/**
	 * Resource info: contentType
	 */
	public static final int RESOURCE_INFO_CONTENT_TYPE = 1 << 7;

	/**
	 * <code>ISemanticResource#validateRemove(int, IProgressMonitor) </code> :
	 * report an error if deletion of the resource would affect any resource
	 * other than the resource itself or one of its direct or indirect children
	 */
	public static final int VALIDATE_REMOVE_RAISE_ERROR_ON_SCOPE_EXTENSION = 1 << 1;
	/**
	 * <code>ISemanticResource.validateRemove(int, IProgressMonitor)</code> : do
	 * not report an error even if deletion of the resource would affect any
	 * resource with outstanding changes or other state that would get lost
	 */
	public static final int VALIDATE_REMOVE_IGNORE_RESOURCE_STATE = 1 << 2;

	/**
	 * Returns the names of the root nodes in the Semantic File System
	 * <p>
	 * Since the "virtual root node" of the Semantic File System itself is not
	 * accessible, this method offers a way to obtain the names of the root
	 * nodes.
	 * 
	 * @return the root names
	 * @throws CoreException
	 *             upon failure, for example because the database was not
	 *             initialized
	 */
	public String[] getRootNames() throws CoreException;

	/**
	 * Returns the path to the Semantic File System database.
	 * 
	 * @return the path
	 */
	public String getPathToDb();

	/**
	 * @return the trace
	 */
	public ISemanticFileSystemTrace getTrace();

	/**
	 * @return the log
	 */
	public ISemanticFileSystemLog getLog();

	/**
	 * TODO 0.1: javadoc
	 * 
	 * @param monitor
	 *            may be null
	 * 
	 * @return URI locator service
	 * @throws CoreException
	 */
	public ISemanticURILocatorService getURILocatorService(IProgressMonitor monitor) throws CoreException;

}
