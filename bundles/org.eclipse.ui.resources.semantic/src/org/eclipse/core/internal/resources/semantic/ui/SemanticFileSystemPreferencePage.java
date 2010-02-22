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

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.spi.SemanticFileCache;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page; allows deletion of the data and caches
 * 
 */
public class SemanticFileSystemPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	@Override
	protected Control createContents(Composite parent) {

		Composite main = new Composite(parent, SWT.NONE);

		main.setLayout(new GridLayout(2, false));

		Label pathLabel = new Label(main, SWT.NONE);
		pathLabel.setText(Messages.SemanticFileSystemPreferencePage_PathToDb_XFLD);

		final Text path = new Text(main, SWT.BORDER);
		path.setEditable(false);

		try {
			ISemanticFileSystem sfs = (ISemanticFileSystem) EFS.getFileSystem(ISemanticFileSystem.SCHEME);
			path.setText(sfs.getPathToDb());
		} catch (CoreException e) {
			// $JL-EXC$ ignore and write exception text
			path.setText(Messages.SemanticFileSystemPreferencePage_Exception_GetPath_XMSG);
		}

		Label cachePathLabel = new Label(main, SWT.NONE);
		cachePathLabel.setText(Messages.SemanticFileSystemPreferencePage_PathToCache_XFLD);

		final Text cachePath = new Text(main, SWT.BORDER);
		cachePath.setEditable(false);

		try {
			File cacheFile = SemanticFileCache.getCache().getCacheDir();
			cachePath.setText(cacheFile.getAbsolutePath());
		} catch (CoreException e1) {
			// $JL-EXC$ ignore and write exception text
			cachePath.setText(Messages.SemanticFileSystemPreferencePage_Exception_GetPath_XMSG);
		}

		Button delete = new Button(main, SWT.PUSH);
		GridDataFactory.fillDefaults().span(2, 1).align(SWT.LEFT, SWT.CENTER).applyTo(delete);
		delete.setText(Messages.SemanticFileSystemPreferencePage_Delete_XBUT);
		delete.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {

				boolean ok = MessageDialog.openConfirm(getShell(), Messages.SemanticFileSystemPreferencePage_Confirm_XGRP,
						Messages.SemanticFileSystemPreferencePage_Confirm_XMSG);
				if (ok) {
					boolean deleted = true;
					File db = new File(path.getText());
					if (db.exists()) {
						deleted = db.delete();
						if (!deleted) {
							db.deleteOnExit();
						}
					}

					boolean cacheDeleted = true;
					File cache = new File(cachePath.getText());
					if (cache.exists()) {
						try {
							delete(cache);
						} catch (CoreException ce) {
							// $JL-EXC$
							cacheDeleted = false;

						}
						if (!cacheDeleted) {
							cache.deleteOnExit();
						}
					}

					MessageDialog.openConfirm(getShell(), Messages.SemanticFileSystemPreferencePage_Restart_XGRP,
							Messages.SemanticFileSystemPreferencePage_Restart_XMSG);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing
			}
		});

		return main;
	}

	public void init(IWorkbench workbench) {
		// nothing to initialize

	}

	void delete(File file) throws CoreException {

		MultiStatus status = new MultiStatus(SemanticResourcesUIPlugin.PLUGIN_ID, IStatus.OK, NLS.bind(
				Messages.SemanticFileSystemPreferencePage_DeletingFile_XMSG, file.getPath()), null);

		if (file.exists()) {
			// we do it recursively so that we can trace the correct file
			deleteFile(file, status);

			if (!status.isOK()) {
				throw new CoreException(status);
			}
		}
	}

	private void deleteFile(File actFile, MultiStatus status) {

		if (actFile.isDirectory()) {

			for (File child : actFile.listFiles()) {
				deleteFile(child, status);
			}
		}

		if (!actFile.delete()) {
			status.add(new Status(IStatus.ERROR, SemanticResourcesUIPlugin.PLUGIN_ID, NLS.bind(
					Messages.SemanticFileSystemPreferencePage_CouldNotDelete_XMSG, actFile.getPath().toString())));
		}

	}

}
