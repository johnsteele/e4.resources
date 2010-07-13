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
package org.eclipse.e4.demo.e4twitterclient.services;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.demo.e4twitterclient.model.TwitterContentUtil;
import org.eclipse.e4.demo.e4twitterclient.parts.MyPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

@SuppressWarnings("restriction")
public class UIRefreshService {

	static String[] partIDs = {"org.eclipse.e4.demo.e4twitterclient.InfoPart", "org.eclipse.e4.demo.e4twitterclient.FollowersPart",
			"org.eclipse.e4.demo.e4twitterclient.FollowingPart", "org.eclipse.e4.demo.e4twitterclient.TweetsPart"};

	public static void refreshUserData(final IWorkspace workspace, final String selection) {
		UIRefreshService.runWithProgress(null, true, false, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				final IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {

					public void run(IProgressMonitor monitor1) throws CoreException {
						TwitterContentUtil.refreshUserContent(workspace.getRoot().getProject(TwitterContentUtil.PROJECT_NAME), selection,
								null);
					}
				};

				try {
					workspace.run(myRunnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	public static void updateAllParts(EPartService partService, String userName) {
		for (String id : partIDs) {
			MPart part = partService.findPart(id);

			if (part != null) {
				Object impl = part.getObject();

				if (impl instanceof IUserRefresh) {
					((IUserRefresh) impl).refreshUser(userName);
				}
			}
		}
	}

	public static String getSelectedUser(EPartService partService) {
		MPart part = partService.findPart(MyPart.TWITTERS_PART_ID);

		if (part != null) {
			MyPart myPart = (MyPart) part.getObject();

			if (myPart != null) {
				return myPart.getSelectedUser();
			}
		}
		return null;
	}

	public static void runInUI(Runnable runnable) {
		if (Display.getCurrent() != null) {
			runnable.run();
		} else {
			Display.getDefault().syncExec(runnable);
		}
	}

	public static void runWithProgress(final Shell shell, final boolean fork, final boolean cancelable, final IRunnableWithProgress runnable) {
		Runnable runnable1 = new Runnable() {
			public void run() {
				ProgressMonitorDialog dlg = new ProgressMonitorDialog(shell);

				try {
					dlg.run(fork, cancelable, runnable);
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		};

		if (Display.getCurrent() != null) {
			runnable1.run();
		} else {
			Display.getDefault().asyncExec(runnable1);
		}
	}
}
