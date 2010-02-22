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
package org.eclipse.core.resources.semantic.test.provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.resources.semantic.SemanticResourcesPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.examples.remote.RemoteFile;
import org.eclipse.core.resources.semantic.examples.remote.RemoteFolder;
import org.eclipse.core.resources.semantic.examples.remote.RemoteItem;
import org.eclipse.core.resources.semantic.examples.remote.RemoteStoreTransient;
import org.eclipse.core.resources.semantic.examples.remote.RemoteItem.Type;
import org.eclipse.core.resources.semantic.spi.CachingContentProvider;
import org.eclipse.core.resources.semantic.spi.ICacheService;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderLocking;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderRemote;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.ISemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.SemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore.ResourceType;
import org.eclipse.core.resources.semantic.test.TestPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * Base for memory and file caching
 * 
 */
public abstract class CachingTestContentProviderBase extends CachingContentProvider implements ISemanticContentProviderRemote,
ISemanticContentProviderLocking {
	/**
	 * If set to any value, this indicates that write-through should be
	 * performed
	 */
	public static final QualifiedName WRITE_THROUGH = new QualifiedName(TestPlugin.PLUGIN_ID, "WriteThrough");

	private static final QualifiedName LOCKHANDLE = new QualifiedName(TestPlugin.PLUGIN_ID, "LockHandle");
	private static final QualifiedName READONLY = new QualifiedName(TestPlugin.PLUGIN_ID, "ReadOnly");

	@Override
	public void onCacheUpdate(ISemanticFileStore semanticFileStore, InputStream newContent, long cacheTimestamp, boolean append,
			IProgressMonitor monitor) {
		boolean writeThrough;
		try {
			writeThrough = semanticFileStore.getSessionProperty(WRITE_THROUGH) != null;
		} catch (CoreException e) {
			// $JL-EXC$ ignore here
			writeThrough = false;
		}
		if (writeThrough) {
			RemoteItem parentItem = getStore().getItemByPath(semanticFileStore.getPath().removeFirstSegments(2));
			if (parentItem.getType() == Type.FILE) {
				OutputStream os = ((RemoteFile) parentItem).getOutputStream(append);
				try {
					Util.transferStreams(newContent, os, monitor);
				} catch (CoreException e) {
					// $JL-EXC$ TODO proper error handling
				}
			}
		} else {
			super.onCacheUpdate(semanticFileStore, newContent, cacheTimestamp, append, monitor);
		}
	}

	public void addResource(ISemanticFileStore parentStore, String name, ResourceType resourceType, IProgressMonitor monitor)
	throws CoreException {
		switch (resourceType) {
		case FILE_TYPE:
			this.addFileFromRemote(parentStore, name, monitor);
			break;
		case FOLDER_TYPE:
			this.addFolderFromRemote(parentStore, name, monitor);
			break;
		case UNKNOWN_TYPE:
			addResourceFromRemote(parentStore, name, monitor);
			break;
		default:
			throw new CoreException(new Status(IStatus.ERROR, SemanticResourcesPlugin.PLUGIN_ID, "Can not create resource of type "
					+ resourceType.name()));
		}
	}

	protected void addFileFromRemote(ISemanticFileStore childStore, String name, IProgressMonitor monitor) throws CoreException {
		RemoteItem parentItem = getStore().getItemByPath(childStore.getPath().removeFirstSegments(2));
		if (parentItem.getType() != Type.FOLDER) {
			throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, parentItem.getPath(), "");
		}
		RemoteItem item = ((RemoteFolder) parentItem).getChild(name);
		if (item == null) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_RESOURCE_NOT_FOUND, childStore.getPath(),
			"No such resource");
		}
		if (item.getType() != RemoteItem.Type.FILE) {
			throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, item.getPath(), "");
		}
		childStore.addChildFile(name);
		ISemanticFileStore newChild = (ISemanticFileStore) childStore.getChild(name);
		setReadOnly(newChild, true, monitor);
	}

	protected void addFolderFromRemote(ISemanticFileStore childStore, String name, IProgressMonitor monitor) throws CoreException {
		RemoteItem parentItem = getStore().getItemByPath(childStore.getPath().removeFirstSegments(2));
		if (parentItem.getType() != Type.FOLDER) {
			throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, parentItem.getPath(), "");
		}
		RemoteItem item = ((RemoteFolder) parentItem).getChild(name);
		if (item == null) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_RESOURCE_NOT_FOUND, childStore.getPath(),
			"No such resource");
		}
		if (item.getType() != RemoteItem.Type.FOLDER) {
			throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, item.getPath(), "");
		}
		childStore.addChildFolder(name);
		ISemanticFileStore newChild = (ISemanticFileStore) childStore.getChild(name);
		setReadOnly(newChild, true, monitor);

	}

	protected void addResourceFromRemote(ISemanticFileStore childStore, String name, IProgressMonitor monitor) throws CoreException {
		RemoteItem parentItem = getStore().getItemByPath(childStore.getPath().removeFirstSegments(2));
		if (parentItem.getType() != Type.FOLDER) {
			throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, parentItem.getPath(), "");
		}
		RemoteItem item = ((RemoteFolder) parentItem).getChild(name);
		if (item == null) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_RESOURCE_NOT_FOUND, childStore.getPath(),
			"No such resource");
		}
		if (item.getType() == Type.FILE) {
			addFileFromRemote(childStore, name, monitor);
		} else if (item.getType() == Type.FOLDER) {
			addFolderFromRemote(childStore, name, monitor);
		} else {
			Status result = new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, "Unknown resource type");
			throw new CoreException(result);
		}
	}

	public void createFileRemotely(ISemanticFileStore childStore, String name, InputStream source, Object context, IProgressMonitor monitor)
	throws CoreException {

		byte[] buffer;
		if (source != null) {
			try {
				int size = source.available();
				buffer = new byte[size];
				source.read(buffer);
			} catch (IOException e) {
				// $JL-EXC$
				throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
			}
		} else {
			buffer = new byte[0];
		}

		RemoteItem parentItem = getStore().getItemByPath(childStore.getPath().removeFirstSegments(2));
		if (parentItem.getType() != Type.FOLDER) {
			throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, parentItem.getPath(), "");
		}
		((RemoteFolder) parentItem).addFile(name, buffer, System.currentTimeMillis());

		childStore.addChildFile(name);
		ISemanticFileStore newStore = (ISemanticFileStore) childStore.getChild(name);
		// let's assume we have a lock after the remote creation
		setReadOnly(newStore, false, monitor);
		lockResource(newStore, monitor);

	}

	public void createResourceRemotely(ISemanticFileStore childStore, String name, Object context, IProgressMonitor monitor)
	throws CoreException {

		createFileRemotely(childStore, name, null, context, monitor);

	}

	public void deleteRemotely(ISemanticFileStore childStore, IProgressMonitor monitor) throws CoreException {
		RemoteItem parentItem = getStore().getItemByPath(childStore.getPath().removeFirstSegments(2).removeLastSegments(1));
		if (parentItem.getType() != Type.FOLDER) {
			throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, parentItem.getPath(), "");
		}
		((RemoteFolder) parentItem).deleteChild(childStore.getName());
		childStore.remove(monitor);

	}

	public IStatus lockResource(ISemanticFileStore childStore, IProgressMonitor monitor) throws CoreException {

		RemoteItem item = getStore().getItemByPath(childStore.getPath().removeFirstSegments(2));
		item.setLocked(true);
		childStore.setSessionProperty(LOCKHANDLE, "");
		return new Status(IStatus.OK, TestPlugin.PLUGIN_ID, "");
	}

	public IStatus unlockResource(ISemanticFileStore childStore, IProgressMonitor monitor) throws CoreException {

		RemoteItem item = getStore().getItemByPath(childStore.getPath().removeFirstSegments(2));
		item.setLocked(false);
		childStore.setSessionProperty(LOCKHANDLE, null);
		return new Status(IStatus.OK, TestPlugin.PLUGIN_ID, "");
	}

	public ISemanticSpiResourceInfo fetchResourceInfo(ISemanticFileStore semanticFileStore, int options, IProgressMonitor monitor)
	throws CoreException {
		return new SemanticSpiResourceInfo(options, semanticFileStore
				.getSessionProperty(LOCKHANDLE) != null, true, semanticFileStore
				.getPersistentProperty(READONLY) != null,
				getStore().getItemByPath(semanticFileStore.getPath().removeFirstSegments(2)) != null,
				null, null);
	}

	@Override
	public InputStream openInputStreamInternal(ISemanticFileStore store, IProgressMonitor monitor,
			ICacheTimestampSetter timeStampSetter) throws CoreException {
		RemoteItem item = getStore().getItemByPath(store.getPath().removeFirstSegments(2));
		if (item.getType() != Type.FILE) {
			throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, item.getPath(), "");
		}
		timeStampSetter.setTimestamp(((RemoteFile) item).getTimestamp());
		return new ByteArrayInputStream(((RemoteFile) item).getContent());
	}

	public void removeResource(ISemanticFileStore childStore, IProgressMonitor monitor) throws CoreException {
		deleteCache(childStore, monitor);
		childStore.remove(monitor);
	}

	public void revertChanges(ISemanticFileStore store, final IProgressMonitor monitor) throws CoreException {

		RemoteItem item = getStore().getItemByPath(store.getPath().removeFirstSegments(2));
		if (item.getType() != Type.FILE) {
			throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, item.getPath(), "");
		}
		((RemoteFile) item).getOutputStream(false).reset();
		MultiStatus status = new MultiStatus(TestPlugin.PLUGIN_ID, IStatus.OK, NLS.bind("Revert Change Result for {0}", store.getPath()
				.toString()), null);
		deleteCache(store, monitor);
		setReadOnly(store, true, null);

		Util.safeClose(openInputStream(store, monitor));

		if (!status.isOK()) {
			throw new CoreException(status);
		}

	}

	public void setReadOnly(ISemanticFileStore childStore, boolean readonly, IProgressMonitor monitor) throws CoreException {
		if (readonly) {
			childStore.setPersistentProperty(READONLY, "");
		} else {
			childStore.setPersistentProperty(READONLY, null);
		}
	}

	public void synchronizeContentWithRemote(ISemanticFileStore store, SyncDirection direction, IProgressMonitor monitor,
			MultiStatus status) {
		RemoteItem item = getStore().getItemByPath(store.getPath().removeFirstSegments(2));
		if (item.getType() != Type.FILE) {
			status.add(new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, item.getPath(), "")
			.getStatus());
			return;
		}
		try {
			if (item.isLocked()) {
				store.setSessionProperty(LOCKHANDLE, "");
			} else {
				store.setSessionProperty(LOCKHANDLE, null);
			}
		} catch (CoreException e) {
			status.add(e.getStatus());
		}

		RemoteFile file = (RemoteFile) item;
		try {

			ICacheService srv = getCacheService();
			IPath storePath = store.getPath();

			long localStamp = srv.getContentTimestamp(storePath);
			long remoteStamp = file.getTimestamp();

			// we need more elaborate checks here (read-only...)
			// we update remotely
			boolean syncOut = (direction != SyncDirection.INCOMING) && localStamp > remoteStamp;
			// we update locally
			boolean syncIn = (direction != SyncDirection.OUTGOING) && remoteStamp > localStamp;

			if (syncOut) {
				InputStream is = srv.getContent(storePath);
				OutputStream os = null;
				try {
					os = file.getOutputStream(false);
					Util.transferStreams(is, os, monitor);
				} finally {
					Util.safeClose(is);
					Util.safeClose(os);
				}
				// TODO test timestamps after sync out
				setResourceTimestamp(store, file.getTimestamp(), monitor);

			}

			if (syncIn) {

				srv.addContentWithTimestamp(storePath, new ByteArrayInputStream(file.getContent()), file.getTimestamp(),
						ISemanticFileSystem.NONE, monitor);

			}
		} catch (CoreException ce) {
			status.add(ce.getStatus());
		}

		try {
			IFileStore[] children = store.childStores(EFS.NONE, monitor);
			for (IFileStore childStore : children) {
				synchronizeContentWithRemote((ISemanticFileStore) childStore, direction, monitor, status);
			}
		} catch (CoreException e) {
			status.add(e.getStatus());
		}

	}

	public IStatus validateEdit(ISemanticFileStore[] stores, Object shell) {
		MultiStatus multi = new MultiStatus(TestPlugin.PLUGIN_ID, IStatus.OK, "Validate Edit Result", null);
		for (ISemanticFileStore store : stores) {

			RemoteItem item = getStore().getItemByPath(store.getPath().removeFirstSegments(2));
			if (item == null) {
				multi.add(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, "Resource not found"));
				continue;
			}
			if (item.getType() != Type.FILE) {
				multi.add(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, "Can only edit files"));
				continue;
			}
			try {
				lockResource(store, null);
				setReadOnly(store, false, null);
			} catch (CoreException e) {
				// $JL-EXC$
				multi.add(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, "Exception while locking resource"));
			}
		}
		return multi;
	}

	public IStatus validateRemoteCreate(ISemanticFileStore childStore, String childName, Object shell) {
		RemoteItem item = getStore().getItemByPath(childStore.getPath().removeFirstSegments(2));
		if (item != null) {
			return new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, "Resource already exists");
		}
		return null;
	}

	public IStatus validateRemoteDelete(ISemanticFileStore childStore, Object shell) {
		RemoteItem item = getStore().getItemByPath(childStore.getPath().removeFirstSegments(2));
		if (item == null) {
			return new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, "Resource not found");
		}
		return new Status(IStatus.OK, TestPlugin.PLUGIN_ID, "");
	}

	public IStatus validateSave(ISemanticFileStore childStore) {
		RemoteItem item = getStore().getItemByPath(childStore.getPath().removeFirstSegments(2));
		if (item == null) {
			return new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, "Resource not found");
		}
		return new Status(IStatus.OK, TestPlugin.PLUGIN_ID, "");
	}

	protected RemoteStoreTransient getStore() {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().findMember(getRootStore().getPath()).getProject();

		RemoteStoreTransient store = (RemoteStoreTransient) project.getAdapter(RemoteStoreTransient.class);

		return store;
	}

}
