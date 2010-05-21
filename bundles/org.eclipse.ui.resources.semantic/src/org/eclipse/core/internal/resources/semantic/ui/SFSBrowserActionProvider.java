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

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.internal.resources.semantic.ui.util.SFSBrowserTreeObject;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.progress.IProgressService;

public class SFSBrowserActionProvider extends CommonActionProvider {

	public SFSBrowserActionProvider() {
		super();
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		IStructuredSelection sel = (IStructuredSelection) getContext().getSelection();
		if (sel.isEmpty())
			return;

		final Object[] objects = sel.toArray();
		final Shell shell = getActionSite().getViewSite().getShell();

		Action deleteAction = new Action(Messages.SFSBrowserActionProvider_Delete_XMIT) {

			@SuppressWarnings("synthetic-access")
			@Override
			public void run() {

				final boolean[] refresh = new boolean[] {false};

				boolean dontAskAgain = false;

				for (Object object : objects) {

					final SFSBrowserTreeObject selected = (SFSBrowserTreeObject) object;

					String question = NLS.bind(Messages.SFSBrowserActionProvider_ConfirmDelete_XMSG, selected.getPath().toString());

					if (objects.length == 1) {
						dontAskAgain = MessageDialog.openConfirm(shell, Messages.SFSBrowserActionProvider_ConfirmDelete_XGRP, question);
						if (!dontAskAgain) {
							return;
						}
					}

					if (!dontAskAgain) {
						MessageDialogWithToggle toggle = new MessageDialogWithToggle(shell,
								Messages.SFSBrowserActionProvider_ConfirmDelete_XMSG, null, question, MessageDialog.WARNING, new String[] {
										IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 0,
								Messages.SFSBrowserActionProvider_DontAskAgain_XMSG, false);
						int result = toggle.open();
						dontAskAgain = toggle.getToggleState();
						if (result != 0) {
							if (dontAskAgain) {
								break;
							}
							continue;
						}
					}

					IProgressService srv = PlatformUI.getWorkbench().getProgressService();

					try {
						srv.run(true, false, new IRunnableWithProgress() {

							public void run(IProgressMonitor monitor) throws InvocationTargetException {
								try {
									selected.getStore().delete(0, monitor);
									refresh[0] = true;
								} catch (CoreException ce) {
									throw new InvocationTargetException(ce);
								}
							}
						});
					} catch (InvocationTargetException e1) {
						SemanticResourcesUIPlugin.handleError(e1.getCause().getMessage(), e1.getCause(), true);
					} catch (InterruptedException e1) {
						// ignore
					}
				}

				if (refresh[0]) {
					getActionSite().getStructuredViewer().refresh();
				}
			}
		};

		menu.add(deleteAction);

		if (objects.length == 1) {
			final SFSBrowserTreeObject ob = (SFSBrowserTreeObject) objects[0];
			if (!ob.getInfo().isDirectory()) {

				Action showAction = new Action(Messages.SFSBrowserActionProvider_OpenInTextEditor_XMIT) {

					@Override
					public void run() {
						try {
							ISemanticFileStore sfstore = (ISemanticFileStore) ob.getStore();
							File tempFile = sfstore.toLocalFile(EFS.CACHE, new NullProgressMonitor());
							IEditorInput input = new FileStoreEditorInput(EFS.getStore(tempFile.toURI())) {

								@Override
								public String getName() {
									return ob.getStore().getName();
								}

							};
							IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), input,
									EditorsUI.DEFAULT_TEXT_EDITOR_ID);
							// TODO can we somehow listen to close events from
							// the editor and remove the cached file?
							// TODO avoid multiple editors on the same store and
							// link with editor
						} catch (CoreException e) {
							SemanticResourcesUIPlugin.handleError(e.getMessage(), e, true);
						}
					}

				};
				menu.add(showAction);
			}

			Action openPropsAction = new Action(Messages.SFSBrowserActionProvider_OpenInProps_XMIT) {

				@Override
				public void run() {
					try {
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(IPageLayout.ID_PROP_SHEET);
					} catch (PartInitException e) {
						// ignore
					}

				}

			};
			menu.add(openPropsAction);

		}
	}

}
