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
package org.eclipse.core.resources.semantic.spi;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.resources.semantic.SemanticResourcesPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.osgi.util.NLS;

/**
 * This will delegate calls to openInputStream and openOutputStream to local
 * copies of the semantic files.
 * <p>
 * The local copies are created lazily as needed. If creation of such a copy is
 * required, i.e. upon the first call to
 * {@link #openInputStream(ISemanticFileStore, IProgressMonitor)}, the original
 * data will be retrieved from the remote repository by a call to
 * {@link #openInputStreamInternal(ISemanticFileStore, IProgressMonitor, ICacheTimestampSetter)}
 * . Later calls to
 * {@link #openInputStream(ISemanticFileStore, IProgressMonitor)} will be
 * delegated to that copy.
 * <p>
 * By default, calls to
 * {@link #openOutputStream(ISemanticFileStore, int, IProgressMonitor)} will
 * open an {@link OutputStream} on the cached copy, i.e. only the cached copy
 * will be updated. If the cached copy and the remote repository are to be kept
 * in sync ("write-through"), it is possible to override
 * {@link #onCacheUpdate(ISemanticFileStore, InputStream, long, boolean, IProgressMonitor)}
 * and do the remote update there.
 * <p>
 * TODO 0.1: describe options for error handling for write-through scenario
 * <p>
 * This class is intended to be subclassed.
 * 
 * @since 4.0
 * 
 */
public abstract class CachingContentProvider extends ContentProvider {

	private static final QualifiedName RESOURCE_TIMESTAMP = new QualifiedName(SemanticResourcesPlugin.PLUGIN_ID, "ResourceTimestamp"); //$NON-NLS-1$
	// TODO 0.1: add convenience/helper methods to cleanup cache along with
	// file/folder removal
	// TODO 0.1: add helpers for write-through error handling
	/**
	 * Call back to set the timestamp of the cache.
	 * <p>
	 * This enables content providers to update the cache timestamp
	 */

	public final IDropCacheVisitor deleteAllVisitor = new IDropCacheVisitor() {

		public boolean shouldDrop(ISemanticFileStore store) {
			return store.getType() == ISemanticFileStore.FILE;
		}
	};

	private ICacheService m_cacheService;

	/**
	 * Call-back interface for setting the cache timestamp.
	 * 
	 */
	public interface ICacheTimestampSetter {
		/**
		 * sets the cache timestamp
		 * 
		 * @param timestamp
		 *            the timestamp to set for the cache entry
		 */
		public void setTimestamp(long timestamp);

		/**
		 * 
		 * @return the current timestamp
		 */
		public long getTimestamp();
	}

	/**
	 * A visitor used for dropping the cache.
	 */
	public interface IDropCacheVisitor {
		/**
		 * @param store
		 *            the semantic file store visited
		 * @return <code>true</code> if the store should be deleted
		 */
		public boolean shouldDrop(ISemanticFileStore store);

	}

	/**
	 * 
	 * @return the cache service
	 * @throws CoreException
	 */
	public ICacheService getCacheService() throws CoreException {
		if (this.m_cacheService == null) {
			this.m_cacheService = this.getCacheServiceFactory().getCacheService();
		}
		return this.m_cacheService;
	}

	public final InputStream openInputStream(final ISemanticFileStore childStore, IProgressMonitor monitor) throws CoreException {
		ICacheService cacheService = this.getCacheService();

		IPath path = childStore.getPath();

		if (!cacheService.hasContent(path)) {

			ICacheTimestampSetter setter = new ICacheTimestampSetter() {

				long lastTimestamp = 0;

				public void setTimestamp(long timestamp) {
					this.lastTimestamp = timestamp;
				}

				public long getTimestamp() {
					return this.lastTimestamp;
				}
			};

			InputStream is = null;

			try {
				is = openInputStreamInternal(childStore, monitor, setter);

				cacheService.addContent(path, is, EFS.NONE, monitor);

				setResourceTimestamp(childStore, setter.getTimestamp(), monitor);
			} finally {
				Util.safeClose(is);
			}
		}

		return cacheService.getContent(path);
	}

	public final OutputStream openOutputStream(final ISemanticFileStore childStore, int options, final IProgressMonitor monitor)
			throws CoreException {
		ICacheService cacheService = this.getCacheService();
		IPath path = childStore.getPath();

		boolean appendMode = (options & ISemanticFileSystem.CONTENT_APPEND) > 0;

		ICacheUpdateCallback callback = new ICacheUpdateCallback() {

			public void cacheUpdated(InputStream newContent, long timestamp, boolean append) throws CoreException {
				onCacheUpdate(childStore, newContent, timestamp, append, monitor);
			}
		};
		return cacheService.wrapOutputStream(path, appendMode, callback, monitor);
	}

