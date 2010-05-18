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
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;

/**
 * Synchronize
 * 
 */
public class SynchronizeAction extends ActionBase {

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

		super.selectionChanged(action, selection);
		// all objects must be non-local
		action.setEnabled(checkSelectionNonLocalOnly());
	}

	public void run(IAction action) {

		IRunnableWithProgress outerRunnable = new IRunnableWithProgress() {

			@SuppressWarnings( {"rawtypes"})
			public void run(IProgressMonitor outerMonitor) throws InvocationTargetException, InterruptedException {

				for (Iterator it = getSelection().iterator(); it.hasNext();) {

					final ISemanticResource resource = (ISemanticResource) it.next();

					outerMonitor.subTask(NLS.bind(Messages.SynchronizeAction_Synchronizing_XMSG, resource.getAdaptedResource().getName()));

					if (outerMonitor.isCanceled()) {
						throw new InterruptedException();
					}

					IWorkspaceRunnable wsRunnable = new IWorkspaceRunnable() {

						public void run(IProgressMonitor monitor) throws CoreException {
							resource.synchronizeContentWithRemote(SyncDirection.BOTH, SynchronizeAction.this.options, monitor);
						}
					};

					try {
						IWorkspace ws = ResourcesPlugin.getWorkspace();
						ISchedulingRule rule = ws.getRuleFactory().refreshRule(resource.getAdaptedResource());
						ResourcesPlugin.getWorkspace().run(wsRunnable, rule, IWorkspace.AVOID_UPDATE, outerMonitor);
					} catch (CoreException ce) {
						// pack the error text into the
						// InvocationTargetException
						throw new InvocationTargetException(ce, NLS.bind(Messages.SynchronizeAction_SyncFailedForResource_XMSG,
								resource.getAdaptedResource().getFullPath().toString()));
					}

				}

			}

		};

		run(outerRunnable);

	}

}
