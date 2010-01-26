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

/**
 * A depth-first visitor for {@link ISemanticFileStore} hierarchies.
 * <p>
 * 
 */
public interface ISemanticTreeDeepFirstVisitor {

	/**
	 * Visits an {@link ISemanticFileStore}.
	 * 
	 * @param store
	 *            the store
	 * @param monitor
	 *            a progress monitor, may be <code>null</code>
	 * @throws CoreException
	 *             upon failure
	 */
	public void visit(ISemanticFileStore store, IProgressMonitor monitor) throws CoreException;

	/**
	 * Determines whether to continue down from a given
	 * {@link ISemanticFileStore}.
	 * 
	 * @param store
	 *            the store
	 * @param monitor
	 *            a progress monitor, may be <code>null</code>.
	 * @return whether children should be visited
	 * @throws CoreException
	 *             upon failure
	 */
	public boolean shouldVisitChildren(ISemanticFileStore store, IProgressMonitor monitor) throws CoreException;

}
