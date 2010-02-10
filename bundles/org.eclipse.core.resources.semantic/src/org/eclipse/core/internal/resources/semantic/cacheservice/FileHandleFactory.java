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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.semantic.spi.ICacheService;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * The file-based content handle factory
 * 
 * @since 4.0
 */
public class FileHandleFactory implements IContentHandleFactory {

	private static final String FAILED_DELETIONS_DOLLAR = ".failedDeletions.$$$"; //$NON-NLS-1$
	private static final String DOT_SEPARATOR = "."; //$NON-NLS-1$
	private static final String TEMP_FILE_EXTENSION = ".$$$"; //$NON-NLS-1$
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock writeLock = this.rwl.writeLock();
	private HashSet<String> failedDeletions = new HashSet<String>();
	private final File deletionsFile;

	private final File cacheRoot;
	private long uniqueID = 0;

	/**
	 * @param cacheRoot
	 *            the cache root directory
	 */
	public FileHandleFactory(File cacheRoot) {
		this.cacheRoot = cacheRoot;
		this.deletionsFile = new File(this.cacheRoot, FAILED_DELETIONS_DOLLAR);

		try {
			lockForWrite();
			load();
		} finally {
			unlockForWrite();
		}
	}

	private void lockForWrite() {
		this.writeLock.lock();
	}

	private void unlockForWrite() {
		this.writeLock.unlock();
	}

	private File getCacheFile(IPath path) {
		File cacheFile = new File(this.cacheRoot, path.toString());
		return cacheFile;
	}

	public ICachedContentHandle createCacheContentHandle(ICacheService service, IPath path) {
		File cacheFile = new File(this.cacheRoot, path.toString());
		return new CachedFileHandle(this, cacheFile);
	}

	public ITemporaryContentHandle createTemporaryHandle(ICacheService service, IPath path, boolean append) throws CoreException {
		try {
			lockForWrite();

			File cacheFile = getCacheFile(path);

			if (append) {
				try {
					return new TemporaryFileHandle(this, path, cacheFile, this.openOputputStream(cacheFile, append));
				} catch (IOException e) {
					throw new SemanticResourceException(SemanticResourceStatusCode.FILECACHE_CACHEFILE_CREATION_FAILED, path, null, e);
				}
			}

			this.uniqueID++;

			File tempFile = new File(this.cacheRoot, path + DOT_SEPARATOR + this.uniqueID + TEMP_FILE_EXTENSION);

			while (tempFile.exists()) {
				this.uniqueID++;
				tempFile = new File(this.cacheRoot, path + DOT_SEPARATOR + this.uniqueID + TEMP_FILE_EXTENSION);
			}

			try {
				return new TemporaryFileHandle(this, path, tempFile, cacheFile, this.openOputputStream(tempFile, append));
			} catch (IOException e) {
				throw new SemanticResourceException(SemanticResourceStatusCode.FILECACHE_CACHEFILE_CREATION_FAILED, path, null, e);
			}
		} finally {
			unlockForWrite();
		}
	}

	/**
	 * Renames the file, deletes destination file before, deletes source file in
	 * case of error
	 * 
	 * @param source
	 *            the source file
	 * @param target
	 *            the target file
	 * @throws CoreException
	 *             upon failure
	 */
	public void doRename(File source, File target) throws CoreException {
		try {
			lockForWrite();

			if (target.exists()) {
				try {
					this.doDelete(target);
				} catch (CoreException e) {
					// delete source content
					this.tryDelete(source);
					throw e;
				}
			}

			if (!source.renameTo(target)) {
				IPath path = new Path(target.getAbsolutePath());
				throw new SemanticResourceException(SemanticResourceStatusCode.FILECACHE_CACHEFILE_RENAME_FAILED, path, MessageFormat
						.format(Messages.FileHandleFactory_TempFileNotRenamed_XMSG, target.getAbsolutePath()));
			}
		} finally {
			unlockForWrite();
		}
	}

	/**
	 * Checks the existence of the file.
	 * 
	 * @param file
	 * @return false if file doesn't exist or an unsuccessful attempt to delete
	 *         the file has been made
	 */
	public boolean checkFileExists(File file) {
		try {
			lockForWrite();
			if (this.hasFailedDeletion(file)) {
				// try deletion again
				tryDelete(file);
				// return the "logical" deletion state
				return false;
			}
			return file.exists();
		} finally {
			unlockForWrite();
		}
	}

