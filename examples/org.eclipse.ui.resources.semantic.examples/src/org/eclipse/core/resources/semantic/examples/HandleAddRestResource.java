/*******************************************************************************
 * Copyright (c) 2009 SAP AG. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Eduard Bartsch (SAP AG) - initial API and implementation
 * Mathias Kinzler (SAP AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources.semantic.examples;

import java.net.URI;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Handles the "Add Rest Resource" command
 * 
 */
public class HandleAddRestResource extends HandlerUtilities {

	private final static class MyWizard extends Wizard {

		final ISemanticFolder myFolder;

		/**
		 * 
		 * @param folder
		 *            the folder
		 */
		MyWizard(ISemanticFolder folder) {
			this.myFolder = folder;
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

					MyWizard.this.myFolder.addFile(resourceName, URI.create(resourceURL), ISemanticFileSystem.NONE, monitor);

					MyWizard.this.myFolder.getAdaptedResource().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			};

			try {
				workspace.run(myRunnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);
			} catch (CoreException e) {
				MessageDialog.openError(getShell(), Messages.NewDemoRESTResourceWizard_Error_XGRP, e.getMessage());
				SemanticResourcesPluginExamples.getDefault().getLog().log(e.getStatus());
				return false;
			}

			return true;
		}

	}

	/**
	 * @throws ExecutionException
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		ISemanticFolder sFolder = getSelectedObject(event, ISemanticFolder.class, true);
		if (sFolder == null) {
			showPopup(Messages.HandleAddFromRemote_Error_XGRP, Messages.HandleAddFromRemote_NotSemantic_XMSG);
			return null;
		}

		MyWizard wizard = new MyWizard(sFolder);

		new WizardDialog(Display.getCurrent().getActiveShell(), wizard).open();

		return null;
	}

}
