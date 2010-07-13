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

import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.demo.e4twitterclient.model.TwitterContentUtil;
import org.eclipse.e4.demo.e4twitterclient.model.UserType;
import org.eclipse.e4.demo.e4twitterclient.services.IUserRefresh;
import org.eclipse.e4.demo.e4twitterclient.services.UIRefreshService;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

@SuppressWarnings("restriction")
public class FollowersPart implements IUserRefresh {

	final IWorkspace workspace;
	final IProject project;

	@Inject
	ESelectionService selectionService;

	@Inject
	EPartService partService;

	// private Composite main;
	Table tableFollowers;

	String[] titles = {"ID", "Name"}; //$NON-NLS-1$ //$NON-NLS-2$

	@Inject
	public FollowersPart(Composite parent, final IWorkspace workspace) {
		this.workspace = workspace;
		this.project = workspace.getRoot().getProject(TwitterContentUtil.PROJECT_NAME); //$NON-NLS-1$

		// main = new Composite(parent, SWT.NONE);
		// main.setLayout(new GridLayout(2, false));

		this.tableFollowers = createTable(parent);
	}

	private Table createTable(Composite parent) {
		Table table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);

		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titles[i]);
		}

		for (int i = 0; i < titles.length; i++) {
			table.getColumn(i).pack();
		}

		table.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (e.item != null) {
					if (e.item instanceof TableItem) {
						String userID = ((TableItem) e.item).getText(0);

						MPart part = partService.findPart(MyPart.TWITTERS_PART_ID);

						if (part != null) {
							MyPart myPart = (MyPart) part.getObject();

							myPart.selectUser(userID);
						}
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// empty
			}
		});

		return table;
	}

	public void refreshUser(String userName) {
		final List<UserType> users;
		try {
			if (userName != null) {
				users = TwitterContentUtil.getFollowers(this.project, userName);
			} else {
				users = null;
			}

			UIRefreshService.runInUI(new Runnable() {

				public void run() {
					tableFollowers.removeAll();
					if (users != null) {
						for (UserType user : users) {
							TableItem item = new TableItem(tableFollowers, SWT.NONE);
							item.setText(0, user.getScreenName());
							item.setText(1, user.getName());
						}
					}
					for (int i = 0; i < titles.length; i++) {
						tableFollowers.getColumn(i).pack();
					}
				}
			});
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