	/**
	 * Deletes the file and compacts the file system
	 * 
	 * @param file
	 *            the file
	 * @throws CoreException
	 *             upon failure
	 */
	public void doDelete(File file) throws CoreException {
		try {
			lockForWrite();

			if (!file.delete()) {
				this.reportDeletionFailed(file);
				throw new SemanticResourceException(SemanticResourceStatusCode.FILECACHE_CACHEFILE_DELETION_FAILED, new Path(file
						.getAbsolutePath()), MessageFormat.format(Messages.FileHandleFactory_CacheFileNotDeleted_XMSG, file
						.getAbsolutePath()));
			}

			this.reportDeletionSucceeded(file);
			this.compactFileSystem(file);
		} finally {
			unlockForWrite();
		}
	}

	/**
	 * Tries to delete the file and compacts the file system
	 * 
	 * @param file
	 *            the file
	 */
	public void tryDelete(File file) {
		try {
			lockForWrite();

			if (!file.delete()) {
				this.reportDeletionFailed(file);
				return;
			}

			this.reportDeletionSucceeded(file);
			this.compactFileSystem(file);
		} finally {
			unlockForWrite();
		}
	}

	private void safeLog(CoreException e) {

		try {
			ISemanticFileSystem sfs = (ISemanticFileSystem) EFS.getFileSystem(ISemanticFileSystem.SCHEME);
			sfs.getLog().log(e);
		} catch (CoreException e1) {
			// $JL-EXC$
			// TODO 0.1: fallback using log via bundle
		}

	}

	private void compactFileSystem(File deletedFile) {
		// delete parent folder if this is the last file in this folder
		File parent = deletedFile.getParentFile();

		if (parent.exists() && !parent.equals(this.cacheRoot)) {
			File[] children = parent.listFiles();

			if (children != null) {
				if (children.length == 0) {
					parent.delete();
				}
			}
		}
	}

	private void reportDeletionFailed(File file) {
		this.failedDeletions.add(file.getAbsolutePath());
		this.save();
	}

	private void reportDeletionSucceeded(File file) {
		this.failedDeletions.remove(file.getAbsolutePath());
		this.save();
	}

	private boolean hasFailedDeletion(File file) {
		return this.failedDeletions.contains(file.getAbsolutePath());
	}

	private OutputStream openOputputStream(File file, boolean appendMode) throws FileNotFoundException {
		file.getParentFile().mkdirs();

		return new FileOutputStream(file, appendMode);
	}

	private void save() {
		if (!this.failedDeletions.isEmpty()) {
			FileOutputStream fos = null;
			ObjectOutputStream oos = null;
			try {
				fos = new FileOutputStream(this.deletionsFile);
				oos = new ObjectOutputStream(fos);

				oos.writeObject(this.failedDeletions);

			} catch (IOException e) {
				IPath path = new Path(this.deletionsFile.getAbsolutePath());
				safeLog(new SemanticResourceException(SemanticResourceStatusCode.FILECACHE_ERROR_WRITING_METADATA, path,
						Messages.FileHandleFactory_FileHandleFactory_FileHandleFactory_SaveError_XMSG, e));
			} finally {
				Util.safeClose(fos);
				Util.safeClose(oos);
			}
		} else {
			if (this.deletionsFile.exists()) {
				if (!this.deletionsFile.delete()) {
					// TODO 0.1: should we be more robust here and switch to
					// another file?
					IPath path = new Path(this.deletionsFile.getAbsolutePath());
					safeLog(new SemanticResourceException(SemanticResourceStatusCode.FILECACHE_ERROR_WRITING_METADATA, path,
							Messages.FileHandleFactory_FileHandleFactory_FileHandleFactory_SaveError_XMSG));
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void load() {
		if (this.deletionsFile.exists()) {
			FileInputStream fis = null;
			ObjectInputStream ois = null;
			try {
				fis = new FileInputStream(this.deletionsFile);
				ois = new ObjectInputStream(fis);

				this.failedDeletions = (HashSet<String>) ois.readObject();
			} catch (IOException e) {
				IPath path = new Path(this.deletionsFile.getAbsolutePath());
				safeLog(new SemanticResourceException(SemanticResourceStatusCode.FILECACHE_INITIALIZATION_ERROR, path,
						Messages.FileHandleFactory_FileHandleFactory_LoadError_XMSG, e));
			} catch (ClassNotFoundException e) {
				IPath path = new Path(this.deletionsFile.getAbsolutePath());
				safeLog(new SemanticResourceException(SemanticResourceStatusCode.FILECACHE_INITIALIZATION_ERROR, path,
						Messages.FileHandleFactory_FileHandleFactory_LoadError_XMSG, e));
			} finally {
				Util.safeClose(fis);
				Util.safeClose(ois);
			}
		}
	}
}
