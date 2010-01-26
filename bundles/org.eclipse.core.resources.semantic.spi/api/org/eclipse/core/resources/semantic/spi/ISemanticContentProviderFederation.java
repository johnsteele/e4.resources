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

import org.eclipse.core.resources.semantic.spi.ISemanticFileStore.ResourceType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;

/**
 * This interface must be implemented by content providers that support
 * federation, i.e. nesting of other content providers that take control over a
 * resource subtree.
 * <p>
 * Note that the rule factory must deal with federated children properly for
 * create and delete rules (i.e. must not delegate back to the federated content
 * provider).
 * 
 * @since 4.0
 * 
 * @see ISemanticContentProvider#getRuleFactory()
 */
public interface ISemanticContentProviderFederation {

	/**
	 * Adds a resource with content provider ID
	 * 
	 * @param parentStore
	 *            the parent resource handle
	 * @param name
	 *            the name of the resource
	 * @param resourceType
	 *            {@link ResourceType#FILE_TYPE} or
	 *            {@link ResourceType#FOLDER_TYPE}
	 * @param providerId
	 *            the content provider ID
	 * @param properties
	 *            properties, may be <code>null</code>
	 * @throws CoreException
	 *             upon failure
	 */
	public void addResource(ISemanticFileStore parentStore, String name, ResourceType resourceType, String providerId,
			Map<QualifiedName, String> properties) throws CoreException;

	/**
	 * Returns the content provider ID for a path that belongs to a federated
	 * content provider, and <code>null</code> in all other cases.
	 * <p>
	 * This allows to dynamically link other content providers under the current
	 * content provider.
	 * <p>
	 * Since it is possible to create resource handle for arbitrary paths, the
	 * implementation should simply return <code>null</code> for any path that
	 * it can not validate.
	 * 
	 * @param path
	 *            the full path of the resource
	 * @return a provider ID to be used for the resource handle, or
	 *         <code>null</code>
	 */
	public String getFederatedProviderIDForPath(IPath path);

}
