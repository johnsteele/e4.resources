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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;

/**
 * This corresponds to the {@link IResourceRuleFactory}
 * <p>
 * This works on instances of {@link ISemanticFileStore}, whereas the workspace
 * locks are based on instances of {@link IResource} as scheduling rule. The
 * returned {@link ISemanticFileStore} will be automatically converted to a
 * resource; if the returned store points to a non-existing resource, the parent
 * hierarchy of the resource will be followed upward up to the first existing
 * resource that will returned as scheduling rule.
 * 
 * @since 4.0
 * 
 * @noextend This interface is not intended to be extended by clients.
 * 
 */
public interface ISemanticResourceRuleFactory {

	/**
	 * Returns the scheduling rule that is required for creating a project,
	 * folder, or file.
	 * 
	 * @param store
	 *            a store that corresponds to the resource being created
	 * @return a store that corresponds to a resource that should be used as
	 *         scheduling rule; if <code>null</code> is returned, the workspace
	 *         root will be locked
	 */
	public ISemanticFileStore createRule(ISemanticFileStore store);

	/**
	 * Returns the scheduling rule that is required for changing the charset
	 * setting for a file or the default charset setting for a container.
	 * 
	 * @param store
	 *            the store for which the charset will be changed
	 * @return a store that corresponds to a resource that should be used as
	 *         scheduling rule; if <code>null</code> is returned, the workspace
	 *         root will be locked
	 */
	public ISemanticFileStore charsetRule(ISemanticFileStore store);

	/**
	 * Returns the scheduling rule that is required for copying a resource.
	 * 
	 * @param source
	 *            the source of the copy
	 * @param destination
	 *            the destination of the copy
	 * @return a store that corresponds to a resource that should be used as
	 *         scheduling rule; if <code>null</code> is returned, the workspace
	 *         root will be locked
	 */
	public ISemanticFileStore copyRule(ISemanticFileStore source, ISemanticFileStore destination);

	/**
	 * Returns the scheduling rule that is required for deleting a resource.
	 * 
	 * @param store
	 *            the store to be deleted
	 * @return a store that corresponds to a resource that should be used as
	 *         scheduling rule; if <code>null</code> is returned, the workspace
	 *         root will be locked
	 */
	public ISemanticFileStore deleteRule(ISemanticFileStore store);

	/**
	 * Returns the scheduling rule that is required for creating, modifying, or
	 * deleting markers on a resource.
	 * 
	 * @param store
	 *            the store owning the marker to be modified
	 * @return a store that corresponds to a resource that should be used as
	 *         scheduling rule; if <code>null</code> is returned, the workspace
	 *         root will be locked
	 */
	public ISemanticFileStore markerRule(ISemanticFileStore store);

	/**
	 * Returns the scheduling rule that is required for modifying a resource.
	 * For files, modification includes setting and appending contents. For
	 * projects, modification includes opening or closing the project, or
	 * setting the project description using the
	 * {@link IResource#AVOID_NATURE_CONFIG} flag. For all resources
	 * <code>touch</code> is considered to be a modification.
	 * 
	 * @param store
	 *            the store being modified
	 * @return a store that corresponds to a resource that should be used as
	 *         scheduling rule; if <code>null</code> is returned, the workspace
	 *         root will be locked
	 */
	public ISemanticFileStore modifyRule(ISemanticFileStore store);

	/**
	 * Returns the scheduling rule that is required for moving a resource.
	 * 
	 * @param source
	 *            the source of the move
	 * @param destination
	 *            the destination of the move
	 * @return a store that corresponds to a resource that should be used as
	 *         scheduling rule; if <code>null</code> is returned, the workspace
	 *         root will be locked
	 */
	public ISemanticFileStore moveRule(ISemanticFileStore source, ISemanticFileStore destination);

	/**
	 * Returns the scheduling rule that is required for performing
	 * <code>refreshLocal</code> on a resource.
	 * 
	 * @param store
	 *            the store to refresh
	 * @return a store that corresponds to a resource that should be used as
	 *         scheduling rule; if <code>null</code> is returned, the workspace
	 *         root will be locked
	 */
	public ISemanticFileStore refreshRule(ISemanticFileStore store);

	/**
	 * Returns the scheduling rule that is required for a
	 * <code>validateEdit</code>
	 * 
	 * @param stores
	 *            the stores to be validated
	 * @return a store that corresponds to a resource that should be used as
	 *         scheduling rule; if <code>null</code> is returned, the workspace
	 *         root will be locked
	 */
	public ISemanticFileStore validateEditRule(ISemanticFileStore[] stores);

}
