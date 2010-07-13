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
package org.eclipse.core.resources.semantic.examples.providers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.examples.remote.SemanticResourcesPluginExamplesCore;
import org.eclipse.core.resources.semantic.examples.webdav.WebDAVResourceNotFoundException;
import org.eclipse.core.resources.semantic.examples.webdav.WebDAVUtil;
import org.eclipse.core.resources.semantic.examples.webdav.WebDAVUtil.IWebDAVCallback;
import org.eclipse.core.resources.semantic.examples.webdav.WebDAVUtil.InputStreamProvider;
import org.eclipse.core.resources.semantic.examples.webdav.WebDAVUtil.WebDAVNode;
import org.eclipse.core.resources.semantic.spi.CachingContentProvider;
import org.eclipse.core.resources.semantic.spi.DefaultMinimalSemanticResourceRuleFactory;
import org.eclipse.core.resources.semantic.spi.FileCacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.ICacheService;
import org.eclipse.core.resources.semantic.spi.ICacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderLocking;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderREST;
import org.eclipse.core.resources.semantic.spi.ISemanticFileHistoryProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore.ResourceType;
import org.eclipse.core.resources.semantic.spi.ISemanticResourceRuleFactory;
import org.eclipse.core.resources.semantic.spi.ISemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.ISemanticTreeVisitor;
import org.eclipse.core.resources.semantic.spi.SemanticFileRevision;
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
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.ITag;

