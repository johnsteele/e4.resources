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
package org.eclipse.core.internal.resources.semantic.provider;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.resources.semantic.SemanticResourcesPlugin;
import org.eclipse.core.internal.resources.semantic.spi.SemanticFileSystemSpiCore;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.spi.CachingContentProvider;
import org.eclipse.core.resources.semantic.spi.FileCacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.ICacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderFederation;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderREST;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.ISemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.ISemanticTreeDeepFirstVisitor;
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

/**
 * The default content provider.
 * <p>
 * TODO 0.1: document
 * 
 */
public class DefaultContentProvider extends CachingContentProvider implements ISemanticContentProviderFederation,
		ISemanticContentProviderREST {

	public String getFederatedProviderIDForPath(IPath path) {
		IPath checkPath = path.removeFirstSegments(getRootStore().getPath().segmentCount());
		return SemanticFileSystemSpiCore.getInstance().getFolderTemplateMapping(checkPath);
	}

	public void revertChanges(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException {
		throw new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, semanticFileStore.getPath(),
				Messages.DefaultContentProvider_NotSupported_XMSG);
	}

	public void addFileFromRemoteByURI(ISemanticFileStore childStore, String name, URI uri, IProgressMonitor monitor) throws CoreException {
		// create internal data
		childStore.addChildResource(name, false, getFederatedProviderIDForPath(childStore.getPath().append(name)), null);
		ISemanticFileStore newChild = (ISemanticFileStore) childStore.getChild(name);

		this.setURIStringInternal(newChild, uri.toString());
		setReadOnly(newChild, true, monitor);
	}

	public void addFolderFromRemoteByURI(ISemanticFileStore childStore, String name, URI uri, IProgressMonitor monitor)
			throws CoreException {
		childStore.addChildResource(name, true, getFederatedProviderIDForPath(childStore.getPath().append(name)), null);
		ISemanticFileStore newChild = (ISemanticFileStore) childStore.getChild(name);

		this.setURIStringInternal(newChild, uri.toString());
		setReadOnly(newChild, true, monitor);
	}

	public String getURIString(ISemanticFileStore childStore) throws CoreException {
		return getURIStringInternal(childStore);
	}

	public void setURIString(ISemanticFileStore semanticFileStore, URI uri, IProgressMonitor monitor) throws CoreException {

		if (semanticFileStore.getType() == ISemanticFileStore.FILE) {
			this.setURIStringInternal(semanticFileStore, uri.toString());
			this.deleteCache(semanticFileStore, monitor);

			MultiStatus status = new MultiStatus(SemanticResourcesPlugin.PLUGIN_ID, IStatus.OK, NLS.bind(
					Messages.DefaultContentProvider_CacheFillError_XMSG, uri.toString()), null);
			this.fillCache(semanticFileStore, monitor, status);

			if (!status.isOK()) {
				throw new CoreException(status);
			}
		} else {
			throw new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, semanticFileStore.getPath(),
					Messages.DefaultContentProvider_NotSupported_XMSG);
		}
	}

	/**
	 * @throws CoreException
	 */
	@Override
	public ICacheServiceFactory getCacheServiceFactory() throws CoreException {
		return new FileCacheServiceFactory();
	}

	public ISemanticSpiResourceInfo fetchResourceInfo(ISemanticFileStore semanticFileStore, int options, IProgressMonitor monitor)
			throws CoreException {

		String uriString;
		if (SemanticSpiResourceInfo.isOptionRequested(ISemanticFileSystem.RESOURCE_INFO_URI_STRING, options)) {
			uriString = this.getURIStringInternal(semanticFileStore);
		} else {
			uriString = null;
		}
		boolean readOnly;
		if (SemanticSpiResourceInfo.isOptionRequested(ISemanticFileSystem.RESOURCE_INFO_READ_ONLY, options)) {
			readOnly = isReadOnlyInternal(semanticFileStore);
		} else {
			readOnly = false;
		}

		boolean existsRemotely = false;
		if (SemanticSpiResourceInfo.isOptionRequested(ISemanticFileSystem.RESOURCE_INFO_EXISTS_REMOTELY, options)) {

			String remoteURI = getURIString(semanticFileStore);

			if (remoteURI == null) {
				throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_URI_NOT_FOUND, semanticFileStore.getPath(), NLS.bind(
						Messages.DefaultContentProvider_RemotURINotSet_XMSG, semanticFileStore.getPath().toString()));
			}

			final URI uri = URI.create(remoteURI);

			URL url;
			try {
				url = uri.toURL();
				InputStream is = null;
				try {
					is = url.openConnection().getInputStream();
					existsRemotely = is != null;
				} finally {
					Util.safeClose(is);
				}
			} catch (MalformedURLException e) {
				// $JL-EXC$ ignore and simply return false here
			} catch (IOException e) {
				// $JL-EXC$ ignore and simply return false here
			}

		}
		return new SemanticSpiResourceInfo(options, false, false, readOnly, existsRemotely, uriString, null);
	}

	@Override
	public InputStream openInputStreamInternal(ISemanticFileStore childStore, IProgressMonitor monitor, ICacheTimestampSetter setter)
			throws CoreException {

		String remoteURI = this.getURIString(childStore);

		if (remoteURI == null) {
			// TODO 0.1: this will happen upon folder move in the default
			// content
			// provider

			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_URI_NOT_FOUND, childStore.getPath(), NLS.bind(
					Messages.DefaultContentProvider_RemotURINotSet_XMSG, childStore.getPath().toString()));
		}

		try {
			final URI uri = URI.create(remoteURI);

			URL url = uri.toURL();

			URLConnection conn = url.openConnection();
			if (conn.getLastModified() != 0) {
				setter.setTimestamp(conn.getLastModified());
			} else {
				setter.setTimestamp(conn.getDate());
			}

			return conn.getInputStream();
		} catch (IOException e) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_CONNECT_EXCEPTION, childStore.getPath(), null, e);
		}
	}

	public void setReadOnly(ISemanticFileStore childStore, boolean readonly, IProgressMonitor monitor) throws CoreException {
		this.setReadOnlyInternal(childStore, readonly);
	}

	public void removeResource(ISemanticFileStore childStore, IProgressMonitor monitor) throws CoreException {

		ISemanticTreeDeepFirstVisitor visitor = new ISemanticTreeDeepFirstVisitor() {

			public void visit(ISemanticFileStore store, IProgressMonitor actMonitor) throws CoreException {
				if (store.getContentProviderID() == null || store.getPath().equals(getRootStore().getPath())) {

					if (store.getType() == ISemanticFileStore.FILE) {

						try {
							getCacheService().removeContent(store.getPath(), actMonitor);
						} catch (CoreException ce) {
							// $JL-EXC$ TODO 0.1: trace and continue
						}
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

		SemanticTreeWalker.accept(childStore, visitor, monitor);

	}

	public void synchronizeContentWithRemote(ISemanticFileStore semanticFileStore, SyncDirection direction, IProgressMonitor monitor,
			MultiStatus status) {

		if (semanticFileStore.getType() == ISemanticFileStore.FILE) {
			if (semanticFileStore.isLocalOnly()) {
				return;
			}
			try {
				if (!isReadOnlyInternal(semanticFileStore)) {
					return;
				}
			} catch (CoreException e) {
				status.add(e.getStatus());
				return;
			}
			// no outgoing sync
			if (direction == SyncDirection.INCOMING || direction == SyncDirection.BOTH) {
				this.dropCache(semanticFileStore, monitor, this.deleteAllVisitor, status);
				this.fillCache(semanticFileStore, monitor, status);
			}
		} else {
			// folders and projects: we need to look at depth

			IFileStore[] childStores;
			try {
				childStores = semanticFileStore.childStores(EFS.NONE, monitor);
			} catch (CoreException e) {
				status.add(e.getStatus());
				return;
			}
			for (IFileStore store : childStores) {
				if (store instanceof ISemanticFileStore) {
					// check if we have federation
					ISemanticFileStore sfs = (ISemanticFileStore) store;
					String providerId = sfs.getContentProviderID();
					if (providerId == null) {
						synchronizeContentWithRemote(sfs, direction, monitor, status);
					} else {
						try {
							sfs.getEffectiveContentProvider().synchronizeContentWithRemote(sfs, direction, monitor, status);
						} catch (CoreException e) {
							status.add(e.getStatus());
						}
					}
				}
			}

		}
	}

	public IStatus validateEdit(ISemanticFileStore[] stores, Object shell) {
		// silently mark as checked out
		MultiStatus status = new MultiStatus(SemanticResourcesPlugin.PLUGIN_ID, IStatus.OK,
				Messages.DefaultContentProvider_ValidateEditResult_XGRP, null);

		for (ISemanticFileStore store : stores) {
			// if all stores are local-only then we return OK, otherwise CANCEL
			if (!store.isLocalOnly()) {
				return new Status(IStatus.CANCEL, SemanticResourcesPlugin.PLUGIN_ID, Messages.DefaultContentProvider_NoRemoteEdit_XMSG);
			}
			try {
				setReadOnly(store, false, null);
			} catch (CoreException e) {
				status.add(new Status(IStatus.ERROR, SemanticResourcesPlugin.PLUGIN_ID, e.getMessage(), e));
			}
		}
		return status;
	}

	public IStatus validateSave(ISemanticFileStore childStore) {
		return validateEdit(new ISemanticFileStore[] {childStore}, null);
	}

	// not supported stuff

	public void addResource(ISemanticFileStore parentStore, String name, ResourceType resourceType, IProgressMonitor monitor)
			throws CoreException {
		throw new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, parentStore.getPath(),
				Messages.DefaultContentProvider_NotSupported_XMSG);
	}

	public void addResource(ISemanticFileStore parentStore, String name, ResourceType resourceType, String contentProviderID,
			Map<QualifiedName, String> properties) throws CoreException {
		switch (resourceType) {
			case FILE_TYPE :
				parentStore.addChildResource(name, false, contentProviderID, properties);
				break;
			case FOLDER_TYPE :
				parentStore.addChildResource(name, true, contentProviderID, properties);
				break;
			default :
				// TODO 0.1 error handling
				break;
		}
	}

}
