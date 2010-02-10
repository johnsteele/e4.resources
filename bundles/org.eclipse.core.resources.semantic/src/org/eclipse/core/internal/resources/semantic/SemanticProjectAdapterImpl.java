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
package org.eclipse.core.internal.resources.semantic;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The {@link ISemanticProject} implementation.
 * 
 */
public class SemanticProjectAdapterImpl extends SemanticFolderAdapterImpl implements ISemanticProject {

	private final IProject myProject;

	SemanticProjectAdapterImpl(IProject project, ISemanticFileSystem fileSystem) {
		super(project, fileSystem);
		this.myProject = project;
	}

	public IProject getAdaptedProject() {
		return this.myProject;
	}

	public void remove(int options, IProgressMonitor monitor) throws CoreException {

		boolean internalMode = (options & ISemanticFileSystem.INTERNAL_DELETE_PROJECT) > 0;

		if (internalMode) {
			super.remove(options, monitor);
		} else {

			getAdaptedProject().delete(false, monitor);
			// no refresh necessary

		}
	}

}
