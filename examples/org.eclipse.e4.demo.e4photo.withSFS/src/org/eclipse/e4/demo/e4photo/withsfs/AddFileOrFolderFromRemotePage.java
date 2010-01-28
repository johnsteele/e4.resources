/*******************************************************************************
 * Copyright (c) 2009 SAP AG. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Eduard Bartsch (SAP AG) - initial API and implementation
 * Mathias Kinzler (SAP AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.e4photo.withsfs;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Wizard page to add a local file or folder, potentially deep
 * 
 */
public class AddFileOrFolderFromRemotePage extends WizardPage {

	/** the page name */
	public static final String PAGE_NAME = AddFileOrFolderFromRemotePage.class.getName();

	/** preferences */
	private final static String PREF_FOLDERMODE = AddFileOrFolderFromRemotePage.class.getName() + "FolderMode";//$NON-NLS-1$
	private final static String PREF_PATH = AddFileOrFolderFromRemotePage.class.getName() + "Path";//$NON-NLS-1$
	private final static String PREF_NAME = AddFileOrFolderFromRemotePage.class.getName() + "Name";//$NON-NLS-1$
	private final static String PREF_DEEP = AddFileOrFolderFromRemotePage.class.getName() + "Deep"; //$NON-NLS-1$

	final ISemanticFolder parentFolder;

	Text pathText;
	Text childNameText;
	Button deepMode;
	Button addFile;

	boolean folderMode;
	String path = ""; //$NON-NLS-1$
	String childName = ""; //$NON-NLS-1$
	boolean deep;

	boolean initialized = false;

	/**
	 * 
	 * @param parent
	 *            the parent folder
	 */
	public AddFileOrFolderFromRemotePage(ISemanticFolder parent) {
		super(AddFileOrFolderFromRemotePage.PAGE_NAME);
		this.parentFolder = parent;
	}

	/**
	 * Constructs an instance with predefined folder (not taken from
	 * preferences)
	 * 
	 * @param parent
	 *            the parent folder
	 * @param initialPath
	 *            the initial path
	 * @param folderMode
	 *            the folder mode
	 */
	public AddFileOrFolderFromRemotePage(ISemanticFolder parent, String initialPath, boolean folderMode) {
		super(AddFileOrFolderFromRemotePage.PAGE_NAME);
		this.parentFolder = parent;
		this.path = initialPath;
		this.folderMode = folderMode;
		this.initialized = true;
	}

	/**
	 * 
	 * @return the child name
	 */
	public String getChildName() {
		return this.childName;
	}

	/**
	 * @return the path (with '/' separator)
	 */
	public String getPath() {
		return this.path.replace('\\', '/');
	}

	/**
	 * 
	 * @return the folder mode flag
	 */
	public boolean isFolderMode() {
		return this.folderMode;
	}

	/**
	 * 
	 * @return the deep flag
	 */
	public boolean isDeep() {
		return this.deep;
	}

