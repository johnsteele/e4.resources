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
package org.eclipse.core.resources.semantic.examples.wizard;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.examples.Messages;
import org.eclipse.core.resources.semantic.examples.SemanticResourcesPluginExamples;
import org.eclipse.core.resources.semantic.examples.providers.SampleWebDAVContentProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class AddWebDAVResourceWizard extends Wizard implements INewWizard {

	AddWebDAVResourceNameAndURLPage nameAndUrlPage = new AddWebDAVResourceNameAndURLPage();
	AddWebDAVResourceCheckURLPage checkPage = new AddWebDAVResourceCheckURLPage();

	public AddWebDAVResourceWizard() {
		setWindowTitle(Messages.AddWebDAVResourceWizard_WizardTitle);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		Object testObject = selection.getFirstElement();
		if (testObject instanceof IContainer)
			nameAndUrlPage.setResourceContainer((IContainer) testObject);
		else
			nameAndUrlPage.setResourceContainer(null);

	}

	@Override
	public void addPages() {
		addPage(nameAndUrlPage);
		addPage(checkPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == nameAndUrlPage)
			checkPage.setUrl(nameAndUrlPage.getUrl());
		return super.getNextPage(page);
	}

	@Override
	public boolean performFinish() {

		try {

			getContainer().run(false, false, new IRunnableWithProgress() {

				public void run(IProgressMonitor outerMonitor) throws InvocationTargetException, InterruptedException {

					final ISemanticFolder parentResource = (ISemanticFolder) nameAndUrlPage.getResourceContainer().getAdapter(
							ISemanticFolder.class);
					final URI uri;
					final boolean asFolder = checkPage.isFolder();
					final boolean shouldRetrieveContent = checkPage.shouldRetrieveContent();
					final String fileOrFolder;
					if (asFolder) {
						fileOrFolder = "folder"; //$NON-NLS-1$
					} else {
						fileOrFolder = "file"; //$NON-NLS-1$
					}

					final boolean forceMode = nameAndUrlPage.getForceOverwrite();
					try {
						if (parentResource != null) {
							uri = new URI(nameAndUrlPage.getUrl());
						} else {
							// TODO adjust this in order to create files
							IPath newPath = nameAndUrlPage.getResourceContainer().getFullPath().append(nameAndUrlPage.getChildName());
							uri = new URI(ISemanticFileSystem.SCHEME, null, newPath.toString(), "type=" + fileOrFolder //$NON-NLS-1$
									+ ";create=true;provider=" + SampleWebDAVContentProvider.class.getName() //$NON-NLS-1$
									+ ";uri=" + nameAndUrlPage.getUrl(), null); //$NON-NLS-1$

						}
					} catch (URISyntaxException e1) {
						throw new InvocationTargetException(e1);
					}

					if (outerMonitor.isCanceled()) {
						throw new InterruptedException();
					}

					try {
						IWorkspaceRunnable wsr = new IWorkspaceRunnable() {

							public void run(IProgressMonitor monitor) throws CoreException {
								if (parentResource != null) {
									if (parentResource.hasResource(nameAndUrlPage.getChildName())) {
										if (!forceMode) {
											throw new CoreException(new Status(IStatus.ERROR, SemanticResourcesPluginExamples.PLUGIN_ID,
													Messages.AddWebDAVResourceWizard_AlreadyExistsMessaage));
										}

										parentResource.getResource(nameAndUrlPage.getChildName()).remove(
												ISemanticFileSystem.SUPPRESS_REFRESH, monitor);
									}
									if (asFolder) {
										ISemanticFolder sFolder = parentResource.addFolder(nameAndUrlPage.getChildName(),
												SampleWebDAVContentProvider.class.getName(), null, ISemanticFileSystem.NONE, monitor);

										sFolder.setRemoteURI(uri, ISemanticFileSystem.NONE, monitor);
									} else {
										ISemanticFile sFile = parentResource.addFile(nameAndUrlPage.getChildName(),
												SampleWebDAVContentProvider.class.getName(), null, ISemanticFileSystem.NONE, monitor);

										sFile.setRemoteURI(uri, ISemanticFileSystem.NONE, monitor);
									}

									if (shouldRetrieveContent) {
										final ISemanticResource resource = parentResource.getResource(nameAndUrlPage.getChildName());

										if (resource != null) {
											WorkspaceJob job = new WorkspaceJob(Messages.AddWebDAVResourceWizard_RetrieveWebDAVContent) {

												@Override
												public IStatus runInWorkspace(IProgressMonitor monitor1) throws CoreException {
													resource.synchronizeContentWithRemote(SyncDirection.INCOMING, ISemanticFileSystem.NONE,
															monitor1);
													return Status.OK_STATUS;
												}
											};
											job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().refreshRule(
													resource.getAdaptedResource()));
											job.schedule();
										}
									}
								} else {
									IContainer container = nameAndUrlPage.getResourceContainer();
									IPath childPath = new Path(nameAndUrlPage.getChildName());

									if (container.exists(childPath)) {
										if (!forceMode) {
											throw new CoreException(new Status(IStatus.ERROR, SemanticResourcesPluginExamples.PLUGIN_ID,
													Messages.AddWebDAVResourceWizard_AlreadyExistsMessaage));
										}

										IResource child = container.findMember(nameAndUrlPage.getChildName());
										if (child != null && child.isLinked()) {
											URI oldURI = child.getLocationURI();

											child.delete(true, monitor);

											if (oldURI != null && oldURI.getScheme().equals(ISemanticFileSystem.SCHEME)) {
												IFileStore store = EFS.getStore(oldURI);

												if (store != null && store instanceof ISemanticFileStore) {
													store.delete(EFS.NONE, monitor);
												}
											}
										} else {
											IFolder childFolder = container.getFolder(childPath);
											if (childFolder.exists()) {
												childFolder.delete(false, monitor);
											}

											IFile childFile = container.getFile(childPath);
											if (childFile.exists()) {
												childFile.delete(false, monitor);
											}
										}
									}

									final IResource resource;

									if (asFolder) {
										IFolder folder = container.getFolder(childPath);
										resource = folder;
										folder.createLink(uri, 0, monitor);
									} else {
										IFile file = container.getFile(childPath);
										resource = file;
										file.createLink(uri, IResource.ALLOW_MISSING_LOCAL, monitor);
									}

									if (shouldRetrieveContent) {
										final ISemanticResource sResource = (ISemanticResource) resource
												.getAdapter(ISemanticResource.class);

										if (sResource != null) {
											WorkspaceJob job = new WorkspaceJob(Messages.AddWebDAVResourceWizard_RetrieveWebDAVContent) {

												@Override
												public IStatus runInWorkspace(IProgressMonitor monitor1) throws CoreException {
													sResource.synchronizeContentWithRemote(SyncDirection.INCOMING,
															ISemanticFileSystem.NONE, monitor1);
													return Status.OK_STATUS;
												}
											};
											job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().refreshRule(resource));
											job.schedule();
										}
									}
								}
							}
						};
						ResourcesPlugin.getWorkspace().run(wsr, outerMonitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}

				}
			});
		} catch (InvocationTargetException e) {
			SemanticResourcesPluginExamples.handleError(Messages.AddWebDAVResourceWizard_CreationFailedMessage, e.getCause(), true);
			return false;
		} catch (InterruptedException e) {
			SemanticResourcesPluginExamples.handleError(Messages.AddWebDAVResourceWizard_ActionAbortedMessage, e, true);
			return false;
		}

		return true;
	}
}