	/**
	 * Cache deletion; this corresponds to the deletion of an element.
	 * <p>
	 * Other than the
	 * {@link #dropCache(ISemanticFileStore, IProgressMonitor, IDropCacheVisitor, MultiStatus)}
	 * method, this will try to delete the cache ignoring the state of the cache
	 * file.
	 * <p>
	 * Recursively goes down the child hierarchy and tries do delete the
	 * corresponding cache entry.
	 * 
	 * @param store
	 * @param monitor
	 * @throws CoreException
	 */
	protected void deleteCache(ISemanticFileStore store, IProgressMonitor monitor) throws CoreException {
		MultiStatus status = new MultiStatus(SemanticResourcesPlugin.PLUGIN_ID, IStatus.OK, NLS.bind(
				Messages.CachingContentProvider_DeletingCache_XMSG, store.getPath().toString()), null);
		dropCache(store, monitor, this.deleteAllVisitor, status);
		if (!status.isOK()) {
			throw new CoreException(status);
		}

	}

	/**
	 * Cache dropping; this corresponds to a refresh of an element.
	 * <p>
	 * Note that federated resources will <em>not</em> be visited.
	 * <p>
	 * Recursively goes down the child hierarchy and tries to delete the
	 * corresponding cache entry; it's up to the provided visitor to indicate
	 * whether a given cache entry should be deleted. In particular, cache
	 * entries pertaining to "locally changed" resources should not be dropped.
	 * <p>
	 * 
	 * @param childStore
	 *            the store
	 * @param monitor
	 *            may be null
	 * @param visitor
	 *            the visitor
	 * @param status
	 *            the result status
	 */
	public void dropCache(ISemanticFileStore childStore, IProgressMonitor monitor, IDropCacheVisitor visitor, MultiStatus status) {

		if (childStore.getType() == ISemanticFileStore.FILE) {

			if (visitor.shouldDrop(childStore)) {
				try {
					ICacheService cacheService = this.getCacheService();

					cacheService.removeContent(childStore.getPath(), monitor);
				} catch (CoreException e) {
					status.add(e.getStatus());
					return;
				}
			}
		} else {

			try {

				for (IFileStore store : childStore.childStores(EFS.NONE, monitor)) {

					if (!(store instanceof ISemanticFileStore)) {
						continue;
					}

					// check if we have federation
					ISemanticFileStore sfs = (ISemanticFileStore) store;
					String providerId = sfs.getContentProviderID();
					// only if there is no federation, otherwise simply ignore
					// since we don't know if the semantics of the visitor
					// is correct for the federated provider
					if (providerId == null) {
						dropCache((ISemanticFileStore) store, monitor, visitor, status);
					}

				}
			} catch (CoreException e) {
				status.add(e.getStatus());
				return;
			}
		}
	}

	/**
	 * Recursively goes down the hierarchy and fills the cache.
	 * <p>
	 * If the resource is writable, the cache will not be filled.
	 * 
	 * @param semanticFileStore
	 * @param monitor
	 * @param status
	 */
	protected void fillCache(ISemanticFileStore semanticFileStore, IProgressMonitor monitor, MultiStatus status) {

		if (semanticFileStore.getType() != ISemanticFileStore.FILE) {

			try {

				for (IFileStore store : semanticFileStore.childStores(EFS.NONE, monitor)) {

					if (!(store instanceof ISemanticFileStore)) {
						continue;
					}
					// check if we have federation
					ISemanticFileStore sfs = (ISemanticFileStore) store;
					String providerId = sfs.getContentProviderID();
					if (providerId == null) {
						fillCache((ISemanticFileStore) store, monitor, status);
					} else {
						try {
							// delegation to other content provider
							ISemanticContentProvider provider = sfs.getEffectiveContentProvider();
							if (provider instanceof CachingContentProvider) {
								((CachingContentProvider) provider).fillCache((ISemanticFileStore) store, monitor, status);
							}
						} catch (CoreException e) {
							status.add(e.getStatus());
						}
					}
				}

			} catch (CoreException e) {
				status.add(e.getStatus());
			}
		} else {
			// in case of file

			// local only content is always in cache
			if (semanticFileStore.isLocalOnly()) {
				return;
			}
			// if this is not read-only, we don't overwrite the timestamp
			boolean readOnly;
			try {
				readOnly = fetchResourceInfo(semanticFileStore, ISemanticFileSystem.RESOURCE_INFO_READ_ONLY, monitor).isReadOnly();
			} catch (CoreException e) {
				status.add(e.getStatus());
				return;
			}

			if (readOnly) {
				// required to obtain the correct timestamp
				try {
					Util.safeClose(openInputStream(semanticFileStore, monitor));
				} catch (CoreException e) {
					status.add(e.getStatus());
				}
			}
		}
	}

	/**
	 * 
	 * @return the cache service factory
	 * @throws CoreException
	 */
	public abstract ICacheServiceFactory getCacheServiceFactory() throws CoreException;

