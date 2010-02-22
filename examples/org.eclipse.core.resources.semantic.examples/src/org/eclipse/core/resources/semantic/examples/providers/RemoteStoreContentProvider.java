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
package org.eclipse.core.resources.semantic.examples.providers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.examples.remote.RemoteFile;
import org.eclipse.core.resources.semantic.examples.remote.RemoteFolder;
import org.eclipse.core.resources.semantic.examples.remote.RemoteItem;
import org.eclipse.core.resources.semantic.examples.remote.RemoteStore;
import org.eclipse.core.resources.semantic.examples.remote.SemanticResourcesPluginExamplesCore;
import org.eclipse.core.resources.semantic.examples.remote.RemoteItem.Type;
import org.eclipse.core.resources.semantic.spi.CachingContentProvider;
import org.eclipse.core.resources.semantic.spi.FileCacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.ICacheService;
import org.eclipse.core.resources.semantic.spi.ICacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderRemote;
import org.eclipse.core.resources.semantic.spi.ISemanticFileHistoryProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.ISemanticResourceRuleFactory;
import org.eclipse.core.resources.semantic.spi.ISemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.ISemanticTreeDeepFirstVisitor;
import org.eclipse.core.resources.semantic.spi.SemanticRevisionStorage;
import org.eclipse.core.resources.semantic.spi.SemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.SemanticTreeWalker;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore.ResourceType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.ITag;

/**
 * Keeps the remote data in a XML file
 * 
 */
public class RemoteStoreContentProvider extends CachingContentProvider implements ISemanticContentProviderRemote {
	/** If set, this should use the own project for remote storage */
	public static final QualifiedName USE_PROJECT = new QualifiedName(SemanticResourcesPluginExamplesCore.PLUGIN_ID, "UseOwnProject"); //$NON-NLS-1$

	private static final IStatus OKSTATUS = new Status(IStatus.OK, SemanticResourcesPluginExamplesCore.PLUGIN_ID, ""); //$NON-NLS-1$
	// private static final IStatus CANCELSTATUS = new Status(IStatus.CANCEL,
	// SemanticResourcesPluginExamplesCore.PLUGIN_ID,
	// Messages.RemoteStoreContentProvider_Canceld_XMSG);

	private static final QualifiedName ATTR_READONLY = new QualifiedName(SemanticResourcesPluginExamplesCore.PLUGIN_ID, "ReadOnly"); //$NON-NLS-1$

	private final static class RuleFactory implements ISemanticResourceRuleFactory {

		// since we may work across projects, we always return the workspace
		// root

		RuleFactory() {
			// nothing
		}

		public ISemanticFileStore charsetRule(ISemanticFileStore store) {
			return null;
		}

		public ISemanticFileStore copyRule(ISemanticFileStore source, ISemanticFileStore destination) {
			return null;
		}

		public ISemanticFileStore createRule(ISemanticFileStore store) {
			return null;
		}

		public ISemanticFileStore deleteRule(ISemanticFileStore store) {
			return null;
		}

		public ISemanticFileStore markerRule(ISemanticFileStore store) {
			return null;
		}

		public ISemanticFileStore modifyRule(ISemanticFileStore store) {
			return null;
		}

		public ISemanticFileStore moveRule(ISemanticFileStore source, ISemanticFileStore destination) {
			return null;
		}

		public ISemanticFileStore refreshRule(ISemanticFileStore store) {
			return null;
		}

		public ISemanticFileStore validateEditRule(ISemanticFileStore[] stores) {
			return null;
		}

	}

