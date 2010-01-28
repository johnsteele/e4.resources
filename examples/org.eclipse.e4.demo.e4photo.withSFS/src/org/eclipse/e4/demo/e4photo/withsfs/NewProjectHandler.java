/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    Eduard Bartsch (SAP AG) - extension to support Semantic File System
 *******************************************************************************/
package org.eclipse.e4.demo.e4photo.withsfs;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.RepositoryProvider;

public class NewProjectHandler {

	private int counter;

	public void execute(IWorkspace workspace, IProgressMonitor monitor) {

		String projectName = "Album " + (++counter);
		final IProject project = workspace.getRoot().getProject(projectName);
		final IProjectDescription pd = workspace
				.newProjectDescription(projectName);

		try {
			pd.setLocationURI(new URI(ISemanticFileSystem.SCHEME, null, "/"
					+ projectName, null));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		try {
			workspace.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					if (!project.exists()) {
						project.create(pd, monitor);
					}
					if (!project.isOpen()) {
						project.open(monitor);
						RepositoryProvider.map(project,
								ISemanticFileSystem.SFS_REPOSITORY_PROVIDER);
						System.out.println("Created at: "
								+ project.getLocationURI());
					}
				}
			}, monitor);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

}
