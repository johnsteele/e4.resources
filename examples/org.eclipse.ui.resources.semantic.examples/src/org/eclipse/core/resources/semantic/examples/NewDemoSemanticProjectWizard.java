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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticProject;
import org.eclipse.core.resources.semantic.examples.SelectScenarioPage.Scenario;
import org.eclipse.core.resources.semantic.examples.providers.RemoteStoreContentProvider;
import org.eclipse.core.resources.semantic.examples.remote.RemoteFolder;
import org.eclipse.core.resources.semantic.examples.remote.RemoteStore;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard for creating a demo project
 * 
 */
public class NewDemoSemanticProjectWizard extends Wizard implements INewWizard {

	/** The property (on the project) to hold the temporary directory name */
	public static final QualifiedName TEMP_DIR_NAME = new QualifiedName(SemanticResourcesPluginExamples.PLUGIN_ID, "TemporaryDirectory"); //$NON-NLS-1$
	static final String DEFAULT_PROJECT_NAME = "SemanticFileSystemDemo"; //$NON-NLS-1$

	/**
	 * Constructor
	 */
	public NewDemoSemanticProjectWizard() {
		super();
	}

	public void addPages() {
		setWindowTitle(Messages.CreateDemoProjectPage_CreateProject_XGRP);
		addPage(new SelectScenarioPage());
		addPage(new CreateDemoProjectPage());
	}

	public IWizardPage getNextPage(IWizardPage page) {
		if (page instanceof SelectScenarioPage) {
			SelectScenarioPage prev = (SelectScenarioPage) getPage(SelectScenarioPage.class.getName());
			CreateDemoProjectPage next = (CreateDemoProjectPage) getPage(CreateDemoProjectPage.class.getName());
			next.setScenarios(prev.getScenarios());
			return next;
		}
		return super.getNextPage(page);
	}

