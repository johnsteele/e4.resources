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
package org.eclipse.core.internal.resources.semantic;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBFactory;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeNodeType;
import org.eclipse.core.internal.resources.semantic.util.ISemanticFileSystemLog;
import org.eclipse.core.internal.resources.semantic.util.ISemanticFileSystemTrace;
import org.eclipse.core.internal.resources.semantic.util.TraceLocation;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticResourceInfo;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderFederation;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderLocal;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderLocking;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderREST;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderRemote;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.ISemanticSpiResourceInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.osgi.util.NLS;

/**
 * The Semantic File Store implementation.
 * 
 */
public class SemanticFileStore extends SemanticProperties implements ISemanticFileStore, ISemanticFileStoreInternal {

	// we can't use the class name, since we don't have it in the class loader
	private final static String DEFAULT_CONTENT_PROVIDER_ID = "org.eclipse.core.resources.semantic.provider.DefaultContentProvider"; //$NON-NLS-1$
	private final ISemanticFileSystemTrace trace;
	private final ISemanticFileSystemLog log;
	private ISemanticContentProvider provider;

	private final static class TraceException extends Exception {

		private static final long serialVersionUID = 1L;

		public TraceException() {
			super(Messages.SemanticFileStore_TraceException_XMSG);
		}
	}

	SemanticFileStore(SemanticFileSystem fs, ResourceTreeNode node) {
		super(fs, node);
		this.trace = fs.getTrace();
		this.log = fs.getLog();
	}

	//
	// IFileStore/FileStore
	//
	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {

		try {
			this.fs.lockForRead();
			EList<ResourceTreeNode> children = this.node.getChildren();
			String[] names = new String[children.size()];
			int i = 0;

			for (ResourceTreeNode resourceTreeNode : children) {
				names[i] = resourceTreeNode.getName();
				i++;
			}
			return names;
		} finally {
			this.fs.unlockForRead();
		}
	}

	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		FileInfo info;
		ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();
		boolean askContentProviderForReadonly = false;
		boolean askContentProviderForTimestamp = false;

