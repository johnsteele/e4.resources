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

import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.semantic.ISemanticProperties;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;

/**
 * This interface establishes the contract between content providers and the
 * Semantic File System.
 * <p>
 * <em> This interface is not intended to be used by tools (e.g. editors); it
 *  may only be used by content providers for interacting with the Semantic 
 *  File System.
 * </em>
 * <p>
 * The methods in this interface can be categorized as follows:
 * <p>
 * <h3>Child list manipulation</h3>
 * The following methods are used for manipulating the child list of a Semantic
 * File System node.
 * <ul>
 * <li>{@link #addChildFile(String)}</li>
 * <li>{@link #addChildFolder(String)}</li>
 * <li>{@link #addLocalChildResource(String, String)}</li>
 * <li>{@link #remove(IProgressMonitor)}</li>
 * </ul>
 * <h3>Attribute handling</h3>
 * Persistent and volatile attribute handling is provided through super
 * interface {@link ISemanticProperties}
 * <h3>State handling</h3>
 * The following methods can be used to access the state of the Semantic File
 * System node:
 * <ul>
 * <li>{@link #getType()}</li>
 * <li>{@link #getContentProviderID()}</li>
 * </ul>
 * 
 * <h3>Other methods</h3>
 * {@link #getPath()} is provided here so that content providers can use this
 * information where needed.
 * <p>
 * 
 * @since 4.0
 * @see ISemanticContentProvider
 * @see ISemanticContentProviderREST
 * @see ISemanticProperties
 * @see IFileStore
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISemanticFileStore extends ISemanticProperties, IFileStore {

	/** The Resource Type enumeration */
	public enum ResourceType {
		/** Unknown */
		UNKNOWN_TYPE(0),
		/** File */
		FILE_TYPE(1),
		/** Folder */
		FOLDER_TYPE(2),
		/** Project */
		PROJECT_TYPE(3);

		private final int code;

		private ResourceType(int value) {
			this.code = value;
		}

		/**
		 * Converts the type to it's int value.
		 * 
		 * @return the int value
		 */
		public int toInt() {
			return this.code;
		}
	}

	/**
	 * The type of a resource might be set lazily
	 */
	public static int UNKNOWN = 0;
	/**
	 * This store represents a File
	 */
	public static int FILE = 1;
	/**
	 * This store represents a Folder
	 */
	public static int FOLDER = 2;
	/**
	 * This store represents a Project
	 */
	public static int PROJECT = 3;

	/**
	 * Returns the type of this store.
	 * 
	 * @return {@link #FILE}, {@link #FOLDER}, or {@link #UNKNOWN}
	 */
	public int getType();

	/**
	 * Returns the path of this store relative to the Semantic File System root
	 * 
	 * @return the path
	 */
	public IPath getPath();

	/**
	 * Adds a child file with the given name.
	 * 
	 * @param name
	 *            the name of the child
	 * 
	 * @throws CoreException
	 *             in case of failure
	 */
	public void addChildFile(String name) throws CoreException;

	/**
	 * Adds a local child resource with the given name.
	 * 
	 * @param name
	 *            the name of the child
	 * @param contentProviderID
	 *            the ID of content provider that should be responsible for the
	 *            child resource, or <code>null</code> if this content provider
	 *            is responsible for it.
	 * @throws CoreException
	 *             in case of failure
	 */
	public void addLocalChildResource(String name, String contentProviderID) throws CoreException;

	/**
	 * Adds a child folder with the given name.
	 * 
	 * @param name
	 *            the name of the child
	 * @throws CoreException
	 *             in case of failure
	 */
	public void addChildFolder(String name) throws CoreException;

	/**
	 * Adds a file store as child and sets a content provider id and properties
	 * <p>
	 * 
	 * @param name
	 *            the name of the child
	 * @param asFolder
	 *            <code>true</code> if the child should be a folder or
	 *            <code>false</code> in case of file
	 * 
	 * @param contentProviderID
	 *            the ID of content provider that should be responsible for the
	 *            child store, or <code>null</code> if this content provider is
	 *            responsible for it.
	 * @param properties
	 *            properties to be set as persistent properties on the new file
	 *            store
	 * @throws CoreException
	 *             in case of failure
	 */
	public void addChildResource(String name, boolean asFolder, String contentProviderID, Map<QualifiedName, String> properties)
			throws CoreException;

	/**
	 * Removes a child from the list of children.
	 * 
	 * @param monitor
	 *            may be null
	 * @throws CoreException
	 *             in case of failure
	 */
	public void remove(IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns the "effective" content provider.
	 * <p>
	 * If this is a resource which has a content provider ID assigned, this
	 * method returns the content provider associated with this content provider
	 * ID, otherwise the folder hierarchy is traversed upwards until a folder is
	 * found that has a content provider ID assigned and the corresponding
	 * content provider is returned. If no such folder is found in the parent
	 * hierarchy, the default content provider is returned.
	 * 
	 * @return the "effective" content provider
	 * @throws CoreException
	 *             in case of failure
	 */
	public ISemanticContentProvider getEffectiveContentProvider() throws CoreException;

	/**
	 * Returns the content provider ID.
	 * 
	 * @return the ID string if this is a root store for a content provider, or
	 *         <code>null</code> otherwise
	 */
	public String getContentProviderID();

	/**
	 * Returns the exists flag.
	 * <p>
	 * TODO javadoc
	 * 
	 * @return <code>true</code> if the underlying resource exists
	 */
	public boolean isExists();

	/**
	 * @return <code>true</code> if this refers to a local-only resource
	 */
	public boolean isLocalOnly();

}
