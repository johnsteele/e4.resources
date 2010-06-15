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
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBFactory;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeNodeType;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeRoot;
import org.eclipse.core.internal.resources.semantic.util.ISemanticFileSystemLog;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticResourceInfo;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.spi.FileCacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderFederation;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderFederation2;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderFederation2.FederatedProviderInfo;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderLocal;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderLocking;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderREST;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderRemote;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.ISemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.MemoryCacheServiceFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
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

	private final ISemanticFileSystemLog log;
	private ISemanticContentProvider provider;

	SemanticFileStore(SemanticFileSystem fs, ResourceTreeNode node) {
		super(fs, node);
		this.log = fs.getLog();
	}

	//
	// IFileStore/FileStore
	//
	/**
	 * @throws CoreException
	 */
	@Override
	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {

		try {
			this.fs.lockForRead();

			this.checkAndJoinTreeIfAnotherEntryExists();

			EList<ResourceTreeNode> children = this.node.getChildren();
			int counter = children.size();

			String[] names = new String[counter];
			int i = 0;

			for (ResourceTreeNode resourceTreeNode : children) {
				if (resourceTreeNode.isExists()) {
					names[i] = resourceTreeNode.getName();
					i++;
				}
			}
			return names;
		} finally {
			this.fs.unlockForRead();
		}
	}

	@Override
	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		FileInfo info;

		this.checkAndJoinTreeIfAnotherEntryExists();

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
				if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {
					SfsTraceLocation.getTrace().trace(SfsTraceLocation.CONTENTPROVIDER.getLocation(), e.getMessage(), e);
				}

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
				if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {
					SfsTraceLocation.getTrace().trace(SfsTraceLocation.CONTENTPROVIDER.getLocation(), e.getMessage(), e);
				}
			}
		}

		return info;
	}

	@Override
	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		if (SfsTraceLocation.CORE.isActive()) {
			SfsTraceLocation.getTrace().trace(SfsTraceLocation.CORE.getLocation(),
					NLS.bind(Messages.SemanticFileStore_UpdateFileInfo_XMSG, getPath().toString()));
		}

		checkAccessible();

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
		try {
			this.fs.lockForWrite();
			// TODO 0.1: check if we can use more fine grained locks here
			if (this.provider != null) {
				return this.provider;
			}

			ISemanticContentProvider effectiveProvider = getEffectiveContentProviderInternal();

			if (effectiveProvider instanceof ISemanticContentProviderFederation2) {
				IPath thisPath = this.getPath();

				return findFederatedContentProvider(thisPath, this.fs, this.node, effectiveProvider);
			}

			return effectiveProvider;
		} finally {
			this.fs.unlockForWrite();
		}
	}

	private static ISemanticContentProvider findFederatedContentProvider(IPath path, SemanticFileSystem fs, ResourceTreeNode node,
			ISemanticContentProvider parentProvider) throws CoreException {

		int relativePathLength = path.segmentCount() - parentProvider.getRootStore().getPath().segmentCount();

		if (relativePathLength > 0) {
			ISemanticContentProviderFederation2 federatingProvider = (ISemanticContentProviderFederation2) parentProvider;

			FederatedProviderInfo info = federatingProvider.getFederatedProviderInfoForPath(path);
			if (info != null) {
				if (info.contentProviderID == null) {
					throw new SemanticResourceException(SemanticResourceStatusCode.FEDERATION_EMPTY_FEDERATED_PROVIDER_ID, path, NLS.bind(
							Messages.SemanticFileStore_FederatingContentProviderReturnedNull_XMSG, parentProvider.getClass().getName(), path
									.toString()));
				}

				if (info.rootNodePosition <= 0 && info.rootNodePosition > relativePathLength) {
					throw new SemanticResourceException(SemanticResourceStatusCode.FEDERATION_INVALID_ROOT_NODE_POSITION, path, NLS.bind(
							Messages.SemanticFileStore_FederatingContentProviderReturnedInvalidRootNodePosition_XMSG, parentProvider.getClass()
									.getName(), path.toString()));
				}

				ResourceTreeNode parent = node;

				for (int i = 0; i < (relativePathLength - info.rootNodePosition); i++) {
					if (parent != null) {
						parent = parent.getParent();
					}
				}

				if (parent != null) {
					parent.setDynamicContentProviderID(info.contentProviderID);
				} else {
					String pathString = path.removeLastSegments(relativePathLength - info.rootNodePosition).toString();
					parent = fs.getNodeByPath(pathString);
				}

				ISemanticContentProvider nestedProvider = initProvider(info.contentProviderID, fs.getStore(parent));

				if (nestedProvider instanceof ISemanticContentProviderFederation2) {
					return findFederatedContentProvider(path, fs, node, nestedProvider);
				}
				return nestedProvider;
			}
		}
		return parentProvider;
	}

	private ISemanticContentProvider getEffectiveContentProviderInternal() throws CoreException {
		ISemanticFileStore parentStore;
		String parentContentProviderId = SemanticFileStore.DEFAULT_CONTENT_PROVIDER_ID;
		ResourceTreeNode parent = null;
		ResourceTreeNode cpRootParent;

		this.checkAndJoinTreeIfAnotherEntryExists();

		if (this.node.getTemplateID() != null) {
			String contentProviderID = this.node.getTemplateID();
			// TODO move outside of lock
			initProvider(contentProviderID, this);

			if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.CORE_VERBOSE.getLocation(), contentProviderID);
			}

			return this.provider;
		}

		if (this.node.getDynamicContentProviderID() != null) {
			String contentProviderID = this.node.getDynamicContentProviderID();
			// TODO move outside of lock
			initProvider(contentProviderID, this);

			if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.CORE_VERBOSE.getLocation(), contentProviderID);
			}

			return this.provider;
		}

		if (!this.node.isExists()) {
			List<ResourceTreeNode> nodes = this.fs.getNodesByPath(this.node.getPath());
			// go down to find a contentProviderID
			cpRootParent = nodes.get(0);
			int counter = 0;

			// scan the existing part where CP IDs are already assigned
			for (ResourceTreeNode resourceTreeNode : nodes) {
				if (resourceTreeNode.isExists()) {
					if (resourceTreeNode.getTemplateID() != null) {
						parentContentProviderId = resourceTreeNode.getTemplateID();
						cpRootParent = resourceTreeNode;
					}
					counter++;
				} else {
					break;
				}
			}

			// construct the root parent
			parentStore = this.fs.getStore(cpRootParent);

			// TODO move outside of lock?
			ISemanticContentProvider parentProvider = initProvider(parentContentProviderId, parentStore);

			// scan through non existing part
			for (int i = counter; i < nodes.size(); i++) {
				ResourceTreeNode resourceTreeNode = nodes.get(i);

				if (parentProvider instanceof ISemanticContentProviderFederation) {
					String federatedContentProviderId = ((ISemanticContentProviderFederation) parentProvider)
							.getFederatedProviderIDForPath(new Path(resourceTreeNode.getPath()));

					if (federatedContentProviderId != null) {
						parentContentProviderId = federatedContentProviderId;
						cpRootParent = resourceTreeNode;
						parentStore = this.fs.getStore(cpRootParent);
						// TODO move outside of lock?
						parentProvider = initProvider(parentContentProviderId, parentStore);
					}
				}
			}

			if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.CORE_VERBOSE.getLocation(), parentContentProviderId);
			}

			return parentProvider;
		}

		// walk up to find a contentProviderID
		parent = this.node.getParent();

		if (parent == null) {
			// this is a root store with default content provider
			ISemanticContentProvider parentProvider = initProvider(parentContentProviderId, this);

			return parentProvider;
		}

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

		// TODO move outside of lock?
		// the first parent with a provider ID
		ISemanticContentProvider parentProvider = initProvider(parentContentProviderId, parentStore);

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.CORE_VERBOSE.getLocation(), parentContentProviderId);
		}

		return parentProvider;
	}

	@Override
	public File toLocalFile(int options, IProgressMonitor monitor) throws CoreException {
		// some tools are operating directly on the file system; this enables
		// these tools
		// for content providers that have a file system cache by delegating to
		// this cache
		// non-caching content providers can not support this
		// TODO 0.1: think of a more generic solution

		this.checkAndJoinTreeIfAnotherEntryExists();

		if (options != EFS.CACHE && this.node.isExists()) {
			ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();
			if (effectiveProvider instanceof ISemanticContentProviderLocal) {
				return ((ISemanticContentProviderLocal) effectiveProvider).toLocalFile(this);
			}
			// TODO 0.1: check exception handling in tools, adjust error message
			return null;

		}
		return super.toLocalFile(options, monitor);

	}

	@Override
	public IFileStore getChild(String name) {

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

		// TODO 0.1: concurrency we need to detect and handle broken parent
		// hierarchy
		// situations
		// due to concurrency (e.g. parent was deleted in another thread)

		// check if there is federation

		String federatedContentProviderId = null;

		if (effectiveProvider instanceof ISemanticContentProviderFederation) {
			federatedContentProviderId = ((ISemanticContentProviderFederation) effectiveProvider).getFederatedProviderIDForPath(getPath()
					.append(name));
		}

		ISemanticFileStore result;
		try {
			this.fs.lockForWrite();

			this.checkAndJoinTreeIfAnotherEntryExists();

			store = findChild(name);

			if (store != null) {
				return store;
			}

			IPath childPath = this.getPath().append(name);

			ResourceTreeNode newnode = createLocalChildNode(name, childPath, federatedContentProviderId);

			result = this.fs.getStore(newnode);
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
					if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {
						SfsTraceLocation.getTrace().trace(SfsTraceLocation.CONTENTPROVIDER.getLocation(), e.getMessage(), e);
					}
				}
			}
		}

		return result;

	}

	@Override
	public String getName() {
		try {
			this.fs.lockForRead();

			this.checkAndJoinTreeIfAnotherEntryExists();

			return this.node.getName();
		} finally {
			this.fs.unlockForRead();
		}
	}

	@Override
	public IFileStore getParent() {
		try {
			this.fs.lockForRead();

			this.checkAndJoinTreeIfAnotherEntryExists();

			ResourceTreeNode parentNode = this.fs.getParentNode(this.node);
			if (parentNode != null) {
				return this.fs.getStore(parentNode);
			}
			return null;

		} finally {
			this.fs.unlockForRead();
		}
	}

	@Override
	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		checkAccessible();

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

		if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {

			SfsTraceLocation.getTrace()
					.trace(
							SfsTraceLocation.CONTENTPROVIDER.getLocation(),
							NLS.bind(Messages.SemanticFileStore_OpeningInputInfo_XMSG, effectiveProvider.getClass().getName(), getPath()
									.toString()));

		}

		return effectiveProvider.openInputStream(this, monitor);
	}

	@Override
	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
		// TODO 0.1: concurrency/race condition with IFile.getContent()

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		this.checkAndJoinTreeIfAnotherEntryExists();

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

		if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {
			String message;
			if (append) {
				message = NLS.bind(Messages.SemanticFileStore_AppendingInfo_XMSG, effectiveProvider.getClass().getName(), getPath()
						.toString());
			} else {
				message = NLS.bind(Messages.SemanticFileStore_OpeningInfo_XMSG, effectiveProvider.getClass().getName(), getPath()
						.toString());
			}
			SfsTraceLocation.getTrace().trace(SfsTraceLocation.CONTENTPROVIDER.getLocation(), message);
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
				this.fs.switchToExists(this.node, this.fs.getParentNode(this.node));
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

	@Override
	public URI toURI() {
		try {
			this.fs.lockForRead();
			try {
				return new URI(ISemanticFileSystem.SCHEME, null, null, -1, getPath().toString(), node.getQueryPart(), null);
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		} finally {
			this.fs.unlockForRead();
		}
	}

	@Override
	public SemanticFileSystem getFileSystem() {
		return this.fs;
	}

	@Override
	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().trace(SfsTraceLocation.CORE_VERBOSE.getLocation(),
					NLS.bind(Messages.SemanticFileStore_MkDir_XMSG, getPath().toString()));
		}

		try {
			this.fs.lockForWrite();

			this.checkAndJoinTreeIfAnotherEntryExists();

			ResourceTreeNode parent = this.fs.getParentNode(this.node);
			ISemanticContentProvider parentProvider = null;

			if ((options & EFS.SHALLOW) != 0) {
				if (parent != null && !parent.isExists()) {
					throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_PARENT_DOESNT_EXIST, getPath(),
							Messages.SemanticFileStore_ShallowMkDirFailed_XMSG);
				} else if (parent != null) { // and exists
					parentProvider = fs.getStore(parent).getEffectiveContentProvider();
				} else { // parent == null
					parentProvider = getEffectiveContentProvider();
				}
			} else {
				if (parent != null && !parent.isExists()) {
					parentProvider = this.fs.mkdir(parent, this.fs.getParentNode(parent));
				} else if (parent != null) { // and exists
					parentProvider = fs.getStore(parent).getEffectiveContentProvider();
				} else { // parent == null
					parentProvider = getEffectiveContentProvider();
				}
			}
			this.fs.makeFolder(this.node, parent, parentProvider);

		} finally {
			this.fs.unlockForWrite();
		}

		return this;
	}

	//
	// ISemantiFileStoreInternal
	//
	public void createFileRemotely(String name, InputStream source, Object context, IProgressMonitor monitor) throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		this.mkdir(EFS.NONE, monitor);
		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {
			SfsTraceLocation.getTrace().trace(
					SfsTraceLocation.CONTENTPROVIDER.getLocation(),
					MessageFormat.format(Messages.SemanticFileStore_CreateFileRemote_XMSG, name, effectiveProvider.getClass().getName(),
							getPath().toString()));
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

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		this.mkdir(EFS.NONE, monitor);
		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {

			SfsTraceLocation.getTrace().trace(
					SfsTraceLocation.CONTENTPROVIDER.getLocation(),
					MessageFormat.format(Messages.SemanticFileStore_CreateResourceRemtoe_XMSG, name,
							effectiveProvider.getClass().getName(), getPath().toString()));

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

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		this.mkdir(EFS.NONE, monitor);
		ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();
		if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {

			SfsTraceLocation.getTrace().trace(
					SfsTraceLocation.CONTENTPROVIDER.getLocation(),
					MessageFormat.format(Messages.SemanticFileStore_AddFileRemote_XMSG, name, effectiveProvider.getClass().getName(),
							getPath().toString()));
		}

		// delegate to content provider
		effectiveProvider.addResource(this, name, ResourceType.FILE_TYPE, monitor);

	}

	public void addFileFromRemoteByURI(String name, URI uri, IProgressMonitor monitor) throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}
		this.mkdir(EFS.NONE, monitor);
		ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		// delegate to content provider
		if (effectiveProvider instanceof ISemanticContentProviderREST) {

			if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {

				SfsTraceLocation.getTrace().trace(
						SfsTraceLocation.CONTENTPROVIDER.getLocation(),
						MessageFormat.format(Messages.SemanticFileStore_AddFileRemoteURI_XMSG, name, uri.toString(), effectiveProvider
								.getClass().getName(), getPath().toString()));

			}

			((ISemanticContentProviderREST) effectiveProvider).addFileFromRemoteByURI(this, name, uri, monitor);

		} else {
			throw new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, getPath(), NLS.bind(
					Messages.SemanticFileStore_IntefaceNotImplemented_XMSG, effectiveProvider.getClass().getName(),
					ISemanticContentProviderREST.class.getName()));
		}

	}

	public void addFolderFromRemoteByURI(String name, URI uri, IProgressMonitor monitor) throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		this.mkdir(EFS.NONE, monitor);
		ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		// delegate to content provider
		if (effectiveProvider instanceof ISemanticContentProviderREST) {

			if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {

				SfsTraceLocation.getTrace().trace(
						SfsTraceLocation.CONTENTPROVIDER.getLocation(),
						MessageFormat.format(Messages.SemanticFileStore_AddFileRemoteURI_XMSG, name, uri.toString(), effectiveProvider
								.getClass().getName(), getPath().toString()));
			}

			((ISemanticContentProviderREST) effectiveProvider).addFolderFromRemoteByURI(this, name, uri, monitor);

		} else {
			throw new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, getPath(), NLS.bind(
					Messages.SemanticFileStore_IntefaceNotImplemented_XMSG, effectiveProvider.getClass().getName(),
					ISemanticContentProviderREST.class.getName()));
		}

	}

	public void addFolderFromRemote(String name, IProgressMonitor monitor) throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		this.mkdir(EFS.NONE, monitor);
		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {

			SfsTraceLocation.getTrace().trace(
					SfsTraceLocation.CONTENTPROVIDER.getLocation(),
					MessageFormat.format(Messages.SemanticFileStore_AddFolderRemote_XMSG, name, effectiveProvider.getClass().getName(),
							getPath().toString()));
		}

		// delegate to content provider
		effectiveProvider.addResource(this, name, ResourceType.FOLDER_TYPE, monitor);

	}

	public ISemanticFileStoreInternal addResourceFromRemote(String name, IProgressMonitor monitor) throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		this.mkdir(EFS.NONE, monitor);
		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {

			SfsTraceLocation.getTrace().trace(
					SfsTraceLocation.CONTENTPROVIDER.getLocation(),
					MessageFormat.format(Messages.SemanticFileStore_AddResourceRemote_XMSG, name, effectiveProvider.getClass().getName(),
							getPath().toString()));
		}

		// delegate to content provider
		effectiveProvider.addResource(this, name, ResourceType.UNKNOWN_TYPE, monitor);

		return findChild(name);
	}

	public void addResource(String name, boolean asFolder, String contentProviderID, Map<QualifiedName, String> properties,
			IProgressMonitor monitor) throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

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

	@Override
	public void delete(int options, IProgressMonitor monitor) throws CoreException {
		// this will be called instead of delete hook if no team provider is
		// found
		// or if resource is marked as derived
		// TODO 0.1: review this behavior e.g. with respect to linked resources

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		removeFromWorkspace(monitor);

	}

	public void deleteRemotely(IProgressMonitor monitor) throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		this.checkAndJoinTreeIfAnotherEntryExists();

		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {
			SfsTraceLocation.getTrace().trace(
					SfsTraceLocation.CONTENTPROVIDER.getLocation(),
					NLS.bind(Messages.SemanticFileStore_DeleteResourceRemote_XMSG, getPath().toString(), effectiveProvider.getClass()
							.getName()));
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
		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {

			SfsTraceLocation.getTrace().trace(
					SfsTraceLocation.CONTENTPROVIDER.getLocation(),
					NLS.bind(Messages.SemanticFileStore_RemoveResourceRemote_XMSG, getPath().toString(), effectiveProvider.getClass()
							.getName()));
		}

		checkAccessible();

		effectiveProvider.removeResource(this, monitor);
	}

	public void forceRemoveFromWorkspace(int options, IProgressMonitor monitor) throws CoreException {
		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		checkAccessible();

		new FileCacheServiceFactory().getCacheService().removeContentRecursive(getPath(), monitor);
		new MemoryCacheServiceFactory().getCacheService().removeContentRecursive(getPath(), monitor);

		this.remove(monitor);
	}

	public void forceRemove(int options, IProgressMonitor monitor) throws CoreException {
		forceRemoveFromWorkspace(options, monitor);
	}

	public void synchronizeContentWithRemote(SyncDirection direction, IProgressMonitor monitor) throws CoreException {
		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {
			SfsTraceLocation.getTrace().trace(SfsTraceLocation.CONTENTPROVIDER.getLocation(),
					NLS.bind(Messages.SemanticFileStore_SynchContent_XMSG, effectiveProvider.getClass().getName(), getPath().toString()));

		}

		checkAccessible();

		MultiStatus status = new MultiStatus(SemanticResourcesPlugin.PLUGIN_ID, IStatus.OK, NLS.bind(
				Messages.SemanticFileStore_SyncContent_XGRP, getPath().toString()), null);
		effectiveProvider.synchronizeContentWithRemote(this, direction, monitor, status);
		if (!status.isOK()) {
			throw new CoreException(status);
		}
	}

	public void setRemoteURI(URI uri, IProgressMonitor monitor) throws CoreException {

		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		checkAccessible();

		// delegate to content provider
		if (effectiveProvider instanceof ISemanticContentProviderREST) {

			if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {

				SfsTraceLocation.getTrace().trace(
						SfsTraceLocation.CONTENTPROVIDER.getLocation(),
						MessageFormat.format(Messages.SemanticFileStore_SettingURI_XMSG, uri.toString(), effectiveProvider.getClass()
								.getName(), getPath().toString()));
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

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {

			SfsTraceLocation.getTrace().trace(SfsTraceLocation.CONTENTPROVIDER.getLocation(),
					NLS.bind(Messages.SemanticFileStore_Revert_XMSG, getPath().toString(), effectiveProvider.getClass().getName()));
		}

		checkAccessible();

		effectiveProvider.revertChanges(this, monitor);
	}

	public IStatus lockResource(IProgressMonitor monitor) throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {

			SfsTraceLocation.getTrace().trace(SfsTraceLocation.CONTENTPROVIDER.getLocation(),
					NLS.bind(Messages.SemanticFileStore_Locking_XMSG, getPath().toString(), effectiveProvider.getClass().getName()));
		}

		checkAccessible();

		if (effectiveProvider instanceof ISemanticContentProviderLocking) {
			return ((ISemanticContentProviderLocking) effectiveProvider).lockResource(this, monitor);
		}

		return new Status(IStatus.ERROR, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(
				Messages.SemanticFileStore_IntefaceNotImplemented_XMSG, effectiveProvider.getClass().getName(),
				ISemanticContentProviderLocking.class.getName()));
	}

	public IStatus unlockResource(IProgressMonitor monitor) throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {

			SfsTraceLocation.getTrace().trace(SfsTraceLocation.CONTENTPROVIDER.getLocation(),
					NLS.bind(Messages.SemanticFileStore_Unlocking_XMSG, getPath().toString(), effectiveProvider.getClass().getName()));
		}

		checkAccessible();

		if (effectiveProvider instanceof ISemanticContentProviderLocking) {
			return ((ISemanticContentProviderLocking) effectiveProvider).unlockResource(this, monitor);
		}

		return new Status(IStatus.ERROR, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(
				Messages.SemanticFileStore_IntefaceNotImplemented_XMSG, effectiveProvider.getClass().getName(),
				ISemanticContentProviderLocking.class.getName()));
	}

	public IStatus validateRemoteCreate(String name, Object shell) {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		ISemanticContentProvider effectiveProvider;
		try {
			checkAccessible();
			effectiveProvider = getEffectiveContentProvider();
		} catch (CoreException ce) {
			this.log.log(ce);
			return ce.getStatus();
		}

		if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {

			SfsTraceLocation.getTrace().trace(
					SfsTraceLocation.CONTENTPROVIDER.getLocation(),
					MessageFormat.format(Messages.SemanticFileStore_ValidateRemoteCreate_XMSG, name,
							effectiveProvider.getClass().getName(), getPath().toString()));
		}

		if (effectiveProvider instanceof ISemanticContentProviderRemote) {
			return ((ISemanticContentProviderRemote) effectiveProvider).validateRemoteCreate(this, name, shell);
		}

		return new Status(IStatus.ERROR, SemanticResourcesPlugin.PLUGIN_ID, NLS.bind(
				Messages.SemanticFileStore_IntefaceNotImplemented_XMSG, effectiveProvider.getClass().getName(),
				ISemanticContentProviderRemote.class.getName()));
	}

	public IStatus validateRemoteDelete(Object shell) {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		try {
			checkAccessible();
		} catch (CoreException e) {
			return e.getStatus();
		}

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
			if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {

				SfsTraceLocation.getTrace().trace(
						SfsTraceLocation.CONTENTPROVIDER.getLocation(),
						MessageFormat.format(Messages.SemanticFileStore_ValidateRemoteDelete_XMSG, getPath().toString(), effectiveProvider
								.getClass().getName()));
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

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		try {
			checkAccessible();
		} catch (CoreException e) {
			return e.getStatus();
		}

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
			if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {
				SfsTraceLocation.getTrace().trace(
						SfsTraceLocation.CONTENTPROVIDER.getLocation(),
						NLS.bind(Messages.SemanticFileStore_ValidateRemove_XMSG, getPath().toString(), effectiveProvider.getClass()
								.getName()));
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

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		if (SfsTraceLocation.CORE.isActive()) {

			SfsTraceLocation.getTrace().trace(SfsTraceLocation.CORE.getLocation(),
					NLS.bind(Messages.SemanticFileStore_AddChildFolder_XMSG, name, getPath().toString()));
		}

		try {
			this.fs.lockForWrite();

			checkAccessible();

			checkChildExists(name);

			createChildNode(name, true, null);

			this.fs.requestFlush(false);
		} finally {
			this.fs.unlockForWrite();
		}
	}

	public void addChildResource(String name, boolean asFolder, String contentProviderID, Map<QualifiedName, String> properties)
			throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		if (SfsTraceLocation.CORE.isActive()) {
			String message;
			if (asFolder) {
				message = NLS.bind(Messages.SemanticFileStore_AddContentProviderRootFolder_XMSG, name, getPath().toString());
			} else {
				message = NLS.bind(Messages.SemanticFileStore_AddContentProviderRootFile_XMSG, name, getPath().toString());
			}

			SfsTraceLocation.getTrace().trace(SfsTraceLocation.CORE.getLocation(), message);

		}

		ResourceTreeNode child;
		try {
			this.fs.lockForWrite();

			checkAccessible();

			checkChildExists(name);

			child = createChildNode(name, asFolder, contentProviderID);

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

		if (contentProviderID != null) {
			ISemanticFileStore store = this.fs.getStore(child);

			store.getEffectiveContentProvider().onRootStoreCreate(store);
		}
	}

	public void addChildFile(String name) throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}
		if (SfsTraceLocation.CORE.isActive()) {

			SfsTraceLocation.getTrace().trace(SfsTraceLocation.CORE.getLocation(),
					NLS.bind(Messages.SemanticFileStore_AddChildFile_XMSG, name, getPath().toString()));
		}

		try {
			this.fs.lockForWrite();

			checkAccessible();

			checkChildExists(name);

			createChildNode(name, false, null);

			this.fs.requestFlush(false);
		} finally {
			this.fs.unlockForWrite();
		}
	}

	public void addLocalChildResource(String name, String contentProviderID) throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		if (SfsTraceLocation.CORE.isActive()) {

			SfsTraceLocation.getTrace().trace(SfsTraceLocation.CORE.getLocation(),
					NLS.bind(Messages.SemanticFileStore_AddLocalChild_XMSG, name, getPath().toString()));
		}

		try {
			this.fs.lockForWrite();

			checkAccessible();

			checkChildExists(name);

			createLocalChildNode(name, this.getPath().append(name), contentProviderID);

			this.fs.requestFlush(false);
		} finally {
			this.fs.unlockForWrite();
		}
	}

	public boolean hasResource(String name) {

		try {
			this.fs.lockForRead();

			this.checkAndJoinTreeIfAnotherEntryExists();

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

	public boolean hasChild(String name) {
		return hasResource(name);
	}

	public String getContentProviderID() {
		try {
			this.fs.lockForRead();

			this.checkAndJoinTreeIfAnotherEntryExists();

			return this.node.getTemplateID();
		} finally {
			this.fs.unlockForRead();
		}
	}

	public URI getRemoteURI() throws CoreException {

		checkAccessible();

		final ISemanticContentProvider effectiveProvider = getEffectiveContentProvider();

		if (effectiveProvider instanceof ISemanticContentProviderREST) {
			try {
				String uriString = ((ISemanticContentProviderREST) effectiveProvider).getURIString(this);

				return new URI(uriString);
			} catch (URISyntaxException e) {
				throw new SemanticResourceException(SemanticResourceStatusCode.INVALID_URI_SYNTAX, getPath(), NLS.bind(
						Messages.SemanticFileStore_InvalidURISyntax_XMSG, effectiveProvider.getClass().getName(),
						ISemanticContentProviderREST.class.getName()));
			}
		}
		throw new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, getPath(), NLS.bind(
				Messages.SemanticFileStore_IntefaceNotImplemented_XMSG, effectiveProvider.getClass().getName(),
				ISemanticContentProviderREST.class.getName()));

	}

	public boolean isExists() {
		try {
			this.fs.lockForRead();

			this.checkAndJoinTreeIfAnotherEntryExists();

			return this.node.isExists();
		} finally {
			this.fs.unlockForRead();
		}
	}

	public boolean isLocalOnly() {
		try {
			this.fs.lockForRead();

			this.checkAndJoinTreeIfAnotherEntryExists();

			return this.node.isLocalOnly();
		} finally {
			this.fs.unlockForRead();
		}
	}

	public void setLocalOnly(boolean isLocalOnly) {
		try {
			this.fs.lockForWrite();

			this.checkAndJoinTreeIfAnotherEntryExists();

			this.node.setLocalOnly(isLocalOnly);
		} finally {
			this.fs.unlockForWrite();
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

			this.checkAndJoinTreeIfAnotherEntryExists();

			EList<ResourceTreeNode> children = this.node.getChildren();

			for (ResourceTreeNode resourceTreeNode : children) {
				if (resourceTreeNode.getName().equals(name)) {
					return (SemanticFileStore) this.fs.getStore(resourceTreeNode);
				}
			}
		} finally {
			this.fs.unlockForRead();
		}
		return null;
	}

	private ResourceTreeNode createChildNode(String name, boolean isFolder, String contentProviderID) {
		// all callers have a lock, so we don't lock here
		ResourceTreeNode child = SemanticResourceDBFactory.eINSTANCE.createResourceTreeNode();

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

	private ResourceTreeNode createLocalChildNode(String name, IPath childPath, String contentProviderID) {
		// all callers have a lock, so we don't lock here
		ResourceTreeNode child = SemanticResourceDBFactory.eINSTANCE.createResourceTreeNode();

		child.setName(name);
		child.setTemplateID(contentProviderID);
		child.setType(TreeNodeType.UNKNOWN);
		child.setExists(false); // needed to comply with resource creation
		// behavior
		child.setPath(childPath.toString());
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
	 * throws exception if child exists
	 */
	private void checkChildExists(String name) throws CoreException {
		// all callers have obtained a lock, so we don't lock here
		EList<ResourceTreeNode> children = this.node.getChildren();

		for (ResourceTreeNode resourceTreeNode : children) {
			if (resourceTreeNode.getName().equals(name)) {
				if (resourceTreeNode.isExists()) {
					IPath newPath = getPath().append(name);
					throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_ALREADY_EXISTS, newPath, NLS.bind(
							Messages.SemanticFileStore_ResourceWithPathExists_XMSG, newPath.toString()));
				}
			}
		}
	}

	private static void cleanupNodeAndChildren(ResourceTreeNode node, IPath path) {
		EList<ResourceTreeNode> children = node.getChildren();

		for (ResourceTreeNode resourceTreeNode : children) {
			cleanupNodeAndChildren(resourceTreeNode, path.append(resourceTreeNode.getName()));
		}

		// keep the last path so that getPath returns something meaningful after
		// remove
		node.setPath(path.toString());
		node.setExists(false);

		node.setPersistentProperties(null);
		node.setSessionProperties(null);

		children.clear();
	}

	public IStatus validateEdit(Object shell) {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		try {
			checkAccessible();
		} catch (CoreException e) {
			return e.getStatus();
		}

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

				if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {
					SfsTraceLocation.getTrace().trace(
							SfsTraceLocation.CONTENTPROVIDER.getLocation(),
							NLS.bind(Messages.SemanticFileStore_ValidateEdit_XMSG, effectiveProvider.getClass().getName(), getPath()
									.toString()));

				}

				return effectiveProvider.validateEdit(new ISemanticFileStore[] {this}, shell);
			}
			// already checked out
			return new Status(IStatus.OK, SemanticResourcesPlugin.PLUGIN_ID, null);

		}
		return new Status(IStatus.CANCEL, SemanticResourcesPlugin.PLUGIN_ID, null);

	}

	public IStatus validateSave() {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		try {
			checkAccessible();
		} catch (CoreException e) {
			return e.getStatus();
		}

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
				if (SfsTraceLocation.CONTENTPROVIDER.isActive()) {

					SfsTraceLocation.getTrace().trace(
							SfsTraceLocation.CONTENTPROVIDER.getLocation(),
							NLS.bind(Messages.SemanticFileStore_ValidateSave_XMSG, effectiveProvider.getClass().getName(), getPath()
									.toString()));
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

			this.checkAndJoinTreeIfAnotherEntryExists();

			return this.fs.getPathForNode(this.node);
		} finally {
			this.fs.unlockForRead();
		}

	}

	public void remove(IProgressMonitor monitor) throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceDumpStack(SfsTraceLocation.CORE_VERBOSE.getLocation());
		}

		if (SfsTraceLocation.CORE.isActive()) {

			SfsTraceLocation.getTrace().trace(SfsTraceLocation.CORE.getLocation(),
					NLS.bind(Messages.SemanticFileStore_RemovingResource_XMSG, getPath().toString()));
		}

		try {
			this.fs.lockForWrite();

			this.checkAccessible();

			SemanticFileStore.cleanupNodeAndChildren(node, getPath());

			if (this.node instanceof TreeRoot) {
				((TreeRoot) this.node).setParentDB(null);
			} else {
				this.node.setParent(null);
			}

			this.fs.requestFlush(false);

			this.fs.requestURILocatorRebuild();
		} finally {
			this.fs.unlockForWrite();
		}
	}

	public ISemanticResourceInfo fetchResourceInfo(int options, IProgressMonitor monitor) throws CoreException {
		// checkAccessible();

		ISemanticSpiResourceInfo providerInfo = getEffectiveContentProvider().fetchResourceInfo(this, options, getNotNullMonitor(monitor));
		return new SemanticResourceInfo(options, providerInfo, isLocalOnly());
	}

	public int getType() {
		try {
			this.fs.lockForRead();

			this.checkAndJoinTreeIfAnotherEntryExists();

			return this.node.getType().getValue();
		} finally {
			this.fs.unlockForRead();
		}

	}

	public ISemanticFileStoreInternal getChildResource(String name) {
		// TODO checkAccessible()??
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

	/**
	 * @throws CoreException
	 */
	@Override
	protected void notifyPersistentPropertySet(String keyString, String oldValue, String newValue) throws CoreException {
		// nothing to do for now
	}

	public IPath[] findURI(URI uri, IProgressMonitor monitor) throws CoreException {
		return this.fs.getURILocatorService(monitor).locateURI(uri, getPath());
	}

	public String getRemoteURIString() {
		return node.getRemoteURI();
	}

	public void setRemoteURIString(String uriString) {
		String oldValue = node.getRemoteURI();

		node.setRemoteURI(uriString);

		if (this.fs.getURILocator() != null) {
			if ((oldValue == null && uriString != null) || (oldValue != null && !oldValue.equals(uriString))) {
				IPath path = getPath();
				if (oldValue != null) {
					this.fs.getURILocator().removeURI(path, oldValue);
				}
				if (uriString != null) {
					this.fs.getURILocator().addURI(path, uriString);
				}
			}
		}
	}

	private IProgressMonitor getNotNullMonitor(IProgressMonitor monitor) {
		if (monitor == null) {
			return new NullProgressMonitor();
		}
		return monitor;
	}

}
