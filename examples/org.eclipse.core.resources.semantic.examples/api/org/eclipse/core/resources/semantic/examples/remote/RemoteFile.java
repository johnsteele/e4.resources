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
package org.eclipse.core.resources.semantic.examples.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.SemanticFileRevision;
import org.eclipse.core.resources.semantic.spi.SemanticRevisionStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileRevision;

/**
 * Represents a remote file
 * 
 */
public class RemoteFile extends RemoteItem {

	/**
	 * 
	 */
	public final DateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss_SSS"); //$NON-NLS-1$
	private long lastTime = System.currentTimeMillis();

	long myRemoteTimestamp;
	byte[] myContent;

	final Map<String, byte[]> myVersions = new HashMap<String, byte[]>();
	private final MyBytes myStream;

	// we synchronize all methods
	final class MyBytes extends ByteArrayOutputStream {

		private boolean isOpen = false;

		public synchronized void write(byte[] b, int off, int len) {
			this.isOpen = true;
			super.write(b, off, len);
		}

		public synchronized void write(int b) {
			this.isOpen = true;
			super.write(b);
		}

		public synchronized void write(byte[] b) throws IOException {
			this.isOpen = true;
			super.write(b);
		}

		public synchronized void close() throws IOException {
			super.close();
			if (this.isOpen) {
				RemoteFile.this.myVersions.put(Long.toString(RemoteFile.this.myRemoteTimestamp), RemoteFile.this.myContent);
				RemoteFile.this.myContent = this.toByteArray();
				RemoteFile.this.myRemoteTimestamp = newTime();
				this.isOpen = false;
			}
		}

		public synchronized void reset() {
			super.reset();
			if (this.isOpen) {
				this.isOpen = false;
			}
		}

		public void setAppend(boolean append) {
			if (!append) {
				super.reset();
				this.isOpen = true;
			}
		}

	}

	/**
	 * Constructor
	 * 
	 * @param store
	 * 
	 * @param parent
	 *            parent folder
	 * @param name
	 *            name
	 * @param content
	 *            content
	 * @param timestamp
	 *            timestamp
	 */
	public RemoteFile(RemoteStoreTransient store, RemoteFolder parent, String name, byte[] content, long timestamp) {

		super(store, parent, name, Type.FILE);
		if (parent.getType() != Type.FOLDER) {
			throw new IllegalArgumentException(Messages.RemoteFile_FilesOnlyUnderFolders_XMSG);
		}

		this.myContent = content;
		this.myRemoteTimestamp = timestamp;
		this.myStream = new MyBytes();

	}

	/**
	 * @return the content
	 */
	public byte[] getContent() {
		return this.myContent;
	}

	/**
	 * @param append
	 * @return the stream
	 */
	public ByteArrayOutputStream getOutputStream(boolean append) {
		this.myStream.setAppend(append);
		return this.myStream;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return this.myRemoteTimestamp;
	}

	/**
	 * @param value
	 *            the timestamp
	 */
	public void setTimestamp(long value) {
		this.myRemoteTimestamp = value;
	}

	/**
	 * @param store
	 *            the store
	 * @return the history
	 */
	public IFileHistory getHistory(final ISemanticFileStore store) {

		List<IFileRevision> revisions = new ArrayList<IFileRevision>();
		for (final Map.Entry<String, byte[]> entry : this.myVersions.entrySet()) {
			long time = Long.parseLong(entry.getKey());

			String name = NLS.bind(Messages.RemoteFile_Version_XGRP, this.format.format(new Date(time)));
			revisions.add(new SemanticFileRevision(store, time, "", name) { //$NON-NLS-1$

						public IStorage getStorage(IProgressMonitor monitor) throws CoreException {
							SemanticRevisionStorage storage = new SemanticRevisionStorage(store);
							storage.setContents(new ByteArrayInputStream(entry.getValue()), monitor);
							return storage;
						}
					});

		}
		revisions.add(getCurrentRevision(store));

		final IFileRevision[] revs = revisions.toArray(new IFileRevision[0]);
		return new IFileHistory() {

			public IFileRevision[] getTargets(IFileRevision revision) {
				return null;
			}

			public IFileRevision[] getFileRevisions() {
				return revs;
			}

			public IFileRevision getFileRevision(String id) {
				for (IFileRevision rev : getFileRevisions()) {
					if (rev.getContentIdentifier().equals(id)) {
						return rev;
					}
				}
				return null;
			}

			public IFileRevision[] getContributors(IFileRevision revision) {
				return null;
			}
		};
	}

	/**
	 * @param store
	 *            the store
	 * @return the current revision
	 */
	public IFileRevision getCurrentRevision(final ISemanticFileStore store) {
		String name = NLS.bind(Messages.RemoteFile_Current_XGRP, this.format.format(new Date(getTimestamp())));
		return new SemanticFileRevision(store, getTimestamp(), "", name) { //$NON-NLS-1$

			public IStorage getStorage(IProgressMonitor monitor) throws CoreException {
				SemanticRevisionStorage storage = new SemanticRevisionStorage(store);
				storage.setContents(new ByteArrayInputStream(RemoteFile.this.myContent), monitor);
				return storage;
			}
		};

	}

	synchronized long newTime() {

		long actTime = this.lastTime;
		try {
			actTime = System.currentTimeMillis();
			if (actTime > this.lastTime) {
				return actTime;
			}
			int add = 1;
			while (actTime <= this.lastTime) {
				actTime = System.currentTimeMillis() + add;
				add++;
			}
			return actTime;
		} finally {
			this.lastTime = actTime;
		}

	}

}
