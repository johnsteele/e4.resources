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

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.team.FileModificationValidator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

/**
 * 
 * This SPI should be implemented by adapters between Semantic File System and
 * various repositories/content storages.
 * <p>
 * Since content provider instances will be created or disposed on demand, the
 * following guidelines must be followed for an implementation:
 * <ul>
 * <li>
 * Implementing classes must have a public no-arguments (or default) constructor
 * </li>
 * <li>
 * Implementing classes must not interact with the Eclipse Resource Management</li>
 * <li>
 * No exceptions are allowed within the constructor</li>
 * <li>
 * Constructors or static initializers are called from within a global Semantic
 * File System lock and must take measures to avoid deadlocks; in particular, no
 * calls into the Semantic File System are allowed</li>
 * <li>
 * No assumptions must be made about instance lifetime</li>
 * <li>
 * The implementation must be stateless
 * <p>
 * If some transient data/state must be cached between calls,
 * {@link ISemanticFileStore#setSessionProperty(org.eclipse.core.runtime.QualifiedName, Object)}
 * should be used to store the data/state. Transient attributes are not
 * available after Eclipse restart.
 * <p>
 * If some data/state must be preserved between Eclipse restarts,
 * {@link ISemanticFileStore#setPersistentProperty(org.eclipse.core.runtime.QualifiedName, String)}
 * should be used.</li>
 * </ul>
 * The Semantic File System will initialize content providers via extension
 * point <code>org.eclipse.core.resources.semantic.spi.contentProvider</code>
 * using contentProviderID provided as parameter to
 * <code>ISemanticFolder#addFolder(String,String,Map<QualifiedName,String>,int,IProgressMonitor)</code>
 * or
 * <code>ISemanticFolder#addFile(String,String,Map<QualifiedName,String>,int,IProgressMonitor)</code>
 * <p>
 * Important: change of Content Provider ID is an incompatible change that may
 * lead to data loss. In cases when is is necessary to provide a new Content
 * Provider ID for some reasons, the content provider must be additionally
 * remain registered under the old Content Provider ID until it is ensured that
 * all content that uses the old ID is migrated.
 * <p>
 * <h2>Methods to be implemented by concrete content providers</h2>
 * 
 * <h3>Building the resource hierarchy</h3>
 * <p>
 * {@link #addResource(ISemanticFileStore, String, org.eclipse.core.resources.semantic.spi.ISemanticFileStore.ResourceType, IProgressMonitor)}
 * is used to build the local resource hierarchy resource by resource. Note that
 * adding a resource does not mean that it is created in the remote repository,
 * rather a reference to it is created in the local workspace.
 * <p>
 * Alternatively, resource hierarchies can be built implicitly by using methods
 * of the Eclipse Resource Management API (e.g. <code>getFile()</code> and
 * subsequent <code>create()</code> or <code>getContents()</code>). In this
 * case, the content provider is notified about (implicitly) created resource
 * handles via method {@link #onImplicitStoreCreate(ISemanticFileStore)}.
 * 
 * <h3>Resource Removal</h3>
 * Resources can be removed from the local workspace without being deleted in
 * the remote repository (see
 * {@link #removeResource(ISemanticFileStore, IProgressMonitor)}).
 * 
 * <h3>Locking of Resources</h3>
 * Locking may not supported at all by a content provider. In this case,
 * {@link ISemanticSpiResourceInfo#isLockingSupported()} should return false;
 * otherwise this method should return true and the content provider must
 * implement the interface {@link ISemanticContentProviderLocking}.
 * 
 * <h3>Read-only flag</h3>
 * The read-only flag ( {@link ISemanticSpiResourceInfo#isReadOnly()} must be
 * implemented properly in order to take advantage of the
 * 
 * {@link #validateEdit(ISemanticFileStore[], Object)} method. If the content
 * provider needs to "prepare" (e.g. lock) a resource in one way or another
 * before it can be modified (in memory), method
 * {@link ISemanticSpiResourceInfo#isReadOnly()} should initially return
 * <code>true</code>. See that method for more documentation.
 * 
 * <h3>Remote resource creation and deletion</h3>
 * Remote resource creation and deletion is optional and declared on interface
 * {@link ISemanticContentProviderRemote}. Read-only content providers do not
 * have to implement this interface.
 * 
 * <h2>Adapters to implement</h2>
 * The {@link ISemanticFileHistoryProvider} should be adapted in order to
 * provider generic access to the remote history.
 * <p>
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 *              Clients should subclass {@link ContentProvider} or one of its
 *              subclasses instead.
 * 
 */
public interface ISemanticContentProvider extends ISemanticContentProviderBase, IAdaptable {