	public void createControl(Composite parent) {

		initFromPreferences();
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(parent);

		Composite myParent = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(myParent);

		myParent.setLayout(new GridLayout(2, false));

		Label parentFolderLabel = new Label(myParent, SWT.NONE);
		parentFolderLabel.setText(Messages.AddFileOrFolderFromRemotePage_ParentFolder_XFLD);

		Text parentPath = new Text(myParent, SWT.BORDER);
		parentPath.setText(this.parentFolder.getAdaptedContainer().getFullPath().toString());
		parentPath.setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(parentPath);

		Group localFs = new Group(myParent, SWT.SHADOW_ETCHED_IN);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(localFs);

		localFs.setText(Messages.AddFileOrFolderFromRemotePage_LocalFileOrFolder_XGRP);
		localFs.setLayout(new GridLayout(3, false));

		this.addFile = new Button(localFs, SWT.RADIO);
		this.addFile.setText(Messages.AddFileOrFolderFromRemotePage_File_XRBL);
		this.addFile.setSelection(!this.folderMode);
		GridDataFactory.fillDefaults().span(3, 1).applyTo(this.addFile);

		Button addFolder = new Button(localFs, SWT.RADIO);
		addFolder.setSelection(this.folderMode);
		addFolder.setText(Messages.AddFileOrFolderFromRemotePage_Folder_XRBL);

		GridDataFactory.fillDefaults().span(3, 1).applyTo(addFolder);

		this.addFile.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				AddFileOrFolderFromRemotePage.this.folderMode = !AddFileOrFolderFromRemotePage.this.addFile.getSelection();
				AddFileOrFolderFromRemotePage.this.deepMode.setEnabled(AddFileOrFolderFromRemotePage.this.folderMode);
				check();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing
			}
		});

		Label pathLabel = new Label(localFs, SWT.NONE);
		pathLabel.setText(Messages.AddFileOrFolderFromRemotePage_LocalPath_XFLD);

		this.pathText = new Text(localFs, SWT.BORDER);
		this.pathText.setText(this.path);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(this.pathText);

		this.pathText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				AddFileOrFolderFromRemotePage.this.path = AddFileOrFolderFromRemotePage.this.pathText.getText();
				try {
					Path selPath = new Path(AddFileOrFolderFromRemotePage.this.path);
					AddFileOrFolderFromRemotePage.this.childNameText.setText(selPath.lastSegment());
				} catch (Exception e1) {
					// $JL-EXC$ ignore
				}
				check();

			}
		});

		Button browseButton = new Button(localFs, SWT.PUSH);
		browseButton.setText(Messages.AddFileOrFolderFromRemotePage_Browse_XBUT);
		browseButton.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				String selected;
				if (AddFileOrFolderFromRemotePage.this.folderMode) {
					DirectoryDialog dialog = new DirectoryDialog(getShell());
					dialog.setFilterPath(new Path(AddFileOrFolderFromRemotePage.this.path).toOSString());
					selected = dialog.open();
				} else {
					FileDialog dialog = new FileDialog(getShell());
					dialog.setFilterPath(new Path(AddFileOrFolderFromRemotePage.this.path).toOSString());
					selected = dialog.open();
				}
				if (selected == null) {
					selected = ""; //$NON-NLS-1$
				}
				AddFileOrFolderFromRemotePage.this.pathText.setText(selected);

			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing
			}
		});

		this.deepMode = new Button(localFs, SWT.CHECK);
		this.deepMode.setText(Messages.AddFileOrFolderFromRemotePage_Deep_XRBL);
		this.deepMode.setEnabled(this.folderMode);
		this.deepMode.setSelection(this.deep);
		this.deepMode.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				AddFileOrFolderFromRemotePage.this.deep = AddFileOrFolderFromRemotePage.this.deepMode.getSelection();
				check();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing
			}
		});

		GridDataFactory.fillDefaults().span(3, 1).applyTo(this.deepMode);

		Label nameLabel = new Label(myParent, SWT.NONE);
		nameLabel.setText(Messages.AddFileOrFolderFromRemotePage_ChildName_XFLD);

		this.childNameText = new Text(myParent, SWT.BORDER);
		this.childNameText.setText(this.childName);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(this.childNameText);

		this.childNameText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				AddFileOrFolderFromRemotePage.this.childName = AddFileOrFolderFromRemotePage.this.childNameText.getText();
				check();
			}
		});

		setMessage(Messages.AddFileOrFolderFromRemotePage_Select_XMSG);
		setControl(myParent);
		setPageComplete(!this.childName.equals("") && !this.path.equals("")); //$NON-NLS-1$ //$NON-NLS-2$
		browseButton.setFocus();

	}

	private void initFromPreferences() {

		if (this.initialized) {
			return;
		}
		Preferences store = new ConfigurationScope().getNode(Activator.PLUGIN_ID);

		this.folderMode = store.getBoolean(AddFileOrFolderFromRemotePage.PREF_FOLDERMODE, true);
		this.path = store.get(AddFileOrFolderFromRemotePage.PREF_PATH, ""); //$NON-NLS-1$
		this.childName = store.get(AddFileOrFolderFromRemotePage.PREF_NAME, ""); //$NON-NLS-1$
		this.deep = store.getBoolean(AddFileOrFolderFromRemotePage.PREF_DEEP, false);
	}

	private void saveToPreferences() {

		Preferences store = new ConfigurationScope().getNode(Activator.PLUGIN_ID);

		store.putBoolean(AddFileOrFolderFromRemotePage.PREF_FOLDERMODE, this.folderMode);
		store.put(AddFileOrFolderFromRemotePage.PREF_PATH, this.path);
		store.put(AddFileOrFolderFromRemotePage.PREF_NAME, this.childName);
		store.putBoolean(AddFileOrFolderFromRemotePage.PREF_DEEP, this.deep);

		try {
			store.flush();
		} catch (BackingStoreException e) {
			// $JL-EXC$ simply ignore
		}

	}

	protected void check() {

		setErrorMessage(null);
		setPageComplete(false);

		if (this.path.equals("")) { //$NON-NLS-1$
			setErrorMessage(Messages.AddFileOrFolderFromRemotePage_PathMssing_XMSG);
			return;
		}

		File test = new File(this.pathText.getText());
		if (!test.exists()) {
			setErrorMessage(Messages.AddFileOrFolderFromRemotePage_NotFoundAtPath_XMSG);
			return;
		}

		if (test.isDirectory() != this.folderMode) {
			if (this.folderMode) {
				setErrorMessage(Messages.AddFileOrFolderFromRemotePage_NotFolder_XMSG);
			} else {
				setErrorMessage(Messages.AddFileOrFolderFromRemotePage_NotFile_XMSG);
			}

			return;
		}

		if (this.childNameText.getText().equals("")) { //$NON-NLS-1$
			setErrorMessage(Messages.AddFileOrFolderFromRemotePage_NameMissing_XMSG);
			return;
		}

		IPath validationPath = this.parentFolder.getAdaptedContainer().getFullPath().append(this.childNameText.getText());
		int type;
		if (this.folderMode) {
			type = IResource.FOLDER;
		} else {
			type = IResource.FILE;
		}
		IStatus stat = ResourcesPlugin.getWorkspace().validatePath(validationPath.toString(), type);
		if (!stat.isOK()) {
			setErrorMessage(stat.getMessage());
			return;
		}

		try {
			if (AddFileOrFolderFromRemotePage.this.parentFolder.hasResource(AddFileOrFolderFromRemotePage.this.childNameText.getText())) {
				setErrorMessage(NLS.bind(Messages.AddFileOrFolderFromRemotePage_NameInUse_XMSG, this.childNameText.getText()));
				return;

			}
		} catch (CoreException e) {
			// $JL-EXC$ ignore here
		}

		setPageComplete(!this.childName.equals("") && !this.path.equals("")); //$NON-NLS-1$ //$NON-NLS-2$
		if (isPageComplete()) {
			saveToPreferences();
		}

	}

}
