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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Handles the "Add from remote" command
 * 
 */
public class HandleCreateRemotely extends HandlerUtilities {

	private final class MyWizard extends Wizard {

		final ISemanticFolder parentFolder;

		MyWizard(ISemanticFolder folder) {
			this.parentFolder = folder;
			setWindowTitle(Messages.HandleCreateRemotely_CreateRemotely_XGRP);
			addPage(new CreateRemotelyPage(this.parentFolder));
		}

		@Override
		public boolean performFinish() {

			final CreateRemotelyPage page = (CreateRemotelyPage) getPage(CreateRemotelyPage.PAGENAME);

			IRunnableWithProgress runnable = new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor) throws InvocationTargetException {

					IWorkspaceRunnable inner = new IWorkspaceRunnable() {

						public void run(IProgressMonitor actMonitor) throws CoreException {
							if (page.isFolderMode()) {
								MyWizard.this.parentFolder.createResourceRemotely(page.getChildName(), null, ISemanticFileSystem.NONE,
										actMonitor);
							} else {
								byte[] contents;
								try {
									contents = page.getContent().getBytes("UTF-8"); //$NON-NLS-1$
								} catch (UnsupportedEncodingException e) {
									// $JL-EXC$ ignore
									contents = new byte[0];
								}
								MyWizard.this.parentFolder.createFileRemotely(page.getChildName(), new ByteArrayInputStream(contents),
										null, ISemanticFileSystem.NONE, actMonitor);
							}

						}
					};
					try {
						ResourcesPlugin.getWorkspace().run(inner, monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}

				}
			};

			try {
				submit(Display.getCurrent().getActiveShell(), runnable);
			} catch (InterruptedException e) {
				// $JL-EXC$ ignore here
			} catch (InvocationTargetException e) {
				page.setErrorMessage(e.getCause().getMessage());
				return false;
			}
			return true;

		}
	}

	/**
	 * @throws ExecutionException
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final ISemanticFolder sFolder = getSelectedObject(event, ISemanticFolder.class, true);
		if (sFolder == null) {
			showPopup(Messages.HandleAddFromRemote_Error_XGRP, Messages.HandleAddFromRemote_NotSemantic_XMSG);
			return null;
		}

		new WizardDialog(Display.getCurrent().getActiveShell(), new MyWizard(sFolder)).open();

		return null;
	}

}
