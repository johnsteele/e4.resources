/*******************************************************************************
 * Copyright (c) 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eduard Bartsch (SAP AG) - initial API and implementation
 *    Mathias Kinzler (SAP AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources.semantic.examples.wizard;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.resources.semantic.examples.Messages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionValidator;

/**
 * Allows the user to enter a parent folder, a name, and a (WebDAV-) URL
 */
public class AddWebDAVResourceNameAndURLPage extends WizardPage {

	private IContainer myContainer;

	private Text parentText;
	private Text childText;
	private Text urlText;
	private Button forceOverwrite;

	public AddWebDAVResourceNameAndURLPage() {
		super(AddWebDAVResourceNameAndURLPage.class.getName());
		setTitle(Messages.AddWebDAVResourceNameAndURLPage_PageTitle_XGRP);
	}

	public void setResourceContainer(IContainer container) {
		myContainer = container;
		if (myContainer != null) {
			if (myContainer.getAdapter(ISemanticResource.class) == null) {
				setMessage(Messages.AddWebDAVResourceNameAndURLPage_ParentNotSemanticMessage_XMSG);
			} else {
				setMessage(Messages.AddWebDAVResourceNameAndURLPage_ParentIsSemanticMessage_XMSG);
			}
			if (parentText != null)
				parentText.setText(myContainer.getFullPath().toString());
		}
	}

	public IContainer getResourceContainer() {
		return myContainer;
	}

	public boolean getForceOverwrite() {
		return forceOverwrite.getSelection();
	}

	public void createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);

		main.setLayout(new GridLayout(3, false));

		new Label(main, SWT.NONE).setText(Messages.AddWebDAVResourceNameAndURLPage_ParentLabel_XFLD);
		parentText = new Text(main, SWT.BORDER);
		if (myContainer != null)
			parentText.setText(myContainer.getFullPath().toString());
		parentText.setEnabled(false);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(parentText);
		Button browseButton = new Button(main, SWT.PUSH);
		browseButton.setText(Messages.AddWebDAVResourceNameAndURLPage_BrowseButton_XBUT);
		browseButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				ContainerSelectionDialog csd = new ContainerSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
						Messages.AddWebDAVResourceNameAndURLPage_SelectFolderMessage_XMSG);
				csd.setValidator(new ISelectionValidator() {

					public String isValid(Object selection) {
						IPath path = (IPath) selection;
						if (path.segmentCount() > 1) {
							IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
							if (folder.isLinked())
								return Messages.AddWebDAVResourceNameAndURLPage_LinksForbiddenMessage_XMSG;
							return null;
						}
						IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0));
						if (project.isLinked())
							return Messages.AddWebDAVResourceNameAndURLPage_LinksForbiddenMessage_XMSG;
						return null;
					}
				});

				if (csd.open() == Window.OK) {
					Object[] result = csd.getResult();
					IPath path = (IPath) result[0];
					if (path.segmentCount() > 1) {
						IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
						setResourceContainer(folder);
					} else {
						IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0));
						setResourceContainer(project);
					}
					checkPage();
				}
			}

		});

		new Label(main, SWT.NONE).setText(Messages.AddWebDAVResourceNameAndURLPage_NameLabel_XFLD);
		childText = new Text(main, SWT.BORDER);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(childText);
		childText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				checkPage();
			}
		});

		new Label(main, SWT.NONE).setText(Messages.AddWebDAVResourceNameAndURLPage_WebDAVUrlLabel_XFLD);
		urlText = new Text(main, SWT.BORDER);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(urlText);
		urlText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				checkPage();
			}
		});

		forceOverwrite = new Button(main, SWT.CHECK);
		forceOverwrite.setText(Messages.AddWebDAVResourceNameAndURLPage_OverwriteLabel_XFLD);
		GridDataFactory.fillDefaults().span(3, 1).applyTo(forceOverwrite);
		forceOverwrite.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				checkPage();
			}
		});

		// we'll have to enter something
		setPageComplete(false);
		setControl(main);
	}

	protected void checkPage() {
		setErrorMessage(null);
		setMessage(null, IMessageProvider.WARNING);

		try {
			if (myContainer == null) {
				setErrorMessage(Messages.AddWebDAVResourceNameAndURLPage_ChooseParentFolderMessage_XMSG);
				return;
			}
			if (childText.getText().length() == 0) {
				setErrorMessage(Messages.AddWebDAVResourceNameAndURLPage_MustProviderChildMessage_XMSG);
				return;
			}

			if (myContainer.exists(new Path(childText.getText()))) {
				if (forceOverwrite.getSelection())
					setMessage(NLS.bind(Messages.AddWebDAVResourceNameAndURLPage_OverwriteChildMessage_XMSG, childText.getText()),
							IMessageProvider.WARNING);
				else
					setErrorMessage(NLS.bind(Messages.AddWebDAVResourceNameAndURLPage_ChildExistsMessage_XMSG, childText.getText()));
				forceOverwrite.setEnabled(true);
				return;
			}

			// TODO adjust this once we can create files, too
			IStatus valid = ResourcesPlugin.getWorkspace().validateName(childText.getText(), IResource.FOLDER);
			if (!valid.isOK()) {
				setErrorMessage(valid.getMessage());
				return;
			}

			try {
				String childPath = myContainer.getFullPath().append(childText.getText()).toString();
				URI sfsUri = new URI(ISemanticFileSystem.SCHEME, null, childPath, null);
				if (EFS.getStore(sfsUri).fetchInfo().exists()) {

					if (forceOverwrite.getSelection())
						setMessage(NLS.bind(Messages.AddWebDAVResourceNameAndURLPage_StoreOverwriteMessage_XMSG, sfsUri.toString()),
								IMessageProvider.WARNING);
					else
						setErrorMessage(NLS.bind(Messages.AddWebDAVResourceNameAndURLPage_StoreExistsMessage_XMSG, sfsUri.toString()));
					forceOverwrite.setEnabled(true);

					return;
				}
			} catch (URISyntaxException e) {
				// ignore here
			} catch (CoreException e) {
				// ignore here
			}

			if (urlText.getText().length() == 0) {
				setErrorMessage(Messages.AddWebDAVResourceNameAndURLPage_ChooseURLMessage_XMSG);
				return;
			}

		} finally {
			setPageComplete(getErrorMessage() == null);
		}
	}

	public String getChildName() {
		return childText.getText();
	}

	public String getUrl() {
		return urlText.getText();
	}
}