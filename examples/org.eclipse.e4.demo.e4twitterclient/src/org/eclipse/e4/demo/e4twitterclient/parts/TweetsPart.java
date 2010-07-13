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
import org.eclipse.e4.demo.e4twitterclient.model.StatusType;
import org.eclipse.e4.demo.e4twitterclient.model.TwitterContentUtil;
import org.eclipse.e4.demo.e4twitterclient.services.IUserRefresh;
import org.eclipse.e4.demo.e4twitterclient.services.UIRefreshService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TweetsPart implements IUserRefresh {
	final IWorkspace workspace;
	final IProject project;

	private Composite main;
	Table table;

	String[] titles = {"Text", "Time", "Source"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	@Inject
	public TweetsPart(Composite parent, final IWorkspace workspace) {
		this.workspace = workspace;
		this.project = workspace.getRoot().getProject(TwitterContentUtil.PROJECT_NAME); //$NON-NLS-1$

		main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(2, false));

		table = new Table(main, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);

		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titles[i]);
		}

		for (int i = 0; i < titles.length; i++) {
			table.getColumn(i).pack();
		}
	}

	public void refreshUser(String userName) {
		final List<StatusType> statuses;
		try {
			if (userName != null) {
				statuses = TwitterContentUtil.getUserTimeline(this.project, userName);
			} else {
				statuses = null;
			}

			UIRefreshService.runInUI(new Runnable() {

				public void run() {
					table.removeAll();

					if (statuses != null) {
						for (StatusType user : statuses) {
							TableItem item = new TableItem(table, SWT.NONE);
							item.setText(0, user.getText());
							item.setText(1, user.getCreatedAt());
							item.setText(2, user.getSource());
						}
					}

					for (int i = 0; i < titles.length; i++) {
						table.getColumn(i).pack();
					}
				}
			});
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
