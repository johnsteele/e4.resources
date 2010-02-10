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

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Walks down a hierarchy of {@link ISemanticFileStore}s
 * 
 */
public class SemanticTreeWalker {

	/**
	 * @param root
	 * @param visitor
	 * @param monitor
	 * @throws CoreException
	 */
	public static void accept(ISemanticFileStore root, ISemanticTreeVisitor visitor, IProgressMonitor monitor) throws CoreException {
		boolean recurse = visitor.visit(root, monitor);
		if (recurse) {
			for (IFileStore store : root.childStores(EFS.NONE, monitor)) {
				recurse = visitor.visit((ISemanticFileStore) store, monitor);
				if (recurse) {
					accept((ISemanticFileStore) store, visitor, monitor);
				}
			}
		}
	}

	/**
	 * @param root
	 * @param visitor
	 * @param monitor
	 * @throws CoreException
	 */
	public static void accept(ISemanticFileStore root, ISemanticTreeDeepFirstVisitor visitor, IProgressMonitor monitor)
			throws CoreException {
		boolean recurse = visitor.shouldVisitChildren(root, monitor);
		if (recurse) {
			for (IFileStore store : root.childStores(EFS.NONE, monitor)) {
				accept((ISemanticFileStore) store, visitor, monitor);

			}
		}
		visitor.visit(root, monitor);
	}
}
