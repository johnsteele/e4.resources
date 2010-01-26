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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.core.history.IFileRevision;

/**
 * The Semantic File History provider.
 * <p>
 * This can be adapted from an {@link ISemanticContentProvider} to support
 * generic history support for Semantic File System resources.
 * 
 * @since 4.0
 * 
 */
public interface ISemanticFileHistoryProvider {

	/**
	 * Returns the file history for a given semantic file store.
	 * 
	 * @param store
	 *            the store
	 * @param options
	 *            see {@link IFileHistoryProvider} for more details
	 * @param monitor
	 *            may be null
	 * @return the history
	 * @throws CoreException
	 *             upon failure
	 */
	public IFileHistory getHistoryFor(ISemanticFileStore store, int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns the file revision of the workspace
	 * 
	 * @param store
	 *            the store
	 * @return the revision
	 * @throws CoreException
	 *             upon failure
	 */
	public IFileRevision getWorkspaceFileRevision(ISemanticFileStore store) throws CoreException;

	/**
	 * 
	 * Returns the Resource Variants for providing Team Synchronization support.
	 * <p>
	 * If team synchronization is not supported, this should return
	 * <code>null</code> .
	 * <p>
	 * Team synchronization offers generic diff/merge (also known as
	 * two-way/three-way merge) support. The returned array is expected to
	 * contain two entries, the first one being the "base" version, i.e. the
	 * "common ancestor", while the second entry would be the "active" or
	 * "remote" version. If the "base" version can not be determined, the first
	 * entry in the returned array should be <code>null</code>. In the latter
	 * case, only diff (two-way merge) will be available.
	 * 
	 * @param semanticFileStore
	 *            the store for which to obtain the variants
	 * @param monitor
	 *            may be null
	 * @return the {@link IFileRevision}s, or <code>null</code> if team
	 *         synchronization is not supported
	 * @throws CoreException
	 *             upon failure
	 */
	public IFileRevision[] getResourceVariants(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException;

}
