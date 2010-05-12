/*******************************************************************************
 * Copyright (c) 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eduard Bartsch (SAP AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources.semantic.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
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
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestsLinkedResources extends TestsContentProviderUtil {
	private static final String HTTP_TEST_URL = "http://www.eclipse.org";
	private static final String DEFAULT_PROVIDER_ID = "org.eclipse.core.resources.semantic.provider.DefaultContentProvider";
	final IWorkspace workspace = ResourcesPlugin.getWorkspace();

	public TestsLinkedResources() {
		this(true);
	}

	TestsLinkedResources(boolean withAutoRefresh) {
		super(withAutoRefresh, "TestsLinkedResources", null);
	}

	TestsLinkedResources(boolean withAutoRefresh, String projectName, String providerName) {
		super(withAutoRefresh, projectName, providerName);
	}

	@Override
	@Before
	public void beforeMethod() throws Exception {
		final IProject project = workspace.getRoot().getProject(this.projectName);

		if (project.exists()) {
			throw new IllegalStateException("Project exists");
		}

		IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IProjectDescription description = workspace.newProjectDescription(TestsLinkedResources.this.projectName);

				project.create(description, monitor);
				project.open(monitor);
			}
		};

		workspace.run(myRunnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);

		this.testProject = project;

		final IFolder folder = this.testProject.getFolder("test");

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					URI uri = new URI("semanticfs", null, "/" + projectName + "/test", null, null);
					folder.createLink(uri, IResource.ALLOW_MISSING_LOCAL, monitor);
				} catch (URISyntaxException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		};

		workspace.run(runnable, workspace.getRuleFactory().refreshRule(folder), 0, new NullProgressMonitor());
	}

	@Override
	@After
	public void afterMethod() throws Exception {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				testProject.delete(true, monitor);
			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		this.testProject = null;
	}

	@Test
	public void testAddLinkToSFS() throws Exception {
		final IFolder folder = this.testProject.getFolder("test");

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sfolder = (ISemanticFolder) folder.getAdapter(ISemanticFolder.class);

				if (sfolder != null) {
					sfolder.addFolder("test1", null, null, 0, monitor);
				}
			}
		};

		workspace.run(runnable, workspace.getRuleFactory().refreshRule(folder), 0, new NullProgressMonitor());

		Assert.assertTrue(folder.exists());
		Assert.assertTrue(folder.isLinked());
		Assert.assertTrue(folder.getFolder("test1").exists());

		final IFolder folder2 = this.testProject.getFolder("test").getFolder("test1");

		IWorkspaceRunnable runnable2 = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sfolder = (ISemanticFolder) folder2.getAdapter(ISemanticFolder.class);

				if (sfolder != null) {
					sfolder.addFolder("test2", null, null, 0, monitor);
				}
			}
		};

		workspace.run(runnable2, workspace.getRuleFactory().refreshRule(folder2), 0, new NullProgressMonitor());

		Assert.assertTrue(folder2.exists());
		Assert.assertTrue(folder2.getFolder("test2").exists());

		final IFolder folder3 = this.testProject.getFolder("test").getFolder("test3");

		IWorkspaceRunnable runnable3 = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				folder3.create(true, true, monitor);
			}
		};

		workspace.run(runnable3, workspace.getRuleFactory().createRule(folder3), 0, new NullProgressMonitor());

		Assert.assertTrue(folder3.exists());
	}

	@Test
	public void testAddAndRemoveFile() throws Exception {
		final IFolder folder = this.testProject.getFolder("test").getFolder("someFolder");

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sFolder = (ISemanticFolder) folder.getAdapter(ISemanticFolder.class);
				try {
					File testfile = createTestFile("test.txt");
					boolean created = testfile.createNewFile();
					if (!created) {
						new FileOutputStream(testfile).close();
					}

					ISemanticFile sfile = sFolder.addFile("SomeFile", createURI4File(testfile), 0, monitor);

					Util.safeClose(sfile.getAdaptedFile().getContents());

					Assert.assertTrue("Resource should exist " + sfile.getAdaptedResource().getLocationURI(), sfile.getAdaptedResource()
							.exists());

				} catch (URISyntaxException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				}

			}
		};

		workspace.run(runnable, workspace.getRuleFactory().refreshRule(folder), 0, new NullProgressMonitor());

		final IFile file = folder.getFile("SomeFile");

		Assert.assertTrue(file.exists());

		IWorkspaceRunnable runnable2 = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				file.delete(true, monitor);
			}
		};

		workspace.run(runnable2, workspace.getRuleFactory().deleteRule(file), 0, new NullProgressMonitor());

		Assert.assertFalse(file.exists());
	}

	@Test
	public void testAddQueryLinkWithURLToSFS() throws Exception {
		final IFolder folder = this.testProject.getFolder("querytest");

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					URI uri = new URI("semanticfs", null, "/querytest", "type=folder;create=true;uri=" + HTTP_TEST_URL, null);
					folder.createLink(uri, 0, monitor);
					ISemanticFolder sFolder = (ISemanticFolder) folder.getAdapter(ISemanticFolder.class);

					Assert.assertTrue(folder.exists());

					ISemanticProject sProject = (ISemanticProject) folder.getAdapter(ISemanticProject.class);

					Assert.assertNull(sProject);

					ISemanticResourceInfo info = sFolder.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_URI_STRING, monitor);

					Assert.assertEquals(HTTP_TEST_URL, info.getRemoteURIString());
				} catch (URISyntaxException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		};

		workspace.run(runnable, workspace.getRuleFactory().refreshRule(folder), 0, new NullProgressMonitor());
	}

	@Test
	public void testAddFileLinkWithURLToSFS() throws Exception {
		final IFile file = this.testProject.getFile("querytest");

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					URI uri = new URI("semanticfs", null, "/filetest/test.html", "type=file;create=true;uri=" + HTTP_TEST_URL, null);
					file.createLink(uri, 0, monitor);
					ISemanticFile sFile = (ISemanticFile) file.getAdapter(ISemanticFile.class);

					Assert.assertTrue(file.exists());

					ISemanticResourceInfo info = sFile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_URI_STRING, monitor);

					Assert.assertEquals(HTTP_TEST_URL, info.getRemoteURIString());
				} catch (URISyntaxException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		};

		workspace.run(runnable, workspace.getRuleFactory().refreshRule(file), 0, new NullProgressMonitor());
	}

	@Test
	public void testAddQueryLinkWithProviderToSFS() throws Exception {
		final IFolder folder = this.testProject.getFolder("querytest2");

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				URI uri = null;
				try {
					uri = new URI("semanticfs", null, "/querytest2", "type=folder;create=true;provider=" + DEFAULT_PROVIDER_ID + ";uri="
							+ HTTP_TEST_URL, null);
					folder.createLink(uri, 0, monitor);

					Assert.assertTrue(folder.exists());

					ISemanticFolder sFolder = (ISemanticFolder) folder.getAdapter(ISemanticFolder.class);

					Assert.assertEquals(DEFAULT_PROVIDER_ID, sFolder.getContentProviderID());

					ISemanticProject sProject = (ISemanticProject) folder.getAdapter(ISemanticProject.class);

					Assert.assertNull(sProject);

					ISemanticResourceInfo info = sFolder.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_URI_STRING, monitor);

					Assert.assertEquals(HTTP_TEST_URL, info.getRemoteURIString());
				} catch (URISyntaxException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				} finally {
					cleanupSFS(uri, monitor);
				}
			}

		};

		workspace.run(runnable, workspace.getRuleFactory().refreshRule(folder), 0, new NullProgressMonitor());

		final IFolder folder2 = this.testProject.getFolder("querytest3");

		IWorkspaceRunnable runnable2 = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				URI uri = null;
				try {
					uri = new URI("semanticfs", null, "/querytest3/test", "type=folder;create=true;provider=" + DEFAULT_PROVIDER_ID
							+ ";uri=" + HTTP_TEST_URL, null);

					folder2.createLink(uri, 0, monitor);

					Assert.assertTrue(folder2.exists());

					ISemanticFolder sFolder = (ISemanticFolder) folder2.getAdapter(ISemanticFolder.class);

					Assert.assertEquals(DEFAULT_PROVIDER_ID, sFolder.getContentProviderID());

					ISemanticProject sProject = (ISemanticProject) folder2.getAdapter(ISemanticProject.class);

					Assert.assertNull(sProject);

					ISemanticResourceInfo info = sFolder.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_URI_STRING, monitor);

					Assert.assertEquals(HTTP_TEST_URL, info.getRemoteURIString());
				} catch (URISyntaxException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				} finally {
					cleanupSFS(uri, monitor);
				}
			}
		};

		workspace.run(runnable2, workspace.getRuleFactory().refreshRule(folder), 0, new NullProgressMonitor());
	}

	@Test
	public void testRuleFactory() throws Exception {
		final IFolder folder = this.testProject.getFolder("ruletest");

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				URI uri = null;
				try {
					uri = new URI("semanticfs", null, "/rulerest", "type=folder;create=true;provider=" + DEFAULT_PROVIDER_ID, null);
					folder.createLink(uri, 0, monitor);

					Assert.assertTrue(folder.exists());

					ISemanticFolder sFolder = (ISemanticFolder) folder.getAdapter(ISemanticFolder.class);

					sFolder.synchronizeContentWithRemote(SyncDirection.BOTH, options, monitor);
				} catch (URISyntaxException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				} finally {
					cleanupSFS(uri, monitor);
				}
			}

		};

		workspace.run(runnable, workspace.getRuleFactory().refreshRule(folder), 0, new NullProgressMonitor());

	}

	void cleanupSFS(URI uri, IProgressMonitor monitor) throws CoreException {
		if (uri != null) {
			IFileStore store = EFS.getStore(uri);

			if (!store.fetchInfo(EFS.NONE, monitor).exists()) {
				store.delete(options, monitor);
			}
		}
	}
}
