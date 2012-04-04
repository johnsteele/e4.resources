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
package org.eclipse.core.internal.resources.semantic.ui.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticProject;
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.team.core.RepositoryProvider;

/**
 * Remove project mapping
 * 
 */
public class UnmapAction extends ActionBase {

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

		super.selectionChanged(action, selection);
		// only on single selection of project
		if (getSelection().size() != 1) {
			action.setEnabled(false);
			return;
		}

		ISemanticResource resource = (ISemanticResource) getSelection().getFirstElement();

		if (resource != null && resource instanceof ISemanticProject) {
			IProject project = (IProject) resource.getAdaptedResource();
			try {
				if (resource.getPersistentProperty(DISABLE_ALL_SFS_ACTIONS) != null) {
					action.setEnabled(false);
				} else {
					action.setEnabled(RepositoryProvider.getProvider(project, ISemanticFileSystem.SFS_REPOSITORY_PROVIDER) != null);
				}
			} catch (CoreException e) {
				action.setEnabled(false);
			}
		} else {
			action.setEnabled(false);
		}
	}

	public void run(IAction action) {

		final IProject project = (IProject) ((ISemanticProject) getSelection().getFirstElement()).getAdaptedResource();

		try {
			IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {

				public void run(IProgressMonitor monitor) throws CoreException {

					RepositoryProvider.unmap(project);

					project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

				}
			};

			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.run(myRunnable, workspace.getRuleFactory().refreshRule(project), 0, null);
		} catch (CoreException e) {
			ErrorDialog.openError(getShell(), Messages.UnmapAction_Unmap_XGRP, null, e.getStatus());
		}

	}

}
