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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Represents a remote item
 * 
 */
public abstract class RemoteItem {

	/**
	 * the type
	 * 
	 */
	public enum Type {
		/**
		 * File
		 */
		FILE, //

		/**
		 * Folder
		 */
		FOLDER
	}

	protected final RemoteStoreTransient myStore;
	protected final RemoteFolder myParent;
	private final IPath myPath;
	private final Type myType;
	private final String myName;
	private boolean lockFlag;

	/**
	 * Constructor
	 * 
	 * @param store
	 *            the store
	 * 
	 * @param parent
	 *            the parent
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 */
	public RemoteItem(RemoteStoreTransient store, RemoteFolder parent, String name, Type type) {

		if (parent != null && parent.getType() != Type.FOLDER) {
			throw new IllegalArgumentException(Messages.RemoteItem_ItemsUnderFolderOrRoot_XMSG);
		}

		this.myParent = parent;
		this.myStore = store;
		this.myType = type;
		this.myName = name;
		this.lockFlag = false;

		if (parent != null) {
			this.myPath = parent.getPath().append(name);
		} else {
			this.myPath = new Path(""); //$NON-NLS-1$
		}
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return this.myType;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.myName;
	}

	/**
	 * @return the path
	 */
	public IPath getPath() {
		return this.myPath;
	}

	/**
	 * @return the locked flag
	 */
	public boolean isLocked() {
		return this.lockFlag;
	}

	/**
	 * @param lockFlag
	 *            the lock flag
	 */
	public void setLocked(boolean lockFlag) {
		this.lockFlag = lockFlag;
	}

	/**
	 * 
	 * @return the {@link RemoteStore}
	 */
	public RemoteStoreTransient getStore() {
		return this.myStore;
	}

	/**
	 * @return folder
	 */
	public RemoteFolder getParent() {
		return this.myParent;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.myPath == null) ? 0 : this.myPath.hashCode());
		result = prime * result + ((this.myStore == null) ? 0 : this.myStore.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemoteItem other = (RemoteItem) obj;
		if (this.myPath == null) {
			if (other.myPath != null)
				return false;
		} else if (!this.myPath.equals(other.myPath))
			return false;
		if (this.myStore == null) {
			if (other.myStore != null)
				return false;
		} else if (!this.myStore.equals(other.myStore))
			return false;
		return true;
	}

}
