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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.internal.resources.semantic.spi.SfsSpiTraceLocation;
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
	private static final DateFormat DFFORTRACE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss::SSS"); //$NON-NLS-1$

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

		if (SfsSpiTraceLocation.CACHESERVICE.isActive()) {
			SfsSpiTraceLocation.getTrace().traceEntry(SfsSpiTraceLocation.CACHESERVICE.getLocation(),
					new Object[] { path.toString(), new Boolean(append) });
		}

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

		if (SfsSpiTraceLocation.CACHESERVICE.isActive()) {
			SfsSpiTraceLocation.getTrace().traceEntry(SfsSpiTraceLocation.CACHESERVICE.getLocation(),
					new Object[] { path.toString(), DFFORTRACE.format(new Date(timestamp)), new Boolean(append) });
		}

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

		if (SfsSpiTraceLocation.CACHESERVICE.isActive()) {
			SfsSpiTraceLocation.getTrace().traceEntry(SfsSpiTraceLocation.CACHESERVICE.getLocation(), path.toString());
		}

		InputStream result;

		try {
			lockForRead();

			ICachedContentHandle cacheFile = createCacheContentHandle(path);
			if (cacheFile.exists()) {
				result = cacheFile.openInputStream();
			} else {
				return null;
			}
			if (SfsSpiTraceLocation.CACHESERVICE.isActive()) {
				if (result == null) {
					SfsSpiTraceLocation.getTrace().traceExit(SfsSpiTraceLocation.CACHESERVICE.getLocation(), null);
				} else {
					int available;
					try {
						available = result.available();
					} catch (IOException e) {
						// $JL-EXC$
						available = -1;
					}
					SfsSpiTraceLocation.getTrace().traceExit(SfsSpiTraceLocation.CACHESERVICE.getLocation(),
							"InputStream[" + available + "]");
				}

			}
			return result;
		} finally {
			unlockForRead();
		}
	}

	public boolean hasContent(IPath path) throws CoreException {

		if (SfsSpiTraceLocation.CACHESERVICE.isActive()) {
			SfsSpiTraceLocation.getTrace().traceEntry(SfsSpiTraceLocation.CACHESERVICE.getLocation(), path.toString());
		}

		try {
			lockForRead();

			ICachedContentHandle cacheFile = createCacheContentHandle(path);

			if (SfsSpiTraceLocation.CACHESERVICE.isActive()) {
				SfsSpiTraceLocation.getTrace().traceExit(SfsSpiTraceLocation.CACHESERVICE.getLocation(), new Boolean(cacheFile.exists()));
			}

			return cacheFile.exists();
		} finally {
			unlockForRead();
		}
	}

	public long getContentTimestamp(IPath path) throws CoreException {

		if (SfsSpiTraceLocation.CACHESERVICE.isActive()) {
			SfsSpiTraceLocation.getTrace().traceEntry(SfsSpiTraceLocation.CACHESERVICE.getLocation(), path.toString());
		}

		try {
			lockForRead();

			ICachedContentHandle cacheFile = createCacheContentHandle(path);

			long result;
			if (cacheFile.exists()) {
				result = cacheFile.lastModified();
			} else {
				result = -1;
			}

			if (SfsSpiTraceLocation.CACHESERVICE.isActive()) {
				if (result >= 0) {
					SfsSpiTraceLocation.getTrace().traceExit(SfsSpiTraceLocation.CACHESERVICE.getLocation(),
							DFFORTRACE.format(new Date(result)));
				} else {
					SfsSpiTraceLocation.getTrace().traceExit(SfsSpiTraceLocation.CACHESERVICE.getLocation(), new Long(result));
				}
			}

			return result;
		} finally {
			unlockForRead();
		}
	}

	public void setContentTimestamp(IPath path, long timestamp) throws CoreException {

		if (SfsSpiTraceLocation.CACHESERVICE.isActive()) {
			SfsSpiTraceLocation.getTrace().traceEntry(SfsSpiTraceLocation.CACHESERVICE.getLocation(),
					new Object[] { path.toString(), DFFORTRACE.format(new Date(timestamp)) });
		}

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

		if (SfsSpiTraceLocation.CACHESERVICE.isActive()) {
			SfsSpiTraceLocation.getTrace().traceEntry(SfsSpiTraceLocation.CACHESERVICE.getLocation(), path.toString());
		}

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
