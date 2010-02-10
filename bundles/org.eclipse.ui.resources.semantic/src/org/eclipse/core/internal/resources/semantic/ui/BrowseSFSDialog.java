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
package org.eclipse.core.internal.resources.semantic.ui;

import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for browsing the Semantic File System and selecting a path.
 * <p>
 * Re-uses {@link SemanticResourcesView} in order to create the controls.
 */
public class BrowseSFSDialog extends Dialog {

	private final SemanticResourcesView myView;

	/**
	 * @param parentShell
	 * @param initialPath
	 *            the initial path to select (with or without semanticfs:/
	 *            prefix)
	 */
	public BrowseSFSDialog(Shell parentShell, String initialPath) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.SHELL_TRIM);
		this.myView = new SemanticResourcesView();
		if (initialPath.startsWith(ISemanticFileSystem.SCHEME)) {
			String subPath = initialPath.substring(ISemanticFileSystem.SCHEME.length() + 1);
			this.myView.setSelectedPath(subPath);
		} else {
			this.myView.setSelectedPath(initialPath);
		}
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.BrowseSFSDialog_Browser_XGRP);
	}

	/**
	 * @return the selected path
	 */
	public String getSelectedPathString() {
		String selectedPath = this.myView.getSelectedPath();
		if (selectedPath != null) {
			return ISemanticFileSystem.SCHEME + ':' + selectedPath;
		}
		return ""; //$NON-NLS-1$
	}

	protected Control createDialogArea(Composite parent) {

		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, false));
		GridDataFactory.fillDefaults().grab(true, true).hint(500, 300).applyTo(main);
		this.myView.createPartControl(main);
		return main;
	}
}