public class SampleWebDAVContentProvider extends CachingContentProvider implements ISemanticContentProviderREST,
		ISemanticContentProviderLocking {

	private static final QualifiedName RESOURCE_ETAG = new QualifiedName(SemanticResourcesPluginExamplesCore.PLUGIN_ID, "ETag"); //$NON-NLS-1$
	private static final QualifiedName LOCAL_CHANGE = new QualifiedName(SemanticResourcesPluginExamplesCore.PLUGIN_ID, "LocalChange"); //$NON-NLS-1$
	private static final QualifiedName SUPPORTS_LOCKING = new QualifiedName(SemanticResourcesPluginExamplesCore.PLUGIN_ID,
			"SupportsLocking"); //$NON-NLS-1$
	private static final QualifiedName LOCK_TOKEN = new QualifiedName(SemanticResourcesPluginExamplesCore.PLUGIN_ID, "LockToken"); //$NON-NLS-1$
	private static final String TRUE = "true"; //$NON-NLS-1$

	@Override
	public void onCacheUpdate(ISemanticFileStore childStore, InputStream newContent, long timestamp, boolean append,
			IProgressMonitor monitor) {
		super.onCacheUpdate(childStore, newContent, timestamp, append, monitor);
		try {
			childStore.setLocalOnly(false);
			childStore.setPersistentProperty(LOCAL_CHANGE, TRUE);
			childStore.setPersistentProperty(RESOURCE_ETAG, null);
		} catch (CoreException e) {
			SemanticResourcesPluginExamplesCore.getDefault().getLog()
					.log(new Status(IStatus.ERROR, SemanticResourcesPluginExamplesCore.PLUGIN_ID, e.getMessage(), e));
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == ISemanticFileHistoryProvider.class) {
			return new ISemanticFileHistoryProvider() {

				public IFileRevision getWorkspaceFileRevision(final ISemanticFileStore store) {
					return new SemanticFileRevision(store, store.fetchInfo().getLastModified(), null, null) {

						public IStorage getStorage(IProgressMonitor monitor) {
							return new IStorage() {

								public Object getAdapter(Class adapter1) {
									return null;
								}

								public boolean isReadOnly() {
									return store.fetchInfo().getAttribute(EFS.ATTRIBUTE_READ_ONLY);
								}

								public String getName() {
									return store.getName();
								}

								public IPath getFullPath() {
									return null;
								}

								public InputStream getContents() throws CoreException {
									return store.openInputStream(EFS.NONE, null);
								}
							};
						}
					};
				}

				public IFileRevision[] getResourceVariants(final ISemanticFileStore store, IProgressMonitor monitor) {
					if (store.getType() == ISemanticFileStore.FILE && !store.fetchInfo().getAttribute(EFS.ATTRIBUTE_READ_ONLY)) {
						IFileRevision remote = new IFileRevision() {

							public IFileRevision withAllProperties(IProgressMonitor monitor1) {
								return this;
							}

							public boolean isPropertyMissing() {
								return false;
							}

							public URI getURI() {
								try {
									return new URI(store.getRemoteURIString());
								} catch (URISyntaxException e) {
									// ignore
								} catch (CoreException e) {
									// ignore
								}
								return null;
							}

							public long getTimestamp() {
								return -1;
							}

							public ITag[] getTags() {
								return new ITag[0];
							}

							public IStorage getStorage(IProgressMonitor monitor1) {
								return new IStorage() {

									public Object getAdapter(Class adapter1) {
										return null;
									}

									public boolean isReadOnly() {
										return true;
									}

									public String getName() {
										return store.getName();
									}

									public IPath getFullPath() {
										return null;
									}

									public InputStream getContents() throws CoreException {
										URI uri = getWebDAVURIForStore(store);

										IWebDAVCallback setter = new IWebDAVCallback() {

											public void setTimestamp(long timestamp) {
												//
											}

											public void setETag(String value) {
												//
											}

											public void setContentType(String contentType) {
												//
											}
										};
										try {
											return WebDAVUtil.openInputStream(uri.toString(), setter);
										} catch (IOException e) {
											throw new CoreException(new Status(IStatus.ERROR,
													SemanticResourcesPluginExamplesCore.PLUGIN_ID, e.getMessage(), e));
										}

									}
								};
							}

							public String getName() {
								return store.getName();
							}

							public String getContentIdentifier() {
								try {
									URI uri = getWebDAVURIForStore(store);

									WebDAVNode node = WebDAVUtil.retrieveRemoteState(uri, new NullProgressMonitor());

									if (node != null) {
										return node.etag;
									}
								} catch (Exception e) {
									// ignore;
								}
								return "unknown"; //$NON-NLS-1$
							}

							public String getComment() {
								return null;
							}

							public String getAuthor() {
								return null;
							}

							public boolean exists() {
								return false;
							}
						};
						return new IFileRevision[] {null, remote};
					}
					return null;
				}

				public IFileHistory getHistoryFor(ISemanticFileStore store, int options, IProgressMonitor monitor) {
					// TODO Auto-generated method stub
					return null;
				}
			};
		}
		return null;
	}

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
		// TODO Auto-generated method stub

	}

	public void addFileFromRemoteByURI(ISemanticFileStore parentStore, String name, URI uri, IProgressMonitor monitor) throws CoreException {
		parentStore.addChildFile(name);

		ISemanticFileStore newChild = (ISemanticFileStore) parentStore.getChild(name);

		String uriString = uri.toString();

		newChild.setRemoteURIString(uriString);
		this.setReadOnlyInternal(newChild, true);

		try {
			refreshStoreHierarchy(SyncDirection.INCOMING, newChild, uri, monitor);
		} catch (CoreException e) {
			deleteCache(newChild, monitor);
			newChild.remove(monitor);
			throw e;
		}
	}

	public void addFolderFromRemoteByURI(ISemanticFileStore parentStore, String name, URI uri, IProgressMonitor monitor)
			throws CoreException {
		parentStore.addChildFolder(name);

		ISemanticFileStore newChild = (ISemanticFileStore) parentStore.getChild(name);

		String uriString = uri.toString();

		newChild.setRemoteURIString(uriString);

		try {
			refreshStoreHierarchy(SyncDirection.INCOMING, newChild, uri, monitor);
		} catch (CoreException e) {
			deleteCache(newChild, monitor);
			newChild.remove(monitor);
			throw e;
		}
	}

	private void refreshStoreHierarchy(SyncDirection direction, ISemanticFileStore root, URI rootURI, IProgressMonitor monitor)
			throws CoreException {
		try {
			if (root.getType() == ISemanticFileStore.FILE) {
				updateSingleFile(direction, root, rootURI, monitor);
			} else {
				WebDAVNode rootNode = WebDAVUtil.retrieveRemoteState(rootURI, monitor);
				if (direction.equals(SyncDirection.INCOMING) || direction.equals(SyncDirection.BOTH)) {
					updateChildrenIncoming(root, rootNode, monitor);
				}

				if (direction.equals(SyncDirection.OUTGOING) || direction.equals(SyncDirection.BOTH)) {
					updateChildrenOutgoing(root, monitor);
				}
			}
		} catch (IOException e) {
			// TODO define proper error code and improve error handling
			throw new SemanticResourceException(SemanticResourceStatusCode.SYNC_ERROR, root.getPath(), e.getMessage(), e);
		}

	}

	private void updateSingleFile(SyncDirection direction, ISemanticFileStore store, URI rootURI, IProgressMonitor monitor)
			throws CoreException {
		if (direction.equals(SyncDirection.INCOMING) || direction.equals(SyncDirection.BOTH)) {
			if (this.isReadOnlyInternal(store)) {
				try {
					WebDAVNode rootNode = WebDAVUtil.retrieveRemoteState(rootURI, monitor);

					boolean changed = checkAndSetWebDAVModificationProperties(store, rootNode.lastModified, rootNode.etag,
							rootNode.contentType, monitor);
					if (changed) {
						this.deleteCache(store, monitor);
					}
				} catch (WebDAVResourceNotFoundException e) {
					store.remove(monitor);
				} catch (IOException e) {
					throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_CONNECT_EXCEPTION, store.getPath(),
							e.getMessage(), e);
				}
				return;
			}
		}
		if (direction.equals(SyncDirection.OUTGOING) || direction.equals(SyncDirection.BOTH)) {
			this.uploadFileStoreContent(store, monitor);
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

	private void updateChildrenIncoming(ISemanticFileStore root, WebDAVNode rootNode, IProgressMonitor monitor) throws CoreException {
		for (WebDAVNode node : rootNode.children) {
			String nodeName = node.path.segment(node.path.segmentCount() - 1);
			if (root.hasChild(nodeName)) {
				// child exists
				ISemanticFileStore child = (ISemanticFileStore) root.getChild(nodeName);
				if (node.isFolder) {
					updateChildrenIncoming(child, node, monitor);
				} else {
					if (this.isReadOnlyInternal(child)) {
						boolean changed = checkAndSetWebDAVModificationProperties(child, node.lastModified, node.etag, node.contentType,
								monitor);
						if (changed) {
							this.deleteCache(child, monitor);
						}
					}
				}
			} else {
				// new child
				if (node.isFolder) {
					root.addChildFolder(nodeName);
					ISemanticFileStore child = (ISemanticFileStore) root.getChild(nodeName);
					updateChildrenIncoming(child, node, monitor);
				} else {
					root.addChildResource(nodeName, false, null, null);

					ISemanticFileStore child = (ISemanticFileStore) root.getChild(nodeName);
					this.setContentTypeInternal(child, node.contentType);
					this.setResourceTimestamp(child, node.lastModified, monitor);
					this.setReadOnly(child, true, monitor);
					child.setPersistentProperty(RESOURCE_ETAG, node.etag);
					if (node.supportsLocking) {
						child.setPersistentProperty(SUPPORTS_LOCKING, TRUE);
					}
				}
			}
		}

		IFileStore[] existingChildren = root.childStores(EFS.NONE, monitor);

		for (IFileStore store : existingChildren) {
			ISemanticFileStore child = (ISemanticFileStore) store;
			boolean found = false;
			for (WebDAVNode node : rootNode.children) {
				String nodeName = node.path.segment(node.path.segmentCount() - 1);
				if (nodeName.equals(child.getName())) {
					found = true;
					break;
				}
			}
			if (!found) {
				if (!this.isModifiedLocally(child)) {
					child.remove(monitor);
				}
			}
		}
	}

	private void updateChildrenOutgoing(ISemanticFileStore root, IProgressMonitor monitor) throws CoreException {
		IFileStore[] existingChildren = root.childStores(EFS.NONE, monitor);

		for (IFileStore store : existingChildren) {
			ISemanticFileStore child = (ISemanticFileStore) store;

			if (child.getType() == ISemanticFileStore.FILE) {
				uploadFileStoreContent(child, monitor);
			} else {
				// TODO create remote WebDAV folder if needed
				updateChildrenOutgoing(child, monitor);
			}
		}
	}

	private void uploadFileStoreContent(final ISemanticFileStore child, final IProgressMonitor monitor) throws CoreException {
		if (!this.isReadOnlyInternal(child)) {
			if (child.getPersistentProperty(LOCAL_CHANGE) != null || child.isLocalOnly()) {
				URI remoteURI = this.getWebDAVURIForStore(child);
				try {
					WebDAVUtil.sendData(remoteURI.toString(), new InputStreamProvider() {

						public InputStream getInputStream() throws IOException {
							try {
								return getCachedContent(child, monitor);
							} catch (CoreException e) {
								throw new IOException(e.getMessage());
							}
						}
					}, monitor);

					child.setPersistentProperty(LOCAL_CHANGE, null);
					child.setLocalOnly(false);
					this.setReadOnly(child, true, monitor);

					WebDAVNode node = WebDAVUtil.retrieveRemoteState(remoteURI, monitor);

					this.setContentTypeInternal(child, node.contentType);
					this.setResourceTimestamp(child, node.lastModified, monitor);
					child.setPersistentProperty(RESOURCE_ETAG, node.etag);
					if (node.supportsLocking) {
						child.setPersistentProperty(SUPPORTS_LOCKING, TRUE);
					}
				} catch (IOException e) {
					// ignore and leave as changed
					// TODO report failed operations into status
					e.printStackTrace();
				}
			}
		}
	}

	public void synchronizeContentWithRemote(ISemanticFileStore store, final SyncDirection direction, final IProgressMonitor monitor,
			final MultiStatus status) {
		try {
			ISemanticFileStore parent = findParentWithURI(store);

			if (parent == null) {
				ISemanticTreeVisitor visitor = new ISemanticTreeVisitor() {

					public boolean visit(ISemanticFileStore store1, IProgressMonitor monitor1) throws CoreException {
						String remoteURI = store1.getRemoteURIString();

						if (remoteURI != null) {
							synchronizeContentWithRemote(store1, direction, monitor, status);
							return false;
						}
						return true;
					}
				};
				SemanticTreeWalker.accept(store, visitor, monitor);
				return;
			}

			URI uri = getWebDAVURIForStore(store);

			refreshStoreHierarchy(direction, store, uri, monitor);
		} catch (CoreException e) {
			IStatus innerStatus = e.getStatus();
			IStatus newStatus = new Status(innerStatus.getSeverity(), innerStatus.getPlugin(), e.getMessage(), e);
			status.add(newStatus);
			return;
		}
	}

	URI getWebDAVURIForStore(ISemanticFileStore store) throws CoreException {
		ISemanticFileStore parent = findParentWithURI(store);

		if (parent == null) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_URI_NOT_FOUND, store.getPath(), NLS.bind(
					Messages.SampleRESTReadonlyContentProvider_RemoteUriNotSet_XMSG, store.getPath().toString()));
		}

		String remoteURI = parent.getRemoteURIString();

		if (remoteURI == null) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_URI_NOT_FOUND, store.getPath(), NLS.bind(
					Messages.SampleRESTReadonlyContentProvider_RemoteUriNotSet_XMSG, store.getPath().toString()));
		}

		IPath relativePath = store.getPath().removeFirstSegments(parent.getPath().segmentCount());

		String fileURIString;

		if (!relativePath.isEmpty()) {
			if (remoteURI.endsWith("/")) { //$NON-NLS-1$
				fileURIString = remoteURI + relativePath.toString();
			} else {
				fileURIString = remoteURI + "/" + relativePath.toString(); //$NON-NLS-1$
			}
		} else {
			fileURIString = remoteURI;
		}

		try {
			URI uri = new URI(fileURIString);
			return uri;
		} catch (URISyntaxException e) {
			throw new SemanticResourceException(SemanticResourceStatusCode.INVALID_URI_SYNTAX, store.getPath(), NLS.bind(
					Messages.RemoteStoreContentProvider_URIError_XMSG, store.getPath().toString()), e);
		}
	}

	public void removeResource(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException {
		this.deleteCache(semanticFileStore, monitor);

		semanticFileStore.remove(monitor);
	}

	public void revertChanges(ISemanticFileStore store, IProgressMonitor monitor) throws CoreException {
		this.deleteCache(store, monitor);
		this.setReadOnlyInternal(store, true);
		this.setResourceTimestamp(store, 0, monitor);
		store.setPersistentProperty(LOCAL_CHANGE, null);

		MultiStatus status = new MultiStatus(SemanticResourcesPluginExamplesCore.PLUGIN_ID, IStatus.OK, "", null); //$NON-NLS-1$
		this.fillCache(store, monitor, status);
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

		boolean supportsLocking = false;
		if (SemanticSpiResourceInfo.isOptionRequested(ISemanticFileSystem.RESOURCE_INFO_LOCKING_SUPPORTED, options)) {
			if (semanticFileStore.getPersistentProperty(SUPPORTS_LOCKING) != null) {
				supportsLocking = true;
			}
		}

		boolean locked = false;
		if (SemanticSpiResourceInfo.isOptionRequested(ISemanticFileSystem.RESOURCE_INFO_LOCKED, options)) {
			if (semanticFileStore.getPersistentProperty(LOCK_TOKEN) != null) {
				locked = true;
			}
		}

		boolean existsRemotely = false;
		if (SemanticSpiResourceInfo.isOptionRequested(ISemanticFileSystem.RESOURCE_INFO_EXISTS_REMOTELY, options)) {

			String remoteURI;
			try {
				remoteURI = this.getWebDAVURIForStore(semanticFileStore).toString();
			} catch (CoreException ce) {
				remoteURI = null;
			}
			if (remoteURI != null) {
				existsRemotely = WebDAVUtil.checkExistence(remoteURI, semanticFileStore.getType() != ISemanticFileStore.FILE, monitor);
			}

		}
		return new SemanticSpiResourceInfo(options, locked, supportsLocking, readOnly, existsRemotely, uriString,
				this.getContentTypeInternal(semanticFileStore));
	}

	@Override
	public InputStream openInputStreamInternal(final ISemanticFileStore store, IProgressMonitor monitor,
			final ICacheTimestampSetter timeStampSetter) throws CoreException {
		ISemanticFileStore parent = findParentWithURI(store);

		if (parent == null) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_URI_NOT_FOUND, store.getPath(), NLS.bind(
					Messages.SampleRESTReadonlyContentProvider_RemoteUriNotSet_XMSG, store.getPath().toString()));
		}

		String remoteURI = parent.getRemoteURIString();

		if (remoteURI == null) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_URI_NOT_FOUND, store.getPath(), NLS.bind(
					Messages.SampleRESTReadonlyContentProvider_RemoteUriNotSet_XMSG, store.getPath().toString()));
		}

		try {
			IPath relativePath = store.getPath().removeFirstSegments(parent.getPath().segmentCount());

			String fileURIString;

			if (!relativePath.isEmpty()) {
				if (remoteURI.endsWith("/")) { //$NON-NLS-1$
					fileURIString = remoteURI + relativePath.toString();
				} else {
					fileURIString = remoteURI + "/" + relativePath.toString(); //$NON-NLS-1$
				}
			} else {
				fileURIString = remoteURI;
			}

			final long timestamp[] = new long[1];
			final String contentType[] = new String[1];
			final String eTag[] = new String[1];

			InputStream is = WebDAVUtil.openInputStream(fileURIString, new IWebDAVCallback() {
				public void setTimestamp(long timestamp1) {
					timestamp[0] = timestamp1;
				}

				public void setContentType(String contentType1) {
					contentType[0] = contentType1;
				}

				public void setETag(String value) {
					eTag[0] = value;
				}

			});

			try {
				checkAndSetWebDAVModificationProperties(store, timestamp[0], eTag[0], contentType[0], monitor);
			} catch (CoreException e) {
				Util.safeClose(is);
				throw e;
			} catch (RuntimeException e) {
				Util.safeClose(is);
				throw e;
			}
			timeStampSetter.setTimestamp(getResourceTimestampInternal(store));
			return is;
		} catch (IOException e) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_CONNECT_EXCEPTION, store.getPath(), e.getMessage(), e);
		}
	}

	private ISemanticFileStore findParentWithURI(ISemanticFileStore store) throws CoreException {
		if (store.getRemoteURIString() != null) {
			return store;
		}

		ISemanticFileStore parent = (ISemanticFileStore) store.getParent();

		while (parent != null) {
			if (parent.getRemoteURIString() != null) {
				break;
			}
			parent = (ISemanticFileStore) parent.getParent();
		}
		return parent;
	}

	public String getURIString(ISemanticFileStore semanticFileStore) throws CoreException {
		return semanticFileStore.getRemoteURIString();
	}

	public void setURIString(ISemanticFileStore store, URI uri, IProgressMonitor monitor) throws CoreException {
		store.setRemoteURIString(uri.toString());

		// TODO 0.4: what to do on folders?
		if (store.getType() == ISemanticFileStore.FILE) {
			this.deleteCache(store, monitor);
			this.setResourceTimestamp(store, 0, monitor);
			this.setReadOnlyInternal(store, true);
			store.setPersistentProperty(LOCAL_CHANGE, null);

			WebDAVNode rootNode;
			try {
				rootNode = WebDAVUtil.retrieveRemoteState(uri, monitor);
				checkAndSetWebDAVModificationProperties(store, rootNode.lastModified, rootNode.etag, rootNode.contentType, monitor);
			} catch (IOException e) {
				throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_CONNECT_EXCEPTION, store.getPath(), e.getMessage(), e);
			}
		}
	}

	private boolean checkAndSetWebDAVModificationProperties(ISemanticFileStore child, long lastModified, String eTag, String contentType,
			IProgressMonitor monitor) throws CoreException {
		boolean changed = false;
		long oldTimestamp = getResourceTimestampInternal(child);
		String oldETag = child.getPersistentProperty(RESOURCE_ETAG);

		if (oldETag != null) {
			if (!oldETag.equals(eTag)) {
				changed = true;
			}
		} else {
			if (oldTimestamp != lastModified) {
				changed = true;
			}
		}

		if (changed) {
			this.setResourceTimestamp(child, lastModified, monitor);
			this.setContentTypeInternal(child, contentType);
			child.setPersistentProperty(RESOURCE_ETAG, eTag);
		}
		return changed;
	}

	public IStatus lockResource(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException {
		if (!isLockingSupported(semanticFileStore)) {
			return Status.CANCEL_STATUS;
		}
		if (!isLocked(semanticFileStore)) {
			String remoteURI = this.getWebDAVURIForStore(semanticFileStore).toString();
			try {
				String lockToken = WebDAVUtil.sendLockRequest(remoteURI, monitor);

				semanticFileStore.setPersistentProperty(LOCK_TOKEN, lockToken);
			} catch (IOException e) {
				return new Status(IStatus.ERROR, SemanticResourcesPluginExamplesCore.PLUGIN_ID, e.getMessage(), e);
			}
		}
		return Status.OK_STATUS;
	}

	public IStatus unlockResource(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException {
		if (!isLockingSupported(semanticFileStore)) {
			return Status.CANCEL_STATUS;
		}
		if (isLocked(semanticFileStore)) {
			String remoteURI = this.getWebDAVURIForStore(semanticFileStore).toString();
			try {
				String lockToken = semanticFileStore.getPersistentProperty(LOCK_TOKEN);

				WebDAVUtil.sendUnlockRequest(remoteURI, lockToken, monitor);

				semanticFileStore.setPersistentProperty(LOCK_TOKEN, null);
			} catch (IOException e) {
				return new Status(IStatus.ERROR, SemanticResourcesPluginExamplesCore.PLUGIN_ID, e.getMessage(), e);
			}
		}
		return Status.OK_STATUS;
	}

	private boolean isLocked(ISemanticFileStore store) throws CoreException {
		return store.getPersistentProperty(LOCK_TOKEN) != null;
	}

	private boolean isLockingSupported(ISemanticFileStore store) throws CoreException {
		return store.getPersistentProperty(SUPPORTS_LOCKING) != null;
	}

	private boolean isModifiedLocally(ISemanticFileStore store) throws CoreException {
		if (!this.isReadOnlyInternal(store) && store.getPersistentProperty(LOCAL_CHANGE) != null || store.isLocalOnly()) {
			return true;
		}
		return false;
	}
}
