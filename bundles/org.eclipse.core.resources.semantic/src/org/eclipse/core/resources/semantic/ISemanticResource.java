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

import java.net.URI;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

/**
 * Provides additional Semantic File System methods for workspace resources.
 * <p>
 * Types of Semantic File System resources correspond 1-to-1 to workspace
 * resources. There are three <i>types</i> of semantic resources: semantic
 * files, semantic folders and semantic projects. There is no correspondence to
 * the workspace root resource because it can not be located on the (Semantic)
 * EFS.
 * <p>
 * Semantic file resources are similar to files in that they provide access to
 * resource content represented as streams. Semantic folder resources are
 * analogous to directories in that they hold other semantic resources but
 * cannot directly hold data. Semantic project resources group files and folders
 * into reusable clusters.
 * <p>
 * Semantic File System can be rooted at any level in the workspace resource
 * tree with the exception of workspace root. In order get full functionality,
 * however, it is advisable to put a whole project on the Semantic File System
 * and assign it to the Semantic Repository Provider.
 * </p>
 * TODO 0.1 add state and transition diagram and discuss which operation are
 * possible in which state
 * <p>
 * An instance of <code>ISemanticResource</code> can be obtained from a
 * workspace resource instance using the following code sequence:
 * <p>
 * <code><pre>
 *  IResource resource = ...;
 *  ISemanticResource semanticResource = (ISemanticResource) resource.getAdapter(ISemanticResource.class);
 *  if ( semanticResource != null ) {
 *    ...				
 *  }
 * </code></pre>
 * </p>
 * </p>
 * <p>
 * Features of semantic resources:
 * <ul>
 * <li>TODO 0.1 javadoc</li>
 * </ul>
 * </p>
 * <p>
 * Resources implement the <code>IAdaptable</code> interface; extensions are
 * managed by the platform's adapter manager.
 * </p>
 * 
 * @see IResource
 * @see ISemanticFileSystem#SFS_REPOSITORY_PROVIDER
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISemanticResource extends ISemanticProperties {

	/**
	 * 
	 * @return the adapted workspace resource
	 */
	public IResource getAdaptedResource();

	/**
	 * Validates that this resource can be deleted in the repository.
	 * <p>
	 * Note that this is not called by any framework but can be used by
	 * applications for checking before they delete a resource.
	 * <p>
	 * The optional UI shell may be supplied if UI-based validation is required.
	 * If the shell is <code>null</code>, the responsible content provider must
	 * attempt to perform the validation in a headless manner.
	 * <p>
	 * The returned status is <code>IStatus.OK</code> if the content provider
	 * believes the given resource can be deleted. Other return statuses
	 * indicate the reason why the operation can not be performed.
	 * 
	 * @param shell
	 *            the shell to aid in UI-based validation or <code>null</code>
	 *            if the validation must be headless
	 * @return a status object that is either {@link IStatus#OK} or describes
	 *         the reason why remote deletion is not possible
	 */
	public IStatus validateRemoteDelete(Object shell);

	/**
	 * Returns the content provider ID for this resource.
	 * <p>
	 * This method returns the ID only for resources that have been added using
	 * {@link ISemanticFolder#addFolder(String, String, Map, int, IProgressMonitor)}
	 * or
	 * {@link ISemanticFolder#addFile(String, String, Map, int, IProgressMonitor)}
	 * . It returns <code>null</code> for all other resources.
	 * 
	 * @return the ID string if this resource is a root resource for a content
	 *         provider, or <code>null</code>
	 * @throws CoreException
	 */
	public String getContentProviderID() throws CoreException;

	/**
	 * Validates that this resource can be removed locally.
	 * <p>
	 * If {@link ISemanticFileSystem#NONE} is specified in the options, this
	 * method should check all resources that would be deleted by
	 * {@link #remove(int, IProgressMonitor)} and return an error or cancel
	 * status if deletion should not be allowed, e.g. because it would result in
	 * loss of data.
	 * <p>
	 * If
	 * {@link ISemanticFileSystem#VALIDATE_REMOVE_RAISE_ERROR_ON_SCOPE_EXTENSION}
	 * is specified in the options, this method must return an error or cancel
	 * status if deletion of this resource will also delete resources that are
	 * not direct or indirect children of this resource. In addition, the checks
	 * for data loss described above should be performed.
	 * <p>
	 * {@link ISemanticFileSystem#VALIDATE_REMOVE_IGNORE_RESOURCE_STATE} should
	 * only be specified together with
	 * {@link ISemanticFileSystem#VALIDATE_REMOVE_RAISE_ERROR_ON_SCOPE_EXTENSION}
	 * . In this case, the checks for data loss should be skipped.
	 * 
	 * @param options
	 *            the following options are supported (separately and in
	 *            combination):
	 *            <ul>
	 *            <li>
	 *            {@link ISemanticFileSystem#VALIDATE_REMOVE_RAISE_ERROR_ON_SCOPE_EXTENSION}
	 *            </li> <li>
	 *            {@link ISemanticFileSystem#VALIDATE_REMOVE_IGNORE_RESOURCE_STATE}
	 *            </li> <li>{@link ISemanticFileSystem#NONE}</li>
	 *            </ul>
	 * 
	 * @param monitor
	 *            may be null
	 * 
	 * 
	 * @return a status object that is either {@link IStatus#OK} or describes
	 *         the reason why local deletion should not be performed
	 */
	public IStatus validateRemove(int options, IProgressMonitor monitor);

	/**
	 * Deletes this resource in the remote repository and also removes it from
	 * the workspace.
	 * <p>
	 * Some other resources may be also deleted as result of this operation. In
	 * order to avoid lock extension, a proper delete scheduling rule must be
	 * obtained before executing this operation.
	 * <p>
	 * Unless {@link ISemanticFileSystem#SUPPRESS_REFRESH} is specified in the
	 * options, the parent of this resource will be refreshed locally.
	 * 
	 * @param options
	 *            only {@link ISemanticFileSystem#SUPPRESS_REFRESH} is supported
	 * @param monitor
	 *            a progress monitor, or <code>null</code> if progress reporting
	 *            is not desired
	 * @throws CoreException
	 *             in case of operation failure
	 * 
	 * @see IWorkspace#getRuleFactory()
	 * @see IResourceRuleFactory#deleteRule(IResource)
	 */
	public void deleteRemotely(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Removes the resource locally without deleting it from the remote
	 * repository
	 * <p>
	 * Some other resources may be also removed as result of this operation. In
	 * order to avoid lock extension, a proper delete scheduling rule must be
	 * obtained before executing this operation.
	 * <p>
	 * Unless {@link ISemanticFileSystem#SUPPRESS_REFRESH} is specified in the
	 * options, the parent of this resource will be refreshed locally.
	 * <p>
	 * This operation does <em>not</em> take resource state into account (e.g.
	 * read only flag, lock state...).
	 * 
	 * @param options
	 *            only {@link ISemanticFileSystem#SUPPRESS_REFRESH} is supported
	 * @param monitor
	 *            a progress monitor, or <code>null</code> if progress reporting
	 *            is not desired
	 * @throws CoreException
	 *             in case of operation failure
	 * 
	 * @see IWorkspace#getRuleFactory()
	 * @see IResourceRuleFactory#deleteRule(IResource)
	 */
	public void remove(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Recursively synchronizes all local content changes with the remote
	 * repository with infinite depth.
	 * <p>
	 * Content providers may decide to synchronize some resources outside of the
	 * requested subtree in order to keep content consistent. In order to avoid
	 * lock extension, a proper update scheduling rule must be obtained before
	 * executing this operation.
	 * <p>
	 * Since this traverses the tree, content providers are not allowed to throw
	 * Exceptions directly, instead they will be provided with a
	 * {@link MultiStatus} object to which they can add error status objects; if
	 * the resulting {@link MultiStatus} is not ok (see {@link IStatus#isOK()}),
	 * then it will be wrapped into the declared {@link CoreException} and
	 * returned here.
	 * <p>
	 * Content providers are encouraged to add instances of
	 * {@link ISemanticResourceStatus} so that consumers can provide
	 * resource-based error markers using
	 * {@link ISemanticResourceStatus#getPath()}. In that case,
	 * {@link SemanticResourceStatusCode#SYNC_ERROR},
	 * {@link SemanticResourceStatusCode#SYNC_WARNING}, or
	 * {@link SemanticResourceStatusCode#SYNC_INFO} should be used by the
	 * content providers to identify content-provider specific problems.
	 * However, callers should make no assumptions about the returned status
	 * objects.
	 * <p>
	 * In order to avoid lock extension, a proper refresh scheduling rule must
	 * be obtained before executing this operation.
	 * <p>
	 * Unless {@link ISemanticFileSystem#SUPPRESS_REFRESH} is specified in the
	 * options, this resource will be refreshed locally.
	 * 
	 * @param direction
	 *            determines in which direction the sync is done {
	 *            {@link SyncDirection#BOTH}, {@link SyncDirection#INCOMING},
	 *            {@link SyncDirection#OUTGOING}
	 * @param options
	 *            only {@link ISemanticFileSystem#SUPPRESS_REFRESH} is supported
	 * @param monitor
	 *            a progress monitor, or <code>null</code> if progress reporting
	 *            is not desired
	 * @throws CoreException
	 *             in case of operation failure
	 * 
	 * @see IWorkspace#getRuleFactory()
	 * @see IResourceRuleFactory#refreshRule(IResource)
	 */
	public void synchronizeContentWithRemote(SyncDirection direction, int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Retrieves and returns resource information.
	 * <p>
	 * The info object represents the state of the resource at the time of
	 * retrieval. Later changes of resource state are not reflected.
	 * 
	 * @param options
	 *            a bit mask to indicate the attributes to read; currently, the
	 *            following are supported:
	 *            <p>
	 *            <ul>
	 *            <li> {@link ISemanticFileSystem#RESOURCE_INFO_LOCAL_ONLY}</li>
	 *            <li> {@link ISemanticFileSystem#RESOURCE_INFO_LOCKED}</li>
	 *            <li>
	 *            {@link ISemanticFileSystem#RESOURCE_INFO_LOCKING_SUPPORTED}</li>
	 *            <li> {@link ISemanticFileSystem#RESOURCE_INFO_READ_ONLY}</li>
	 *            <li>
	 *            {@link ISemanticFileSystem#RESOURCE_INFO_EXISTS_REMOTELY}</li>
	 *            <li>
	 *            {@link ISemanticFileSystem#RESOURCE_INFO_URI_STRING}</li>
	 *            <li>{@link ISemanticFileSystem#RESOURCE_INFO_CONTENT_TYPE}</li>
	 *            </ul>
	 *            <p>
	 *            Multiple values can be specified using bitwise OR (i.e. the
	 *            <code>int</code> "|" operator)
	 *            <p>
	 *            Use {@link ISemanticFileSystem#NONE} to read all attributes
	 * 
	 * @param monitor
	 *            a progress monitor, or <code>null</code> if progress reporting
	 *            is not desired
	 * @return resource info object
	 * @throws CoreException
	 *             in case of operation failure
	 * 
	 * @see ISemanticResourceInfo
	 */
	public ISemanticResourceInfo fetchResourceInfo(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Locks the resource in the remote repository.
	 * <p>
	 * Semantics of lock/unlock operations are repository-dependent. It is
	 * expected that object content will not be modified until the lock is
	 * released, e.g. validateEdit or validateSave should fail when issued by
	 * other users or other sessions of the same user.
	 * <p>
	 * Repositories/content providers that do not support locking should return
	 * {@link IStatus#CANCEL}. Applications can check whether locking is
	 * supported using {@link ISemanticResourceInfo#isLockingSupported()}.
	 * <p>
	 * In order to avoid lock extension, a proper refresh scheduling rule must
	 * be obtained before executing this method.
	 * <p>
	 * Unless {@link ISemanticFileSystem#SUPPRESS_REFRESH} is specified in the
	 * options, this resource will be refreshed locally if the lock operation
	 * returns an {@link IStatus#OK} status.
	 * 
	 * @param options
	 *            only {@link ISemanticFileSystem#SUPPRESS_REFRESH} is supported
	 * @param monitor
	 *            a progress monitor, or <code>null</code> if progress reporting
	 *            is not desired
	 * @return a status object that is {@link IStatus#OK} if locking succeeded
	 *         or if the resource was already locked before this call, otherwise
	 *         a status describing the reason for failure;
	 *         {@link IStatus#CANCEL} if locking is not supported
	 * @throws CoreException
	 *             in case of failure
	 * 
	 * @see IWorkspace#getRuleFactory()
	 * @see IResourceRuleFactory#refreshRule(IResource)
	 */
	public IStatus lockResource(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Unlocks the resource in the remote repository.
	 * <p>
	 * Semantics of lock/unlock operations are repository-dependent. It is
	 * expected that object content will not be modified until lock is released,
	 * e.g. validateEdit or validateSave should fail when issued by other users
	 * or other sessions of the same user.
	 * <p>
	 * Repositories/content providers that do not support locking should return
	 * {@link IStatus#CANCEL}. Applications can check whether locking is
	 * supported using {@link ISemanticResourceInfo#isLockingSupported()}.
	 * <p>
	 * In order to avoid lock extension, a proper refresh scheduling rule must
	 * be obtained before executing this method.
	 * <p>
	 * Unless {@link ISemanticFileSystem#SUPPRESS_REFRESH} is specified in the
	 * options, this resource will be refreshed locally if the lock operation
	 * returns an {@link IStatus#OK} status.
	 * 
	 * @param options
	 *            only {@link ISemanticFileSystem#SUPPRESS_REFRESH} is supported
	 * @param monitor
	 *            a progress monitor, or <code>null</code> if progress reporting
	 *            is not desired
	 * @return a status object that is {@link IStatus#OK} if unlock succeeded or
	 *         if the resource was already unlocked before this call, otherwise
	 *         a status describing reasons for failure; {@link IStatus#CANCEL}
	 *         if locking is not supported
	 * @throws CoreException
	 *             in case of failure
	 * 
	 * @see IWorkspace#getRuleFactory()
	 * @see IResourceRuleFactory#refreshRule(IResource)
	 */
	public IStatus unlockResource(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Sets or updates the remote URI of the resource.
	 * 
	 * <p>
	 * In order to avoid lock extension, a proper refresh scheduling rule must
	 * be obtained before executing this method.
	 * <p>
	 * Unless {@link ISemanticFileSystem#SUPPRESS_REFRESH} is specified in the
	 * options, this resource will be refreshed locally.
	 * 
	 * @param uri
	 *            new URI or <code>null</code> if the URI should be unset
	 * @param options
	 *            only {@link ISemanticFileSystem#SUPPRESS_REFRESH} is supported
	 * @param monitor
	 *            a progress monitor, or <code>null</code> if progress reporting
	 *            is not desired
	 * @throws CoreException
	 *             in case of failure
	 * 
	 * @see IWorkspace#getRuleFactory()
	 * @see IResourceRuleFactory#refreshRule(IResource)
	 */
	public void setRemoteURI(URI uri, int options, IProgressMonitor monitor) throws CoreException;
}
