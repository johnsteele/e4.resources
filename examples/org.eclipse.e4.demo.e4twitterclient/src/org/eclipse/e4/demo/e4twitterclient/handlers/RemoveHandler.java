/*******************************************************************************
 * Copyright (c) 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eduard Bartsch (SAP AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.e4twitterclient.handlers;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.demo.e4twitterclient.model.TwitterContentUtil;
import org.eclipse.e4.demo.e4twitterclient.parts.MyPart;
import org.eclipse.e4.demo.e4twitterclient.services.UIRefreshService;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.operation.IRunnableWithProgress;

@SuppressWarnings("restriction")
public class RemoveHandler {
	@CanExecute
	public boolean canExecute(EPartService partService) {
		final String selection = UIRefreshService.getSelectedUser(partService);

		if (selection != null) {
			return true;
		}
		return false;
	}

	@Execute
	public void execute(final IWorkspace workspace, final EPartService partService) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) {
				final String selection = UIRefreshService.getSelectedUser(partService);

				if (selection != null) {
					final IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {

						public void run(IProgressMonitor monitor1) throws CoreException {
							TwitterContentUtil.removeUserContent(workspace.getRoot().getProject(TwitterContentUtil.PROJECT_NAME),
									selection, null);
						}
					};

					try {
						workspace.run(myRunnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);
					} catch (CoreException e) {
						e.printStackTrace();
					}

					MPart part = partService.findPart(MyPart.TWITTERS_PART_ID);

					if (part != null) {
						MyPart myPart = (MyPart) part.getObject();

						try {
							myPart.reload();
						} catch (CoreException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};

		UIRefreshService.runWithProgress(null, true, false, runnable);
	}
}
