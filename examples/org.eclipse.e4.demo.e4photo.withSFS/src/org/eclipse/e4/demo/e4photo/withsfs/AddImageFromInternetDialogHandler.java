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

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

public class AddImageFromInternetDialogHandler {

	private final static class AddImageFromInternetWizard extends Wizard {
		private IEclipseContext dlgContext;

		final ISemanticFolder myFolder;

		/**
		 * 
		 * @param folder
		 *            the folder
		 */
		AddImageFromInternetWizard(IEclipseContext context, ISemanticFolder folder) {
			this.myFolder = folder;
			this.dlgContext = context;

		}

		@Override
		public void addPages() {
			addPage(new AddDemoRESTResourcePage(this.myFolder));
		}

		@Override
		public boolean performFinish() {

			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			final String resourceName = ((AddDemoRESTResourcePage) getPage(AddDemoRESTResourcePage.NAME)).getResourceName();
			final String resourceURL = ((AddDemoRESTResourcePage) getPage(AddDemoRESTResourcePage.NAME)).getResourceURL();

			IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {

				public void run(IProgressMonitor monitor) throws CoreException {
					AddImageFromInternetWizard.this.myFolder.addFile(resourceName, URI.create(resourceURL), ISemanticFileSystem.NONE,
							monitor);
				}
			};

			try {
				workspace.run(myRunnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);
			} catch (CoreException e) {
				MessageDialog.openError(getShell(), Messages.NewDemoRESTResourceWizard_Error_XGRP, e.getMessage());
				Logger logger = (Logger) this.dlgContext.get(Logger.class.getName());
				logger.error(e);
				return false;
			}

			return true;
		}

	}

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
			// if a file is selected, go to it's parent
			if (res.getType() == IResource.FILE) {
				res = res.getParent();
			}

			ISemanticFolder sfolder = (ISemanticFolder) res.getAdapter(ISemanticFolder.class);

			if (sfolder != null) {
				AddImageFromInternetWizard wizard = new AddImageFromInternetWizard(context, sfolder);

				new WizardDialog(shell, wizard).open();
			}
		}
	}

}