	@Override
	@SuppressWarnings({"rawtypes"})
	public Object getAdapter(Class adapter) {

		if (adapter == ISemanticFileHistoryProvider.class) {

			return new ISemanticFileHistoryProvider() {

				/**
				 * @throws CoreException
				 */
				public IFileRevision getWorkspaceFileRevision(ISemanticFileStore store) throws CoreException {
					RemoteItem item = getRemoteItem(getStore(), store.getPath());
					if (item instanceof RemoteFile) {
						return ((RemoteFile) item).getCurrentRevision(store);
					}
					return null;
				}

				/**
				 * @throws CoreException
				 */
				public IFileHistory getHistoryFor(ISemanticFileStore store, int options, IProgressMonitor monitor) throws CoreException {
					RemoteItem item = getRemoteItem(getStore(), store.getPath());
					if (item instanceof RemoteFile) {
						return ((RemoteFile) item).getHistory(store);
					}
					return null;
				}

				public IFileRevision[] getResourceVariants(final ISemanticFileStore semanticFileStore, IProgressMonitor monitor) {

					RemoteItem item = getStore().getItemByPath(semanticFileStore.getPath().removeFirstSegments(2));

					if (!(item instanceof RemoteFile)) {
						return null;
					}

					final RemoteFile file = (RemoteFile) item;

					IFileRevision remote = new IFileRevision() {

						/**
						 * @throws CoreException
						 */
						public IFileRevision withAllProperties(IProgressMonitor actmonitor) throws CoreException {
							return this;
						}

						public boolean isPropertyMissing() {
							return false;
						}

						public URI getURI() {
							try {
								return new URI(file.getPath().toString());
							} catch (URISyntaxException e) {
								// $JL-EXC$
								throw new RuntimeException(Messages.RemoteStoreContentProvider_URIError_XMSG);
							}
						}

						public long getTimestamp() {
							return file.getTimestamp();
						}

						public ITag[] getTags() {
							return null;
						}

						public IStorage getStorage(IProgressMonitor actmonitor) throws CoreException {
							SemanticRevisionStorage storage = new SemanticRevisionStorage(semanticFileStore);
							storage.setContents(new ByteArrayInputStream(file.getContent()), actmonitor);
							return storage;
						}

						public String getName() {
							return file.getName();
						}

						public String getContentIdentifier() {
							return Long.toString(file.getTimestamp());
						}

						public String getComment() {
							return null;
						}

						public String getAuthor() {
							return null;
						}

						public boolean exists() {
							return true;
						}
					};

					return new IFileRevision[] {null, remote};
				}
			};
		}
		return super.getAdapter(adapter);
	}

	public void synchronizeContentWithRemote(ISemanticFileStore semanticFileStore, SyncDirection direction, IProgressMonitor monitor,
			MultiStatus status) {

		monitor.setTaskName(NLS.bind(Messages.RemoteStoreContentProvider_Syncing_XMSG, semanticFileStore.getPath().toString()));

		RemoteStore store;
		RemoteItem item;
		try {
			store = getStore();
			item = getRemoteItem(store, semanticFileStore.getPath());
			if (item == null) {
				throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_RESOURCE_NOT_FOUND, semanticFileStore.getPath(),
						Messages.RemoteStoreContentProvider_RemoteItemNotFound_XMSG);
			}

		} catch (CoreException e) {
			status.add(e.getStatus());
			return;
		}

