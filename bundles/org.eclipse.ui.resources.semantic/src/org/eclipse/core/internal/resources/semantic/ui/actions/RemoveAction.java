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

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;

/**
 * Remove from Workspace
 * 
 */
public class RemoveAction extends ActionBase {
	// TODO popup: are you sure (deletes all resources, even checked-out
	// and local only)?
	@Override
	public void selectionChanged(IAction action, ISelection selection) {

		super.selectionChanged(action, selection);
		// all objects must be semantic resources
		action.setEnabled(checkSelectionSemanticResource());
	}

	@SuppressWarnings( {"rawtypes"})
	public void run(IAction action) {
		boolean askForForceRemoval = false;

		for (Iterator it = getSelection().iterator(); it.hasNext();) {

			final ISemanticResource resource = (ISemanticResource) it.next();

			IStatus status = resource.validateRemove(ISemanticFileSystem.NONE, null);

			if (!status.isOK()) {
				askForForceRemoval = true;
			}
		}

		if (askForForceRemoval) {
			if (!MessageDialog.openConfirm(getShell(), Messages.RemoveAction_ConfirmResourceRemoval_XGRP,
					Messages.RemoveAction_DoYouWantToRemove_XMSG)) {
				return;
			}
		}

		IRunnableWithProgress outerRunnable = new IRunnableWithProgress() {

			public void run(IProgressMonitor outerMonitor) throws InvocationTargetException, InterruptedException {

				for (Iterator it = getSelection().iterator(); it.hasNext();) {

					final ISemanticResource resource = (ISemanticResource) it.next();

					outerMonitor.subTask(NLS.bind(Messages.RemoveAction_Removing_XMSG, resource.getAdaptedResource().getName()));

					if (outerMonitor.isCanceled()) {
						throw new InterruptedException();
					}

					IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {

						public void run(IProgressMonitor monitor) throws CoreException {

							resource.remove(RemoveAction.this.options, monitor);
						}
					};

					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					try {
						workspace.run(myRunnable, workspace.getRuleFactory().refreshRule(resource.getAdaptedResource()), 0, null);
					} catch (CoreException e) {
						throw new InvocationTargetException(e, NLS.bind(Messages.RemoveAction_CouldNotRemoveResource_XMSG, resource
								.getAdaptedResource().getFullPath().toString()));
					}

				}

			}

		};

		run(outerRunnable);

	}

}
