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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.IFileRevision;

/**
 * Utility class for implementing
 * {@link IFileRevision#getStorage(org.eclipse.core.runtime.IProgressMonitor)}
 * in {@link SemanticFileRevision}
 * 
 */
public class SemanticRevisionStorage implements IStorage {

	private final IPath myPath;
	private final String myName;
	private byte[] myContents = new byte[0];
	private boolean inputSet = false;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @param store
	 *            the file store
	 */
	public SemanticRevisionStorage(ISemanticFileStore store) {
		this.myPath = store.getPath();
		this.myName = store.getName();

	}

	/**
	 * 
	 * @param contents
	 *            the contents, will be closed after execution of this
	 *            constructor
	 * @param monitor
	 *            a progress monitor, may be <code>null</code>
	 * @throws CoreException
	 */
	public void setContents(InputStream contents, IProgressMonitor monitor) throws CoreException {
		int currSize = 0;
		try {
			int available = contents.available();
			while (available > 0) {
				byte[] buffer = new byte[available];
				contents.read(buffer);
				currSize = currSize + available;
				byte[] lastContent = this.myContents;
				this.myContents = new byte[currSize];
				System.arraycopy(lastContent, 0, this.myContents, 0, lastContent.length);
				System.arraycopy(buffer, 0, this.myContents, lastContent.length, buffer.length);
				available = contents.available();
			}
			this.inputSet = true;
		} catch (IOException ioe) {
			// $JL-EXC$
			// TODO 0.1 status code
			throw new SemanticResourceException(SemanticResourceStatusCode.CACHED_CONTENT_NOT_FOUND, this.myPath, ioe.getMessage());
		} finally {
			Util.safeClose(contents);
		}
	}

	/**
	 * @throws CoreException
	 */
	public InputStream getContents() throws CoreException {
		if (!this.inputSet) {
			throw new IllegalStateException("Contents was not set on before calling getContents()"); //$NON-NLS-1$
		}
		return new ByteArrayInputStream(this.myContents);
	}

	public IPath getFullPath() {
		return this.myPath;
	}

	public String getName() {
		return this.myName;
	}

	public boolean isReadOnly() {
		return true;
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

}
