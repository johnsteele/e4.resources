package org.eclipse.core.resources.semantic.examples;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Create remotely
 * 
 */
public class CreateRemotelyPage extends WizardPage {
	/** the page name */
	public static final String PAGENAME = CreateRemotelyPage.class.getName();

	private final ISemanticFolder parentFolder;

	Text childNameText;
	Group fileContentGroup;
	Text fileContentText;
	Button addFile;

	boolean folderMode;
	String childName = ""; //$NON-NLS-1$ since SWT Text can not deal with null
	String fileText = ""; //$NON-NLS-1$

	/**
	 * 
	 * @param parent
	 *            the folder
	 */
	public CreateRemotelyPage(ISemanticFolder parent) {
		super(CreateRemotelyPage.PAGENAME);
		this.parentFolder = parent;
	}

	/**
	 * 
	 * @return the child name
	 */
	public String getChildName() {
		return this.childName;
	}

	/**
	 * 
	 * @return the text content for the file
	 */
	public String getContent() {
		return this.fileText;
	}

	/**
	 * 
	 * @return the flag
	 */
	public boolean isFolderMode() {
		return this.folderMode;
	}

	public void createControl(Composite parent) {

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(parent);

		final Composite myParent = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(myParent);

		myParent.setLayout(new GridLayout(2, false));

		Label parentFolderLabel = new Label(myParent, SWT.NONE);
		parentFolderLabel.setText(Messages.AddFileOrFolderFromRemotePage_ParentFolder_XFLD);

		Text parentPath = new Text(myParent, SWT.BORDER);
		parentPath.setText(this.parentFolder.getAdaptedContainer().getFullPath().toString());
		parentPath.setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(parentPath);

		this.addFile = new Button(myParent, SWT.RADIO);
		this.addFile.setText(Messages.AddFileOrFolderFromRemotePage_File_XRBL);
		this.addFile.setSelection(!this.folderMode);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(this.addFile);

		Button addFolder = new Button(myParent, SWT.RADIO);
		addFolder.setSelection(this.folderMode);
		addFolder.setText(Messages.AddFileOrFolderFromRemotePage_Folder_XRBL);

		GridDataFactory.fillDefaults().span(2, 1).applyTo(addFolder);

		this.addFile.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				CreateRemotelyPage.this.folderMode = !CreateRemotelyPage.this.addFile.getSelection();
				check();
				GridData gd = (GridData) CreateRemotelyPage.this.fileContentGroup.getLayoutData();
				gd.exclude = CreateRemotelyPage.this.folderMode;
				CreateRemotelyPage.this.fileContentGroup.setVisible(!gd.exclude);
				CreateRemotelyPage.this.fileContentText.setEnabled(!gd.exclude);
				myParent.layout(true);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing
			}
		});

		Label nameLabel = new Label(myParent, SWT.NONE);
		nameLabel.setText(Messages.AddFileOrFolderFromRemotePage_ChildName_XFLD);

		this.childNameText = new Text(myParent, SWT.BORDER);
		this.childNameText.setText(this.childName);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(this.childNameText);

		this.childNameText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				CreateRemotelyPage.this.childName = CreateRemotelyPage.this.childNameText.getText();
				check();
			}
		});

		this.fileContentGroup = new Group(myParent, SWT.SHADOW_ETCHED_IN);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, true).applyTo(this.fileContentGroup);
		this.fileContentGroup.setLayout(new GridLayout(2, false));
		this.fileContentGroup.setText(Messages.CreateRemotelyPage_FileContent_XGRP);
		Label fileContentLabel = new Label(this.fileContentGroup, SWT.NONE);
		fileContentLabel.setText(Messages.CreateRemotelyPage_TextContent_XFLD);
		this.fileContentText = new Text(this.fileContentGroup, SWT.BORDER | SWT.MULTI);

		this.fileContentText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				CreateRemotelyPage.this.fileText = CreateRemotelyPage.this.fileContentText.getText();

			}
		});

		GridDataFactory.fillDefaults().grab(true, true).applyTo(this.fileContentText);

		setMessage(Messages.AddFileOrFolderFromRemotePage_Select_XMSG);
		setControl(myParent);
		setPageComplete(!this.childName.equals("")); //$NON-NLS-1$

	}

	void check() {

		setErrorMessage(null);
		setPageComplete(false);

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
			if (this.parentFolder.hasResource(this.childNameText.getText())) {
				setErrorMessage(NLS.bind(Messages.AddFileOrFolderFromRemotePage_NameInUse_XMSG, this.childNameText.getText()));
				return;

			}
		} catch (CoreException e) {
			// $JL-EXC$ ignore here
		}

		setPageComplete(!this.childName.equals("")); //$NON-NLS-1$

	}

}
