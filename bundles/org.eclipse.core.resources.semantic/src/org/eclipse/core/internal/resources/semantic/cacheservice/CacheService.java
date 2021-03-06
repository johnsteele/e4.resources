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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.internal.resources.semantic.spi.SfsSpiTraceLocation;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.spi.ICacheService;
import org.eclipse.core.resources.semantic.spi.ICacheUpdateCallback;
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

		if (SfsSpiTraceLocation.CACHESERVICE.isActive()) {
			SfsSpiTraceLocation.getTrace().traceEntry(SfsSpiTraceLocation.CACHESERVICE.getLocation(),
					new Object[] {path.toString(), Boolean.valueOf(append)});
		}

		ITemporaryContentHandle tempHandle;

		try {
			lockForWrite();

			tempHandle = this.handleFactory.createTemporaryHandle(this, path, append);

			tempHandle.setContents(input, monitor);

		} finally {
			unlockForWrite();
		}

		this.addFromTempHandle(tempHandle);
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
							"InputStream[" + available + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				}

			}
			return result;
		} finally {
			unlockForRead();
		}
	}

	public void moveContent(IPath path, IPath targetPath, IProgressMonitor monitor) throws CoreException {
		if (SfsSpiTraceLocation.CACHESERVICE.isActive()) {
			SfsSpiTraceLocation.getTrace().traceEntry(SfsSpiTraceLocation.CACHESERVICE.getLocation(), path.toString());
		}

		try {
			lockForWrite();

			InputStream input = getContent(path);

			if (input != null) {
				try {
					addContent(targetPath, input, 0, monitor);
				} finally {
					if (input != null) {
						try {
							input.close();
						} catch (IOException e) {
							// ignore
						}
					}
				}
			}

			ICachedContentHandle cacheFile = createCacheContentHandle(path);

			if (cacheFile.exists()) {
				cacheFile.delete();
			}
		} finally {
			unlockForWrite();
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
				SfsSpiTraceLocation.getTrace().traceExit(SfsSpiTraceLocation.CACHESERVICE.getLocation(),
						Boolean.valueOf(cacheFile.exists()));
			}

			return cacheFile.exists();
		} finally {
			unlockForRead();
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

	public void removeContentRecursive(IPath path, IProgressMonitor monitor) throws CoreException {

		if (SfsSpiTraceLocation.CACHESERVICE.isActive()) {
			SfsSpiTraceLocation.getTrace().traceEntry(SfsSpiTraceLocation.CACHESERVICE.getLocation(), path.toString());
		}

		try {
			lockForWrite();

			this.handleFactory.removeContentRecursive(this, path);
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
	 * @throws CoreException
	 */
	void addFromTempHandle(ITemporaryContentHandle tempHandle) throws CoreException {

		try {
			lockForWrite();

			tempHandle.commit();

		} finally {
			unlockForWrite();
		}
	}

}
