/*******************************************************************************
 * Copyright (c) 2009 SAP AG. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Eduard Bartsch (SAP AG) - initial API and implementation
 * Mathias Kinzler (SAP AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources.semantic.examples;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

/**
 * Handles the "Add Rest Resource" command
 * 
 */
public class HandleAddFileFromRemote extends HandlerUtilities {

	private final class MyWizard extends Wizard {

		private final ISemanticFolder myFolder;

		public MyWizard(ISemanticFolder folder) {
			this.myFolder = folder;
			addPage(new AddFileOrFolderFromRemotePage(this.myFolder));
			setWindowTitle(Messages.HandleAddFileFromRemote_AddLocalResource_XGRP);
		}

		public boolean performFinish() {
			AddFileOrFolderFromRemotePage page = (AddFileOrFolderFromRemotePage) getPage(AddFileOrFolderFromRemotePage.PAGE_NAME);
			try {
				doIt(this.myFolder, page);
			} catch (InterruptedException e) {
				// $JL-EXC$ ignore
				page.setErrorMessage(Messages.HandleAddFileFromRemote_Interrupted_XMSG);
				return false;
			} catch (InvocationTargetException e) {
				page.setErrorMessage(e.getCause().getMessage());
				return false;
			}
			return true;
		}
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {

		final ISemanticFolder sFolder = getSelectedObject(event, ISemanticFolder.class, true);
		if (sFolder == null) {
			showPopup(Messages.HandleAddFromRemote_Error_XGRP, Messages.HandleAddFromRemote_NotSemantic_XMSG);
			return null;
		}

		WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), new MyWizard(sFolder));
		dialog.open();

		return null;
	}

	void doIt(final ISemanticFolder parentFolder, final AddFileOrFolderFromRemotePage page) throws InterruptedException,
			InvocationTargetException {

		submit(Display.getCurrent().getActiveShell(), new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

					public void run(IProgressMonitor actMonitor) throws CoreException {

						boolean folderMode = page.isFolderMode();
						String childName = page.getChildName();

						File rootFile = new File(page.getPath());
						URI uri = rootFile.toURI();

						int childCount;
						if (folderMode && page.isDeep()) {
							childCount = countChildren(0, rootFile.listFiles());
						} else {
							childCount = 1;
						}

						actMonitor.beginTask("", childCount * 2); //$NON-NLS-1$

						actMonitor.setTaskName(Messages.HandleAddFileFromRemote_Adding_XMSG + uri.toString());
						if (folderMode) {
							ISemanticFolder folder = parentFolder.addFolder(childName, uri, ISemanticFileSystem.SUPPRESS_REFRESH,
									actMonitor);
							if (page.isDeep()) {

								addChildren(0, folder, rootFile.listFiles(), actMonitor);
							} else {
								actMonitor.worked(1);
							}
						} else {
							parentFolder.addFile(childName, uri, ISemanticFileSystem.SUPPRESS_REFRESH, actMonitor);
							actMonitor.worked(1);
						}
						actMonitor.setTaskName(Messages.HandleAddFileFromRemote_Refreshing_XMSG);
						parentFolder.getAdaptedContainer().refreshLocal(IResource.DEPTH_INFINITE, actMonitor);
						actMonitor.done();

					}

					private int countChildren(int input, File[] children) {
						int result = input;
						result += children.length;
						for (File child : children) {
							if (child.isDirectory()) {
								result = countChildren(result, child.listFiles());
							}
						}
						return result;
					}

					private int addChildren(int actIndex, ISemanticFolder folder, File[] childFiles, IProgressMonitor actMonitor)
							throws CoreException {

						int currentChildIndex = actIndex;

						for (File childFile : childFiles) {
							if (actMonitor.isCanceled()) {
								throw new CoreException(new Status(IStatus.ERROR, SemanticResourcesPluginExamples.PLUGIN_ID, null,
										new InterruptedException()));
							}
							URI childUri = childFile.toURI();
							actMonitor.setTaskName(NLS.bind(Messages.HandleAddFileFromRemote_AddingResource_XMSG, childUri.toString()));
							currentChildIndex++;
							if (childFile.isDirectory()) {
								ISemanticFolder childFolder = folder.addFolder(childFile.getName(), childUri,
										ISemanticFileSystem.SUPPRESS_REFRESH, actMonitor);
								currentChildIndex = addChildren(currentChildIndex, childFolder, childFile.listFiles(), actMonitor);
							} else {
								folder.addFile(childFile.getName(), childUri, ISemanticFileSystem.SUPPRESS_REFRESH, actMonitor);
							}
							actMonitor.worked(1);
						}
						return currentChildIndex;
					}
				};

				try {
					ResourcesPlugin.getWorkspace().run(runnable, monitor);
				} catch (CoreException e) {
					if (e.getCause() instanceof InterruptedException) {
						throw (InterruptedException) e.getCause();
					}
					throw new InvocationTargetException(e);
				}

			}
		});

	}

}