		if (item.getType() == Type.FOLDER) {

			RemoteFolder folder = (RemoteFolder) item;

			// check for deleted folders
			try {
				String[] childNames = semanticFileStore.childNames(EFS.NONE, monitor);
				for (String name : childNames) {
					if (!folder.hasChild(name)) {
						((ISemanticFileStore) semanticFileStore.getChild(name)).remove(monitor);
					}
				}
			} catch (CoreException e) {
				status.add(e.getStatus());
				return;
			}

			for (RemoteItem childItem : folder.getChildren()) {
				ISemanticFileStore fileStore = (ISemanticFileStore) semanticFileStore.getChild(childItem.getName());
				if (!fileStore.fetchInfo().exists()) {
					try {
						if (childItem.getType() == Type.FOLDER) {
							semanticFileStore.addChildFolder(childItem.getName());
						} else if (childItem.getType() == Type.FILE) {
							semanticFileStore.addChildFile(childItem.getName());
							setReadOnly((ISemanticFileStore) semanticFileStore.getChild(childItem.getName()), true, monitor);
						}

					} catch (CoreException e) {
						status.add(e.getStatus());
						return;
					}
				}

				try {
					store.serialize(monitor);
				} catch (CoreException e) {
					status.add(e.getStatus());
				}

				synchronizeContentWithRemote(fileStore, direction, monitor, status);
			}

		} else if (item.getType() == Type.FILE) {
			try {
				RemoteFile file = (RemoteFile) item;
				long remoteTime = file.getTimestamp();
				long localTime = -1l;
				ICacheService srv = getCacheService();
				if (srv.hasContent(semanticFileStore.getPath())) {
					localTime = srv.getContentTimestamp(semanticFileStore.getPath());
				}
				if (direction == SyncDirection.OUTGOING || localTime > remoteTime) {
					// outgoing
					OutputStream os = file.getOutputStream(false);
					Util.transferStreams(srv.getContent(semanticFileStore.getPath()), os, monitor);
					file.setTimestamp(srv.getContentTimestamp(semanticFileStore.getPath()));
					store.serialize(monitor);
				} else if (direction == SyncDirection.INCOMING || remoteTime > localTime) {
					// incoming
					srv.addContent(semanticFileStore.getPath(), new ByteArrayInputStream(file.getContent()), ISemanticFileSystem.NONE,
							monitor);
					setResourceTimestamp(semanticFileStore, remoteTime, monitor);
				}

			} catch (CoreException e) {
				status.add(e.getStatus());
			}
		}

	}

	@Override
	public ISemanticResourceRuleFactory getRuleFactory() {
		return new RuleFactory();
	}

	public void addResource(ISemanticFileStore parentStore, String name, ResourceType resourceType, IProgressMonitor monitor)
			throws CoreException {
		switch (resourceType) {
			case UNKNOWN_TYPE :
				this.addResourceFromRemote(parentStore, name, monitor);
				break;
			case FILE_TYPE :
				this.addFileFromRemote(parentStore, name, monitor);
				break;
			case FOLDER_TYPE :
				this.addFolderFromRemote(parentStore, name, monitor);
				break;
			case PROJECT_TYPE :
				throw new CoreException(new Status(IStatus.ERROR, SemanticResourcesPluginExamplesCore.PLUGIN_ID,
						Messages.RemoteStoreContentProvider_CannotCreateProject_XMSG));
		}
	}

	private void addFileFromRemote(ISemanticFileStore parentStore, String name, IProgressMonitor monitor) throws CoreException {

		RemoteItem item = getRemoteItem(getStore(), parentStore.getPath().append(name));

		if (item == null) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_RESOURCE_NOT_FOUND, parentStore.getPath(),
					Messages.RemoteStoreContentProvider_RemoteItemNotFound_XMSG);
		}
		if (item.getType() != Type.FILE) {
			IStatus error = new Status(IStatus.ERROR, SemanticResourcesPluginExamplesCore.PLUGIN_ID,
					Messages.RemoteStoreContentProvider_RemoteNotFile_XMSG);
			throw new CoreException(error);
		}

		parentStore.addChildFile(name);

		ISemanticFileStore newChild = (ISemanticFileStore) parentStore.getChild(name);
		setReadOnly(newChild, true, monitor);

	}

	private void addFolderFromRemote(ISemanticFileStore parentStore, String name, IProgressMonitor monitor) throws CoreException {
		parentStore.addChildFolder(name);

	}

	private void addResourceFromRemote(ISemanticFileStore parentStore, String name, IProgressMonitor monitor) throws CoreException {
		RemoteItem item = getRemoteItem(getStore(), parentStore.getPath().append(name));
		if (item == null) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_RESOURCE_NOT_FOUND, parentStore.getPath(),
					Messages.RemoteStoreContentProvider_RemoteNotFound_XMSG);
		}
		if (item.getType() == Type.FOLDER) {
			addFolderFromRemote(parentStore, name, monitor);
		} else {
			addFileFromRemote(parentStore, name, monitor);
		}

	}

	public void createResourceRemotely(ISemanticFileStore parentStore, String name, Object context, IProgressMonitor monitor)
			throws CoreException {
		// let's simply assume that a folder is wanted
		RemoteStore store = getStore();
		RemoteItem item = getRemoteItem(store, parentStore.getPath());
		if (item.getType() == Type.FILE) {
			throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, parentStore.getPath(),
					Messages.RemoteStoreContentProvider_ParentIsFile_XMSG);
		}
		((RemoteFolder) item).addFolder(name);
		store.serialize(monitor);
		// need to store before add
		addFolderFromRemote(parentStore, name, monitor);
	}

	public void deleteRemotely(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException {

		RemoteStore store = getStore();
		RemoteItem item = getRemoteItem(store, semanticFileStore.getPath());

		item.getParent().deleteChild(item.getName());
		store.serialize(monitor);
		// need to store before update
		removeResource(semanticFileStore, monitor);

	}

	public void revertChanges(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException {

		RemoteStore store = getStore();
		RemoteItem item = getRemoteItem(store, semanticFileStore.getPath());
		if (item.getType() != Type.FILE) {
			throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, semanticFileStore.getPath(),
					Messages.RemoteStoreContentProvider_FoldersNoRevert_XMSG);
		}

		RemoteFile file = ((RemoteFile) item);

		getCacheService().addContentWithTimestamp(semanticFileStore.getPath(), new ByteArrayInputStream(file.getContent()),
				file.getTimestamp(), ISemanticFileSystem.NONE, monitor);
		setReadOnly(semanticFileStore, true, monitor);

	}

	public IStatus validateRemoteCreate(ISemanticFileStore parentStore, String childName, final Object shell) {
		return OKSTATUS;
	}

	public IStatus validateRemoteDelete(ISemanticFileStore semanticFileStore, final Object shell) {

		return OKSTATUS;
	}

	public IStatus validateEdit(ISemanticFileStore[] stores, final Object shell) {

		if (shell != null) {

			MultiStatus multi = new MultiStatus(SemanticResourcesPluginExamplesCore.PLUGIN_ID, IStatus.OK,
					Messages.RemoteStoreContentProvider_ValidateEdit_XGRP, null);

			for (ISemanticFileStore store : stores) {

				try {
					this.setReadOnly(store, false, null);
					continue;
				} catch (CoreException e) {
					SemanticResourcesPluginExamplesCore.getDefault().getLog().log(e.getStatus());
					multi.add(e.getStatus());
				}

			}
			return multi;

		}
		// UI-less mode
		for (ISemanticFileStore store : stores) {
			try {
				setReadOnly(store, false, null);
			} catch (CoreException e) {
				return e.getStatus();
			}
		}

		return OKSTATUS;

	}

	RemoteItem getRemoteItem(RemoteStore remoteStore, IPath path) {

		if (remoteStore == null) {
			return null;
		}
		IPath relPath = path.removeFirstSegments(getRootStore().getPath().segmentCount());

		return remoteStore.getItemByPath(relPath);

	}

	RemoteStore getStore() {

		IProject ownProject = ResourcesPlugin.getWorkspace().getRoot().findMember(getRootStore().getPath()).getProject();
		RemoteStore store = (RemoteStore) ownProject.getAdapter(RemoteStore.class);
		return store;

	}

	/**
	 * @throws CoreException
	 */
	@Override
	public ICacheServiceFactory getCacheServiceFactory() throws CoreException {
		return new FileCacheServiceFactory();
	}

	@Override
	public InputStream openInputStreamInternal(ISemanticFileStore store, IProgressMonitor monitor, ICacheTimestampSetter timeStampSetter)
			throws CoreException {
		RemoteFile file = (RemoteFile) getRemoteItem(getStore(), store.getPath());
		if (file == null) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_RESOURCE_NOT_FOUND, store.getPath(),
					Messages.RemoteStoreContentProvider_RemoteItemNotFound_XMSG);
		}
		return new ByteArrayInputStream(file.getContent());
	}

	public void createFileRemotely(ISemanticFileStore parentStore, String name, InputStream source, Object context, IProgressMonitor monitor)
			throws CoreException {

		RemoteStore store = getStore();
		RemoteItem item = getRemoteItem(store, parentStore.getPath());
		if (item.getType() == Type.FILE) {
			throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, parentStore.getPath(),
					Messages.RemoteStoreContentProvider_CannotCreateFileChild_XMSG);
		}
		RemoteFile file = ((RemoteFolder) item).addFile(name, new byte[0], System.currentTimeMillis());
		if (source != null) {
			Util.transferStreams(source, file.getOutputStream(false), monitor);
		}
		store.serialize(monitor);
		// need to store before add
		addFileFromRemote(parentStore, name, monitor);
	}

	public ISemanticSpiResourceInfo fetchResourceInfo(ISemanticFileStore semanticFileStore, int options, IProgressMonitor monitor)
			throws CoreException {

		String uriString = null;

		boolean readOnly;
		if (SemanticSpiResourceInfo.isOptionRequested(ISemanticFileSystem.RESOURCE_INFO_READ_ONLY, options)) {
			readOnly = isReadOnly(semanticFileStore);
		} else {
			readOnly = false;
		}

		boolean existsRemotely = false;
		if (SemanticSpiResourceInfo.isOptionRequested(ISemanticFileSystem.RESOURCE_INFO_EXISTS_REMOTELY, options)) {

			existsRemotely = getRemoteItem(getStore(), semanticFileStore.getPath()) != null;

		}
		return new SemanticSpiResourceInfo(options, false, false, readOnly, existsRemotely, uriString, null);
	}

	private boolean isReadOnly(ISemanticFileStore semanticFileStore) throws CoreException {
		return semanticFileStore.getPersistentProperty(ATTR_READONLY) != null;
	}

	public void removeResource(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException {

		ISemanticTreeDeepFirstVisitor visitor = new ISemanticTreeDeepFirstVisitor() {

			@SuppressWarnings("synthetic-access")
			public void visit(ISemanticFileStore store, IProgressMonitor actMonitor) throws CoreException {
				if (store.getContentProviderID() == null || store.getPath().equals(getRootStore().getPath())) {
					try {
						deleteCache(store, actMonitor);
					} catch (CoreException ce) {
						// $JL-EXC$ TODO trace? we simply hope to delete this
						// upon
						// re-create
					}
					store.remove(actMonitor);
				} else {
					store.getEffectiveContentProvider().removeResource(store, actMonitor);
				}
			}

			/**
			 * @throws CoreException
			 */
			public boolean shouldVisitChildren(ISemanticFileStore store, IProgressMonitor actMonitor) throws CoreException {
				return store.getContentProviderID() == null;
			}
		};

		SemanticTreeWalker.accept(semanticFileStore, visitor, monitor);

	}

	public void setReadOnly(ISemanticFileStore semanticFileStore, boolean readonly, IProgressMonitor monitor) throws CoreException {
		semanticFileStore.setPersistentProperty(ATTR_READONLY, readonly ? "" : null); //$NON-NLS-1$

	}

	public IStatus validateSave(ISemanticFileStore semanticFileStore) {
		return validateEdit(new ISemanticFileStore[] {semanticFileStore}, null);
	}

}