	/**
	 * Opens an <code>InputStream</code> for reading the content a resource.
	 * 
	 * @param semanticFileStore
	 *            the resource handle
	 * @param monitor
	 *            may be null
	 * @return the stream
	 * @throws CoreException
	 *             in case of failure
	 * @see IFileStore#openInputStream(int, IProgressMonitor)
	 */
	public InputStream openInputStream(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException;

	/**
	 * Opens an <code>OutputStream</code> for writing the content of a resource.
	 * <p>
	 * Note that
	 * {@link #getResourceTimestamp(ISemanticFileStore, IProgressMonitor)} must
	 * return the correct (new) timestamp after the <code>close</code> method
	 * was executed on the returned stream.
	 * 
	 * @param semanticFileStore
	 *            the resource handle
	 * @param options
	 *            specify {@link ISemanticFileSystem#CONTENT_APPEND} in order to
	 *            append to end of the stream, otherwise
	 *            {@link ISemanticFileSystem#NONE} should be used
	 * 
	 * @param monitor
	 *            may be null
	 * @return the stream, never <code>null</code>
	 * @throws CoreException
	 *             if the stream can not be opened
	 * @see IFileStore#openOutputStream(int, IProgressMonitor)
	 */
	public OutputStream openOutputStream(ISemanticFileStore semanticFileStore, int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Checks whether a resource can be modified.
	 * <p>
	 * During the life cycle of a resource under Team control, this is typically
	 * called just before an attempt to modify the (workspace) resource in
	 * memory. For example, a standard text editor will trigger a call to this
	 * method as soon as the user starts changing the editor content.
	 * <p>
	 * However, it is important to note that this will only be done if the
	 * resource is marked as read-only before the resource is modified.
	 * <p>
	 * Correspondingly, upon successful completion of this method, the read-only
	 * flag should be set to <code>false</code>.
	 * <p>
	 * This method may be called from any thread. If the shell parameter is
	 * specified, it is the responsibility of the method implementation to
	 * interact with the UI context in an appropriate thread.
	 * </p>
	 * 
	 * @param semanticFileStore
	 *            the resource handle
	 * @param shell
	 *            the shell, or <code>null</code> for headless validation
	 * @return {@link IStatus#OK} upon success, any other status upon failure;
	 *         upon success, the resource should not be marked as read-only
	 * @see ISemanticSpiResourceInfo#isReadOnly()
	 */
	public IStatus validateEdit(ISemanticFileStore[] semanticFileStore, Object shell);

	/**
	 * Checks whether a resource can be saved.
	 * <p>
	 * This can in principle delegate to
	 * {@link #validateEdit(ISemanticFileStore[], Object)}, provided this method
	 * can perform headless validation. In case of such delegation,
	 * {@link #validateEdit(ISemanticFileStore[], Object)} must be implemented
	 * in a thread-safe manner, since {@link #validateSave(ISemanticFileStore)}
	 * may be called inside or outside of a scheduling rule.
	 * <p>
	 * After successful completion, the read-only flag must be
	 * <code>false</code>.
	 * 
	 * @param semanticFileStore
	 *            the resource handle
	 * @see FileModificationValidator#validateSave(org.eclipse.core.resources.IFile)
	 * @return {@link IStatus#OK} upon success, any other status upon failure
	 */
	public IStatus validateSave(ISemanticFileStore semanticFileStore);

	/**
	 * Adds a resource to the workspace.
	 * <p>
	 * 
	 * @param parentStore
	 *            the parent resource handle
	 * @param name
	 *            the name of the resource
	 * @param resourceType
	 *            the type of the resource or
	 *            {@link ISemanticFileStore.ResourceType#UNKNOWN_TYPE} to let
	 *            the content provider decide on the resource type
	 * @param monitor
	 *            may be null
	 * @throws CoreException
	 *             in case of failure
	 */
	public void addResource(ISemanticFileStore parentStore, String name, ISemanticFileStore.ResourceType resourceType,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Removes the resource from the workspace, but not from the remote
	 * repository; other resources might also be affected by this operation.
	 * <p>
	 * Content providers are responsible to clean up behind the deleted resource
	 * and it's children.
	 * 
	 * @param semanticFileStore
	 *            the resource handle
	 * @param monitor
	 *            may be null
	 * @throws CoreException
	 *             in case of failure
	 */
	public void removeResource(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException;

	/**
	 * Synchronizes the content for a given {@link ISemanticFileStore}.
	 * <p>
	 * This method will be called from
	 * <code>ISemanticResource.synchronizeContentWithRemote(SyncDirection, int, int, IProgressMonitor).</code>
	 * <p>
	 * Since this is a (potentially) deep operation, instead of throwing an
	 * Exception, problems should be recorded in the provided
	 * {@link MultiStatus} object.
	 * <p>
	 * Besides synchronizing the content of resources that are already present
	 * in the resource tree, the implementation may add or remove some resources
	 * in order to maintain consistency (e.g. considering composite resources
	 * that consist of multiple resources).
	 * <p>
	 * If the implementation discovers that some resources are deleted remotely,
	 * they should be removed from the tree without reporting an error (status
	 * with severity {@link IStatus#INFO} may be reported instead). An error
	 * should be reported (using severity {@link IStatus#ERROR}) only if it is
	 * impossible to determine and update the state and content of a resource.
	 * <p>
	 * 
	 * @param semanticFileStore
	 *            the store for which to sync content; for deep operations, the
	 *            root store
	 * @param direction
	 *            one of the {@link SyncDirection} enumeration values
	 * @param monitor
	 *            may be null
	 * @param status
	 *            the {@link MultiStatus}
	 * 
	 * @see ISemanticResource#synchronizeContentWithRemote(SyncDirection, int,
	 *      IProgressMonitor)
	 */
	public void synchronizeContentWithRemote(ISemanticFileStore semanticFileStore, SyncDirection direction, IProgressMonitor monitor,
			MultiStatus status);

	/**
	 * See <code>ISemanticFile.revertChanges(IProgressMonitor)</code>.
	 * 
	 * @param semanticFileStore
	 *            the resource handle
	 * @param monitor
	 *            may be null
	 * @throws CoreException
	 *             in case of failure
	 */
	public void revertChanges(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException;

	/**
	 * Notifies this provider that a {@link ISemanticFileStore} has been created
	 * implicitly during {@link IFileStore#getChild(String)}.
	 * <p>
	 * Implementations may use this to update internal state, but must not
	 * interact with the remote repository.
	 * 
	 * @param newStore
	 *            the new store
	 */
	public void onImplicitStoreCreate(ISemanticFileStore newStore);

	/**
	 * Notifies this provider that a root store for this provider has been
	 * created by a federating content provider.
	 * <p>
	 * Implementations may use this to update internal state, but must not
	 * interact with the remote repository.
	 * 
	 * @param newStore
	 *            the new store
	 * @since 0.4
	 */
	public void onRootStoreCreate(ISemanticFileStore newStore);

	/**
	 * Validates if a resource can be removed locally.
	 * <p>
	 * Please check the documentation on the corresponding
	 * <code>ISemanticResource</code> method for more details.
	 * 
	 * 
	 * @param semanticFileStore
	 *            the resource handle
	 * @param options
	 *            see the documentation on the corresponding
	 *            <code>ISemanticResource</code> method
	 * @param monitor
	 *            may be null
	 * @return <ul>
	 *         <li> {@link IStatus#OK} indicates that local child may be removed
	 *         </li> <li> {@link IStatus#ERROR} results in an error pop-up and
	 *         resource removal failure</li> <li> {@link IStatus#CANCEL} results
	 *         in silent resource removal failure</li>
	 *         </ul>
	 * @throws CoreException
	 *             in case of failure
	 * 
	 */
	public IStatus validateRemove(ISemanticFileStore semanticFileStore, int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Sets the read-only flag on a resource.
	 * <p>
	 * This method may be called by <code>IFileStore.putInfo()</code>.
	 * 
	 * @param semanticFileStore
	 *            the resource handle
	 * @param readonly
	 *            the read-only flag
	 * @param monitor
	 *            may be null
	 * @throws CoreException
	 *             in case of failure
	 * @see IFileStore#putInfo(org.eclipse.core.filesystem.IFileInfo, int,
	 *      IProgressMonitor)
	 * @see ISemanticSpiResourceInfo#isReadOnly()
	 */
	public void setReadOnly(ISemanticFileStore semanticFileStore, boolean readonly, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns resource info from the content provider.
	 * <p>
	 * This is called upon
	 * <code>ISemanticResource.fetchResourceInfo(IProgressMonitor)</code>.
	 * 
	 * @param semanticFileStore
	 *            the resource handle
	 * @param options
	 *            possible value are:
	 *            <ul>
	 *            <li>{@link ISemanticFileSystem#RESOURCE_INFO_CONTENT_TYPE}
	 *            </li> <li>
	 *            {@link ISemanticFileSystem#RESOURCE_INFO_EXISTS_REMOTELY}</li>
	 *            <li>{@link ISemanticFileSystem#RESOURCE_INFO_LOCAL_ONLY}</li>
	 *            <li>{@link ISemanticFileSystem#RESOURCE_INFO_LOCKED}</li> <li>
	 *            {@link ISemanticFileSystem#RESOURCE_INFO_LOCKING_SUPPORTED}
	 *            </li> <li>{@link ISemanticFileSystem#RESOURCE_INFO_READ_ONLY}
	 *            </li> <li>{@link ISemanticFileSystem#RESOURCE_INFO_URI_STRING}
	 *            </li>
	 *            </ul>
	 *            Multiple values can be specified using bitwise OR (i.e. the
	 *            <code>int</code> "|" operator);
	 *            {@link ISemanticFileSystem#NONE} corresponds to <em>all</em>
	 *            attributes
	 * @param monitor
	 *            may be null
	 * @return the resource information
	 * @throws CoreException
	 *             in case of failure
	 */
	public ISemanticSpiResourceInfo fetchResourceInfo(ISemanticFileStore semanticFileStore, int options, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * Returns the resource timestamp.
	 * <p>
	 * This timestamp represents the state of the resource as exposed locally.
	 * <p>
	 * If the content provider manages versions of resources, this is the
	 * version timestamp of the version being exposed currently, otherwise this
	 * is the timestamp of the last modification to the exposed resource.
	 * <p>
	 * Semantically, this timestamp belongs to the remote repository. A good
	 * implementation of this method would use UTC timestamps. If UTC timestamps
	 * are not available due to limitations in the remote repository, content
	 * providers must make sure to implement the usual timestamp requirements.
	 * <p>
	 * In particular, the following operations typically result in timestamp
	 * changes
	 * <ul>
	 * <li>Resource creation</li>
	 * <li>Resource modification</li>
	 * <li>Synchronization (if content was changed remotely or if the exposed
	 * version changes)</li>
	 * </ul>
	 * <p>
	 * 
	 * Since the timestamp is vital when it comes to resource change events, it
	 * is important to notice that the timestamp also must change upon
	 * synchronization (at least if that synchronization does in fact change the
	 * content).
	 * <p>
	 * If the content provider does manipulate the content with respect to
	 * different "versions", e.g. toggling between "runtime" and "working copy"
	 * versions, the same applies: if the actual content is changed due to such
	 * an operation, this timestamp should change.
	 * 
	 * @param semanticFileStore
	 *            the resource handle
	 * @param monitor
	 *            may be null
	 * 
	 * @return the modification time
	 * @throws CoreException
	 *             in case of failure
	 */
	public long getResourceTimestamp(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException;

	/**
	 * This may be called by
	 * {@link IFileStore#putInfo(org.eclipse.core.filesystem.IFileInfo, int, IProgressMonitor)}
	 * or other methods.
	 * <p>
	 * 
	 * @param semanticFileStore
	 *            the resource handle
	 * @param timestamp
	 *            the timestamp
	 * @param monitor
	 *            may be null
	 * @throws CoreException
	 *             in case of failure
	 */
	public void setResourceTimestamp(ISemanticFileStore semanticFileStore, long timestamp, IProgressMonitor monitor) throws CoreException;

	/**
	 * The factory returned here will be used to obtain a proper scheduling rule
	 * 
	 * @return the rule factory
	 */
	public ISemanticResourceRuleFactory getRuleFactory();

	/**
	 * Notifies the source content provider the store is about to be moved.
	 * 
	 * @param semanticFileStore
	 * @param targetParent
	 * @param targetName
	 * @param monitor
	 * @throws CoreException
	 *             in case an error happened during detach
	 * 
	 */
	public void detachMovingStore(ISemanticFileStore semanticFileStore, ISemanticFileStore targetParent, String targetName,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Notifies the target content provider that the store has been moved.
	 * 
	 * The content provider is not allowed to throw any exception in this
	 * method.
	 * 
	 * @param semanticFileStore
	 * @param targetParent
	 * @param targetName
	 * @param monitor
	 */
	public void attachMovingStore(ISemanticFileStore semanticFileStore, ISemanticFileStore targetParent, String targetName,
			IProgressMonitor monitor);

	/**
	 * Checks whether the content providers that are source and target of the
	 * move are capable of doing the move.
	 * 
	 * The method is called on both content providers with the same parameters.
	 * 
	 * The move is only executed if both content providers return
	 * <code>true</code>.
	 * 
	 * @param semanticFileStore
	 * @param targetParent
	 * @param targetName
	 * @param monitor
	 * @return true if move is supported/allowed
	 * @throws CoreException
	 */
	public boolean isMoveSupportedForStore(ISemanticFileStore semanticFileStore, ISemanticFileStore targetParent, String targetName,
			IProgressMonitor monitor) throws CoreException;

}
