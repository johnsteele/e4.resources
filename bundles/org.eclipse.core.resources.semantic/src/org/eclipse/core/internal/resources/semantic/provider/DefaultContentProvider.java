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
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
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
import org.eclipse.core.resources.semantic.spi.DefaultMinimalSemanticResourceRuleFactory;
import org.eclipse.core.resources.semantic.spi.FileCacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.ICacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderFederation;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderREST;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore.ResourceType;
import org.eclipse.core.resources.semantic.spi.ISemanticResourceRuleFactory;
import org.eclipse.core.resources.semantic.spi.ISemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.ISemanticTreeDeepFirstVisitor;
import org.eclipse.core.resources.semantic.spi.SemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.SemanticTreeWalker;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
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

	private static final String TRUE = "true"; //$NON-NLS-1$
	private static final QualifiedName RESOURCE_NOT_ACCESSIBLE = new QualifiedName(SemanticResourcesPlugin.PLUGIN_ID,
			"ResourceNotAccessible"); //$NON-NLS-1$
	private static final QualifiedName RESOURCE_NOT_ACCESSIBLE_MESSAGE = new QualifiedName(SemanticResourcesPlugin.PLUGIN_ID,
			"ResourceNotAccessibleMessage"); //$NON-NLS-1$

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

		newChild.setRemoteURIString(uri.toString());
		setReadOnly(newChild, true, monitor);

		MultiStatus stat = new MultiStatus(SemanticResourcesPlugin.PLUGIN_ID, IStatus.OK, NLS.bind(
				Messages.DefaultContentProvider_CacheFillError_XMSG, newChild.getPath().toString()), null);

		// ok since getResourceTimestamp is called only for files
		fillCache(newChild, monitor, stat);

		if (!stat.isOK()) {
			newChild.remove(monitor);
			throw new CoreException(stat);
		}

	}

	public void addFolderFromRemoteByURI(ISemanticFileStore childStore, String name, URI uri, IProgressMonitor monitor)
			throws CoreException {
		childStore.addChildResource(name, true, getFederatedProviderIDForPath(childStore.getPath().append(name)), null);
		ISemanticFileStore newChild = (ISemanticFileStore) childStore.getChild(name);

		newChild.setRemoteURIString(uri.toString());
		setReadOnly(newChild, true, monitor);
	}

	public String getURIString(ISemanticFileStore childStore) throws CoreException {
		return childStore.getRemoteURIString();
	}

	public void setURIString(ISemanticFileStore semanticFileStore, URI uri, IProgressMonitor monitor) throws CoreException {

		if (semanticFileStore.getType() == ISemanticFileStore.FILE) {
			semanticFileStore.setRemoteURIString(uri.toString());
			setReadOnly(semanticFileStore, true, monitor);

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

		boolean existsRemotely = false;
		if (SemanticSpiResourceInfo.isOptionRequested(ISemanticFileSystem.RESOURCE_INFO_EXISTS_REMOTELY, options)) {

			String remoteURI = getURIString(semanticFileStore);

			if (remoteURI != null) {
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
				} catch (IllegalArgumentException e) {
					// $JL-EXC$ ignore and simply return false here
				} catch (MalformedURLException e) {
					// $JL-EXC$ ignore and simply return false here
				} catch (IOException e) {
					// $JL-EXC$ ignore and simply return false here
				}
			}
		}
		return new SemanticSpiResourceInfo(options, false, false, readOnly, existsRemotely, uriString, null);
	}

	@Override
	public InputStream openInputStreamInternal(ISemanticFileStore childStore, IProgressMonitor monitor, ICacheTimestampSetter setter)
			throws CoreException {

		String remoteURI = this.getURIString(childStore);

		if (childStore.getPersistentProperty(RESOURCE_NOT_ACCESSIBLE) != null) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_CONNECT_EXCEPTION, childStore.getPath(), childStore
					.getPersistentProperty(RESOURCE_NOT_ACCESSIBLE_MESSAGE));
		}

		if (remoteURI == null) {
			// TODO 0.1: this will happen upon folder move in the default
			// content provider

			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_URI_NOT_FOUND, childStore.getPath(), NLS.bind(
					Messages.DefaultContentProvider_RemotURINotSet_XMSG, childStore.getPath().toString()));
		}

		try {
			final URI uri = new URI(remoteURI);

			URL url = uri.toURL();

			URLConnection conn = url.openConnection();
			if (conn.getLastModified() != 0) {
				setter.setTimestamp(conn.getLastModified());
			} else {
				setter.setTimestamp(conn.getDate());
			}

			InputStream is = conn.getInputStream();
			clearStateResourceNotAccessible(childStore);
			return is;
		} catch (UnknownHostException e) {
			setStateResourceNotAccessible(childStore, e);
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_CONNECT_EXCEPTION, childStore.getPath(), NLS.bind(
					Messages.DefaultContentProvider_UnknownHostError_XMSG, e.getMessage()), e);
		} catch (IOException e) {
			setStateResourceNotAccessible(childStore, e);
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_CONNECT_EXCEPTION, childStore.getPath(), e.getMessage(),
					e);
		} catch (URISyntaxException e) {
			setStateResourceNotAccessible(childStore, e);
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_CONNECT_EXCEPTION, childStore.getPath(), e.getMessage(),
					e);
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
				try {
					clearStateResourceNotAccessible(semanticFileStore);
				} catch (CoreException e) {
					status.add(e.getStatus());
					return;
				}
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

	@Override
	public ISemanticResourceRuleFactory getRuleFactory() {
		return new DefaultMinimalSemanticResourceRuleFactory(this.getRootStore());
		/*
		 * return new ISemanticResourceRuleFactory() {
		 * 
		 * public ISemanticFileStore validateEditRule(ISemanticFileStore[]
		 * stores) { if (stores.length == 1) { return stores[0]; } return null;
		 * }
		 * 
		 * private ISemanticFileStore getParent(ISemanticFileStore store) { if
		 * (store.getType() == ISemanticFileStore.PROJECT) { return store; }
		 * return (ISemanticFileStore) store.getParent(); }
		 * 
		 * public ISemanticFileStore refreshRule(ISemanticFileStore store) {
		 * return getParent(store); }
		 * 
		 * public ISemanticFileStore moveRule(ISemanticFileStore source,
		 * ISemanticFileStore destination) { return null; }
		 * 
		 * public ISemanticFileStore modifyRule(ISemanticFileStore store) {
		 * return store; }
		 * 
		 * public ISemanticFileStore deleteRule(ISemanticFileStore store) {
		 * return getParent(store); }
		 * 
		 * public ISemanticFileStore createRule(ISemanticFileStore store) {
		 * return getParent(store); }
		 * 
		 * public ISemanticFileStore copyRule(ISemanticFileStore source,
		 * ISemanticFileStore destination) { return null; }
		 * 
		 * public ISemanticFileStore charsetRule(ISemanticFileStore store) {
		 * return store; } };
		 */
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

				if (contentProviderID == null) {
					// mark own children read only
					ISemanticFileStore newChild = (ISemanticFileStore) parentStore.getChild(name);
					setReadOnly(newChild, true, new NullProgressMonitor());
				}
				break;
			case FOLDER_TYPE :
				parentStore.addChildResource(name, true, contentProviderID, properties);
				break;
			default :
				// TODO 0.1 error handling
				break;
		}
	}

	private void setStateResourceNotAccessible(ISemanticFileStore childStore, Exception e) throws CoreException {
		childStore.setPersistentProperty(RESOURCE_NOT_ACCESSIBLE, TRUE);

		if (e instanceof UnknownHostException) {
			childStore.setPersistentProperty(RESOURCE_NOT_ACCESSIBLE_MESSAGE, NLS.bind(Messages.DefaultContentProvider_UnknownHostError_XMSG, e
					.getMessage()));
		} else {
			childStore.setPersistentProperty(RESOURCE_NOT_ACCESSIBLE_MESSAGE, e.getMessage());
		}
	}

	private void clearStateResourceNotAccessible(ISemanticFileStore semanticFileStore) throws CoreException {
		semanticFileStore.setPersistentProperty(RESOURCE_NOT_ACCESSIBLE, null);
		semanticFileStore.setPersistentProperty(RESOURCE_NOT_ACCESSIBLE_MESSAGE, null);
	}

}
