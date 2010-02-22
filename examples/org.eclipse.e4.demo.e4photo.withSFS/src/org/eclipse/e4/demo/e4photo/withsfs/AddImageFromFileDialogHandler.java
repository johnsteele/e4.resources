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
package org.eclipse.e4.demo.e4photo.withsfs;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;

public class AddImageFromFileDialogHandler {

	private final static class AddImageFromFileWizard extends Wizard {
		final ISemanticFolder myFolder;

		/**
		 * 
		 * @param folder
		 *            the folder
		 */
		AddImageFromFileWizard(IEclipseContext context, ISemanticFolder folder) {
			this.myFolder = folder;
		}

		@Override
		public void addPages() {
			addPage(new AddFileOrFolderFromRemotePage(this.myFolder));
		}

		@Override
		public boolean performFinish() {
			AddFileOrFolderFromRemotePage page = (AddFileOrFolderFromRemotePage) getPage(AddFileOrFolderFromRemotePage.PAGE_NAME);
			try {
				doIt(this.myFolder, page);
			} catch (InterruptedException e) {
				page.setErrorMessage(Messages.HandleAddFileFromRemote_Interrupted_XMSG);
				return false;
			} catch (InvocationTargetException e) {
				page.setErrorMessage(e.getCause().getMessage());
				return false;
			}
			return true;
		}

		void doIt(final ISemanticFolder parentFolder, final AddFileOrFolderFromRemotePage page) throws InterruptedException,
				InvocationTargetException {

			new ProgressMonitorDialog(this.getShell()).run(true, true, new IRunnableWithProgress() {

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
									throw new CoreException(
											new Status(IStatus.ERROR, Activator.PLUGIN_ID, null, new InterruptedException()));
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

	public void execute(Shell shell, IEclipseContext context) {
		Object sel = context.get(IServiceConstants.SELECTION);
		IResource res = null;

		if (sel != null && sel instanceof IResource) {
			res = (IResource) sel;
		} else {
			// get the first project of nothing is selected
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

			if (projects.length > 0) {
				res = projects[0];
			}
		}
		if (res != null) {
			// if a file is selected, go to it's parent
			if (res.getType() == IResource.FILE) {
				res = res.getParent();
			}

			ISemanticFolder sfolder = (ISemanticFolder) res.getAdapter(ISemanticFolder.class);

			if (sfolder != null) {
				AddImageFromFileWizard wizard = new AddImageFromFileWizard(context, sfolder);

				new WizardDialog(shell, wizard).open();
			}
		}
	}

}
