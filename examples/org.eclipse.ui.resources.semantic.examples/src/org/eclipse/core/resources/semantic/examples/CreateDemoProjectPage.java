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
package org.eclipse.core.resources.semantic.examples;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.examples.SelectScenarioPage.Scenario;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard page for creating a demo project
 * 
 */
public class CreateDemoProjectPage extends WizardPage {

	String projectName;
	String directoryName = ""; //$NON-NLS-1$
	boolean useOtherProject = false;

	@SuppressWarnings("unused")
	// currently not evaluated, could be used to fine-tune the UI
	private Set<Scenario> scenarios = new HashSet<Scenario>();

	/**
	 * Constructor
	 * 
	 */
	public CreateDemoProjectPage() {
		super(CreateDemoProjectPage.class.getName());
		setTitle(Messages.CreateDemoProjectPage_CreateProject_XGRP);
	}

	public void createControl(Composite parent) {

		Composite main = new Composite(parent, SWT.NONE);

		main.setLayout(new GridLayout(3, false));

		Label nameLabel = new Label(main, SWT.NONE);
		nameLabel.setText(Messages.CreateDemoProjectPage_ProjectName_XFLD);
		final Text name = new Text(main, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(name);

		Label dirLabel = new Label(main, SWT.NONE);
		dirLabel.setText(Messages.CreateDemoProjectPage_TempDir_XFLD);
		final Text dir = new Text(main, SWT.BORDER);

		dir.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String dirname = dir.getText();
				if (dirname.equals("")) { //$NON-NLS-1$
					setErrorMessage(Messages.CreateDemoProjectPage_DirRequired_XMSG);
					CreateDemoProjectPage.this.directoryName = null;
					setPageComplete(false);
				} else {
					setErrorMessage(null);
					CreateDemoProjectPage.this.directoryName = dirname;
					setPageComplete(true);
				}

			}
		});

		dir.setText(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().grab(true, false).applyTo(dir);

		Button browse = new Button(main, SWT.PUSH);
		browse.setText(Messages.CreateDemoProjectPage_Browse_XBUT);
		browse.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog fd = new DirectoryDialog(getShell());
				fd.setFilterPath(dir.getText());
				String result = fd.open();
				if (result != null) {
					dir.setText(result);
				}

			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing

			}
		});

		final Button separateProject = new Button(main, SWT.CHECK);
		separateProject.setText(Messages.CreateDemoProjectPage_UseSeparateProject_XRBL);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(separateProject);

		separateProject.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				CreateDemoProjectPage.this.useOtherProject = separateProject.getSelection();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing
			}
		});

		name.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String pname = name.getText();
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(pname);
				if (project.exists()) {
					setErrorMessage(Messages.CreateDemoProjectPage_ProjectExists_XMSG);
					CreateDemoProjectPage.this.projectName = null;
					setPageComplete(false);
				} else {
					setErrorMessage(null);
					CreateDemoProjectPage.this.projectName = pname;
					setPageComplete(true);
				}

			}
		});

		name.setText(NewDemoSemanticProjectWizard.DEFAULT_PROJECT_NAME);

		setControl(main);

	}

	/**
	 * 
	 * @return set of scenarios
	 */
	public Set<Scenario> getScenarios() {
		return scenarios;
	}

	/**
	 * 
	 * @param scenarios
	 */
	public void setScenarios(Set<Scenario> scenarios) {
		this.scenarios = scenarios;
	}

	/**
	 * @return the project name
	 */
	public String getProjectName() {
		return this.projectName;
	}

	/**
	 * @return the temporary directory name
	 */
	public String getDirectoryName() {
		return this.directoryName.replace('\\', '/');
	}

	/**
	 * @return the flag
	 */
	public boolean isUseOtherProject() {
		return this.useOtherProject;
	}
}
