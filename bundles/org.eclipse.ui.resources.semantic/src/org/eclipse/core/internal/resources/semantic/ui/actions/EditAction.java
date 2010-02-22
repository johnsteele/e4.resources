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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;

/**
 * Open for Edit
 * 
 */
public class EditAction extends ActionBase {

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

		super.selectionChanged(action, selection);
		// we check for read-only files here
		action.setEnabled(checkFilesWithReadOnlyFlagOnly(true) && checkSelectionNonLocalOnly());
	}

	public void run(IAction action) {

		IRunnableWithProgress outerRunnable = new IRunnableWithProgress() {

			@SuppressWarnings({"rawtypes"})
			public void run(IProgressMonitor outerMonitor) throws InvocationTargetException, InterruptedException {

				for (Iterator it = getSelection().iterator(); it.hasNext();) {

					final ISemanticFile file = (ISemanticFile) it.next();

					outerMonitor.subTask(NLS.bind(Messages.EditAction_Editing_XMSG, file.getAdaptedResource().getName()));

					if (outerMonitor.isCanceled()) {
						throw new InterruptedException();
					}

					IWorkspaceRunnable wsRunnable = new IWorkspaceRunnable() {

						public void run(IProgressMonitor monitor) throws CoreException {

							IStatus validationResult = file.validateEdit(getShell());
							if (!validationResult.isOK()) {
								throw new CoreException(validationResult);
							}
						}
					};

					try {
						IWorkspace ws = ResourcesPlugin.getWorkspace();
						// we use the validate edit rule
						ISchedulingRule rule = ws.getRuleFactory().validateEditRule(new IResource[] {file.getAdaptedResource()});
						ResourcesPlugin.getWorkspace().run(wsRunnable, rule, IWorkspace.AVOID_UPDATE, outerMonitor);
					} catch (CoreException ce) {
						throw new InvocationTargetException(ce);
					}

				}

			}

		};

		run(outerRunnable);

	}

}
