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
package org.eclipse.e4.demo.e4photo.withsfs;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticProject;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

public class RefreshAlbumHandler {

	public void execute(Shell shell, IEclipseContext context) {
		Object sel = context.get(IServiceConstants.SELECTION);
		IResource res = null;

		if (sel != null && sel instanceof IResource) {
			res = (IResource) sel;
		} else {
			// get the first project of nothing is selected
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

			if (projects.length > 0) {
				res = projects[0];
			}
		}
		if (res != null) {
			final IProject project = res.getProject();

			final ISemanticProject sproject = (ISemanticProject) project.getAdapter(ISemanticProject.class);

			if (sproject != null) {

				try {
					new ProgressMonitorDialog(shell).run(true, true, new IRunnableWithProgress() {

						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

								public void run(IProgressMonitor actMonitor) throws CoreException {
									sproject.synchronizeContentWithRemote(SyncDirection.INCOMING, ISemanticFileSystem.NONE, actMonitor);
								}
							};

							try {
								ResourcesPlugin.getWorkspace().run(runnable, project, IWorkspace.AVOID_UPDATE, monitor);
							} catch (CoreException e) {
								if (e.getCause() instanceof InterruptedException) {
									throw (InterruptedException) e.getCause();
								}
								throw new InvocationTargetException(e);
							}
						}
					});
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}
}
