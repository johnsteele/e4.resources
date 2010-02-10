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
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;

/**
 * Revert changes
 * 
 */
public class RevertAction extends ActionBase {

	public void selectionChanged(IAction action, ISelection selection) {

		super.selectionChanged(action, selection);
		// we check for writable files here
		action.setEnabled(checkFilesWithReadOnlyFlagOnly(false) && checkSelectionNonLocalOnly());
	}

	public void run(IAction action) {

		IRunnableWithProgress outerRunnable = new IRunnableWithProgress() {

			@SuppressWarnings("unchecked")
			public void run(IProgressMonitor outerMonitor) throws InvocationTargetException, InterruptedException {

				for (Iterator it = getSelection().iterator(); it.hasNext();) {

					final ISemanticFile file = (ISemanticFile) it.next();

					outerMonitor.subTask(NLS.bind(Messages.RevertAction_Reverting_XMSG, file.getAdaptedResource().getName()));

					if (outerMonitor.isCanceled()) {
						throw new InterruptedException();
					}

					IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {

						public void run(IProgressMonitor monitor) throws CoreException {

							file.revertChanges(RevertAction.this.options, monitor);
						}
					};

					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					try {
						workspace.run(myRunnable, workspace.getRuleFactory().modifyRule(file.getAdaptedResource()), 0, null);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}

				}

			}

		};

		run(outerRunnable);

	}

}
