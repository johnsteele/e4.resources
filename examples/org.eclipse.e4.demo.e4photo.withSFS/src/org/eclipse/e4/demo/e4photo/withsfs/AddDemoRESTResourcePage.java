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
package org.eclipse.e4.demo.e4photo.withsfs;

import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard page for adding a REST Demo resource
 * 
 */
public class AddDemoRESTResourcePage extends WizardPage {

	/**
	 * The page name
	 */
	public static final String NAME = AddDemoRESTResourcePage.class.getName();

	final ISemanticFolder myFolder;

	String resourceName;
	String resourceURI;
	Text folderPath;

	/**
	 * Constructor
	 * 
	 * @param folder
	 *            the parent
	 */
	public AddDemoRESTResourcePage(ISemanticFolder folder) {
		super(AddDemoRESTResourcePage.NAME);
		this.myFolder = folder;
		setTitle(Messages.AddDemoRESTResourcePage_AddRestResource_XGRP);
	}

	public void createControl(Composite parent) {

		Composite main = new Composite(parent, SWT.NONE);

		GridDataFactory.fillDefaults().grab(true, false).applyTo(main);

		main.setLayout(new GridLayout(3, false));

		Label folderPathLabel = new Label(main, SWT.NONE);
		folderPathLabel.setText(Messages.AddDemoRESTResourcePage_Folder_XFLD);

		this.folderPath = new Text(main, SWT.NONE);
		this.folderPath.setEditable(false);
		this.folderPath.setText(this.myFolder.getAdaptedContainer().getFullPath().toString());
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(this.folderPath);

		Label nameLabel = new Label(main, SWT.NONE);
		nameLabel.setText(Messages.AddDemoRESTResourcePage_ResName_XFLD);
		final Text name = new Text(main, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(name);

		Label uriLabel = new Label(main, SWT.NONE);
		uriLabel.setText(Messages.AddDemoRESTResourcePage_ResUrl_XFLD);
		final Text urlInput = new Text(main, SWT.BORDER);

		urlInput.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String urlstring = urlInput.getText();
				if (urlstring.equals("")) { //$NON-NLS-1$
					setErrorMessage(Messages.AddDemoRESTResourcePage_Provide_URL_XMSG);
					AddDemoRESTResourcePage.this.resourceURI = null;
					setPageComplete(false);
				} else {
					setErrorMessage(null);
					AddDemoRESTResourcePage.this.resourceURI = urlstring;
					setPageComplete((AddDemoRESTResourcePage.this.resourceURI != null)
							&& (AddDemoRESTResourcePage.this.resourceName != null));
				}

			}
		});

		GridDataFactory.fillDefaults().grab(true, false).applyTo(urlInput);

		name.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String rname = name.getText();

				if (rname == null || rname.length() == 0) {
					setErrorMessage(Messages.AddDemoRESTResourcePage_ProvideFileName_XMSG);
					AddDemoRESTResourcePage.this.resourceName = null;
					setPageComplete(false);
					return;
				}

				boolean childFound;
				try {
					childFound = AddDemoRESTResourcePage.this.myFolder.hasResource(name.getText());
				} catch (CoreException e1) {
					// $JL-EXC$ ignore
					childFound = false;
				}

				if (childFound) {
					setErrorMessage(Messages.AddDemoRESTResourcePage_FileExists_XMSG);
					AddDemoRESTResourcePage.this.resourceName = null;
					setPageComplete(false);
				} else {
					setErrorMessage(null);
					AddDemoRESTResourcePage.this.resourceName = rname;
					setPageComplete((AddDemoRESTResourcePage.this.resourceURI != null)
							&& (AddDemoRESTResourcePage.this.resourceName != null));
				}

			}
		});

		setControl(main);

		setPageComplete(false);
	}

	/**
	 * @return the resource name as entered by the user
	 */
	public String getResourceName() {
		return this.resourceName;
	}

	/**
	 * @return the resource URL as entered by the user
	 */
	public String getResourceURL() {
		return this.resourceURI.replace('\\', '/');
	}
}