		try {
			this.fs.lockForRead();

			info = new SemanticFileInfo();

			info.setName(this.node.getName());
			info.setExists(this.node.isExists());
			TreeNodeType actType = this.node.getType();
			info.setDirectory(actType.equals(TreeNodeType.FOLDER) || actType.equals(TreeNodeType.PROJECT));

			if (info.isDirectory() || !this.node.isExists() || actType.equals(TreeNodeType.UNKNOWN)) {
				// see IFileInfo.getLastModified()
				info.setLastModified(EFS.NONE);
				// false is the fall back for non-existing resources,
				info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, false);
			} else {
				askContentProviderForTimestamp = true;

				// local-only resources are always writable (for the time being)
				if (!this.node.isLocalOnly()) {
					askContentProviderForReadonly = true;
				} else {
					info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, false);
				}
			}
		} finally {
			this.fs.unlockForRead();
		}

		// communicate with content providers outside of our lock to avoid
		// deadlocks
		if (askContentProviderForReadonly) {
			boolean readOnly = true;
			try {
				readOnly = effectiveProvider.fetchResourceInfo(this, ISemanticFileSystem.RESOURCE_INFO_READ_ONLY, monitor).isReadOnly();
			} catch (CoreException e) {
				this.trace.trace(TraceLocation.CONTENTPROVIDER, e);
			}
			info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, readOnly);
		}

		if (askContentProviderForTimestamp) {
			try {
				info.setLastModified(effectiveProvider.getResourceTimestamp(this, monitor));
			} catch (CoreException e) {
				// we don't want to crash everything if the resource is not
				// accessible
				info.setLastModified(EFS.NONE);
				this.trace.trace(TraceLocation.CONTENTPROVIDER, e);
			}
		}

		return info;
	}

	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {

		if (TraceLocation.CORE.isActive()) {
			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(
					Messages.SemanticFileStore_UpdateFileInfo_XMSG, getPath().toString()), new TraceException());
			this.trace.trace(TraceLocation.CORE, stat);
		}

		if ((options & EFS.SET_ATTRIBUTES) != 0) {
			// we ignore Archive, Hidden, and Executable here
			getEffectiveContentProvider().setReadOnly(this, info.getAttribute(EFS.ATTRIBUTE_READ_ONLY), monitor);
		}
		if ((options & EFS.SET_LAST_MODIFIED) != 0) {
			long timestamp = info.getLastModified();
			getEffectiveContentProvider().setResourceTimestamp(this, timestamp, monitor);
		}
	}

	private static ISemanticContentProvider initProvider(String contentProviderID, ISemanticFileStore store) throws CoreException {

		ISemanticContentProvider actProvider = SemanticFileSystemCore.getInstance().getContentProviderFactory(contentProviderID)
				.createContentProvider();

		actProvider.setRootStore(store);

		((SemanticFileStore) store).setProvider(actProvider);

		return actProvider;
	}

	public ISemanticContentProvider getEffectiveContentProvider() throws CoreException {

		String contentProviderID;
		ISemanticFileStore parentStore;
		String parentContentProviderId = null;

		try {
			this.fs.lockForWrite();
			// TODO 0.1: check if we can use more fine grained locks here
			if (this.provider != null) {
				return this.provider;
			}

			contentProviderID = this.node.getTemplateID();
			if (contentProviderID == null) {
				// walk up to find a contentProviderID
				ResourceTreeNode parent = this.node.getParent();
				ResourceTreeNode oldParent = this.node;

				while (parent != null) {
					parentContentProviderId = parent.getTemplateID();
					if (parentContentProviderId != null) {
						break;
					}
					oldParent = parent;

					parent = parent.getParent();
				}
				if (parentContentProviderId == null) {
					parentContentProviderId = SemanticFileStore.DEFAULT_CONTENT_PROVIDER_ID;
					parent = oldParent;
				}
				// construct the parent
				parentStore = this.fs.getStore(parent);

				// the first parent with a provider ID
				ISemanticContentProvider parentProvider = initProvider(parentContentProviderId, parentStore);

				return parentProvider;

			}
			initProvider(contentProviderID, this);
			return this.provider;

		} finally {
			this.fs.unlockForWrite();
		}

	}

	public File toLocalFile(int options, IProgressMonitor monitor) throws CoreException {
		// some tools are operating directly on the file system; this enables
		// these tools
		// for content providers that have a file system cache by delegating to
		// this cache
		// non-caching content providers can not support this
		// TODO 0.1: think of a more generic solution
		if (options != EFS.CACHE) {
			ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();
			if (effectiveProvider instanceof ISemanticContentProviderLocal) {
				return ((ISemanticContentProviderLocal) effectiveProvider).toLocalFile(this);
			}
			// TODO 0.1: check exception handling in tools, adjust error message
			return null;

		}
		return super.toLocalFile(options, monitor);

	}

	public IFileStore getChild(String name) {

		if (TraceLocation.CORE_VERBOSE.isActive()) {
			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(Messages.SemanticFileStore_GetChild_XMSG,
					name, getPath().toString()), new TraceException());
			this.trace.trace(TraceLocation.CORE_VERBOSE, stat);
		}

		IFileStore store = findChild(name);

		if (store != null) {
			return store;
		}

		ISemanticContentProvider effectiveProvider;

		try {
			effectiveProvider = getEffectiveContentProvider();
		} catch (CoreException ce) {
			this.log.log(ce);
			return EFS.getNullFileSystem().getStore(getPath().append(name));
		}
		// no child found, let's check whether child can be created

		// TODO 0.1: concurrency we need to detect and handle broken parent hierarchy
		// situations
		// due to concurrency (e.g. parent was deleted in another thread)

		// check if there is federation

		String federatedContentProviderId = null;

		if (effectiveProvider instanceof ISemanticContentProviderFederation) {
			federatedContentProviderId = ((ISemanticContentProviderFederation) effectiveProvider).getFederatedProviderIDForPath(getPath()
					.append(name));
		}

		SemanticFileStore result;
		try {
			this.fs.lockForWrite();

			ResourceTreeNode child = checkChildExists(name);

			ResourceTreeNode newnode = createLocalChildNode(name, child, federatedContentProviderId);

			result = this.fs.getStore(newnode);

			this.fs.requestFlush(false);

		} catch (CoreException e) {
			this.trace.trace(TraceLocation.CORE, e);
			return EFS.getNullFileSystem().getStore(getPath().append(name));
		} finally {
			this.fs.unlockForWrite();
		}

		if (result != null) {
			// notify the content provider
			effectiveProvider.onImplicitStoreCreate(result);

			if (federatedContentProviderId != null) {
				// federation
				try {
					result.getEffectiveContentProvider().onImplicitStoreCreate(result);
				} catch (CoreException e) {
					// $JL-EXC$ ignore and just trace
					this.trace.trace(TraceLocation.CONTENTPROVIDER, e);
				}
			}
		}

		return result;

	}

	public String getName() {
		try {
			this.fs.lockForRead();
			return this.node.getName();
		} finally {
			this.fs.unlockForRead();
		}
	}

	public IFileStore getParent() {
		try {
			this.fs.lockForRead();
			if (this.node.getParent() != null) {
				return this.fs.getStore(this.node.getParent());
			}
			return null;

		} finally {
			this.fs.unlockForRead();
		}
	}

	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
		try {
			this.fs.lockForRead();
			TreeNodeType type = this.node.getType();
			if (type != TreeNodeType.FILE) {
				throw new SemanticResourceException(SemanticResourceStatusCode.INVALID_RESOURCE_TYPE, getPath(),
						Messages.SemanticFileStore_OpenInputOnlyOnFiles_XMSG);
			}
		} finally {
			this.fs.unlockForRead();
		}

		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (TraceLocation.CONTENTPROVIDER.isActive()) {
			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(
					Messages.SemanticFileStore_OpeningInputInfo_XMSG, effectiveProvider.getClass().getName(), getPath().toString()),
					new TraceException());
			this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
		}

		return effectiveProvider.openInputStream(this, monitor);
	}

	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
		// TODO 0.1: concurrency/race condition with IFile.getContent()
		try {
			this.fs.lockForRead();
			TreeNodeType type = this.node.getType();
			if (type != TreeNodeType.FILE && type != TreeNodeType.UNKNOWN) {
				throw new SemanticResourceException(SemanticResourceStatusCode.INVALID_RESOURCE_TYPE, getPath(),
						Messages.SemanticFileStore_OpenOutputNotOnFolders_XMSG);
			}
		} finally {
			this.fs.unlockForRead();
		}

		ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();
		boolean append = (options & EFS.APPEND) != 0;

		if (TraceLocation.CONTENTPROVIDER.isActive()) {
			String message;
			if (append) {
				message = NLS.bind(Messages.SemanticFileStore_AppendingInfo_XMSG, effectiveProvider.getClass().getName(), getPath()
						.toString());
			} else {
				message = NLS.bind(Messages.SemanticFileStore_OpeningInfo_XMSG, effectiveProvider.getClass().getName(), getPath()
						.toString());
			}
			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, message, new TraceException());
			this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
		}

		ISemanticSpiResourceInfo info = effectiveProvider.fetchResourceInfo(this, ISemanticFileSystem.RESOURCE_INFO_READ_ONLY, monitor);
		if (info.isReadOnly()) {
			IStatus status = effectiveProvider.validateSave(this);
			if (!status.isOK()) {
				throw new CoreException(status);
			}
		}

		int actOptions = ISemanticFileSystem.NONE;
		if (append) {
			actOptions = ISemanticFileSystem.CONTENT_APPEND;
		}
		OutputStream os = effectiveProvider.openOutputStream(this, actOptions, monitor);

		try {

			this.fs.lockForWrite();

			boolean changeRequired = !this.node.isExists() || this.node.getType() != TreeNodeType.FILE;
			if (changeRequired) {
				this.node.setExists(true);
				this.node.setType(TreeNodeType.FILE);
				try {
					this.fs.requestFlush(false);
				} catch (CoreException e) {
					Util.safeClose(os);
					throw e;
				}
			}
		} finally {
			this.fs.unlockForWrite();
		}

		return os;
	}

	public URI toURI() {
		try {
			this.fs.lockForRead();
			try {
				return new URI(ISemanticFileSystem.SCHEME, null, getPath().toString(), null);
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		} finally {
			this.fs.unlockForRead();
		}
	}

	public SemanticFileSystem getFileSystem() {
		return this.fs;
	}

	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {

		if (TraceLocation.CORE_VERBOSE.isActive()) {
			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(Messages.SemanticFileStore_MkDir_XMSG,
					getPath().toString()), new TraceException());
			this.trace.trace(TraceLocation.CORE_VERBOSE, stat);
		}

		try {
			this.fs.lockForWrite();

			ResourceTreeNode parent = this.node.getParent();

			if ((options & EFS.SHALLOW) != 0) {
				if (parent != null && !parent.isExists()) {
					throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_PARENT_DOESNT_EXIST, getPath(),
							Messages.SemanticFileStore_ShallowMkDirFailed_XMSG);
				}
			} else {
				if (parent != null && !parent.isExists()) {
					mkdir(parent);
				}
			}
			makeFolder(this.node);

		} finally {
			this.fs.unlockForWrite();
		}

		return this;
	}

	private static void mkdir(ResourceTreeNode actNode) throws CoreException {

		ResourceTreeNode parent = actNode.getParent();
		if (parent != null && !parent.isExists()) {
			mkdir(parent);
		}

		makeFolder(actNode);

	}

	private static void makeFolder(ResourceTreeNode actNode) throws CoreException {

		if (actNode.getType() == TreeNodeType.FILE) {
			// wrong type encountered
			SemanticFileSystem fs = (SemanticFileSystem) EFS.getFileSystem(ISemanticFileSystem.SCHEME);
			throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, fs.getStore(actNode).getPath(),
					Messages.SemanticFileStore_MkDirOnFile_XMSG);
		}
		if (!actNode.isExists()) {
			// TODO 0.1: this should be intercepted by the content provider to
			// avoid unwanted folder creation

			actNode.setExists(true);
		}

		if (actNode.getType() != TreeNodeType.PROJECT) {
			// set type folder if it is not already a project
			actNode.setType(TreeNodeType.FOLDER);
		}
	}

	//
	// ISemantiFileStoreInternal
	//
	public void createFileRemotely(String name, InputStream source, Object context, IProgressMonitor monitor) throws CoreException {
		this.mkdir(EFS.NONE, monitor);
		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (TraceLocation.CONTENTPROVIDER.isActive()) {
			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, MessageFormat.format(
					Messages.SemanticFileStore_CreateFileRemote_XMSG, name, effectiveProvider.getClass().getName(), getPath().toString()),
					new TraceException());
			this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
		}

		if (effectiveProvider instanceof ISemanticContentProviderRemote) {
			((ISemanticContentProviderRemote) effectiveProvider).createFileRemotely(this, name, source, context, monitor);
		} else {
			throw new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, getPath(), NLS.bind(
					Messages.SemanticFileStore_IntefaceNotImplemented_XMSG, effectiveProvider.getClass().getName(),
					ISemanticContentProviderRemote.class.getName()));
		}

	}

	public ISemanticFileStoreInternal createResourceRemotely(String name, Object context, IProgressMonitor monitor) throws CoreException {
		this.mkdir(EFS.NONE, monitor);
		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (TraceLocation.CONTENTPROVIDER.isActive()) {
			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, MessageFormat.format(
					Messages.SemanticFileStore_CreateResourceRemtoe_XMSG, name, effectiveProvider.getClass().getName(), getPath()
							.toString()), new TraceException());
			this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
		}

		if (effectiveProvider instanceof ISemanticContentProviderRemote) {
			((ISemanticContentProviderRemote) effectiveProvider).createResourceRemotely(this, name, context, monitor);
		} else {
			throw new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, getPath(), NLS.bind(
					Messages.SemanticFileStore_IntefaceNotImplemented_XMSG, effectiveProvider.getClass().getName(),
					ISemanticContentProviderRemote.class.getName()));
		}

		return findChild(name);
	}

	public void addFileFromRemote(String name, IProgressMonitor monitor) throws CoreException {
		this.mkdir(EFS.NONE, monitor);
		ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();
		if (TraceLocation.CONTENTPROVIDER.isActive()) {
			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, MessageFormat.format(
					Messages.SemanticFileStore_AddFileRemote_XMSG, name, effectiveProvider.getClass().getName(), getPath().toString()),
					new TraceException());
			this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
		}

		// delegate to content provider
		effectiveProvider.addResource(this, name, ResourceType.FILE_TYPE, monitor);

	}

	public void addFileFromRemoteByURI(String name, URI uri, IProgressMonitor monitor) throws CoreException {
		this.mkdir(EFS.NONE, monitor);
		ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		// delegate to content provider
		if (effectiveProvider instanceof ISemanticContentProviderREST) {

			if (TraceLocation.CONTENTPROVIDER.isActive()) {
				IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, MessageFormat.format(
						Messages.SemanticFileStore_AddFileRemoteURI_XMSG, name, uri.toString(), effectiveProvider.getClass().getName(),
						getPath().toString()), new TraceException());
				this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
			}

			((ISemanticContentProviderREST) effectiveProvider).addFileFromRemoteByURI(this, name, uri, monitor);

		} else {
			throw new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, getPath(), NLS.bind(
					Messages.SemanticFileStore_IntefaceNotImplemented_XMSG, effectiveProvider.getClass().getName(),
					ISemanticContentProviderREST.class.getName()));
		}

	}

	public void addFolderFromRemoteByURI(String name, URI uri, IProgressMonitor monitor) throws CoreException {
		this.mkdir(EFS.NONE, monitor);
		ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		// delegate to content provider
		if (effectiveProvider instanceof ISemanticContentProviderREST) {

			if (TraceLocation.CONTENTPROVIDER.isActive()) {
				IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, MessageFormat.format(
						Messages.SemanticFileStore_AddFileRemoteURI_XMSG, name, uri.toString(), effectiveProvider.getClass().getName(),
						getPath().toString()), new TraceException());
				this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
			}

			((ISemanticContentProviderREST) effectiveProvider).addFolderFromRemoteByURI(this, name, uri, monitor);

		} else {
			throw new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, getPath(), NLS.bind(
					Messages.SemanticFileStore_IntefaceNotImplemented_XMSG, effectiveProvider.getClass().getName(),
					ISemanticContentProviderREST.class.getName()));
		}

	}

	public void addFolderFromRemote(String name, IProgressMonitor monitor) throws CoreException {
		this.mkdir(EFS.NONE, monitor);
		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (TraceLocation.CONTENTPROVIDER.isActive()) {
			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, MessageFormat.format(
					Messages.SemanticFileStore_AddFolderRemote_XMSG, name, effectiveProvider.getClass().getName(), getPath().toString()),
					new TraceException());
			this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
		}

		// delegate to content provider
		effectiveProvider.addResource(this, name, ResourceType.FOLDER_TYPE, monitor);

	}

	public ISemanticFileStoreInternal addResourceFromRemote(String name, IProgressMonitor monitor) throws CoreException {
		this.mkdir(EFS.NONE, monitor);
		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (TraceLocation.CONTENTPROVIDER.isActive()) {
			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, MessageFormat.format(
					Messages.SemanticFileStore_AddResourceRemote_XMSG, name, effectiveProvider.getClass().getName(), getPath().toString()),
					new TraceException());
			this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
		}

		// delegate to content provider
		effectiveProvider.addResource(this, name, ResourceType.UNKNOWN_TYPE, monitor);

		return findChild(name);
	}

	public void addResource(String name, boolean asFolder, String contentProviderID, Map<QualifiedName, String> properties,
			IProgressMonitor monitor) throws CoreException {
		this.mkdir(EFS.NONE, monitor);
		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (effectiveProvider instanceof ISemanticContentProviderFederation) {
			((ISemanticContentProviderFederation) effectiveProvider).addResource(this, name, asFolder ? ResourceType.FOLDER_TYPE
					: ResourceType.FILE_TYPE, contentProviderID, properties);
		} else {
			throw new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, getPath(), NLS.bind(
					Messages.SemanticFileStore_IntefaceNotImplemented_XMSG, effectiveProvider.getClass().getName(),
					ISemanticContentProviderFederation.class.getName()));
		}

	}

	public void delete(int options, IProgressMonitor monitor) throws CoreException {
		// this will be called instead of delete hook if no team provider is
		// found
		// or if resource is marked as derived
		// TODO 0.1: review this behavior e.g. with respect to linked resources

		removeFromWorkspace(monitor);

	}

	public void deleteRemotely(IProgressMonitor monitor) throws CoreException {
		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (TraceLocation.CONTENTPROVIDER.isActive()) {

			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(
					Messages.SemanticFileStore_DeleteResourceRemote_XMSG, getPath().toString(), effectiveProvider.getClass().getName()),
					new TraceException());

			this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
		}

		if (effectiveProvider instanceof ISemanticContentProviderRemote) {
			((ISemanticContentProviderRemote) effectiveProvider).deleteRemotely(this, monitor);
		} else {
			throw new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, getPath(), NLS.bind(
					Messages.SemanticFileStore_IntefaceNotImplemented_XMSG, effectiveProvider.getClass().getName(),
					ISemanticContentProviderRemote.class.getName()));
		}
	}

	public void removeFromWorkspace(IProgressMonitor monitor) throws CoreException {
		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (TraceLocation.CONTENTPROVIDER.isActive()) {
			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(
					Messages.SemanticFileStore_RemoveResourceRemote_XMSG, getPath().toString(), effectiveProvider.getClass().getName()),
					new TraceException());
			this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
		}

		effectiveProvider.removeResource(this, monitor);
	}

	public void synchronizeContentWithRemote(SyncDirection direction, IProgressMonitor monitor) throws CoreException {
		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (TraceLocation.CONTENTPROVIDER.isActive()) {

			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(
					Messages.SemanticFileStore_SynchContent_XMSG, effectiveProvider.getClass().getName(), getPath().toString()),
					new TraceException());
			this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
		}
		MultiStatus status = new MultiStatus(SemanticResourcesPlugin.PLUGIN_ID, IStatus.OK, NLS.bind(
				Messages.SemanticFileStore_SyncContent_XGRP, getPath().toString()), null);
		effectiveProvider.synchronizeContentWithRemote(this, direction, monitor, status);
		if (!status.isOK()) {
			throw new CoreException(status);
		}
	}

	public void setRemoteURI(URI uri, IProgressMonitor monitor) throws CoreException {
		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		// delegate to content provider
		if (effectiveProvider instanceof ISemanticContentProviderREST) {

			if (TraceLocation.CONTENTPROVIDER.isActive()) {
				IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, MessageFormat.format(
						Messages.SemanticFileStore_SettingURI_XMSG, uri.toString(), effectiveProvider.getClass().getName(), getPath()
								.toString()), new TraceException());
				this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
			}

			((ISemanticContentProviderREST) effectiveProvider).setURIString(this, uri, monitor);

		} else {
			throw new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, getPath(), NLS.bind(
					Messages.SemanticFileStore_IntefaceNotImplemented_XMSG, effectiveProvider.getClass().getName(),
					ISemanticContentProviderREST.class.getName()));
		}
	}

	public void revertChanges(IProgressMonitor monitor) throws CoreException {
		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (TraceLocation.CONTENTPROVIDER.isActive()) {

			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(Messages.SemanticFileStore_Revert_XMSG,
					getPath().toString(), effectiveProvider.getClass().getName()), new TraceException());

			this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
		}

		effectiveProvider.revertChanges(this, monitor);
	}

	public IStatus lockResource(IProgressMonitor monitor) throws CoreException {
		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (TraceLocation.CONTENTPROVIDER.isActive()) {
			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(Messages.SemanticFileStore_Locking_XMSG,
					getPath().toString(), effectiveProvider.getClass().getName()), new TraceException());
			this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
		}

		if (effectiveProvider instanceof ISemanticContentProviderLocking) {
			return ((ISemanticContentProviderLocking) effectiveProvider).lockResource(this, monitor);
		}

		return new Status(IStatus.ERROR, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(
				Messages.SemanticFileStore_IntefaceNotImplemented_XMSG, effectiveProvider.getClass().getName(),
				ISemanticContentProviderLocking.class.getName()));
	}

	public IStatus unlockResource(IProgressMonitor monitor) throws CoreException {
		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (TraceLocation.CONTENTPROVIDER.isActive()) {
			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(Messages.SemanticFileStore_Unlocking_XMSG,
					getPath().toString(), effectiveProvider.getClass().getName()), new TraceException());
			this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
		}

		if (effectiveProvider instanceof ISemanticContentProviderLocking) {
			return ((ISemanticContentProviderLocking) effectiveProvider).unlockResource(this, monitor);
		}

		return new Status(IStatus.ERROR, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(
				Messages.SemanticFileStore_IntefaceNotImplemented_XMSG, effectiveProvider.getClass().getName(),
				ISemanticContentProviderLocking.class.getName()));
	}

	public IStatus validateRemoteCreate(String name, Object shell) {
		ISemanticContentProvider effectiveProvider;
		try {
			effectiveProvider = getEffectiveContentProvider();
		} catch (CoreException ce) {
			this.log.log(ce);
			return ce.getStatus();
		}

		if (TraceLocation.CONTENTPROVIDER.isActive()) {

			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, MessageFormat.format(
					Messages.SemanticFileStore_ValidateRemoteCreate_XMSG, name, effectiveProvider.getClass().getName(), getPath()
							.toString()), new TraceException());

			this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
		}

		if (effectiveProvider instanceof ISemanticContentProviderRemote) {
			return ((ISemanticContentProviderRemote) effectiveProvider).validateRemoteCreate(this, name, shell);
		}

		return new Status(IStatus.ERROR, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(
				Messages.SemanticFileStore_IntefaceNotImplemented_XMSG, effectiveProvider.getClass().getName(),
				ISemanticContentProviderRemote.class.getName()));
	}

	public IStatus validateRemoteDelete(Object shell) {
		boolean canBeDeleted;
		ISemanticContentProvider effectiveProvider;
		try {
			effectiveProvider = getEffectiveContentProvider();

		} catch (CoreException e) {
			this.log.log(e);
			return e.getStatus();
		}

		try {
			this.fs.lockForRead();
			canBeDeleted = this.node.isExists();

		} finally {
			this.fs.unlockForRead();
		}

		if (canBeDeleted) {
			if (TraceLocation.CONTENTPROVIDER.isActive()) {
				IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID,
						MessageFormat.format(Messages.SemanticFileStore_ValidateRemoteDelete_XMSG, getPath().toString(), effectiveProvider
								.getClass().getName()), new TraceException());
				this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
			}

			if (effectiveProvider instanceof ISemanticContentProviderRemote) {
				return ((ISemanticContentProviderRemote) effectiveProvider).validateRemoteDelete(this, shell);
			}

			return new Status(IStatus.ERROR, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(
					Messages.SemanticFileStore_IntefaceNotImplemented_XMSG, effectiveProvider.getClass().getName(),
					ISemanticContentProviderRemote.class.getName()));
		}
		return new Status(IStatus.CANCEL, SemanticResourcesPlugin.PLUGIN_ID, null);
	}

	public IStatus validateRemove(int options, IProgressMonitor monitor) {
		boolean canBeDeleted;
		ISemanticContentProvider effectiveProvider;
		try {
			effectiveProvider = getEffectiveContentProvider();

		} catch (CoreException e) {
			this.log.log(e);
			return e.getStatus();
		}

		try {
			this.fs.lockForRead();
			canBeDeleted = this.node.isExists();

		} finally {
			this.fs.unlockForRead();
		}

		if (canBeDeleted) {
			if (TraceLocation.CONTENTPROVIDER.isActive()) {
				IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID,
						MessageFormat.format(Messages.SemanticFileStore_ValidateRemoteDelete_XMSG, getPath().toString(), effectiveProvider
								.getClass().getName()), new TraceException());
				this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
			}
			try {
				return effectiveProvider.validateRemove(this, options, monitor);
			} catch (CoreException e) {
				this.log.log(e);
				return e.getStatus();
			}

		}
		return new Status(IStatus.CANCEL, SemanticResourcesPlugin.PLUGIN_ID, null);
	}

	public void addChildFolder(String name) throws CoreException {

		if (TraceLocation.CORE.isActive()) {
			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(
					Messages.SemanticFileStore_AddChildFolder_XMSG, name, getPath().toString()), new TraceException());
			this.trace.trace(TraceLocation.CORE, stat);
		}

		try {
			this.fs.lockForWrite();
			ResourceTreeNode child = checkChildExists(name);

			createChildNode(name, true, child, null);

			this.fs.requestFlush(false);
		} finally {
			this.fs.unlockForWrite();
		}
	}

	public void addChildResource(String name, boolean asFolder, String contentProviderID, Map<QualifiedName, String> properties)
			throws CoreException {

		if (TraceLocation.CORE.isActive()) {
			IStatus stat;
			if (asFolder) {
				stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(
						Messages.SemanticFileStore_AddContentProviderRootFolder_XMSG, name, getPath().toString()), new TraceException());
			} else {
				stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(
						Messages.SemanticFileStore_AddContentProviderRootFile_XMSG, name, getPath().toString()), new TraceException());
			}

			this.trace.trace(TraceLocation.CORE, stat);
		}

		try {
			this.fs.lockForWrite();
			ResourceTreeNode oldchild = checkChildExists(name);
			ResourceTreeNode child = createChildNode(name, asFolder, oldchild, contentProviderID);

			HashMap<String, String> propsMap = new HashMap<String, String>();
			if (properties != null && !properties.isEmpty()) {
				for (Map.Entry<QualifiedName, String> entry : properties.entrySet()) {
					propsMap.put(Util.qualifiedNameToString(entry.getKey()), entry.getValue());
				}
				child.setPersistentProperties(propsMap);
			} else {
				// we don't use an empty map, but null
				child.setPersistentProperties(null);
			}
			this.fs.requestFlush(false);

		} finally {
			this.fs.unlockForWrite();
		}
	}

	public void addChildFile(String name) throws CoreException {

		if (TraceLocation.CORE.isActive()) {
			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(
					Messages.SemanticFileStore_AddChildFile_XMSG, name, getPath().toString()), new TraceException());
			this.trace.trace(TraceLocation.CORE, stat);
		}

		try {
			this.fs.lockForWrite();
			ResourceTreeNode child = checkChildExists(name);

			createChildNode(name, false, child, null);

			this.fs.requestFlush(false);
		} finally {
			this.fs.unlockForWrite();
		}
	}

	public void addLocalChildResource(String name, String contentProviderID) throws CoreException {

		if (TraceLocation.CORE.isActive()) {
			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(
					Messages.SemanticFileStore_AddLocalChild_XMSG, name, getPath().toString()), new TraceException());
			this.trace.trace(TraceLocation.CORE, stat);
		}

		try {
			this.fs.lockForWrite();
			ResourceTreeNode child = checkChildExists(name);

			createLocalChildNode(name, child, contentProviderID);

			this.fs.requestFlush(false);
		} finally {
			this.fs.unlockForWrite();
		}
	}

	public boolean hasResource(String name) {
		try {
			this.fs.lockForRead();
			EList<ResourceTreeNode> children = this.node.getChildren();
			for (ResourceTreeNode resourceTreeNode : children) {
				if (resourceTreeNode.getName().equals(name)) {
					return resourceTreeNode.isExists();
				}
			}
			return false;
		} finally {
			this.fs.unlockForRead();
		}
	}

	public String getContentProviderID() {
		try {
			this.fs.lockForRead();
			return this.node.getTemplateID();
		} finally {
			this.fs.unlockForRead();
		}
	}

	public String getRemoteURIString() throws CoreException {
		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (effectiveProvider instanceof ISemanticContentProviderREST) {
			return ((ISemanticContentProviderREST) effectiveProvider).getURIString(this);
		}
		throw new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, getPath(), NLS.bind(
				Messages.SemanticFileStore_IntefaceNotImplemented_XMSG, effectiveProvider.getClass().getName(),
				ISemanticContentProviderREST.class.getName()));

	}

	public boolean isExists() {
		try {
			this.fs.lockForRead();
			return this.node.isExists();
		} finally {
			this.fs.unlockForRead();
		}
	}

	public boolean isLocalOnly() {
		try {
			this.fs.lockForRead();
			return this.node.isLocalOnly();
		} finally {
			this.fs.unlockForRead();
		}
	}

	//
	// private section
	// 
	/**
	 * @param name
	 */
	private SemanticFileStore findChild(String name) {
		try {
			this.fs.lockForRead();
			EList<ResourceTreeNode> children = this.node.getChildren();

			for (ResourceTreeNode resourceTreeNode : children) {
				if (resourceTreeNode.getName().equals(name)) {
					return this.fs.getStore(resourceTreeNode);
				}
			}
		} finally {
			this.fs.unlockForRead();
		}
		return null;
	}

	private ResourceTreeNode createChildNode(String name, boolean isFolder, ResourceTreeNode previous, String contentProviderID) {
		// all callers have a lock, so we don't lock here
		ResourceTreeNode child;

		if (previous != null) {
			child = previous;
		} else {
			child = SemanticResourceDBFactory.eINSTANCE.createResourceTreeNode();
		}

		child.setName(name);
		child.setTemplateID(contentProviderID);

		if (isFolder) {
			child.setType(TreeNodeType.FOLDER);
			// folders are always local-only
			child.setLocalOnly(true);
		} else {
			child.setType(TreeNodeType.FILE);
			child.setLocalOnly(false);
		}

		child.setExists(true);
		child.setParent(this.node);

		return child;
	}

	private ResourceTreeNode createLocalChildNode(String name, ResourceTreeNode previous, String contentProviderID) {
		// all callers have a lock, so we don't lock here
		ResourceTreeNode child;

		if (previous != null) {
			child = previous;
		} else {
			child = SemanticResourceDBFactory.eINSTANCE.createResourceTreeNode();
		}

		child.setName(name);
		child.setTemplateID(contentProviderID);
		child.setType(TreeNodeType.UNKNOWN);
		child.setExists(false); // needed to comply with resource creation
		// behavior
		child.setParent(this.node);
		child.setLocalOnly(true);

		return child;
	}

	/*
	 * private ResourceTreeNode createPhantomNode(String name, boolean
	 * isDirectory, URI uri) { ResourceTreeNode child =
	 * SemanticResourceDBFactory.eINSTANCE.createResourceTreeNode();
	 * 
	 * child.setName(name); child.setIsDirectory(isDirectory);
	 * child.setExists(false); child.setPhantom(true); child.setReadOnly(false);
	 * child.setParent(node); child.setLastModified(System.currentTimeMillis());
	 * 
	 * if ( uri != null ) { child.setRemoteURI(uri.toString()); } return child;
	 * }
	 */
	/**
	 * throws exception if child exists returns child node if node present but
	 * not exists
	 */
	private ResourceTreeNode checkChildExists(String name) throws CoreException {
		// all callers have obtained a lock, so we don't lock here
		EList<ResourceTreeNode> children = this.node.getChildren();

		for (ResourceTreeNode resourceTreeNode : children) {
			if (resourceTreeNode.getName().equals(name)) {
				if (resourceTreeNode.isExists()) {
					IPath newPath = getPath().append(name);
					throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_ALREADY_EXISTS, newPath, NLS.bind(
							Messages.SemanticFileStore_ResourceWithPathExists_XMSG, newPath.toString()));
				}
				return resourceTreeNode;

			}
		}
		return null;
	}

	private void removeChild(String name) throws CoreException {

		try {
			this.fs.lockForWrite();
			EList<ResourceTreeNode> children = this.node.getChildren();

			for (ResourceTreeNode resourceTreeNode : children) {
				if (resourceTreeNode.getName().equals(name)) {
					children.remove(resourceTreeNode);

					resourceTreeNode.setExists(false);

					this.fs.requestURILocatorRebuild();
					break;
				}
			}

			this.fs.requestFlush(false);
		} finally {
			this.fs.unlockForWrite();
		}
	}

	public IStatus validateEdit(Object shell) {
		boolean canBeEdited;
		ISemanticContentProvider effectiveProvider;
		try {
			effectiveProvider = getEffectiveContentProvider();
		} catch (CoreException e) {
			this.log.log(e);
			return e.getStatus();
		}

		try {
			this.fs.lockForRead();
			// exist could be false if node was created, but no content was
			// written yet
			canBeEdited = this.node.isExists() && this.node.getType().equals(TreeNodeType.FILE);

		} finally {
			this.fs.unlockForRead();
		}

		if (canBeEdited) {

			boolean readOnly;

			try {
				readOnly = effectiveProvider.fetchResourceInfo(this, ISemanticFileSystem.RESOURCE_INFO_READ_ONLY, null).isReadOnly();
			} catch (CoreException e) {
				this.log.log(e);
				return e.getStatus();
			}

			if (readOnly) {

				if (TraceLocation.CONTENTPROVIDER.isActive()) {
					IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(
							Messages.SemanticFileStore_ValidateEdit_XMSG, effectiveProvider.getClass().getName(), getPath().toString()),
							new TraceException());
					this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
				}

				return effectiveProvider.validateEdit(new ISemanticFileStore[] { this }, shell);
			}
			// already checked out
			return new Status(IStatus.OK, SemanticResourcesPlugin.PLUGIN_ID, null);

		}
		return new Status(IStatus.CANCEL, SemanticResourcesPlugin.PLUGIN_ID, null);

	}

	public IStatus validateSave() {
		boolean canBeSaved;
		ISemanticContentProvider effectiveProvider;
		try {
			effectiveProvider = getEffectiveContentProvider();
		} catch (CoreException e) {
			this.log.log(e);
			return e.getStatus();
		}

		try {
			this.fs.lockForRead();
			canBeSaved = this.node.isExists() && this.node.getType().equals(TreeNodeType.FILE);

		} finally {
			this.fs.unlockForRead();
		}

		if (canBeSaved) {
			boolean readOnly;
			try {
				readOnly = effectiveProvider.fetchResourceInfo(this, ISemanticFileSystem.RESOURCE_INFO_READ_ONLY, null).isReadOnly();
			} catch (CoreException e) {
				this.log.log(e);
				return e.getStatus();
			}
			if (!readOnly) {
				if (TraceLocation.CONTENTPROVIDER.isActive()) {
					IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(
							Messages.SemanticFileStore_ValidateSave_XMSG, effectiveProvider.getClass().getName(), getPath().toString()),
							new TraceException());
					this.trace.trace(TraceLocation.CONTENTPROVIDER, stat);
				}
				return effectiveProvider.validateSave(this);
			}
			// not checked out
			return new Status(IStatus.CANCEL, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(Messages.SemanticFileStore_NotWritable_XMSG,
					getPath().toString()));
		}
		return new Status(IStatus.CANCEL, SemanticResourcesPlugin.PLUGIN_ID, null);

	}

	public IPath getPath() {

		try {
			this.fs.lockForRead();

			StringBuilder sb = new StringBuilder(50);
			sb.append('/');
			sb.append(this.node.getName());
			ResourceTreeNode parent = this.node.getParent();
			while (parent != null) {
				sb.insert(0, parent.getName());
				sb.insert(0, '/');
				parent = parent.getParent();
			}
			return new Path(sb.toString());

		} finally {
			this.fs.unlockForRead();
		}

	}

	public void remove(IProgressMonitor monitor) throws CoreException {

		// TODO how do we deal with "removed" stores wrt getPath and other methods?
		if (TraceLocation.CORE.isActive()) {
			IStatus stat = new Status(IStatus.INFO, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(
					Messages.SemanticFileStore_RemovingResource_XMSG, getPath().toString()), new TraceException());
			this.trace.trace(TraceLocation.CORE, stat);
		}

		IFileStore parent = getParent();

		if (parent != null) {
			if (parent instanceof SemanticFileStore) {
				SemanticFileStore sparent = (SemanticFileStore) parent;

				sparent.removeChild(getName());
			} else {
				// TODO 0.1: this needs to be handled if a resource is contained
				// in a non-Semantic container or possibly if it is a symbolic
				// link
			}
		} else {
			// delete children and mark the node non existent
			try {
				this.fs.lockForWrite();

				this.node.getChildren().clear();

				this.node.setExists(false);

				this.fs.requestFlush(false);

				this.fs.requestURILocatorRebuild();
			} finally {
				this.fs.unlockForWrite();
			}
		}
	}

	public ISemanticResourceInfo fetchResourceInfo(int options, IProgressMonitor monitor) throws CoreException {
		ISemanticSpiResourceInfo providerInfo = getEffectiveContentProvider().fetchResourceInfo(this, options, monitor);
		return new SemanticResourceInfo(options, providerInfo, isLocalOnly());
	}

	public int getType() {
		try {
			this.fs.lockForRead();
			return this.node.getType().getValue();
		} finally {
			this.fs.unlockForRead();
		}

	}

	public ISemanticFileStoreInternal getChildResource(String name) {
		return findChild(name);
	}

	/**
	 * This is called during initialization of this class
	 * 
	 * @param contentProvider
	 *            the responsible content provider
	 */
	public void setProvider(ISemanticContentProvider contentProvider) {
		this.provider = contentProvider;
	}

	protected void notifyPersistentPropertySet(String keyString, String oldValue, String newValue) throws CoreException {
		// if uri locator has not been requested yet, it will be rebuild later
		if (this.fs.getURILocator() != null) {
			if (oldValue != newValue) {
				IPath path = getPath();
				if (oldValue != null) {
					this.fs.getURILocator().removeURI(path, oldValue);
				}
				if (newValue != null) {
					this.fs.getURILocator().addURI(path, newValue);
				}
			}
		}
	}

	public IPath[] findURI(URI uri, IProgressMonitor monitor) throws CoreException {
		return this.fs.getURILocatorService(monitor).locateURI(uri, getPath());
	}

}
