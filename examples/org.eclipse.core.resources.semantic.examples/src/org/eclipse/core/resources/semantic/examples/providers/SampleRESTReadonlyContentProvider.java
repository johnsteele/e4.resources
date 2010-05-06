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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.examples.providers.RESTUtil.IRESTCallback;
import org.eclipse.core.resources.semantic.examples.remote.SemanticResourcesPluginExamplesCore;
import org.eclipse.core.resources.semantic.spi.CachingContentProvider;
import org.eclipse.core.resources.semantic.spi.FileCacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.ICacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderREST;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore.ResourceType;
import org.eclipse.core.resources.semantic.spi.ISemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.SemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * A read-only REST content provider
 * 
 */
public class SampleRESTReadonlyContentProvider extends CachingContentProvider implements ISemanticContentProviderREST {

	/**
	 * @throws CoreException
	 */
	@Override
	public ICacheServiceFactory getCacheServiceFactory() throws CoreException {
		return new FileCacheServiceFactory();
	}

	@Override
	public InputStream openInputStreamInternal(final ISemanticFileStore store, IProgressMonitor monitor,
			final ICacheTimestampSetter timeStampSetter) throws CoreException {
		String remoteURI = this.getURIStringInternal(store);

		if (remoteURI == null) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_URI_NOT_FOUND, store.getPath(), NLS.bind(
					Messages.SampleRESTReadonlyContentProvider_RemoteUriNotSet_XMSG, store.getPath().toString()));
		}

		try {
			return RESTUtil.openInputStream(remoteURI, new IRESTCallback() {
				public void setTimestamp(long timestamp) {
					timeStampSetter.setTimestamp(timestamp);
				}

				public void setContentType(String contentType) {
					try {
						SampleRESTReadonlyContentProvider.this.setContentTypeInternal(store, contentType);
					} catch (CoreException e) {
						// $JL-EXC$
						e.printStackTrace();
					}
				}

			});
		} catch (IOException e) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_CONNECT_EXCEPTION, store.getPath(), e.getMessage(), e);
		}
	}

	public void addResource(ISemanticFileStore parentStore, String name, ResourceType resourceType, IProgressMonitor monitor)
			throws CoreException {
		switch (resourceType) {
			case FOLDER_TYPE :
				parentStore.addChildFolder(name);
				break;
			default :
				throw new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, parentStore.getPath(),
						Messages.SampleRESTReadonlyContentProvider_MethodNotSupported_XMSG);
		}
	}

	public ISemanticSpiResourceInfo fetchResourceInfo(ISemanticFileStore semanticFileStore, int options, IProgressMonitor monitor)
			throws CoreException {
		String uriString;
		if (SemanticSpiResourceInfo.isOptionRequested(ISemanticFileSystem.RESOURCE_INFO_URI_STRING, options)) {
			uriString = this.getURIStringInternal(semanticFileStore);
		} else {
			uriString = null;
		}

		boolean existsRemotely = false;
		if (SemanticSpiResourceInfo.isOptionRequested(ISemanticFileSystem.RESOURCE_INFO_EXISTS_REMOTELY, options)) {

			String remoteURI = getURIString(semanticFileStore);

			if (remoteURI == null) {
				throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_URI_NOT_FOUND, semanticFileStore.getPath(), NLS.bind(
						Messages.SampleRESTReadonlyContentProvider_RemoteUriNotSet_XMSG, semanticFileStore.getPath().toString()));
			}

			try {
				InputStream is = RESTUtil.openInputStream(remoteURI, null);
				existsRemotely = is != null;
				Util.safeClose(is);
			} catch (IOException e) {
				// $JL-EXC$ ignore and simply return false here
			}

		}
		return new SemanticSpiResourceInfo(options, false, false, true, existsRemotely, uriString, this
				.getContentTypeInternal(semanticFileStore));
	}

	public void removeResource(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException {
		this.deleteCache(semanticFileStore, monitor);

		semanticFileStore.remove(monitor);
	}

	/**
	 * @throws CoreException
	 */
	public void revertChanges(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException {
		// silently ignore since there is nothing to revert because no editing
		// allowed

	}

	public void setReadOnly(ISemanticFileStore childStore, boolean readonly, IProgressMonitor monitor) throws CoreException {
		this.setReadOnlyInternal(childStore, readonly);
	}

	public void synchronizeContentWithRemote(ISemanticFileStore semanticFileStore, SyncDirection direction, IProgressMonitor monitor,
			MultiStatus status) {
		if (semanticFileStore.getType() == ISemanticFileStore.FILE) {
			if (semanticFileStore.isLocalOnly()) {
				return;
			}

			if (direction == SyncDirection.INCOMING || direction == SyncDirection.BOTH) {
				this.dropCache(semanticFileStore, monitor, this.deleteAllVisitor, status);
				this.fillCache(semanticFileStore, monitor, status);
			}
		} else {
			IFileStore[] childStores;
			try {
				childStores = semanticFileStore.childStores(EFS.NONE, monitor);
			} catch (CoreException e) {
				status.add(e.getStatus());
				return;
			}
			for (IFileStore store : childStores) {
				if (store instanceof ISemanticFileStore) {
					synchronizeContentWithRemote((ISemanticFileStore) store, direction, monitor, status);
				}
			}
		}
	}

	public IStatus validateEdit(ISemanticFileStore[] stores, Object shell) {
		// silently reject
		return new Status(IStatus.CANCEL, SemanticResourcesPluginExamplesCore.PLUGIN_ID, null);
	}

	public IStatus validateSave(ISemanticFileStore semanticFileStore) {
		// silently reject
		return new Status(IStatus.CANCEL, SemanticResourcesPluginExamplesCore.PLUGIN_ID, null);
	}

	public void addFileFromRemoteByURI(ISemanticFileStore parentStore, String name, URI uri, IProgressMonitor monitor) throws CoreException {
		parentStore.addChildFile(name);
		ISemanticFileStore newChild = (ISemanticFileStore) parentStore.getChild(name);

		this.setURIStringInternal(newChild, uri.toString());
		setReadOnly(newChild, true, monitor);
	}

	public void addFolderFromRemoteByURI(ISemanticFileStore parentStore, String name, URI uri, IProgressMonitor monitor)
			throws CoreException {
		throw new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, parentStore.getPath(),
				Messages.SampleRESTReadonlyContentProvider_MethodNotSupported_XMSG);
	}

	public String getURIString(ISemanticFileStore semanticFileStore) throws CoreException {
		return this.getURIStringInternal(semanticFileStore);
	}

	public void setURIString(ISemanticFileStore semanticFileStore, URI uri, IProgressMonitor monitor) throws CoreException {
		this.setURIStringInternal(semanticFileStore, uri.toString());

		// TODO 0.1: what to do on folders?
		if (semanticFileStore.getType() == ISemanticFileStore.FILE) {
			this.deleteCache(semanticFileStore, monitor);
			MultiStatus status = new MultiStatus(SemanticResourcesPluginExamplesCore.PLUGIN_ID, IStatus.OK, NLS.bind(
					Messages.SampleRESTReadonlyContentProvider_MethodResult_XMSG, "setURISting"), null); //$NON-NLS-1$
			this.fillCache(semanticFileStore, monitor, status);

			if (!status.isOK()) {
				throw new CoreException(status);
			}
		}
	}
}
