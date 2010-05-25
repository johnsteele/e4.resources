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
package org.eclipse.core.resources.semantic.examples.webdav;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class UserCredentialsDialog extends Dialog {
	protected Text usernameField;
	protected Text passwordField;

	protected String host;
	protected String message;
	private Credentials credentials;

	public static Credentials askForCredentials(String host, String message) {
		UserCredentialsDialog ui = new UserCredentialsDialog(null, host, message);
		ui.open();

		return ui.getCredentials();
	}

	private UserCredentialsDialog(Shell parentShell, String host, String message) {
		super(parentShell);
		this.host = host;
		this.message = message;
		this.setBlockOnOpen(true);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);

		shell.setText(Messages.UserCredentialsDialog_PasswordRequired_XGRP);
	}

	/**
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		main.setLayout(layout);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(main, SWT.WRAP);
		String text = NLS.bind(Messages.UserCredentialsDialog_ConnectTo_XFLD, host);
		text += "\n\n" + message; //$NON-NLS-1$ 
		label.setText(text);
		GridDataFactory.generate(label, 2, 1);

		Label userLabel = new Label(main, SWT.NONE);
		userLabel.setText(Messages.UserCredentialsDialog_UserName_XFLD);
		GridDataFactory.fillDefaults().applyTo(userLabel);

		usernameField = new Text(main, SWT.BORDER);
		GridDataFactory.fillDefaults().applyTo(usernameField);

		Label pwdLabel = new Label(main, SWT.NONE);
		pwdLabel.setText(Messages.UserCredentialsDialog_Password_XFLD);
		GridDataFactory.fillDefaults().applyTo(pwdLabel);

		passwordField = new Text(main, SWT.BORDER | SWT.PASSWORD);
		GridDataFactory.fillDefaults().applyTo(passwordField);

		return main;
	}

	private Credentials getCredentials() {
		return credentials;
	}

	/**
	 * Notifies that the ok button of this dialog has been pressed.
	 */
	@Override
	protected void okPressed() {
		if (usernameField.getText() != null && passwordField.getText() != null) {
			this.credentials = new UsernamePasswordCredentials(usernameField.getText(), passwordField.getText());
		}
		super.okPressed();
	}
}
