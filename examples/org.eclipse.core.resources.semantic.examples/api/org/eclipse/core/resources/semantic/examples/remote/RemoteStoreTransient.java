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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IPath;

/**
 *
 */
public class RemoteStoreTransient {

	protected final IContainer myContainer;
	private long lastTime = System.currentTimeMillis();

	private RemoteFolder myRoot = new RemoteFolder(this);

	/**
	 * @param container
	 */
	public RemoteStoreTransient(IContainer container) {
		this.myContainer = container;
	}

	/**
	 * @return a new timestamp
	 */
	public synchronized long newTime() {

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

	/**
	 * Removes all content from this repository (except the root folder)
	 */
	public void reset() {
		this.myRoot = new RemoteFolder(this);
	}

	/**
	 * @return the root folder
	 */
	public RemoteFolder getRootFolder() {

		return this.myRoot;
	}

	/**
	 * @param path
	 *            the path
	 * @return the item, or null
	 */
	public RemoteItem getItemByPath(IPath path) {

		RemoteItem item = getRootFolder();
		for (String segment : path.segments()) {
			if (item instanceof RemoteFolder) {
				item = ((RemoteFolder) item).getChild(segment);
				if (item == null) {
					return null;
				}
			}
		}
		return item;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.myContainer.getFullPath() == null) ? 0 : this.myContainer.getFullPath().hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemoteStore other = (RemoteStore) obj;
		if (this.myContainer.getFullPath() == null) {
			if (other.myContainer.getFullPath() != null)
				return false;
		} else if (!this.myContainer.getFullPath().equals(other.myContainer.getFullPath()))
			return false;
		return true;
	}
}
