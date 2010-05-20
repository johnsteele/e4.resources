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
package org.eclipse.core.internal.resources.semantic.ui;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.internal.resources.semantic.ui.util.SFSBrowserTreeContentProvider;
import org.eclipse.core.internal.resources.semantic.ui.util.SFSBrowserTreeLabelProvider;
import org.eclipse.core.internal.resources.semantic.ui.util.SFSBrowserTreeObject;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog for browsing the Semantic File System and selecting a path.
 */
public class BrowseSFSDialog extends Dialog {

	String selectedPath;

	private TreeViewer sfsTree;

	/**
	 * @param parentShell
	 * @param initialPath
	 *            the initial path to select (with or without semanticfs:/
	 *            prefix)
	 */
	public BrowseSFSDialog(Shell parentShell, String initialPath) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.SHELL_TRIM);
		if (initialPath.startsWith(ISemanticFileSystem.SCHEME)) {
			String subPath = initialPath.substring(ISemanticFileSystem.SCHEME.length() + 1);
			selectedPath = subPath;
		} else {
			selectedPath = initialPath;
		}
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.BrowseSFSDialog_Browser_XGRP);
	}

	/**
	 * @return the selected path
	 */
	public String getSelectedPathString() {
		if (selectedPath != null) {
			return ISemanticFileSystem.SCHEME + ':' + selectedPath;
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite main = new Composite(parent, SWT.NONE);

		main.setLayout(new GridLayout(2, false));
		GridDataFactory.fillDefaults().hint(400, 300).grab(true, true).applyTo(main);

		SFSBrowserTreeObject[] input;
		try {
			input = getRootObjects();
		} catch (CoreException e) {
			// for example, the SFS can not be accessed; in this case, we show a
			// label with the
			// status text and return
			Label errorLabel = new Label(main, SWT.NONE);
			errorLabel.setText(e.getStatus().getMessage());
			return main;
		}

		final Label pathLabel = new Label(main, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(pathLabel);
		pathLabel.setText(Messages.BrowseSFSDialog_Path_XFLD);

		final Text path = new Text(main, SWT.BORDER | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(path);

		this.sfsTree = new TreeViewer(main, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

		SFSBrowserTreeLabelProvider treeLabelProvider = new SFSBrowserTreeLabelProvider();
		treeLabelProvider.configureTreeColumns(this.sfsTree);
		this.sfsTree.setContentProvider(new SFSBrowserTreeContentProvider());
		this.sfsTree.setLabelProvider(treeLabelProvider);

		GridDataFactory.fillDefaults().span(2, 1).grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(this.sfsTree.getTree());

		this.sfsTree.setInput(input);

		if (this.selectedPath != null && !this.selectedPath.equals("")) { //$NON-NLS-1$
			try {
				SFSBrowserTreeObject object = getTreeObject(this.selectedPath);
				this.sfsTree.setSelection(new StructuredSelection(object));
				path.setText(object.getPath().toString());
			} catch (CoreException e) {
				Platform.getLog(Platform.getBundle(SemanticResourcesUIPlugin.PLUGIN_ID)).log(e.getStatus());
			}
		}

		this.sfsTree.addSelectionChangedListener(new ISelectionChangedListener() {

			@SuppressWarnings("synthetic-access")
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.isEmpty()) {
					path.setText(""); //$NON-NLS-1$
					selectedPath = null;
				} else {
					SFSBrowserTreeObject selected = (SFSBrowserTreeObject) sel.getFirstElement();
					selectedPath = selected.getPath().toString();
					path.setText(selectedPath);
				}
				getButton(OK).setEnabled(selectedPath != null);
			}
		});

		return main;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(OK).setEnabled(!sfsTree.getSelection().isEmpty());
	}

	private SFSBrowserTreeObject[] getRootObjects() throws CoreException {
		ISemanticFileSystem fs = (ISemanticFileSystem) EFS.getFileSystem(ISemanticFileSystem.SCHEME);
		String[] roots = fs.getRootNames();
		SFSBrowserTreeObject[] paths = new SFSBrowserTreeObject[roots.length];
		for (int i = 0; i < roots.length; i++) {
			IPath path = new Path('/' + roots[i]);
			paths[i] = new SFSBrowserTreeObject((IFileSystem) fs, path);
		}
		return paths;
	}

	private SFSBrowserTreeObject getTreeObject(String path) throws CoreException {
		IFileSystem fs = EFS.getFileSystem(ISemanticFileSystem.SCHEME);
		return new SFSBrowserTreeObject(fs, new Path(path));
	}
}