	public boolean performFinish() {

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();

		SelectScenarioPage scenPage = (SelectScenarioPage) getPage(SelectScenarioPage.class.getName());
		CreateDemoProjectPage projPage = (CreateDemoProjectPage) getPage(CreateDemoProjectPage.class.getName());

		final Set<Scenario> scenarios = scenPage.getScenarios();
		final String projectName = projPage.getProjectName();
		final String directoryName = projPage.getDirectoryName();
		final boolean useOwn = !projPage.isUseOtherProject();

		IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				IProject project = workspace.getRoot().getProject(projectName);
				if (!project.exists()) {
					IProjectDescription description = workspace.newProjectDescription(projectName);

					try {
						description.setLocationURI(new URI(ISemanticFileSystem.SCHEME, null, "/" + projectName, null)); //$NON-NLS-1$
						description.setNatureIds(new String[] { SemanticResourcesPluginExamples.EXAPMLE_NATURE });
					} catch (URISyntaxException e) {
						IStatus status = new Status(IStatus.ERROR, SemanticResourcesPluginExamples.PLUGIN_ID, e.getMessage(), e);
						SemanticResourcesPluginExamples.getDefault().getLog().log(status);
					}

					project.create(description, null);
				}

				if (!project.isOpen()) {
					project.open(monitor);
					RepositoryProvider.map(project, ISemanticFileSystem.SFS_REPOSITORY_PROVIDER);
				}

				for (Scenario scenario : scenarios) {
					project.getFolder(scenario.getFolderName()).create(false, true, monitor);
				}

				File tempFolder = new File(directoryName);

				if (!tempFolder.exists()) {
					tempFolder.mkdirs();
				}

				// now we copy the mimes from the classpath to the "mimes"
				// subdirectory of the
				// temporary folder
				copyMimes(monitor, tempFolder);

				ISemanticProject sproject = (ISemanticProject) project.getAdapter(ISemanticProject.class);

				// we keep the temporary directory as persistent property of the
				// project
				sproject.setPersistentProperty(TEMP_DIR_NAME, directoryName);

				if (scenarios.contains(Scenario.REMOTESTORECONTENTPROVIDER)) {

					if (!useOwn) {
						String remoteName = projectName + "_Remote"; //$NON-NLS-1$
						IProject remote = workspace.getRoot().getProject(remoteName);
						if (!remote.exists()) {
							remote.create(monitor);
						}
						remote.open(monitor);
						sproject.setPersistentProperty(RemoteStoreContentProvider.USE_PROJECT, remoteName);
					} else {
						sproject.setPersistentProperty(RemoteStoreContentProvider.USE_PROJECT, null);
					}

					RemoteStore store = (RemoteStore) project.getAdapter(RemoteStore.class);
					RemoteFolder first = store.getRootFolder().addFolder("First"); //$NON-NLS-1$

					byte[] contents;

					try {
						contents = "Hello, world".getBytes(store.getDefaultCharset()); //$NON-NLS-1$
					} catch (UnsupportedEncodingException e) {
						// $JL-EXC$ ignore here
						contents = "Hello, world".getBytes(); //$NON-NLS-1$
					}

					store.getRootFolder().addFile("File1", contents, System.currentTimeMillis());//$NON-NLS-1$
					RemoteFolder second = first.addFolder("Second");//$NON-NLS-1$
					second.addFile("File1", contents, System.currentTimeMillis());//$NON-NLS-1$ 

					store.serialize(monitor);

				}

				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

			}

		};

		try {
			workspace.run(myRunnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);
		} catch (CoreException e) {
			SemanticResourcesPluginExamples.getDefault().getLog().log(e.getStatus());
			return false;
		}

		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// nothing to initialize

	}

	@SuppressWarnings("nls")
	void copyMimes(IProgressMonitor monitor, File tempFolder) throws CoreException {
		File mimesFolder = new File(tempFolder.getPath() + "/mimes");
		mimesFolder.mkdir();

		File compositeFolder = new File(tempFolder.getPath() + "/composites");
		compositeFolder.mkdir();

		copyMime(mimesFolder, "/mimes/wsdl+xsd/address.xsd", "address.xsd", monitor);
		copyMime(mimesFolder, "/mimes/wsdl+xsd/customer.xsd", "customer.xsd", monitor);
		copyMime(mimesFolder, "/mimes/wsdl+xsd/PurchaseOrder.wsdl", "PurchaseOrder.wsdl", monitor);
		copyMime(mimesFolder, "/mimes/wsdl+xsd/SalesOrder.wsdl", "SalesOrder.wsdl", monitor);
		copyMime(compositeFolder, "/mimes/composite/aaa.txt", "aaa.txt", monitor);
		copyMime(compositeFolder, "/mimes/composite/test.txt", "test.txt", monitor);
		copyMime(compositeFolder, "/mimes/composite/test_en.txt", "test_en.txt", monitor);
		copyMime(compositeFolder, "/mimes/composite/test_en_US.txt", "test_en_US.txt", monitor);
		copyMime(compositeFolder, "/mimes/composite/test2.txt", "test2.txt", monitor);

	}

	private void copyMime(File mimesFolder, String source, String target, IProgressMonitor monitor) throws CoreException {
		InputStream is = SemanticResourcesPluginExamples.getDefault().getClass().getResourceAsStream(source);
		File newFile = new File(mimesFolder.getPath() + "/" + target); //$NON-NLS-1$
		try {
			newFile.createNewFile();
			Util.transferStreams(is, new FileOutputStream(newFile), monitor);
		} catch (FileNotFoundException e) {
			// $JL-EXC$ ignore
			throw new CoreException(new Status(IStatus.ERROR, SemanticResourcesPluginExamples.PLUGIN_ID, e.getMessage()));
		} catch (IOException e) {
			// $JL-EXC$ ignore
			throw new CoreException(new Status(IStatus.ERROR, SemanticResourcesPluginExamples.PLUGIN_ID, e.getMessage()));
		}

	}

}
