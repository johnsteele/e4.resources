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
package org.eclipse.core.resources.semantic.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.internal.resources.semantic.provider.DefaultContentProvider;
import org.eclipse.core.internal.resources.semantic.provider.InvalidContentProvider;
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
import org.eclipse.core.resources.semantic.test.provider.FederatingContentProvider2;
import org.eclipse.core.resources.semantic.test.provider.FederatingContentProvider3;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.RepositoryProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests federation
 * 
 */
public class TestsFederatingProvider2 extends TestsContentProviderUtil {

	public TestsFederatingProvider2() {
		super(false, "TestSFSFederationProject2", FederatingContentProvider2.class.getName());
	}

	/**
	 * Initialization
	 * 
	 * @throws Exception
	 */
	@Override
	@Before
	public void beforeMethod() throws Exception {

		TestsContentProviderUtil.initTrace();

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(TestsFederatingProvider2.this.projectName);

		IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IProjectDescription description = workspace.newProjectDescription(TestsFederatingProvider2.this.projectName);

				try {
					description.setLocationURI(new URI(ISemanticFileSystem.SCHEME + ":/" + TestsFederatingProvider2.this.projectName));
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
				spr.addFolder("root", FederatingContentProvider2.class.getName(), null, ISemanticFileSystem.NONE, monitor);

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
	@Override
	@After
	public void afterMethod() throws Exception {

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
				ISemanticFile sfile = federatingSFolder.addFile("test", TestsFederatingProvider2.this.options, null);
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
				Assert.assertEquals("Wrong content provider", FederatingContentProvider2.class.getName(), cp.getClass().getName());

				ISemanticFolder restSFolder = (ISemanticFolder) restFolder.getAdapter(ISemanticFolder.class);
				ISemanticFile restSFile;
				try {
					File testfile = createTestFile("test.txt");
					boolean created = testfile.createNewFile();
					if (!created) {
						new FileOutputStream(testfile).close();
					}

					restSFile = restSFolder.addFile("Hi.all", createURI4File(testfile), TestsFederatingProvider2.this.options, monitor);
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
				} catch (IOException e) {
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

				Assert.assertTrue("Resource should exist " + restSFile.getAdaptedResource().getLocationURI(), restSFile
						.getAdaptedResource().exists());

				restSFolder.remove(ISemanticFileSystem.FORCE_REMOVE, monitor);

				Assert.assertFalse("Resource should not exist " + restSFile.getAdaptedResource().getLocationURI(), restSFile
						.getAdaptedResource().exists());
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFederationAndInvalidContentProviderID() throws Exception {

		final IFolder federatingFolder = testProject.getFolder(new Path("root/A/D"));

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder federatingSFolder = (ISemanticFolder) federatingFolder.getAdapter(ISemanticFolder.class);

				try {
					federatingSFolder.addFile("test", TestsFederatingProvider2.this.options, null);
					Assert.assertTrue("should have failed", false);
				} catch (CoreException e) {
					// Ignore
				}

				ISemanticFileStore sstore = (ISemanticFileStore) EFS.getStore(federatingSFolder.getAdaptedResource().getLocationURI());
				ISemanticContentProvider cp = sstore.getEffectiveContentProvider();
				// the folder should have the invalid content provider
				Assert.assertEquals("Wrong content provider", InvalidContentProvider.class.getName(), cp.getClass().getName());

				sstore = (ISemanticFileStore) sstore.getParent().getParent();
				cp = sstore.getEffectiveContentProvider();
				// the parent should still have the original one
				Assert.assertEquals("Wrong content provider", FederatingContentProvider2.class.getName(), cp.getClass().getName());

			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFederationAndInvalidContentProviderID2() throws Exception {

		final IFolder federatingFolder = testProject.getFolder(new Path("root/A/D/C"));

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder federatingSFolder = (ISemanticFolder) federatingFolder.getAdapter(ISemanticFolder.class);

				try {
					federatingSFolder.addFile("test", TestsFederatingProvider2.this.options, null);
					Assert.assertTrue("should have failed", false);
				} catch (CoreException e) {
					// Ignore
				}

				ISemanticFileStore sstore = (ISemanticFileStore) EFS.getStore(federatingSFolder.getAdaptedResource().getLocationURI());
				ISemanticContentProvider cp = sstore.getEffectiveContentProvider();
				// the folder should have the invalid content provider
				Assert.assertEquals("Wrong content provider", InvalidContentProvider.class.getName(), cp.getClass().getName());

				sstore = (ISemanticFileStore) sstore.getParent().getParent();
				cp = sstore.getEffectiveContentProvider();
				// the parent should still have the original one
				Assert.assertEquals("Wrong content provider", FederatingContentProvider2.class.getName(), cp.getClass().getName());

			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRecursiveFederation() throws Exception {

		final IFolder federatingFolder = testProject.getFolder(new Path("root/A/E/X/B"));

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder federatingSFolder = (ISemanticFolder) federatingFolder.getAdapter(ISemanticFolder.class);

				ISemanticFile sFile = federatingSFolder.addFile("test", TestsFederatingProvider2.this.options, null);

				// root/A/E/X/B/test
				ISemanticFileStore sstore = (ISemanticFileStore) EFS.getStore(sFile.getAdaptedResource().getLocationURI());
				ISemanticContentProvider cp = sstore.getEffectiveContentProvider();
				Assert.assertEquals("Wrong content provider", FederatedContentProvider.class.getName(), cp.getClass().getName());

				// root/A/E/X/B
				sstore = (ISemanticFileStore) EFS.getStore(federatingSFolder.getAdaptedResource().getLocationURI());
				cp = sstore.getEffectiveContentProvider();
				Assert.assertEquals("Wrong content provider", FederatedContentProvider.class.getName(), cp.getClass().getName());

				// root/A/E/X
				sstore = (ISemanticFileStore) sstore.getParent();
				cp = sstore.getEffectiveContentProvider();
				Assert.assertEquals("Wrong content provider", FederatingContentProvider3.class.getName(), cp.getClass().getName());

				// root/A/E
				sstore = (ISemanticFileStore) sstore.getParent();
				cp = sstore.getEffectiveContentProvider();
				Assert.assertEquals("Wrong content provider", FederatingContentProvider3.class.getName(), cp.getClass().getName());

				// root/A
				sstore = (ISemanticFileStore) sstore.getParent();
				cp = sstore.getEffectiveContentProvider();
				// the parent should still have the original one
				Assert.assertEquals("Wrong content provider", FederatingContentProvider2.class.getName(), cp.getClass().getName());

				// root
				sstore = (ISemanticFileStore) sstore.getParent();
				cp = sstore.getEffectiveContentProvider();
				Assert.assertEquals("Wrong content provider", FederatingContentProvider2.class.getName(), cp.getClass().getName());
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRecursiveFederation2() throws Exception {

		final IFolder federatingFolder = testProject.getFolder(new Path("root"));

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder federatingSFolder = (ISemanticFolder) federatingFolder.getAdapter(ISemanticFolder.class);

				// root
				ISemanticFileStore sstore = (ISemanticFileStore) EFS.getStore(federatingSFolder.getAdaptedResource().getLocationURI());
				ISemanticContentProvider cp = sstore.getEffectiveContentProvider();
				Assert.assertEquals("Wrong content provider", FederatingContentProvider2.class.getName(), cp.getClass().getName());

				// root/A
				sstore = (ISemanticFileStore) sstore.getChild("A");
				cp = sstore.getEffectiveContentProvider();
				Assert.assertEquals("Wrong content provider", FederatingContentProvider2.class.getName(), cp.getClass().getName());

				// root/A/E
				sstore = (ISemanticFileStore) sstore.getChild("E");
				cp = sstore.getEffectiveContentProvider();
				Assert.assertEquals("Wrong content provider", FederatingContentProvider3.class.getName(), cp.getClass().getName());

				// root/A/E/X
				sstore = (ISemanticFileStore) sstore.getChild("X");
				cp = sstore.getEffectiveContentProvider();
				Assert.assertEquals("Wrong content provider", FederatingContentProvider3.class.getName(), cp.getClass().getName());

				// root/A/E/X/B
				sstore = (ISemanticFileStore) sstore.getChild("B");
				cp = sstore.getEffectiveContentProvider();
				Assert.assertEquals("Wrong content provider", FederatedContentProvider.class.getName(), cp.getClass().getName());

				// root/A/E/X/B/test
				final IFolder federatingFolder2 = testProject.getFolder(new Path("root/A/E/X"));
				ISemanticFolder federatingSFolder2 = (ISemanticFolder) federatingFolder2.getAdapter(ISemanticFolder.class);
				ISemanticFile sFile = federatingSFolder2.addFile("test", TestsFederatingProvider2.this.options, null);

				sstore = (ISemanticFileStore) EFS.getStore(sFile.getAdaptedResource().getLocationURI());
				cp = sstore.getEffectiveContentProvider();
				Assert.assertEquals("Wrong content provider", FederatingContentProvider3.class.getName(), cp.getClass().getName());
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

	}
}
