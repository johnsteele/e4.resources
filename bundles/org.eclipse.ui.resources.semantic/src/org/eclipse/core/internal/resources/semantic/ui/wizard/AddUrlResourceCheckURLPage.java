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
package org.eclipse.core.internal.resources.semantic.ui.wizard;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.internal.resources.semantic.ui.Messages;
import org.eclipse.core.internal.resources.semantic.ui.SemanticResourcesUIPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class AddUrlResourceCheckURLPage extends WizardPage {

	public AddUrlResourceCheckURLPage() {
		super(AddUrlResourceNameAndURLPage.class.getName());
		setTitle(Messages.AddUrlResourceCheckURLPage_PageTitle_XGRP);
	}

	public boolean shouldRetrieveContent() {
		return retrieveContent.getSelection();
	}

	Text urlText;

	Button ignoreCheckResults;
	Button retrieveContent;

	String url;

	public void setUrl(String newUrl) {
		setPageComplete(false);
		this.url = newUrl;
		if (urlText != null)
			if (this.url == null)
				urlText.setText(""); //$NON-NLS-1$
			else
				urlText.setText(this.url);
	}

	public void createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(2, false));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(main);

		new Label(main, SWT.NONE).setText(Messages.AddUrlResourceCheckURLPage_Url_XFLD);
		urlText = new Text(main, SWT.BORDER);
		urlText.setEnabled(false);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(urlText);
		if (url != null)
			urlText.setText(url);

		ignoreCheckResults = new Button(main, SWT.CHECK);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(ignoreCheckResults);
		ignoreCheckResults.setText(Messages.AddUrlResourceCheckURLPage_IgnoreChecks_XBUT);
		ignoreCheckResults.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				checkPage();
			}

		});

		retrieveContent = new Button(main, SWT.CHECK);
		retrieveContent.setSelection(true);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(retrieveContent);
		retrieveContent.setText(Messages.AddUrlResourceCheckURLPage_Retrieve_XBUT);

		setPageComplete(false);

		setControl(main);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			checkPage();
		}
	}

	void checkPage() {

		setErrorMessage(null);
		setMessage(null, IMessageProvider.WARNING);

		try {
			if (url == null) {
				setErrorMessage(Messages.AddUrlResourceCheckURLPage_URLMissing_XMSG);
				return;
			}
			URI uri;
			try {
				uri = new URI(url);
			} catch (URISyntaxException e) {
				setErrorMessage(NLS.bind(Messages.AddUrlResourceCheckURLPage_InavlidSyntax_XMSG, url));
				return;
			}

			final URL urlObject;
			try {
				urlObject = uri.toURL();
			} catch (Exception e1) {
				setErrorMessage(NLS.bind(Messages.AddUrlResourceCheckURLPage_InvalidUrlWithMessage_XMSG, url, e1.getMessage()));
				return;
			}

			if (!ignoreCheckResults.getSelection()) {

				final Exception[] exception = new Exception[] {null};

				try {
					getWizard().getContainer().run(false, false, new IRunnableWithProgress() {

						public void run(IProgressMonitor monitor) {
							try {
								urlObject.openConnection().connect();
							} catch (Exception e) {
								exception[0] = e;
							}
						}
					});
				} catch (InvocationTargetException e) {
					SemanticResourcesUIPlugin.handleError(e.getMessage(), e, true);
				} catch (InterruptedException e) {
					SemanticResourcesUIPlugin.handleError(e.getMessage(), e, true);
				}

				if (exception[0] != null) {
					setErrorMessage(NLS.bind(Messages.AddUrlResourceCheckURLPage_ConnectFailed_XMSG, url, exception[0].getMessage()));
					return;
				}
			}

		} finally {
			boolean errorFound = getErrorMessage() != null;
			if (!errorFound && ignoreCheckResults.getSelection()) {
				setMessage(Messages.AddUrlResourceCheckURLPage_IgnoringResult_XMSG, IMessageProvider.WARNING);
			}
			setPageComplete(getErrorMessage() == null);
		}

	}
}
