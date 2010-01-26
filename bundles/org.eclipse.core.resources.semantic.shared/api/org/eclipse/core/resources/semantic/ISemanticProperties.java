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
package org.eclipse.core.resources.semantic;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

/**
 * The Semantic Properties.
 * <p>
 * Property handling is similar to the corresponding methods in
 * <code>IResource</code>.
 * <p>
 * Note that <code>null</code> values can not be stored here explicitly;
 * instead, providing <code>null</code> as value in a setter method will remove
 * the property with the given key.
 * 
 * @since 4.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISemanticProperties {

	/**
	 * Returns a (shallow) copy of the Map with the persistent Properties.
	 * <p>
	 * These Properties are stored persistently with the node.
	 * <p>
	 * Modifications of the returned {@link Map} will have no effect on the
	 * state of the node and vice versa.
	 * 
	 * @return the potentially empty map, never <code>null</code>
	 * @throws CoreException
	 *             in case of failure
	 */
	public Map<QualifiedName, String> getPersistentProperties() throws CoreException;

	/**
	 * Returns the value of the persistent property of this resource.
	 * 
	 * @param key
	 *            the key
	 * @return the value, or <code>null</code> if no such property exists
	 * @throws CoreException
	 *             in case of failure
	 */

	public String getPersistentProperty(QualifiedName key) throws CoreException;

	/**
	 * Sets the value of the persistent property. If the supplied value is
	 * <code>null</code>, the persistent property is removed. The change is made
	 * immediately on disk.
	 * 
	 * @param key
	 *            the key; <code>null</code> as qualifier of this
	 *            {@link QualifiedName} is discouraged; it is good practice to
	 *            use a plug-in ID or a URI; for technical reasons, neither
	 *            qualifier nor local name must contain a "^" character.
	 * @param value
	 *            the value to set; <code>null</code> if the property is to be
	 *            deleted
	 * 
	 * @throws CoreException
	 *             for example if the key has a <code>null</code> qualifier
	 */
	public void setPersistentProperty(QualifiedName key, String value) throws CoreException;

	/**
	 * Returns a (shallow) copy of the Map with the session Properties.
	 * <p>
	 * These Properties are volatile and don't survive an Eclipse restart.
	 * <p>
	 * Modifications of the returned {@link Map} will have no effect on the
	 * state of the node and vice versa.
	 * 
	 * @return the potentially empty map, but never <code>null</code>
	 * @throws CoreException
	 *             in case of failure
	 */
	public Map<QualifiedName, Object> getSessionProperties() throws CoreException;

	/**
	 * Returns the value of the session property of this resource.
	 * 
	 * @param key
	 *            the key
	 * @return the value, or <code>null</code> if no such property exists
	 * @throws CoreException
	 *             in case of failure
	 */
	public Object getSessionProperty(QualifiedName key) throws CoreException;

	/**
	 * Sets the value of the session property. If the supplied value is
	 * <code>null</code>, the session property is removed.
	 * <p>
	 * Sessions properties are maintained in memory (at all times), and the
	 * information is lost when the corresponding node is deleted, when the
	 * parent project is closed, or when the workspace is closed.
	 * 
	 * @param key
	 *            the key; <code>null</code> as qualifier of this
	 *            {@link QualifiedName} is discouraged; it is good practice to
	 *            use a plug-in ID or a URI
	 * @param value
	 *            the value to set; <code>null</code> if the property is to be
	 *            deleted
	 * @throws CoreException
	 *             for example if the key has a <code>null</code> qualifier
	 */
	public void setSessionProperty(QualifiedName key, Object value) throws CoreException;

}
