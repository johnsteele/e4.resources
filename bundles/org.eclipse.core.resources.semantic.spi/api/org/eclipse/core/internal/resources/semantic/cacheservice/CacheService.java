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
package org.eclipse.core.internal.resources.semantic.cacheservice;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.spi.ICacheService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The Cache Service implementation
 * 
 * @since 4.0
 */
public class CacheService implements ICacheService {

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock readLock = this.rwl.readLock();
	private final Lock writeLock = this.rwl.writeLock();
	private IContentHandleFactory handleFactory;

	/**
	 * @param handleFactory
	 */
	public CacheService(IContentHandleFactory handleFactory) {
		this.handleFactory = handleFactory;
	}

	protected void lockForRead() {
		this.readLock.lock();
	}

	protected void unlockForRead() {
		this.readLock.unlock();
	}

	protected void lockForWrite() {
		this.writeLock.lock();
	}

	protected void unlockForWrite() {
		this.writeLock.unlock();
	}

	public void addContent(IPath path, InputStream input, int options, IProgressMonitor monitor) throws CoreException {

		boolean append = (options & ISemanticFileSystem.CONTENT_APPEND) > 0;

		ITemporaryContentHandle tempHandle;

		try {
			lockForWrite();

			tempHandle = this.handleFactory.createTemporaryHandle(this, path, append);

			tempHandle.setContents(input, monitor);

			// os = tempHandle.openOutputStream(append, monitor);
		} finally {
			unlockForWrite();
		}

		// Util.transferStreams(input, os, monitor);

		this.addFromTempHandle(tempHandle, System.currentTimeMillis());
	}

	public void addContentWithTimestamp(IPath path, InputStream input, long timestamp, int options, IProgressMonitor monitor)
			throws CoreException {

		boolean append = (options & ISemanticFileSystem.CONTENT_APPEND) > 0;

		ITemporaryContentHandle tempHandle;

		try {
			lockForWrite();

			tempHandle = this.handleFactory.createTemporaryHandle(this, path, append);

			tempHandle.setContents(input, monitor);

			// os = tempHandle.openOutputStream(append, monitor);
		} finally {
			unlockForWrite();
		}

		// Util.transferStreams(input, os, monitor);

		this.addFromTempHandle(tempHandle, timestamp);
	}

	public InputStream getContent(IPath path) throws CoreException {
		try {
			lockForRead();

			ICachedContentHandle cacheFile = createCacheContentHandle(path);
			if (cacheFile.exists()) {
				return cacheFile.openInputStream();
			}
			return null;
		} finally {
			unlockForRead();
		}
	}

	public boolean hasContent(IPath path) throws CoreException {
		try {
			lockForRead();

			ICachedContentHandle cacheFile = createCacheContentHandle(path);

			return cacheFile.exists();
		} finally {
			unlockForRead();
		}
	}

	public long getContentTimestamp(IPath path) throws CoreException {
		try {
			lockForRead();

			ICachedContentHandle cacheFile = createCacheContentHandle(path);

			if (cacheFile.exists()) {
				return cacheFile.lastModified();
			}

			return -1;
		} finally {
			unlockForRead();
		}
	}

	public void setContentTimestamp(IPath path, long timestamp) throws CoreException {
		try {
			lockForWrite();

			ICachedContentHandle cacheFile = createCacheContentHandle(path);

			if (cacheFile.exists()) {
				cacheFile.setLastModified(timestamp);
			}
			// TODO 0.1: if the entry doesn't exist
		} finally {
			unlockForWrite();
		}
	}

	public void removeContent(IPath path, IProgressMonitor monitor) throws CoreException {
		try {
			lockForWrite();

			ICachedContentHandle cacheFile = createCacheContentHandle(path);

			if (cacheFile.exists()) {
				cacheFile.delete();
			}
		} finally {
			unlockForWrite();
		}
	}

	public OutputStream wrapOutputStream(IPath path, boolean append, ICacheUpdateCallback callback, IProgressMonitor monitor)
			throws CoreException {
		try {
			lockForWrite();

			ITemporaryContentHandle tempHandle = this.handleFactory.createTemporaryHandle(this, path, append);

			return new CachingOutputStream(this, tempHandle, append, callback);

		} finally {
			unlockForWrite();
		}
	}

	private ICachedContentHandle createCacheContentHandle(IPath path) throws CoreException {
		return this.handleFactory.createCacheContentHandle(this, path);
	}

	/**
	 * used from {@link CachingOutputStream#close()}
	 * 
	 * @param tempHandle
	 * @param timestamp
	 * @throws CoreException
	 */
	void addFromTempHandle(ITemporaryContentHandle tempHandle, long timestamp) throws CoreException {

		try {
			lockForWrite();

			tempHandle.commit(timestamp);

		} finally {
			unlockForWrite();
		}
	}

}
