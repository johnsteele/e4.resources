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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.demo.e4twitterclient.model.TwitterContentUtil;
import org.eclipse.e4.demo.e4twitterclient.model.UserType;
import org.eclipse.e4.demo.e4twitterclient.services.IUserRefresh;
import org.eclipse.e4.demo.e4twitterclient.services.UIRefreshService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class UserInfoPart implements IUserRefresh {

	final IWorkspace workspace;
	final IProject project;
	String userName;

	Canvas userPhoto;
	Text nameText;
	Text descriptionText;
	Text locationText;
	Text statusText;
	Composite main;

	@Inject
	public UserInfoPart(Composite parent, final IWorkspace workspace) {
		this.workspace = workspace;
		this.project = workspace.getRoot().getProject(TwitterContentUtil.PROJECT_NAME); //$NON-NLS-1$

		main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(2, false));

		userPhoto = new Canvas(main, SWT.BORDER);
		GridDataFactory.fillDefaults().hint(30, 30).align(SWT.FILL, SWT.BEGINNING).applyTo(userPhoto);

		userPhoto.addPaintListener(new PaintListener() {
			public void paintControl(final PaintEvent event) {
				if (project != null && getUserName() != null) {
					Image userImage1 = TwitterContentUtil.getUserImage(project, getUserName());
					if (userImage1 != null) {
						event.gc.drawImage(userImage1, 0, 0);
						// , userImage.getBounds().width,
						// userImage.getBounds().height, 0, 0, event.width,
						// event.height);
						userImage1.dispose();
					}
				}
			}
		});

		Group group = new Group(main, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(group);
		group.setText("User Data"); //$NON-NLS-1$
		group.setLayout(new GridLayout(2, false));

		this.nameText = createAttributeControls(group, "Name", "", SWT.READ_ONLY | SWT.SINGLE | SWT.BORDER); //$NON-NLS-1$ //$NON-NLS-2$
		this.descriptionText = createAttributeControls(group, "Description", "", SWT.READ_ONLY | SWT.MULTI | SWT.BORDER); //$NON-NLS-1$ //$NON-NLS-2$
		this.locationText = createAttributeControls(group, "Location", "", SWT.READ_ONLY | SWT.SINGLE | SWT.BORDER); //$NON-NLS-1$ //$NON-NLS-2$
		this.statusText = createAttributeControls(group, "Status", "", SWT.READ_ONLY | SWT.MULTI | SWT.BORDER); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected String getUserName() {
		return this.userName;
	}

	private Text createAttributeControls(Group group, String labelString, String textValue, int attributes) {
		Label label = new Label(group, SWT.NONE);
		label.setText(labelString);
		Text text = new Text(group, attributes);
		text.setText(textValue);
		GridDataFactory.fillDefaults().grab(true, (attributes & SWT.MULTI) > 0).applyTo(text);
		return text;
	}

	public void refreshUser(String name) {
		UserType userType;
		try {
			if (name != null) {
				userType = TwitterContentUtil.getUserInfo(project, name);
			} else {
				userType = null;
			}

			displayUserInfo(userType);
			if (userType != null) {
				this.userName = name;

				Image userImage = TwitterContentUtil.getUserImage(project, name);
				final int width;
				final int heigth;

				if (userImage != null) {
					width = userImage.getBounds().width;
					heigth = userImage.getBounds().height;
					userImage.dispose();
				} else {
					width = 20;
					heigth = 20;
				}

				UIRefreshService.runInUI(new Runnable() {
					public void run() {
						GridDataFactory.fillDefaults().hint(width + 1, heigth + 1).align(SWT.FILL, SWT.BEGINNING).applyTo(userPhoto);
						main.layout();
						GridDataFactory.fillDefaults().hint(width, heigth).align(SWT.FILL, SWT.BEGINNING).applyTo(userPhoto);
						main.layout();
					}
				});
				return;
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.userName = null;
	}

	public void displayUserInfo(final UserType userType) {
		UIRefreshService.runInUI(new Runnable() {
			public void run() {
				if (userType != null) {
					nameText.setText(userType.getScreenName());
					descriptionText.setText(userType.getDescription());
					locationText.setText(userType.getLocation());
					if (userType.getStatusTypes().size() > 0) {
						statusText.setText(userType.getStatusTypes().get(0).getText());
					}

					return;
				}

				GridDataFactory.fillDefaults().hint(40, 40).align(SWT.FILL, SWT.BEGINNING).applyTo(userPhoto);
				nameText.setText(""); //$NON-NLS-1$
				descriptionText.setText(""); //$NON-NLS-1$
				locationText.setText(""); //$NON-NLS-1$
				statusText.setText(""); //$NON-NLS-1$
				main.layout();
			}
		});
	}

}
