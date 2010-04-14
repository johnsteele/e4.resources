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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
	private static final String ALTERNATIVE_FILES_DOLLAR = ".alternativeFiles.$$$"; //$NON-NLS-1$
	private static final String FAILED_DELETIONS_OF_ALTERNATIVES_DOLLAR = ".failedDeletionsOfAlternatives.$$$"; //$NON-NLS-1$
	private static final String DOT_SEPARATOR = "."; //$NON-NLS-1$
	private static final String TEMP_FILE_EXTENSION = ".$$$"; //$NON-NLS-1$
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock writeLock = this.rwl.writeLock();
	private HashSet<String> failedDeletions = new HashSet<String>();
	private HashMap<String, String> activeAlternativeFileMapping = new HashMap<String, String>();
	private HashSet<String> deletionsOfAlternatives = new HashSet<String>();
	private final File deletionsFile;
	private final File alternativesMappingFile;
	private final File deletionsOfAlternativesFile;

	private final File cacheRoot;
	private long uniqueID = 0;

	/**
	 * @param cacheRoot
	 *            the cache root directory
	 */
	public FileHandleFactory(File cacheRoot) {
		this.cacheRoot = cacheRoot;
		this.deletionsFile = new File(this.cacheRoot, FAILED_DELETIONS_DOLLAR);
		this.alternativesMappingFile = new File(this.cacheRoot, ALTERNATIVE_FILES_DOLLAR);
		this.deletionsOfAlternativesFile = new File(this.cacheRoot, FAILED_DELETIONS_OF_ALTERNATIVES_DOLLAR);

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

	public void removeContentRecursive(CacheService cacheService, IPath path) {
		try {
			lockForWrite();

			File root = getCacheFile(path);
			if (root.exists() && !root.equals(this.cacheRoot)) {
				ArrayList<File> filesToBeDeleted = new ArrayList<File>();

				this.collectFiles(root, filesToBeDeleted);

				for (File file : filesToBeDeleted) {
					if (!file.delete()) {
						this.reportDeletionFailed(file);
					}
				}

				this.compactFileSystemRecursively(root);

				this.compactFileSystem(root);
			}
		} finally {
			unlockForWrite();
		}
	}

	private void collectFiles(File root, ArrayList<File> filesToBeDeleted) {
		if (root.isDirectory()) {
			File[] children = root.listFiles();

			if (children != null) {
				for (File child : children) {
					if (child.isDirectory()) {
						collectFiles(child, filesToBeDeleted);
					} else {
						filesToBeDeleted.add(child);
					}
				}
			}
		} else {
			// single file
			if (root.exists()) {
				filesToBeDeleted.add(root);
			}
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

			if (!this.cleanupBeforeRename(target)) {
				// make the source content as new alternate content
				addAlternativeFile(source, target);
				return;
			}

			if (!source.renameTo(target)) {
				// should never happen
				IPath path = new Path(target.getAbsolutePath());
				throw new SemanticResourceException(SemanticResourceStatusCode.FILECACHE_CACHEFILE_RENAME_FAILED, path, MessageFormat
						.format(Messages.FileHandleFactory_TempFileNotRenamed_XMSG, target.getAbsolutePath()));
			}
		} finally {
			unlockForWrite();
		}
	}

	/*
	 * returns true if the target could be deleted
	 */
	private boolean cleanupBeforeRename(File target) {
		String targetPath = target.getAbsolutePath();

		// check whether an alternative file is used and try to delete it since
		// it is obsolete now
		tryToDeleteAlternativeFile(targetPath);

		this.retryToDeleteAlternatives();

		// clear the pending deletions flag since target will exist after rename
		reportDeletionSucceeded(target);

		// check whether a file exists with original file name and try to delete
		// it before rename
		if (target.exists()) {
			if (!target.delete()) {
				// alternative name should be used
				// the original file should not be added to failedDeletions here
				// failed deletion will be retried on next rename or on cache
				// entry deletion
				return false;
			}
		}
		// if is safe to rename to original file name
		return true;
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
	 * Tries to delete the file and compacts the file system
	 * 
	 * @param file
	 *            the file
	 */
	public void tryDelete(File file) {
		try {
			lockForWrite();

			tryToDeleteAlternativeFile(file.getAbsolutePath());

			if (!file.delete()) {
				this.reportDeletionFailed(file);
				return;
			}

			this.reportDeletionSucceeded(file);
			this.retryToDeleteAlternatives();
			this.compactFileSystem(file);
		} finally {
			unlockForWrite();
		}
	}

	/**
	 * returns the active file handle for the cache file
	 * 
	 * @param file
	 */
	public File getActiveFileHandle(File file) {
		try {
			lockForWrite();
			String alternatePath = this.activeAlternativeFileMapping.get(file.getAbsolutePath());
			if (alternatePath != null) {
				return new File(alternatePath);
			}
			return file;
		} finally {
			unlockForWrite();
		}
	}

	/**
	 * 
	 * @param cacheFile
	 * @return input stream
	 * @throws FileNotFoundException
	 */
	public InputStream openInputStream(File cacheFile) throws FileNotFoundException {
		try {
			lockForWrite();
			return new FileInputStream(getActiveFileHandle(cacheFile));
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

	private void compactFileSystemRecursively(File root) {
		File[] children = root.listFiles();

		if (children != null && children.length > 0) {
			for (File file : children) {
				if (file.isDirectory()) {
					this.compactFileSystemRecursively(file);
				}
			}
		}

		children = root.listFiles();
		if (children != null) {
			if (children.length == 0) {
				root.delete();
			}
		}
	}

	private void retryToDeleteAlternatives() {
		boolean saveNeeded = false;
		HashSet<String> deletions = this.deletionsOfAlternatives;
		ArrayList<String> toBeRemoved = new ArrayList<String>();

		for (String filePath : deletions) {
			File file = new File(filePath);

			if (file.exists()) {
				if (file.delete()) {
					toBeRemoved.add(filePath);
					saveNeeded = true;
				}
			} else {
				saveNeeded = true;
				toBeRemoved.add(filePath);
			}
		}

		for (String string : toBeRemoved) {
			deletions.remove(string);
		}

		if (saveNeeded) {
			this.saveDeletionsOfAlternatives();
		}
	}

	private void reportDeletionFailed(File file) {
		this.failedDeletions.add(file.getAbsolutePath());
		this.saveFailedDeletions();
	}

	private void reportDeletionSucceeded(File file) {
		String path = file.getAbsolutePath();
		if (this.failedDeletions.contains(path)) {
			this.failedDeletions.remove(path);
			this.saveFailedDeletions();
		}
	}

	private boolean hasFailedDeletion(File file) {
		return this.failedDeletions.contains(file.getAbsolutePath());
	}

	private OutputStream openOputputStream(File file, boolean appendMode) throws FileNotFoundException {
		file.getParentFile().mkdirs();

		return new FileOutputStream(file, appendMode);
	}

	private void addAlternativeFile(File source, File target) {
		this.activeAlternativeFileMapping.put(target.getAbsolutePath(), source.getAbsolutePath());
		this.saveAlternativeMapping();
	}

	private void tryToDeleteAlternativeFile(String targetPath) {
		String alternatePath = this.activeAlternativeFileMapping.get(targetPath);
		if (alternatePath != null) {
			File alternateFile = new File(alternatePath);

			if (!alternateFile.delete()) {
				// remember failed deletion in order to retry it later
				this.deletionsOfAlternatives.add(alternatePath);
				this.saveDeletionsOfAlternatives();
			}

			this.activeAlternativeFileMapping.remove(targetPath);
			this.saveAlternativeMapping();
		}
	}

	private void saveFailedDeletions() {
		if (!this.failedDeletions.isEmpty()) {
			writeObjectToMetadataFile(this.deletionsFile, this.failedDeletions);
		} else {
			removeMetadataFile(this.deletionsFile);
		}
	}

	private void saveDeletionsOfAlternatives() {
		if (!this.deletionsOfAlternatives.isEmpty()) {
			writeObjectToMetadataFile(this.deletionsOfAlternativesFile, this.deletionsOfAlternatives);
		} else {
			removeMetadataFile(this.deletionsOfAlternativesFile);
		}
	}

	private void saveAlternativeMapping() {
		if (!this.activeAlternativeFileMapping.isEmpty()) {
			writeObjectToMetadataFile(this.alternativesMappingFile, this.activeAlternativeFileMapping);
		} else {
			removeMetadataFile(this.alternativesMappingFile);
		}
	}

	@SuppressWarnings("unchecked")
	private void load() {
		if (this.deletionsFile.exists()) {
			HashSet<String> object = (HashSet<String>) readObjectFromMetadataFile(this.deletionsFile);
			if (object != null) {
				this.failedDeletions = object;
			}
		}
		if (this.deletionsOfAlternativesFile.exists()) {
			HashSet<String> object = (HashSet<String>) readObjectFromMetadataFile(this.deletionsOfAlternativesFile);
			if (object != null) {
				this.deletionsOfAlternatives = object;
			}
		}
		if (this.alternativesMappingFile.exists()) {
			HashMap<String, String> object = (HashMap<String, String>) readObjectFromMetadataFile(this.alternativesMappingFile);
			if (object != null) {
				this.activeAlternativeFileMapping = object;
			}
		}
	}

	private Object readObjectFromMetadataFile(File file) {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);

			return ois.readObject();
		} catch (IOException e) {
			IPath path = new Path(file.getAbsolutePath());
			safeLog(new SemanticResourceException(SemanticResourceStatusCode.FILECACHE_INITIALIZATION_ERROR, path,
					Messages.FileHandleFactory_FileHandleFactory_LoadError_XMSG, e));
		} catch (ClassNotFoundException e) {
			IPath path = new Path(file.getAbsolutePath());
			safeLog(new SemanticResourceException(SemanticResourceStatusCode.FILECACHE_INITIALIZATION_ERROR, path,
					Messages.FileHandleFactory_FileHandleFactory_LoadError_XMSG, e));
		} finally {
			Util.safeClose(fis);
			Util.safeClose(ois);
		}
		return null;
	}

	private void removeMetadataFile(File file) {
		if (file.exists()) {
			if (!file.delete()) {
				// TODO 0.1: should we be more robust here and switch to
				// another file?
				IPath path = new Path(file.getAbsolutePath());
				safeLog(new SemanticResourceException(SemanticResourceStatusCode.FILECACHE_ERROR_WRITING_METADATA, path,
						Messages.FileHandleFactory_FileHandleFactory_FileHandleFactory_SaveError_XMSG));
			}
		}
	}

	private void writeObjectToMetadataFile(File file, Object object) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(file);
			oos = new ObjectOutputStream(fos);

			oos.writeObject(object);

		} catch (IOException e) {
			IPath path = new Path(file.getAbsolutePath());
			safeLog(new SemanticResourceException(SemanticResourceStatusCode.FILECACHE_ERROR_WRITING_METADATA, path,
					Messages.FileHandleFactory_FileHandleFactory_FileHandleFactory_SaveError_XMSG, e));
		} finally {
			Util.safeClose(fos);
			Util.safeClose(oos);
		}
	}

}
