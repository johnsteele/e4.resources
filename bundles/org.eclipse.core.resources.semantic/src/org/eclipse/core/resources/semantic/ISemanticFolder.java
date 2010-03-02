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

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;

/**
 * Provides additional Semantic FS methods for workspace resources of type
 * folder.
 * <p>
 * An instance of {@link ISemanticFolder} can be obtained from a workspace
 * folder instance using the following code sequence:
 * <p>
 * <code><pre>
 *  IFolder folder = ...;
 *  ISemanticFolder semanticFolder = (ISemanticFolder) folder.getAdapter(ISemanticFolder.class);
 *  if ( semanticFolder != null ) {
 *    ...				
 *  }
 * </code></pre>
 * <p>
 * There are certain restrictions with respect to the resource names that can be
 * used in the various methods that are used to create children. For the
 * type-safe methods, i.e. <code>addFile...</code>, <code>addFolder...</code>
 * and <code>createFile...</code>, {@link IWorkspace#validateName(String, int)}
 * is called internally to validate the provided names. Note that
 * {@link #addResource(String, int, IProgressMonitor)} and
 * {@link #createResourceRemotely(String, Object, int, IProgressMonitor)} do not
 * check the names since the type of the child that is to be created is not
 * known.
 * 
 * @since 4.0
 * @see ISemanticResource
 * @see IFolder
 * @see IWorkspace#validateName(String, int)
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISemanticFolder extends ISemanticResource {

	/**
	 * @return the container
	 */
	public IContainer getAdaptedContainer();

	/**
	 * Validates whether the resource can be created remotely giving the content
	 * provider the opportunity to present UI for the user.
	 * <p>
	 * Note that this is not called by any framework but can be used by
	 * applications for checking before they create a resource.
	 * 
	 * @param name
	 *            the name of the resource to create; note that this does not
	 *            perform the checks referred to in this interface javadoc
	 * @param shell
	 *            a <code>org.eclipse.swt.widgets.Shell</code> to aid in
	 *            UI-based validation or <code>null</code> if the validation
	 *            must be headless
	 * @return a status object that is either {@link IStatus#OK} or describes
	 *         the reason why remote creation is not possible
	 */
	public IStatus validateRemoteCreate(String name, Object shell);

	/**
	 * Creates a remote resource as member of this folder under the given name.
	 * <p>
	 * The resource is created in the remote repository and added to the members
	 * list of this folder.
	 * <p>
	 * Some other resources may be also added as result of this operation. In
	 * order to avoid lock extension, a proper create scheduling rule must be
	 * obtained before executing this operation.
	 * <p>
	 * Unless {@link ISemanticFileSystem#SUPPRESS_REFRESH} is specified in the
	 * options, this resource will be refreshed locally.
	 * 
	 * @param name
	 *            the name of the new member; note that this does not perform
	 *            the checks referred to in this interface javadoc
	 * @param context
	 *            an arbitrary context object to be passed to content provider,
	 *            may be <code>null</code>
	 * @param monitor
	 *            a progress monitor or <code>null</code> if progress reporting
	 *            is not desired
	 * @param options
	 *            only {@link ISemanticFileSystem#SUPPRESS_REFRESH} is supported
	 * @return the new resource
	 * @throws CoreException
	 *             in case of failure, e.g. when a resource with the same name
	 *             already exists
	 * 
	 * @see IWorkspace#getRuleFactory()
	 * @see IResourceRuleFactory#createRule(IResource)
	 */
	public ISemanticResource createResourceRemotely(String name, Object context, int options, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * Creates a remote file resource under the given name.
	 * <p>
	 * The file resource is created in the remote repository and added to the
	 * members list of this folder.
	 * <p>
	 * Some other resources may be also added as result of this operation. In
	 * order to avoid lock extension, a proper create scheduling rule must be
	 * obtained before executing this operation.
	 * <p>
	 * Unless {@link ISemanticFileSystem#SUPPRESS_REFRESH} is specified in the
	 * options, this resource will be refreshed locally.
	 * 
	 * @param name
	 *            the name of the new file member; please refer to this
	 *            interface javadoc about restrictions for names
	 * @param source
	 *            file content or <code>null</code> if no content is provided
	 * @param context
	 *            an arbitrary context object to be passed to content provider,
	 *            may be <code>null</code>
	 * @param options
	 *            only {@link ISemanticFileSystem#SUPPRESS_REFRESH} is supported
	 * @param monitor
	 *            a progress monitor or <code>null</code> if progress reporting
	 *            is not desired
	 * @return the new file resource
	 * @throws CoreException
	 *             in case of failure, e.g. when a resource with the same name
	 *             already exists
	 * 
	 * @see IWorkspace#getRuleFactory()
	 * @see IResourceRuleFactory#createRule(IResource)
	 */
	public ISemanticFile createFileRemotely(String name, InputStream source, Object context, int options, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * Adds a remote resource to the member list of the folder under the given
	 * name. The content provider decides whether the added resource will be a
	 * file or folder.
	 * <p>
	 * The resource is simply added to the members list of this folder. No
	 * remote content is created.
	 * <p>
	 * Some other resources may be also added as result of this operation. In
	 * order to avoid lock extension, a proper create scheduling rule must be
	 * obtained before executing this operation.
	 * <p>
	 * Unless {@link ISemanticFileSystem#SUPPRESS_REFRESH} is specified in the
	 * options, this resource will be refreshed locally.
	 * 
	 * @param name
	 *            the name of the member; note that this does not perform the
	 *            checks referred to in this interface javadoc
	 * @param options
	 *            only {@link ISemanticFileSystem#SUPPRESS_REFRESH} is supported
	 * @param monitor
	 *            a progress monitor or <code>null</code> if progress reporting
	 *            is not desired
	 * @return the member
	 * @throws CoreException
	 *             in case of failure, e.g. when a resource with the same name
	 *             already exists
	 * 
	 * @see IWorkspace#getRuleFactory()
	 * @see IResourceRuleFactory#createRule(IResource)
	 */
	public ISemanticResource addResource(String name, int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Adds a remote file resource to the member list of the folder under the
	 * given name. No remote content is created.
	 * <p>
	 * Some other resources may be also added as result of this operation. In
	 * order to avoid lock extension, a proper create scheduling rule must be
	 * obtained before executing this operation.
	 * <p>
	 * Unless {@link ISemanticFileSystem#SUPPRESS_REFRESH} is specified in the
	 * options, this resource will be refreshed locally.
	 * 
	 * @param name
	 *            the name of the member file; please refer to this interface
	 *            javadoc about restrictions for names
	 * @param options
	 *            only {@link ISemanticFileSystem#SUPPRESS_REFRESH} is supported
	 * @param monitor
	 *            a progress monitor, or <code>null</code> if progress reporting
	 *            is not desired
	 * @return the member file
	 * @throws CoreException
	 *             in case of failure, e.g. when a resource with the same name
	 *             already exists
	 * 
	 * @see IWorkspace#getRuleFactory()
	 * @see IResourceRuleFactory#createRule(IResource)
	 */
	public ISemanticFile addFile(String name, int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Adds a remote folder resource to the member list of the folder under the
	 * given name.
	 * <p>
	 * Some other resources may be also added as result of this operation. In
	 * order to avoid lock extension, a proper create scheduling rule must be
	 * obtained before executing this operation.
	 * <p>
	 * Unless {@link ISemanticFileSystem#SUPPRESS_REFRESH} is specified in the
	 * options, this resource will be refreshed locally.
	 * 
	 * @param name
	 *            the name of the member folder; please refer to this interface
	 *            javadoc about restrictions for names
	 * @param options
	 *            only {@link ISemanticFileSystem#SUPPRESS_REFRESH} is supported
	 * @param monitor
	 *            a progress monitor or <code>null</code> if progress reporting
	 *            is not desired
	 * @return the member folder
	 * @throws CoreException
	 *             in case of failure, e.g. when a resource with the same name
	 *             already exists
	 * 
	 * @see IWorkspace#getRuleFactory()
	 * @see IResourceRuleFactory#createRule(IResource)
	 */
	ISemanticFolder addFolder(String name, int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Adds a remote URI file resource to the member list of the folder under
	 * the given name.
	 * <p>
	 * The resource is simply added to members list of this folder.
	 * <p>
	 * This functionality is optional. The availability depends on capabilities
	 * of the content provider that is responsible for this semantic folder.
	 * Content providers that support this method must implement
	 * <code>ISemanticContentProviderREST</code>.
	 * <p>
	 * In order to avoid lock extension, a proper create scheduling rule must be
	 * obtained before executing this operation.
	 * <p>
	 * A <code>CoreException</code> with error code
	 * {@link SemanticResourceStatusCode#METHOD_NOT_SUPPORTED} will be thrown if
	 * the content provider doesn't support this method.
	 * <p>
	 * Files created using this method delegate the {@link IFile#getContents()}
	 * method to {@link URL#getContent()} after converting the URI provided into
	 * a URL using {@link URI#toURL()}.
	 * <p>
	 * Unless {@link ISemanticFileSystem#SUPPRESS_REFRESH} is specified in the
	 * options, this resource will be refreshed locally.
	 * 
	 * @param name
	 *            the string name of the member file; please refer to this
	 *            interface javadoc about restrictions for names
	 * @param uri
	 *            under which resource content can be accessed
	 *            (retrieved/persisted)
	 * @param options
	 *            only {@link ISemanticFileSystem#SUPPRESS_REFRESH} is supported
	 * @param monitor
	 *            may be null
	 * @return the member file
	 * @throws CoreException
	 *             in case of failure or when the functionality is not supported
	 *             by content provider or when a resource with the same name
	 *             already exists
	 * 
	 * @see IStatus#getCode()
	 * @see IWorkspace#getRuleFactory()
	 * @see IResourceRuleFactory#createRule(IResource)
	 */
	public ISemanticFile addFile(String name, URI uri, int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Adds a remote URI folder resource to the member list of the folder under
	 * the given name.
	 * <p>
	 * This functionality is optional. The availability depends on capabilities
	 * of the content provider that is responsible for this semantic folder.
	 * Content providers that support this method must implement
	 * <code>ISemanticContentProviderREST</code>.
	 * <p>
	 * In order to avoid lock extension, a proper create scheduling rule must be
	 * obtained before executing this operation.
	 * <p>
	 * A <code>CoreException</code> with error code
	 * {@link SemanticResourceStatusCode#METHOD_NOT_SUPPORTED} will be thrown if
	 * the content provider doesn't support this method.
	 * <p>
	 * Unless {@link ISemanticFileSystem#SUPPRESS_REFRESH} is specified in the
	 * options, this resource will be refreshed locally.
	 * 
	 * @param name
	 *            the string name of the member folder; please refer to this
	 *            interface javadoc about restrictions for names
	 * @param uri
	 *            under which resource content can be accessed
	 *            (retrieved/persisted)
	 * @param options
	 *            only {@link ISemanticFileSystem#SUPPRESS_REFRESH} is supported
	 * @param monitor
	 *            may be null
	 * @return the member folder
	 * @throws CoreException
	 *             in case of failure or when the functionality is not supported
	 *             by content provider or when a resource with the same name
	 *             already exists
	 * 
	 * @see IStatus#getCode()
	 * @see IWorkspace#getRuleFactory()
	 * @see IResourceRuleFactory#createRule(IResource)
	 */
	public ISemanticFolder addFolder(String name, URI uri, int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Adds a semantic folder resource to the member list of this folder under
	 * the given name.
	 * <p>
	 * Some other resources may be also added as result of this operation. In
	 * order to avoid lock extension, a proper create scheduling rule must be
	 * obtained before executing this operation.
	 * <p>
	 * Unless {@link ISemanticFileSystem#SUPPRESS_REFRESH} is specified in the
	 * options, this resource will be refreshed locally.
	 * 
	 * 
	 * @param name
	 *            the name of the member folder; please refer to this interface
	 *            javadoc about restrictions for names
	 * @param contentProviderID
	 *            id of a content provider extension
	 *            <code>org.eclipse.core.resources.semantic.spi.contentProvider</code>
	 * @param properties
	 *            custom properties that will be passed to the semantic content
	 *            provider registered under contentProviderID; may be
	 *            <code>null</code> if no such properties are required
	 * @param options
	 *            only {@link ISemanticFileSystem#SUPPRESS_REFRESH} is supported
	 * @param monitor
	 *            may be <code>null</code>
	 * @return the member folder
	 * @throws CoreException
	 *             in case of failure or when resource with the same name
	 *             already exists
	 * 
	 * @see IWorkspace#getRuleFactory()
	 * @see IResourceRuleFactory#createRule(IResource)
	 */
	public ISemanticFolder addFolder(String name, String contentProviderID, Map<QualifiedName, String> properties, int options,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Adds a semantic file resource to the member list of this folder under the
	 * given name. No remote content is created.
	 * <p>
	 * Some other resources may be also added as result of this operation. In
	 * order to avoid lock extension, a proper create scheduling rule must be
	 * obtained before executing this operation.
	 * <p>
	 * Unless {@link ISemanticFileSystem#SUPPRESS_REFRESH} is specified in the
	 * options, this resource will be refreshed locally.
	 * 
	 * 
	 * @param name
	 *            the name of the file; please refer to this interface javadoc
	 *            about restrictions for names
	 * @param contentProviderID
	 *            id of a content provider extension
	 *            <code>org.eclipse.core.resources.semantic.spi.contentProvider</code>
	 * @param properties
	 *            custom properties that will be passed to the semantic content
	 *            provider registered under contentProviderID; may be
	 *            <code>null</code> if no such properties are required
	 * @param options
	 *            only {@link ISemanticFileSystem#SUPPRESS_REFRESH} is supported
	 * @param monitor
	 *            may be <code>null</code>
	 * @return the member folder
	 * @throws CoreException
	 *             in case of failure or when resource with the same name
	 *             already exists
	 * 
	 * @see IWorkspace#getRuleFactory()
	 * @see IResourceRuleFactory#createRule(IResource)
	 */
	public ISemanticFile addFile(String name, String contentProviderID, Map<QualifiedName, String> properties, int options,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Checks if a member resource with the given name exists.
	 * 
	 * @param name
	 *            the name of the member to check
	 * @return <code>true</code> if the member with the specified name exists
	 * @throws CoreException
	 *             in case of failure
	 */
	public boolean hasResource(String name) throws CoreException;

	/**
	 * Returns the member with the given name.
	 * 
	 * @param name
	 *            the member to read
	 * @return the member, or <code>null</code> if no member with the given name
	 *         exists
	 * @throws CoreException
	 *             in case of failure
	 */
	public ISemanticResource getResource(String name) throws CoreException;

	/**
	 * Find URI
	 * 
	 * @param uri
	 * @param monitor
	 * @return list of resources
	 * @throws CoreException
	 */
	public IResource[] findURI(URI uri, IProgressMonitor monitor) throws CoreException;

}