	/**
	 * To be implemented by concrete subclasses.
	 * <p>
	 * This method is called both from
	 * {@link #openInputStream(ISemanticFileStore, IProgressMonitor)} and
	 * (indirectly) from the default implementation of
	 * {@link #getResourceTimestamp(ISemanticFileStore, IProgressMonitor)}. This
	 * method allows retrieval of content and content timestamp with one
	 * roundtrip (e.g. via plain HTTP GET). But this approach has a drawback
	 * that, after adding new resources, all their content is retrieved on
	 * update of the resource tree.
	 * <p>
	 * Important: The timestamp that is obtained via {@code timeStampSetter}
	 * parameter is then passed to
	 * {@link #setResourceTimestamp(ISemanticFileStore, long, IProgressMonitor)}
	 * method.
	 * <p>
	 * Content providers that are able to retrieve the resource timestamp
	 * independently from content, should override the methods
	 * {@link #getResourceTimestamp(ISemanticFileStore, IProgressMonitor)},
	 * {@link #setResourceTimestamp(ISemanticFileStore, long, IProgressMonitor)}
	 * and
	 * {@link #onCacheUpdate(ISemanticFileStore, InputStream, long, boolean, IProgressMonitor)}
	 * and provide an own timestamp handling that allows lazy content retrieval.
	 * <p>
	 * When implementing own timestamp handling, it is important to take into
	 * account that the method
	 * {@link #getResourceTimestamp(ISemanticFileStore, IProgressMonitor)} is
	 * invoked by {@link IResource#refreshLocal(int, IProgressMonitor)} so that
	 * the implementation of
	 * {@link #getResourceTimestamp(ISemanticFileStore, IProgressMonitor)} must
	 * not rely on
	 * {@link #openInputStreamInternal(ISemanticFileStore, IProgressMonitor, ICacheTimestampSetter)}
	 * being called before. It is also important to ensure that the timestamp is
	 * not changed unduly by
	 * {@link #openInputStreamInternal(ISemanticFileStore, IProgressMonitor, ICacheTimestampSetter)}
	 * because it will result in resource exceptions with error code
	 * {@link IResourceStatus#OUT_OF_SYNC_LOCAL} on subsequent access to
	 * resource content without calling
	 * {@link IResource#refreshLocal(int, IProgressMonitor)} in between.
	 * 
	 * @param store
	 *            the file store
	 * @param monitor
	 *            may be null
	 * @param timeStampSetter
	 *            a callback to report content timestamp
	 * @return the input stream
	 * @throws CoreException
	 *             in case of failure
	 */
	public abstract InputStream openInputStreamInternal(ISemanticFileStore store, IProgressMonitor monitor,
			ICacheTimestampSetter timeStampSetter) throws CoreException;

	/**
	 * Notification about updated cache content.
	 * <p>
	 * After successful update of the cache, this method will be called; content
	 * providers should override this to update the remote repository.
	 * <p>
	 * The cache will <em>not</em> be rolled back upon failure of this method.
	 * The content provider is responsible to take appropriate action, e.g.
	 * re-synchronize the cache content. In order to keep track of the cache
	 * state, persistent properties could be used.
	 * 
	 * @param childStore
	 *            the semantic file store
	 * @param newContent
	 *            the new cache content
	 * @param timestamp
	 *            the timestamp of the change
	 * @param append
	 *            <code>true</code> to indicate that the cache was updated in
	 *            append mode; in this case, only the appended data will be
	 *            provided as new content
	 * @param monitor
	 *            may be null
	 */
	public void onCacheUpdate(ISemanticFileStore childStore, InputStream newContent, long timestamp, boolean append,
			IProgressMonitor monitor) {
		try {
			setResourceTimestamp(childStore, timestamp, monitor);
		} catch (CoreException e) {
			// TODO logging
		}
	}

	public long getResourceTimestamp(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException {

		if (!semanticFileStore.isExists()) {
			return EFS.NONE;
		}

		String stampString = semanticFileStore.getPersistentProperty(RESOURCE_TIMESTAMP);
		if (stampString != null) {
			return Long.parseLong(stampString);
		}

		// no property set, fill cache that will retrieve the content that will
		// set the timestamp

		MultiStatus stat = new MultiStatus(SemanticResourcesPlugin.PLUGIN_ID, IStatus.OK, NLS.bind(
				Messages.CachingContentProvider_FillCache_XGRP, semanticFileStore.getPath().toString()), null);

		// ok since getResourceTimestamp is called only for files
		fillCache(semanticFileStore, monitor, stat);

		if (!stat.isOK()) {
			throw new CoreException(stat);
		}

		// try to read the property again
		stampString = semanticFileStore.getPersistentProperty(RESOURCE_TIMESTAMP);
		if (stampString != null) {
			return Long.parseLong(stampString);
		}

		throw new SemanticResourceException(SemanticResourceStatusCode.CACHED_CONTENT_NOT_FOUND, semanticFileStore.getPath(),
				Messages.CachingContentProvider_TimestampNotInCache_XMSG);
	}

	public void setResourceTimestamp(ISemanticFileStore semanticFileStore, long timestamp, IProgressMonitor monitor) throws CoreException {

		semanticFileStore.setPersistentProperty(RESOURCE_TIMESTAMP, Long.toString(timestamp));

	}

}
