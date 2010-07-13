/*******************************************************************************
 * Copyright (c) 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eduard Bartsch (SAP AG) - initial API and implementation
 *    Mathias Kinzler (SAP AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.e4twitterclient.provider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.spi.CachingContentProvider;
import org.eclipse.core.resources.semantic.spi.DefaultMinimalSemanticResourceRuleFactory;
import org.eclipse.core.resources.semantic.spi.FileCacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.ICacheService;
import org.eclipse.core.resources.semantic.spi.ICacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderREST;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore.ResourceType;
import org.eclipse.core.resources.semantic.spi.ISemanticResourceRuleFactory;
import org.eclipse.core.resources.semantic.spi.ISemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.SemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.demo.e4twitterclient.Activator;
import org.eclipse.osgi.util.NLS;

public class SampleTwitterContentProvider extends CachingContentProvider implements ISemanticContentProviderREST {

	@Override
	public ISemanticResourceRuleFactory getRuleFactory() {
		return new DefaultMinimalSemanticResourceRuleFactory(this.getRootStore());
	}

	@Override
	public ICacheServiceFactory getCacheServiceFactory() {
		return new FileCacheServiceFactory();
	}

	public IStatus validateEdit(ISemanticFileStore[] semanticFileStores, Object shell) {
		// optimistic checkout without locking
		try {
			for (ISemanticFileStore iSemanticFileStore : semanticFileStores) {
				if (this.isReadOnlyInternal(iSemanticFileStore)) {
					this.setReadOnlyInternal(iSemanticFileStore, false);
				}
			}
			return Status.OK_STATUS;
		} catch (CoreException e) {
			// return CANCEL in this case
		}
		return Status.CANCEL_STATUS;
	}

	public IStatus validateSave(ISemanticFileStore semanticFileStore) {
		try {
			if (!this.isReadOnlyInternal(semanticFileStore)) {
				return Status.OK_STATUS;
			}
		} catch (CoreException e) {
			// return CANCEL in this case
		}
		return Status.CANCEL_STATUS;
	}

	@Override
	public void onRootStoreCreate(ISemanticFileStore newStore) {
		try {
			if (newStore.getType() == ISemanticFileStore.FILE) {
				this.setReadOnlyInternal(newStore, true);
			}
		} catch (CoreException e) {
			// ignore and log
		}
	}

	public void addResource(ISemanticFileStore parentStore, String name, ResourceType resourceType, IProgressMonitor monitor) {
		// ignored
	}

	public void addFileFromRemoteByURI(ISemanticFileStore parentStore, String name, URI uri, IProgressMonitor monitor) throws CoreException {
		parentStore.addChildFile(name);

		ISemanticFileStore newChild = (ISemanticFileStore) parentStore.getChild(name);

		String uriString = uri.toString();

		newChild.setRemoteURIString(uriString);
		this.setReadOnlyInternal(newChild, true);

		try {
			updateSingleFile(SyncDirection.INCOMING, newChild, uri, monitor);
		} catch (CoreException e) {
			deleteCache(newChild, monitor);
			newChild.remove(monitor);
			throw e;
		}
	}

	public void addFolderFromRemoteByURI(ISemanticFileStore parentStore, String name, URI uri, IProgressMonitor monitor) {
		// not supported
	}

	private void updateSingleFile(SyncDirection direction, ISemanticFileStore store, URI rootURI, IProgressMonitor monitor)
			throws CoreException {
		if (direction.equals(SyncDirection.INCOMING) || direction.equals(SyncDirection.BOTH)) {
			this.deleteCache(store, monitor);
			Util.safeClose(openInputStream(store, monitor));
		}
	}

	InputStream getCachedContent(final ISemanticFileStore childStore, IProgressMonitor monitor) throws CoreException {
		ICacheService cacheService = this.getCacheService();

		IPath path = childStore.getPath();

		if (!cacheService.hasContent(path)) {
			throw new SemanticResourceException(SemanticResourceStatusCode.CACHED_CONTENT_NOT_FOUND, childStore.getPath(), null);
		}

		return cacheService.getContent(path);
	}

	public void synchronizeContentWithRemote(ISemanticFileStore store, final SyncDirection direction, final IProgressMonitor monitor,
			final MultiStatus status) {
		try {
			if (store.getType() == ISemanticFileStore.FILE) {
				dropCache(store, monitor, deleteAllVisitor, status);
				fillCache(store, monitor, status);
			} else {
				IFileStore[] existingChildren = store.childStores(EFS.NONE, monitor);

				for (IFileStore child : existingChildren) {
					synchronizeContentWithRemote((ISemanticFileStore) child, direction, monitor, status);
				}
			}
		} catch (CoreException e) {
			IStatus innerStatus = e.getStatus();
			IStatus newStatus = new Status(innerStatus.getSeverity(), innerStatus.getPlugin(), e.getMessage(), e);
			status.add(newStatus);
			return;
		}
	}

	URI getURIForStore(ISemanticFileStore store) throws CoreException {
		String remoteURI = store.getRemoteURIString();

		if (remoteURI == null) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_URI_NOT_FOUND, store.getPath(), NLS.bind(
					"No URI set for store {0}", store.getPath().toString()));
		}

		try {
			URI uri = new URI(remoteURI);
			return uri;
		} catch (URISyntaxException e) {
			throw new SemanticResourceException(SemanticResourceStatusCode.INVALID_URI_SYNTAX, store.getPath(), NLS.bind(
					"Invalid URI {1} for store {0}", store.getPath().toString(), remoteURI), e);
		}
	}

	public void removeResource(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException {
		this.deleteCache(semanticFileStore, monitor);

		semanticFileStore.remove(monitor);
	}

	public void revertChanges(ISemanticFileStore store, IProgressMonitor monitor) {
		// ignore
	}

	public void setReadOnly(ISemanticFileStore semanticFileStore, boolean readonly, IProgressMonitor monitor) throws CoreException {
		this.setReadOnlyInternal(semanticFileStore, readonly);
	}

	public ISemanticSpiResourceInfo fetchResourceInfo(ISemanticFileStore semanticFileStore, int options, IProgressMonitor monitor)
			throws CoreException {
		String uriString;
		if (SemanticSpiResourceInfo.isOptionRequested(ISemanticFileSystem.RESOURCE_INFO_URI_STRING, options)) {
			uriString = semanticFileStore.getRemoteURIString();
		} else {
			uriString = null;
		}

		boolean readOnly;
		if (SemanticSpiResourceInfo.isOptionRequested(ISemanticFileSystem.RESOURCE_INFO_READ_ONLY, options)) {
			readOnly = isReadOnlyInternal(semanticFileStore);
		} else {
			readOnly = false;
		}

		return new SemanticSpiResourceInfo(options, false, false, readOnly, false, uriString,
				this.getContentTypeInternal(semanticFileStore));
	}

	@Override
	public InputStream openInputStreamInternal(final ISemanticFileStore store, IProgressMonitor monitor,
			final ICacheTimestampSetter timeStampSetter) throws CoreException {
		String remoteURI = store.getRemoteURIString();

		if (remoteURI == null) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_URI_NOT_FOUND, store.getPath(), NLS.bind(
					"Remote URI not set for store {0}", store.getPath().toString()));
		}

		try {
			InputStream is = HTTPClientUtil.openInputStream(remoteURI, timeStampSetter);

			return is;
		} catch (UnknownHostException e) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_CONNECT_EXCEPTION, store.getPath(), NLS.bind(
					"Unknown host {0}", e.getMessage()), e);
		} catch (IOException e) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_CONNECT_EXCEPTION, store.getPath(), e.getMessage(), e);
		}
	}

	public String getURIString(ISemanticFileStore semanticFileStore) throws CoreException {
		return semanticFileStore.getRemoteURIString();
	}

	public void setURIString(ISemanticFileStore store, URI uri, IProgressMonitor monitor) throws CoreException {
		store.setRemoteURIString(uri.toString());

		MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, IStatus.OK, "", null);
		dropCache(store, monitor, deleteAllVisitor, status);
		fillCache(store, monitor, status);
		if (!status.isOK()) {
			throw new CoreException(status);
		}
	}

}
