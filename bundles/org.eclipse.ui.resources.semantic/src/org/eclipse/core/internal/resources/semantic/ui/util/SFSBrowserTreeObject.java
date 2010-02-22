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
package org.eclipse.core.internal.resources.semantic.ui.util;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.IPath;

/**
 * The browser node
 */
public class SFSBrowserTreeObject {

	private final IPath myPath;
	private final IFileSystem fs;

	/**
	 * Constructor
	 * 
	 * @param fileSystem
	 *            the file system
	 * @param path
	 *            the path
	 */
	public SFSBrowserTreeObject(IFileSystem fileSystem, IPath path) {
		this.myPath = path;
		this.fs = fileSystem;
	}

	/**
	 * @return the path
	 */
	public IPath getPath() {
		return this.myPath;
	}

	/**
	 * @return the file store
	 */
	public IFileStore getStore() {
		return this.fs.getStore(this.myPath);
	}

	/**
	 * @return the file info
	 */
	public IFileInfo getInfo() {
		return getStore().fetchInfo();

	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SFSBrowserTreeObject) {
			return ((SFSBrowserTreeObject) obj).getPath().equals(getPath());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getPath().hashCode();
	}

}
