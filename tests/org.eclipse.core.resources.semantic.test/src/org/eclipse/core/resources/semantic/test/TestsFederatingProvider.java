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
package org.eclipse.core.resources.semantic.test;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.internal.resources.semantic.provider.DefaultContentProvider;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.resources.semantic.ISemanticProject;
import org.eclipse.core.resources.semantic.ISemanticResourceInfo;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.test.provider.FederatedContentProvider;
import org.eclipse.core.resources.semantic.test.provider.FederatingContentProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.RepositoryProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests federation
 * 
 */
public class TestsFederatingProvider {

	private static final String projectName = "TestSFSFederationProject";

	static IProject testProject;
	static final int options = ISemanticFileSystem.SUPPRESS_REFRESH;

	/**
	 * Initialization
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeClass() throws Exception {

		TestsContentProviderUtil.initTrace();

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(projectName);

		IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IProjectDescription description = workspace.newProjectDescription(projectName);

				try {
					description.setLocationURI(new URI(ISemanticFileSystem.SCHEME + ":/" + projectName));
				} catch (URISyntaxException e) {
					// really not likely, though
					throw new RuntimeException(e);
				}
				project.create(description, monitor);
				project.open(monitor);

				// for SFS, we map this to the team provider
				RepositoryProvider.map(project, ISemanticFileSystem.SFS_REPOSITORY_PROVIDER);

				ISemanticProject spr = (ISemanticProject) project.getAdapter(ISemanticProject.class);

				// we add the federating provider to the "root" folder
				spr.addFolder("root", FederatingContentProvider.class.getName(), null, ISemanticFileSystem.NONE, monitor);

				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			}

		};

		workspace.run(myRunnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);

		testProject = project;

	}

	/**
	 * Tear down
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void afterClass() throws Exception {

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(projectName);

		project.delete(true, new NullProgressMonitor());

		TestsContentProviderUtil.resetTrace();

		testProject = null;
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFederationAndImplicitLocalFlag() throws Exception {

		final IFolder federatingFolder = testProject.getFolder(new Path("root/A/B/C"));
		final IFolder restFolder = testProject.getFolder(new Path("root/A/C"));

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder federatingSFolder = (ISemanticFolder) federatingFolder.getAdapter(ISemanticFolder.class);
				ISemanticFile sfile = federatingSFolder.addFile("test", TestsFederatingProvider.this.options, null);
				ISemanticFileStore sstore = (ISemanticFileStore) EFS.getStore(sfile.getAdaptedResource().getLocationURI());
				ISemanticContentProvider cp = sstore.getEffectiveContentProvider();
				// the new file should have the federated content provider
				Assert.assertEquals("Wrong content provider", FederatedContentProvider.class.getName(), cp.getClass().getName());

				sstore = (ISemanticFileStore) EFS.getStore(federatingSFolder.getAdaptedResource().getLocationURI());
				cp = sstore.getEffectiveContentProvider();
				// the folder should have the federated content provider
				Assert.assertEquals("Wrong content provider", FederatedContentProvider.class.getName(), cp.getClass().getName());

				sstore = (ISemanticFileStore) sstore.getParent().getParent();
				cp = sstore.getEffectiveContentProvider();
				// the parent should still have the original one
				Assert.assertEquals("Wrong content provider", FederatingContentProvider.class.getName(), cp.getClass().getName());

				ISemanticFolder restSFolder = (ISemanticFolder) restFolder.getAdapter(ISemanticFolder.class);
				ISemanticFile restSFile;
				try {
					restSFile = restSFolder.addFile("Hi.all", new URI("file:someUri/which/is/long"), TestsFederatingProvider.this.options,
							monitor);
					sstore = (ISemanticFileStore) EFS.getStore(restSFile.getAdaptedResource().getLocationURI());
					cp = sstore.getEffectiveContentProvider();
					// TODO fix this when using another content provider
					Assert.assertEquals("Wrong content provider", DefaultContentProvider.class.getName(), cp.getClass().getName());

					Assert.assertFalse("Resource should not exist " + sfile.getAdaptedResource().getLocationURI(), sfile
							.getAdaptedResource().exists());
					Assert.assertFalse("Resource should not exist " + restSFile.getAdaptedResource().getLocationURI(), restSFile
							.getAdaptedResource().exists());

				} catch (URISyntaxException e) {
					// $JL-EXC$
					Assert.fail(e.getMessage());
					restSFile = null;
				}

				testProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);

				IContainer parentFolder = restFolder.getParent();
				ISemanticFolder parent = (ISemanticFolder) parentFolder.getAdapter(ISemanticFolder.class);

				ISemanticResourceInfo info = parent.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_LOCAL_ONLY, monitor);
				Assert.assertTrue("Should be local-only", info.isLocalOnly());

				Assert.assertTrue("Resource should exist " + sfile.getAdaptedResource().getLocationURI(), sfile.getAdaptedResource()
						.exists());
				// Assert.assertTrue("Resource should exist " +
				// restSFile.getResource().getLocationURI(),
				// restSFile.getResource().exists());

				// Assert.assertFalse("Resource should not exist " +
				// sfile.getResource().getLocationURI(),
				// sfile.getResource().exists());
				// Assert.assertFalse("Resource should not exist " +
				// restSFile.getResource().getLocationURI(),
				// restSFile.getResource()
				// .exists());

				sfile.getAdaptedFile().getContents();
				// restSFile.getFile().getContents();

				Assert.assertTrue("Resource should exist " + sfile.getAdaptedResource().getLocationURI(), sfile.getAdaptedResource()
						.exists());
				// Assert.assertTrue("Resource should exist " +
				// restSFile.getResource().getLocationURI(),
				// restSFile.getResource().exists());

				IFile file = (IFile) sfile.getAdaptedResource();
				IStatus result = ResourcesPlugin.getWorkspace().validateEdit(new IFile[] {file}, Display.getCurrent());

				Assert.assertTrue("Validate Edit should return ok", result.isOK());

				file.setContents(new ByteArrayInputStream(new byte[0]), true, true, monitor);

				// file = (IFile) restSFile.getResource();
				// result = ResourcesPlugin.getWorkspace().validateEdit(new
				// IFile[] { file }, Display.getCurrent());
				// file.setContents(new ByteArrayInputStream(new byte[0]), true,
				// true, monitor);

				Assert.assertTrue("Resource should exist " + sfile.getAdaptedResource().getLocationURI(), sfile.getAdaptedResource()
						.exists());
				// Assert.assertTrue("Resource should exist " +
				// restSFile.getResource().getLocationURI(),
				// restSFile.getResource().exists());

			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

	}

}
