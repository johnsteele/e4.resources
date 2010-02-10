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
import java.io.UnsupportedEncodingException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
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
import org.eclipse.core.resources.semantic.spi.ContentProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderLocking;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderRemote;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.ISemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.SemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore.ResourceType;
import org.eclipse.core.resources.semantic.test.TestPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;

/**
 * Non-caching
 * 
 */
public class PlainTestContentProvider extends ContentProvider implements ISemanticContentProviderRemote, ISemanticContentProviderLocking {

	private static final QualifiedName LOCKHANDLE = new QualifiedName(TestPlugin.PLUGIN_ID, "LockHandle");
	private static final QualifiedName READONLY = new QualifiedName(TestPlugin.PLUGIN_ID, "ReadOnly");
	private static final QualifiedName TIMESTAMP = new QualifiedName(TestPlugin.PLUGIN_ID, "Timestamp");

	public void addResource(ISemanticFileStore parentStore, String name, ResourceType resourceType, IProgressMonitor monitor)
	throws CoreException {
		RemoteItem parentItem = getStore().getItemByPath(parentStore.getPath().removeFirstSegments(2));
		if (parentItem.getType() != Type.FOLDER) {
			throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, parentItem.getPath(), "");
		}
		RemoteItem item = ((RemoteFolder) parentItem).getChild(name);
		if (item == null) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_RESOURCE_NOT_FOUND, parentStore.getPath(),
			"No such resource");
		}
		switch (resourceType) {
		case UNKNOWN_TYPE:
			if (item.getType() == Type.FILE) {
				parentStore.addChildFile(name);
				ISemanticFileStore newStore = (ISemanticFileStore) parentStore.getChild(name);
				setReadOnly(newStore, true, monitor);
				newStore.setPersistentProperty(TIMESTAMP, Long.toString(((RemoteFile) item).getTimestamp()));
			} else if (item.getType() == Type.FOLDER) {
				parentStore.addChildFolder(name);
			} else {
				Status result = new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, "Unknown resource type");
				throw new CoreException(result);
			}
			break;
		case FILE_TYPE:
			if (item.getType() != RemoteItem.Type.FILE) {
				throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, item.getPath(), "");
			}
			parentStore.addChildFile(name);
			ISemanticFileStore newStore = (ISemanticFileStore) parentStore.getChild(name);
			setReadOnly(newStore, true, monitor);
			newStore.setPersistentProperty(TIMESTAMP, Long.toString(((RemoteFile) item).getTimestamp()));
			break;
		case FOLDER_TYPE:
			if (item.getType() != RemoteItem.Type.FOLDER) {
				throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, item.getPath(), "");
			}
			parentStore.addChildFolder(name);
			break;
		case PROJECT_TYPE:
			throw new SemanticResourceException(SemanticResourceStatusCode.INVALID_RESOURCE_TYPE, item.getPath(), "");
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
			if (context == null) {
				buffer = new byte[0];
			} else {
				try {
					buffer = context.toString().getBytes("UTF-8");
				} catch (UnsupportedEncodingException e) {
					// $JL-EXC$
					buffer = new byte[0];
				}
			}

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
		childStore.setSessionProperty(LOCKHANDLE, "");
		return new Status(IStatus.OK, TestPlugin.PLUGIN_ID, "");
	}

	public IStatus unlockResource(ISemanticFileStore childStore, IProgressMonitor monitor) throws CoreException {
		childStore.setSessionProperty(LOCKHANDLE, null);
		return new Status(IStatus.OK, TestPlugin.PLUGIN_ID, "");
	}

	public ISemanticSpiResourceInfo fetchResourceInfo(ISemanticFileStore semanticFileStore, int options, IProgressMonitor monitor)
	throws CoreException {
		return new SemanticSpiResourceInfo(options,
				semanticFileStore
				.getSessionProperty(LOCKHANDLE) != null, true, semanticFileStore
				.getPersistentProperty(READONLY) != null,
				getStore().getItemByPath(semanticFileStore.getPath().removeFirstSegments(2)) != null,
				null, null);
	}

	public InputStream openInputStream(ISemanticFileStore childStore, IProgressMonitor monitor) throws CoreException {
		RemoteItem item = getStore().getItemByPath(childStore.getPath().removeFirstSegments(2));
		if (item.getType() != Type.FILE) {
			throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, item.getPath(), "");
		}
		return new ByteArrayInputStream(((RemoteFile) item).getContent());
	}

	public OutputStream openOutputStream(ISemanticFileStore childStore, int options, IProgressMonitor monitor) throws CoreException {

		RemoteItem item = getStore().getItemByPath(childStore.getPath().removeFirstSegments(2));
		if (item.getType() != Type.FILE) {
			throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, item.getPath(), "");
		}
		boolean append = (options & ISemanticFileSystem.CONTENT_APPEND) > 0;
		return ((RemoteFile) item).getOutputStream(append);

	}

	public void removeResource(ISemanticFileStore childStore, IProgressMonitor monitor) throws CoreException {
		childStore.remove(monitor);
	}

	public void revertChanges(ISemanticFileStore childStore, IProgressMonitor monitor) throws CoreException {
		RemoteItem item = getStore().getItemByPath(childStore.getPath().removeFirstSegments(2));
		if (item.getType() != Type.FILE) {
			throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, item.getPath(), "");
		}
		((RemoteFile) item).getOutputStream(false).reset();

	}

	public void setReadOnly(ISemanticFileStore childStore, boolean readonly, IProgressMonitor monitor) throws CoreException {
		if (readonly) {
			childStore.setPersistentProperty(READONLY, "");
		} else {
			childStore.setPersistentProperty(READONLY, null);
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
				//$JL-EXC$
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

	public long getResourceTimestamp(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException {

		String test = semanticFileStore.getPersistentProperty(TIMESTAMP);
		if (test != null) {
			try {
				return Long.parseLong(test);
			} catch (NumberFormatException e) {
				// $JL-EXC$
				return 0;
			}
		}
		return 0;
	}


	public void setResourceTimestamp(ISemanticFileStore semanticFileStore, long timestamp, IProgressMonitor monitor) throws CoreException {

		semanticFileStore.setPersistentProperty(TIMESTAMP, Long.toString(timestamp));
	}

	public void synchronizeContentWithRemote(ISemanticFileStore store, SyncDirection direction,
			IProgressMonitor monitor, MultiStatus status) {
		// do state sync at least

		RemoteItem item = getStore().getItemByPath(store.getPath().removeFirstSegments(2));
		try {
			if (item.isLocked()) {
				store.setSessionProperty(LOCKHANDLE, "");
			} else {
				store.setSessionProperty(LOCKHANDLE, null);
			}
		} catch (CoreException e) {
			status.add(e.getStatus());
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

	protected RemoteStoreTransient getStore() {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().findMember(getRootStore().getPath()).getProject();
		return (RemoteStoreTransient) project.getAdapter(RemoteStoreTransient.class);
	}
}
