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
package org.eclipse.e4.demo.e4twitterclient.parts;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.demo.e4twitterclient.model.TwitterContentUtil;
import org.eclipse.e4.demo.e4twitterclient.model.TwitterContentUtil.TwitterUser;
import org.eclipse.e4.demo.e4twitterclient.model.UserType;
import org.eclipse.e4.demo.e4twitterclient.services.UIRefreshService;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.osgi.service.prefs.BackingStoreException;

@SuppressWarnings("restriction")
public class MyPart {

	public static final String TWITTERS_PART_ID = "org.eclipse.e4.demo.e4twitterclient.TwittersPart";

	final IWorkspace workspace;
	IProject project;
	Table table;

	String selectedUser;

	@Inject
	ESelectionService selectionService;

	@Inject
	EPartService partService;

	@Inject
	public MyPart(@Named(IServiceConstants.ACTIVE_SHELL) final Shell shell, Composite parent, final IWorkspace workspace,
			final EPartService partService) {

		this.workspace = workspace;
		this.project = initializeWorkspace();
		this.partService = partService;

		table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table.setLinesVisible(false);
		table.setHeaderVisible(false);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);

		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setText("");
		column.setWidth(200);

		column = new TableColumn(table, SWT.NONE);
		column.setText("");
		column.setWidth(0);

		table.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (e.item != null) {
					if (e.item instanceof TableItem) {
						String userID = ((TableItem) e.item).getText(1);
						selectedUser = userID;
						// selectionService.setSelection(userID);

						UIRefreshService.updateAllParts(MyPart.this.partService, userID);
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// empty

			}
		});

		table.addMouseListener(new MouseListener() {

			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			public void mouseDoubleClick(MouseEvent e) {
				UIRefreshService.refreshUserData(workspace, selectedUser);

				UIRefreshService.updateAllParts(partService, selectedUser);
			}
		});

		Job job = new Job("reload") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IRunnableWithProgress runnable = new IRunnableWithProgress() {

					public void run(IProgressMonitor monitor1) {
						try {
							reload();
						} catch (CoreException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};

				UIRefreshService.runWithProgress(shell, true, false, runnable);

				return Status.OK_STATUS;
			}
		};

		job.schedule(1000);

	}

	private void fillTable(final TwitterContentUtil.TwitterUser[] users) {
		Runnable runnable = new Runnable() {

			public void run() {
				table.removeAll();

				for (TwitterContentUtil.TwitterUser twitterUser : users) {
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText(0, twitterUser.getFullName());
					item.setText(1, twitterUser.getName());
				}
			}
		};

		if (Display.getCurrent() != null) {
			runnable.run();
		} else {
			Display.getDefault().asyncExec(runnable);
		}
	}

	private IProject initializeWorkspace() {
		IEclipsePreferences node = new InstanceScope().getNode(ResourcesPlugin.PI_RESOURCES);
		node.putBoolean(ResourcesPlugin.PREF_AUTO_REFRESH, true);
		try {
			node.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}

		WorkspaceJob job = new WorkspaceJob("init workspace") {

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				MyPart.this.project = TwitterContentUtil.getOrCreateTwitterDemoProject(workspace, monitor);
				return Status.OK_STATUS;
			}
		};

		job.setRule(workspace.getRoot());
		job.schedule();

		return TwitterContentUtil.getTwitterDemoProject(workspace);
	}

	UserType getOrCreateUser(final String userName) throws CoreException {
		UserType userType = TwitterContentUtil.getUserInfo(project, userName);
		if (userType == null) {
			final IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {

				public void run(IProgressMonitor monitor) throws CoreException {
					TwitterContentUtil.createUserContent(project, userName, monitor);
				}
			};

			try {
				workspace.run(myRunnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);

				userType = TwitterContentUtil.getUserInfo(project, userName);
			} catch (CoreException e) {
				throw e;
			} finally {
				fillTable(TwitterContentUtil.getUsers(project));
			}
		}
		return userType;
	}

	public void reload() throws CoreException {
		TwitterUser[] users = TwitterContentUtil.getUsers(project);
		this.fillTable(users);
		if (users.length > 0) {
			selectedUser = users[0].getName();
		} else {
			selectedUser = null;
		}

		UIRefreshService.updateAllParts(partService, selectedUser);
	}

	public String getSelectedUser() {
		return selectedUser;
	}

	public void selectUser(final String userID) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) {
				try {
					UserType userType = TwitterContentUtil.getUserInfo(project, userID);
					if (userType == null) {
						getOrCreateUser(userID);
						selectedUser = userID;
					} else {
						selectedUser = userID;
					}

					UIRefreshService.runInUI(new Runnable() {

						public void run() {
							for (int i = 0; i < table.getItemCount(); i++) {
								TableItem item = table.getItem(i);

								if (item.getText(1).equals(userID)) {
									table.select(i);
								} else {
									table.deselect(i);
								}
							}
						}
					});

					UIRefreshService.updateAllParts(partService, userID);
				} catch (CoreException e) {
					final String message;

					if (e.getStatus() instanceof MultiStatus) {
						message = ((MultiStatus) e.getStatus()).getChildren()[0].getMessage();
					} else {
						message = e.getMessage();
					}

					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							MessageDialog.openError(null, "Error Retrieving Twitter Content", message);
						}
					});

					e.printStackTrace();
				}
			}
		};

		UIRefreshService.runWithProgress(null, true, false, runnable);
	}
}
