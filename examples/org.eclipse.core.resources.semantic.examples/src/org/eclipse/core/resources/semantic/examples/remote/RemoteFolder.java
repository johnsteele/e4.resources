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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.osgi.util.NLS;

/**
 * Represents a remote folder
 * 
 */
public class RemoteFolder extends RemoteItem {

	private final List<RemoteItem> myChildren = new ArrayList<RemoteItem>();

	/**
	 * Constructor
	 * 
	 * @param store
	 * 
	 * @param parent
	 *            the parent folder
	 * @param name
	 *            the name
	 */
	public RemoteFolder(RemoteStoreTransient store, RemoteFolder parent, String name) {
		super(store, parent, name, Type.FOLDER);
	}

	/**
	 * Constructor for the root folder
	 * 
	 * @param store
	 */
	public RemoteFolder(RemoteStoreTransient store) {
		super(store, null, "", Type.FOLDER); //$NON-NLS-1$
	}

	/**
	 * Add a child folder
	 * 
	 * @param name
	 *            the name
	 * @return the child folder
	 */
	public RemoteFolder addFolder(String name) {
		if (hasChild(name)) {
			throw new IllegalArgumentException(NLS.bind(Messages.RemoteFolder_ChildAlreadyExists_XMSG, name, this.getPath().toPortableString()));
		}
		RemoteFolder child = new RemoteFolder(this.myStore, this, name);
		this.myChildren.add(child);
		return child;
	}

	/**
	 * Add a child file
	 * 
	 * @param name
	 *            the name
	 * @param content
	 *            the content
	 * @param timestamp
	 *            the timestamp
	 * @return the new file
	 */
	public RemoteFile addFile(String name, byte[] content, long timestamp) {
		if (hasChild(name)) {
			throw new IllegalArgumentException(NLS.bind(Messages.RemoteFolder_ChildAlreadyExists_XMSG, name, this.getPath().toPortableString()));
		}
		RemoteFile child = new RemoteFile(this.myStore, this, name, content, timestamp);
		this.myChildren.add(child);
		return child;
	}

	/**
	 * @return the children
	 */
	public List<RemoteItem> getChildren() {
		return this.myChildren;
	}

	/**
	 * @param name
	 *            the child name
	 * @return true if the child exists
	 */
	public boolean hasChild(String name) {
		return getChild(name) != null;
	}

	/**
	 * @param name
	 *            the name
	 * @return the child, or null
	 */
	public RemoteItem getChild(String name) {
		for (RemoteItem child : this.myChildren) {
			if (child.getName().equals(name)) {
				return child;
			}
		}
		return null;
	}

	/**
	 * @param name
	 *            the name
	 */
	public void deleteChild(String name) {
		this.myChildren.remove(getChild(name));
	}

}
