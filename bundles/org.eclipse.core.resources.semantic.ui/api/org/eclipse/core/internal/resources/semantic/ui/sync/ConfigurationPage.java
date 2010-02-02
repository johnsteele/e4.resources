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
package org.eclipse.core.internal.resources.semantic.ui.sync;

import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Configures two-way vs. three-way compare in team sync
 */
public class ConfigurationPage extends WizardPage {
	/** the page name of this page */
	public final static String NAME = "ConfigureSync"; //$NON-NLS-1$

	private final ISemanticFileSystem sfs;
	boolean isThreeWaySelected;

	/**
	 * The constructor
	 * 
	 * @param sfs
	 *            the semantic file system; if this is null, this page will
	 *            display an error message
	 */
	public ConfigurationPage(ISemanticFileSystem sfs) {
		super(NAME);
		this.sfs = sfs;
	}

	public void createControl(Composite parent) {

		Composite myArea = new Composite(parent, SWT.NONE);
		setTitle(Messages.ConfigurationPage_PageTitle_XGRP);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(myArea);

		myArea.setLayout(new GridLayout(1, false));

		final Button twoWay = new Button(myArea, SWT.RADIO);
		twoWay.setText(Messages.ConfigurationPage_TwoWay_XRBL);
		twoWay.setSelection(true);

		twoWay.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				ConfigurationPage.this.isThreeWaySelected = !twoWay.getSelection();

			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing

			}
		});

		Button threeWay = new Button(myArea, SWT.RADIO);
		threeWay.setText(Messages.ConfigurationPage_ThreeWay_XRBL);
		setControl(myArea);
		if (this.sfs == null) {
			setErrorMessage(Messages.ConfigurationPage_NotInitialized_XMSG);
		}
		setPageComplete(this.sfs != null);

	}

	/**
	 * @return the result
	 */
	public boolean isThreeWay() {
		return this.isThreeWaySelected;
	}
}
