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
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
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
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.resources.semantic.ISemanticResourceInfo;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.examples.remote.RemoteFile;
import org.eclipse.core.resources.semantic.examples.remote.RemoteFolder;
import org.eclipse.core.resources.semantic.examples.remote.RemoteStore;
import org.eclipse.core.resources.semantic.examples.remote.RemoteStoreTransient;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.core.RepositoryProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Base class for content provider tests
 * <p>
 * Creates a test project an initializes the remote repository
 * 
 */
public abstract class TestsContentProviderBase extends TestsContentProviderUtil {
	IWorkspace workspace = ResourcesPlugin.getWorkspace();

	/**
	 * The constructor
	 * 
	 * @param autoRefresh
	 *            if auto-refresh should be used
	 * @param projectName
	 *            the test project name
	 * @param providerName
	 *            the id of the content provider
	 */
	public TestsContentProviderBase(boolean autoRefresh, String projectName, String providerName) {
		super(autoRefresh, projectName, providerName);
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		TestsContentProviderUtil.beforeClass();
	}

	public static void afterClass() throws Exception {
		TestsContentProviderUtil.afterClass();
	}

	public abstract RemoteFile getRemoteFile();

	@Override
	@Before
	public void beforeMethod() throws Exception {

		final IProject project = workspace.getRoot().getProject(this.projectName);

		if (project.exists()) {
			throw new IllegalStateException("Project exists");
		}

		IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IProjectDescription description = workspace.newProjectDescription(TestsContentProviderBase.this.projectName);

				try {
					description.setLocationURI(new URI(ISemanticFileSystem.SCHEME + ":/" + TestsContentProviderBase.this.projectName));
				} catch (URISyntaxException e) {
					// really not likely, though
					throw new RuntimeException(e);
				}
				project.create(description, monitor);
				project.open(monitor);

				// for SFS, we map this to the team provider
				RepositoryProvider.map(project, ISemanticFileSystem.SFS_REPOSITORY_PROVIDER);

				ISemanticProject spr = (ISemanticProject) project.getAdapter(ISemanticProject.class);

				RemoteStoreTransient store = (RemoteStoreTransient) project.getAdapter(RemoteStoreTransient.class);
				store.reset();
				RemoteFolder f1 = store.getRootFolder().addFolder("Folder1");
				f1.addFolder("Folder11");

				try {
					f1.addFile("File1", "Hello".getBytes("UTF-8"), store.newTime());
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}

				Map<QualifiedName, String> properties = new HashMap<QualifiedName, String>();
				properties.put(TEMPLATE_PROP, "World");

				spr.addFolder("root", TestsContentProviderBase.this.providerName, properties, TestsContentProviderBase.this.options,
						monitor);

				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

			}

		};

		workspace.run(myRunnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);

		this.testProject = project;

		String projectName1 = this.testProject.getName();
		String[] roots = ((ISemanticFileSystem) EFS.getFileSystem(ISemanticFileSystem.SCHEME)).getRootNames();
		for (String root : roots) {
			if (root.equals(projectName1)) {
				return;
			}
		}
		Assert.fail("Project should be in the list of root names");

	}

	@Override
	@After
	public void afterMethod() throws Exception {

		RemoteStoreTransient store = (RemoteStoreTransient) this.testProject.getAdapter(RemoteStoreTransient.class);
		store.reset();

		final IProject project = workspace.getRoot().getProject(this.projectName);

		this.testProject = null;

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				project.delete(true, monitor);

				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

			}
		};

		workspace.run(runnable, new NullProgressMonitor());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTemplateProperties() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		ISemanticFileStore store = (ISemanticFileStore) EFS.getStore(root.getLocationURI());
		Assert.assertTrue(store.getPersistentProperties().containsKey(TEMPLATE_PROP));

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddFileFromRemoteAndDeleteFolder() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");
		Assert.assertEquals("Folder existence", false, parent.exists());

		final IFile file = parent.getFile("File1");
		Assert.assertEquals("File existence", false, file.exists());

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sfr = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
				ISemanticFile sfile = sfr.addFile("File1", TestsContentProviderBase.this.options, monitor);
				Assert.assertTrue(sfile.getAdaptedFile().equals(file));
				if (!TestsContentProviderBase.this.autoRefresh) {
					Assert.assertEquals("File existence", false, file.exists());
					file.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}

				Assert.assertEquals("File existence", true, file.exists());
				InputStream is = null;
				try {
					is = sfile.getAdaptedFile().getContents();
					try {
						Assert.assertTrue("Too few bytes available", is.available() > 0);
					} catch (IOException e) {
						// $JL-EXC$
						Assert.fail(e.getMessage());
					}
				} finally {
					Util.safeClose(is);
				}
			}
		};

		// we need to use the project since the root folder is not yet synched
		workspace.run(runnable, this.testProject, 0, new NullProgressMonitor());

		Assert.assertEquals("File existence", true, file.exists());
		Assert.assertEquals("Folder existence", true, parent.exists());

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sfr = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
				try {
					sfr.addResource("File1", TestsContentProviderBase.this.options, monitor);
					// TODO restore after content providers have implemented
					// validateLocalDelete
					// Assert.fail("Exception should have been thrown for adding the same resource");
				} catch (Exception e) {
					// $JL-EXC$ expected
				}

				try {
					sfr.addResource("notexisting", TestsContentProviderBase.this.options, monitor);
					Assert.fail("Exception should have been thrown for adding nonexisting resource");
				} catch (CoreException e) {
					// $JL-EXC$ expected
					// TODO fix test
					// String test = e.getStatus().toString();
					// Assert.assertTrue("Status as String should contain path",
					// test.contains(sfr.getAdaptedContainer()
					// .getProjectRelativePath().toString()));
				}

				// TODO restore after content provider implement
				// validateLocalDelete
				// try {
				// file.delete(false, monitor);
				// Assert.fail("Exception should have been thrown for local delete of semantic resource");
				// } catch (OperationCanceledException e) {
				// // $JL-EXC$ expected
				// // reset canceled:
				// monitor.setCanceled(false);
				// }

				try {
					IFolder test = TestsContentProviderBase.this.testProject.getFolder("FileTarget");
					file.move(test.getFullPath(), false, monitor);
					Assert.fail("Exception should have been thrown for local move of semantic file");
				} catch (OperationCanceledException e) {
					// $JL-EXC$ expected
					// reset canceled
					monitor.setCanceled(false);
				}
				// // TODO problems due to copying in
				// RESTContentProvider.openInputStreamInternal with folders
				// try {
				// IFolder test =
				// TestsContentProviderBase.this.testProject.getFolder("FolderTarget");
				// parent.move(test.getFullPath(), false, monitor);
				// Assert.fail("Exception should have been thrown for local move of semantic folder");
				// } catch (OperationCanceledException e) {
				// // $JL-EXC$ expected
				// // reset canceled
				// monitor.setCanceled(false);
				// }

			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		Assert.assertTrue("File should exist", file.exists());

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sf = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
				sf.remove(TestsContentProviderBase.this.options, monitor);
				if (!TestsContentProviderBase.this.autoRefresh) {
					Assert.assertEquals("File existence", true, file.exists());
					file.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
				Assert.assertEquals("File existence", false, file.exists());
			}
		};

		workspace.run(runnable, workspace.getRuleFactory().refreshRule(parent), 0, new NullProgressMonitor());

		Assert.assertFalse("File should not exist", file.exists());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddFileAndImplicitFolderLocalFlag() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");
		Assert.assertEquals("Folder existence", false, parent.exists());

		final IFile file = parent.getFile("File1");
		Assert.assertEquals("File existence", false, file.exists());

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sfr = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
				ISemanticFile sfile = sfr.addFile("File1", TestsContentProviderBase.this.options, monitor);
				Assert.assertTrue(sfile.getAdaptedFile().equals(file));
				if (!TestsContentProviderBase.this.autoRefresh) {
					Assert.assertEquals("File existence", false, file.exists());
					file.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}

			}
		};

		workspace.run(runnable, workspace.getRuleFactory().refreshRule(parent), 0, new NullProgressMonitor());

		Assert.assertEquals("File existence", true, file.exists());
		Assert.assertEquals("Folder existence", true, parent.exists());

		ISemanticFolder sFolder = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
		ISemanticResourceInfo info = sFolder.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_LOCAL_ONLY, null);
		Assert.assertTrue("Folder should be local", info.isLocalOnly());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddFileAndHandleProperties() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");
		Assert.assertEquals("Folder existence", false, parent.exists());

		final IFile file = parent.getFile("File1");
		Assert.assertEquals("File existence", false, file.exists());

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sfr = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
				ISemanticFile sfile = sfr.addFile("File1", TestsContentProviderBase.this.options, monitor);
				Assert.assertTrue(sfile.getAdaptedFile().equals(file));
				if (!TestsContentProviderBase.this.autoRefresh) {
					Assert.assertEquals("File existence", false, file.exists());
					file.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);

				}
				Assert.assertEquals("File existence", true, file.exists());
				InputStream is = null;
				try {
					is = sfile.getAdaptedFile().getContents();
					try {
						Assert.assertTrue("Too few bytes available", is.available() > 0);
					} catch (IOException e) {
						// $JL-EXC$
						Assert.fail(e.getMessage());
					}
				} finally {
					Util.safeClose(is);
				}
			}
		};

		// we need to use the project since the root folder is not yet synched
		workspace.run(runnable, this.testProject, 0, new NullProgressMonitor());

		Assert.assertEquals("File existence", true, file.exists());
		Assert.assertEquals("Folder existence", true, parent.exists());

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sfile = (ISemanticFile) file.getAdapter(ISemanticFile.class);
				sfile.setPersistentProperty(DUMMY_PROP, "Some Value");
				Assert.assertNotNull("Property should not be null", sfile.getPersistentProperty(DUMMY_PROP));
				Assert.assertTrue("Map should contain the property", sfile.getPersistentProperties().containsKey(DUMMY_PROP));
				Assert.assertFalse("Map should not contain the property", sfile.getPersistentProperties().containsKey(DUMMY_PROP2));
				sfile.setPersistentProperty(DUMMY_PROP2, "Some other Value");
				Assert.assertTrue("Map should contain the property", sfile.getPersistentProperties().containsKey(DUMMY_PROP2));
				sfile.setPersistentProperty(DUMMY_PROP2, null);
				Assert.assertFalse("Map should not contain the property", sfile.getPersistentProperties().containsKey(DUMMY_PROP2));
				sfile.setPersistentProperty(DUMMY_PROP, null);
				Assert.assertFalse("Map should not contain the property", sfile.getPersistentProperties().containsKey(DUMMY_PROP));
				sfile.setPersistentProperty(DUMMY_PROP, null);
				Assert.assertNull("Property should be null", sfile.getPersistentProperty(DUMMY_PROP));
			}
		};

		workspace.run(runnable, workspace.getRuleFactory().modifyRule(file), 0, new NullProgressMonitor());

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sfile = (ISemanticFile) file.getAdapter(ISemanticFile.class);
				sfile.setSessionProperty(DUMMY_PROP, "Some Value");
				Assert.assertNotNull("Property should not be null", sfile.getSessionProperty(DUMMY_PROP));
				Assert.assertTrue("Map should contain the property", sfile.getSessionProperties().containsKey(DUMMY_PROP));
				Assert.assertFalse("Map should not contain the property", sfile.getSessionProperties().containsKey(DUMMY_PROP2));
				sfile.setSessionProperty(DUMMY_PROP2, "Some other Value");
				Assert.assertTrue("Map should contain the property", sfile.getSessionProperties().containsKey(DUMMY_PROP2));
				sfile.setSessionProperty(DUMMY_PROP2, null);
				Assert.assertFalse("Map should not contain the property", sfile.getSessionProperties().containsKey(DUMMY_PROP2));
				sfile.setSessionProperty(DUMMY_PROP, null);
				Assert.assertFalse("Map should not contain the property", sfile.getSessionProperties().containsKey(DUMMY_PROP));
				sfile.setSessionProperty(DUMMY_PROP, null);
				Assert.assertNull("Property should be null", sfile.getSessionProperty(DUMMY_PROP));
			}
		};

		workspace.run(runnable, workspace.getRuleFactory().modifyRule(file), 0, new NullProgressMonitor());

		// TODO project close/open should drop session props
		// runnable = new IWorkspaceRunnable() {
		//
		// @Override
		// public void run(IProgressMonitor monitor) throws CoreException {
		// ISemanticFile sfile = (ISemanticFile)
		// file.getAdapter(ISemanticFile.class);
		// sfile.setSessionProperty(DUMMY_PROP, "Some Value");
		// Assert.assertNotNull("Property should not be null",
		// sfile.getSessionProperty(DUMMY_PROP));
		//
		// TestsContentProviderBase.this.testProject.close(monitor);
		//
		// TestsContentProviderBase.this.testProject.open(monitor);
		//
		// Assert.assertNull("Property should be null",
		// sfile.getSessionProperty(DUMMY_PROP));
		//
		// }
		// };
		//
		// workspace.run(runnable, new
		// NullProgressMonitor());

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
				sf.remove(TestsContentProviderBase.this.options, null);
				if (!TestsContentProviderBase.this.autoRefresh) {
					Assert.assertEquals("File existence", true, file.exists());
					file.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
				Assert.assertEquals("File existence", false, file.exists());
			}
		};

		workspace.run(runnable, workspace.getRuleFactory().deleteRule(file), 0, new NullProgressMonitor());

		Assert.assertFalse("File should not exist", file.exists());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTimestamp() throws Exception {

		final IFolder root = this.testProject.getFolder("root");

		final IFolder parent = root.getFolder("Folder1");

		final IFile file = parent.getFile("File1");
		Assert.assertFalse(file.exists());

		final ISemanticFolder sf = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				sf.addFile("File1", TestsContentProviderBase.this.options, null);
				if (!TestsContentProviderBase.this.autoRefresh) {
					Assert.assertEquals("File existence", false, file.exists());
					sf.getAdaptedContainer().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
				Assert.assertEquals("File existence", true, file.exists());
			}
		};

		workspace.run(runnable, workspace.getRuleFactory().refreshRule(parent), 0, new NullProgressMonitor());

		Assert.assertEquals("File existence", true, file.exists());

		ISemanticFileStore store = (ISemanticFileStore) EFS.getStore(file.getLocationURI());
		long stamp = store.fetchInfo().getLastModified();

		Assert.assertEquals("Wrong date", getRemoteFile().getTimestamp(), stamp);

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				sf.getResource("File1").remove(TestsContentProviderBase.this.options, monitor);
				if (!TestsContentProviderBase.this.autoRefresh) {
					Assert.assertEquals("File existence", true, file.exists());
					sf.getAdaptedContainer().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
				Assert.assertEquals("File existence", false, file.exists());

			}
		};

		workspace.run(runnable, new NullProgressMonitor());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSetTimestamp() throws Exception {

		final IFolder root = this.testProject.getFolder("root");

		final IFolder parent = root.getFolder("Folder1");

		final IFile file = parent.getFile("File1");
		Assert.assertFalse(file.exists());

		final ISemanticFolder sf = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile rfile = sf.addFile("File1", TestsContentProviderBase.this.options, null);
				if (!TestsContentProviderBase.this.autoRefresh) {
					Assert.assertEquals("File existence", false, file.exists());
					sf.getAdaptedContainer().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
				Assert.assertEquals("File existence", true, file.exists());

				long localTime = rfile.getAdaptedFile().getLocalTimeStamp();
				RemoteStoreTransient rstore = (RemoteStoreTransient) TestsContentProviderBase.this.testProject
						.getAdapter(RemoteStoreTransient.class);
				long newTime = rstore.newTime();

				rfile.getAdaptedFile().setLocalTimeStamp(newTime);
				if (!TestsContentProviderBase.this.autoRefresh) {
					sf.getAdaptedContainer().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}

				ISemanticFileStore store = (ISemanticFileStore) EFS.getStore(file.getLocationURI());
				long stamp = store.fetchInfo().getLastModified();

				Assert.assertFalse("Timestamps should differ", localTime == newTime);

				Assert.assertEquals("Timstamps should not differ", stamp, newTime);

			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				sf.getResource("File1").remove(TestsContentProviderBase.this.options, monitor);
				if (!TestsContentProviderBase.this.autoRefresh) {
					Assert.assertEquals("File existence", true, file.exists());
					sf.getAdaptedContainer().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
				Assert.assertEquals("File existence", false, file.exists());

			}
		};

		workspace.run(runnable, new NullProgressMonitor());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExistence() throws Exception {

		IFolder root = this.testProject.getFolder("root");

		IFolder parent = root.getFolder("Folder1");

		final IFile file = parent.getFile("File1");
		Assert.assertFalse(file.exists());

		final IFile nofile = parent.getFile("NoFile");
		Assert.assertFalse(file.exists());

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
				Assert.assertTrue("Should exist remotely", sf.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_EXISTS_REMOTELY, monitor)
						.existsRemotely());

				sf = (ISemanticFile) nofile.getAdapter(ISemanticFile.class);
				Assert.assertFalse("Should not exist remotely",
						sf.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_EXISTS_REMOTELY, monitor).existsRemotely());
			}
		};

		workspace.run(runnable, new NullProgressMonitor());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLockAndUnlock() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");

		final IFile file = parent.getFile("File1");

		final ISemanticFolder sf = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {

				ISemanticFile sfile = sf.addFile("File1", TestsContentProviderBase.this.options, null);
				if (!TestsContentProviderBase.this.autoRefresh) {
					Assert.assertEquals("File existence", false, file.exists());
					sf.getAdaptedContainer().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
				Assert.assertEquals("File existence", true, file.exists());

				ISemanticResourceInfo info = sfile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_LOCKING_SUPPORTED, monitor);
				if (!info.isLockingSupported()) {
					return;
				}

				Assert.assertFalse("Should not be locked", sfile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_LOCKED, monitor)
						.isLocked());
				sfile.lockResource(TestsContentProviderBase.this.options, monitor);
				Assert.assertTrue("Should be locked", sfile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_LOCKED, monitor).isLocked());
				sfile.unlockResource(TestsContentProviderBase.this.options, monitor);
				Assert.assertFalse("Should not be locked", sfile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_LOCKED, monitor)
						.isLocked());
			}
		};

		ISchedulingRule rule = workspace.getRuleFactory().refreshRule(parent);
		workspace.run(runnable, rule, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddResourceFromRemoteAndAddMarker() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");

		final IFile file = parent.getFile("File1");
		// Assert.assertTrue("Folder should exist", parent.exists());
		Assert.assertFalse("File should not exist", file.exists());

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sfolder = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
				ISemanticResource filres = sfolder.addResource("File1", TestsContentProviderBase.this.options, monitor);
				Assert.assertTrue(filres instanceof ISemanticFile);
				if (!TestsContentProviderBase.this.autoRefresh) {
					Assert.assertEquals("File existence", false, file.exists());
					file.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
				Assert.assertEquals("File existence", true, file.exists());
			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		Assert.assertEquals("File existence", true, file.exists());

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				IMarker marker = file.createMarker(IMarker.PROBLEM);
				marker.setAttribute(IMarker.MESSAGE, "Some message");
			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
				sf.remove(TestsContentProviderBase.this.options, null);
				if (!TestsContentProviderBase.this.autoRefresh) {
					Assert.assertEquals("File existence", true, file.exists());
					file.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
				Assert.assertEquals("File existence", false, file.exists());
			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		Assert.assertEquals("File existence", false, file.exists());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddFolderFromRemote() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");

		final IFolder folder = parent.getFolder("Folder11");
		// Assert.assertTrue("Folder should exist", parent.exists());
		Assert.assertEquals("Folder existence", false, folder.exists());

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sfolder = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
				sfolder.addFolder("Folder11", TestsContentProviderBase.this.options, monitor);
				if (!TestsContentProviderBase.this.autoRefresh) {
					Assert.assertEquals("File existence", false, folder.exists());
					folder.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
				Assert.assertEquals("File existence", true, folder.exists());
			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		Assert.assertEquals("Folder existence", true, folder.exists());

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sf = (ISemanticFolder) folder.getAdapter(ISemanticFolder.class);
				sf.remove(TestsContentProviderBase.this.options, null);
				if (!TestsContentProviderBase.this.autoRefresh) {
					Assert.assertEquals("File existence", true, folder.exists());
					folder.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
				Assert.assertEquals("File existence", false, folder.exists());
			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		Assert.assertEquals("Folder existence", false, folder.exists());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testChangeFileContentLocalAndRevert() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");

		final IFile file = parent.getFile("File1");
		Assert.assertFalse("File should not exist", file.exists());

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sfr = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
				sfr.addFile("File1", TestsContentProviderBase.this.options, monitor);
				if (!TestsContentProviderBase.this.autoRefresh) {
					file.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
				Assert.assertEquals("File existence", true, file.exists());
			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		assertContentsEqual(file, "Hello");

		long oldStamp = file.getModificationStamp();

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
					IStatus stat = sf.validateEdit(null);

					Assert.assertTrue("ValidateEdit should have returned OK", stat.isOK());

					file.setContents(new ByteArrayInputStream("NewString".getBytes("UTF-8")), IResource.KEEP_HISTORY, monitor);

				} catch (UnsupportedEncodingException e) {
					// $JL-EXC$
					Assert.fail(e.getMessage());
				}
			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		assertContentsEqual(file, "NewString");

		long newStamp = file.getModificationStamp();

		Assert.assertFalse("Timestamp should differ", oldStamp == newStamp);

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
					IStatus stat = sf.validateEdit(null);

					Assert.assertTrue("ValidateEdit should have returned OK", stat.isOK());

					file.appendContents(new ByteArrayInputStream("Appended".getBytes("UTF-8")), IResource.KEEP_HISTORY, monitor);

					if (!TestsContentProviderBase.this.autoRefresh) {
						file.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
					}

				} catch (UnsupportedEncodingException e) {
					// $JL-EXC$
					Assert.fail(e.getMessage());
				}
			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		assertContentsEqual(file, "NewStringAppended");

		workspace.run(runnable, new NullProgressMonitor());

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
				sf.revertChanges(TestsContentProviderBase.this.options, monitor);
				if (!TestsContentProviderBase.this.autoRefresh) {
					file.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		assertContentsEqual(file, "Hello");

		long revertedStamp = file.getModificationStamp();

		Assert.assertFalse("Timestamp should have been reverted", oldStamp == revertedStamp);

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
				sf.remove(TestsContentProviderBase.this.options, null);
				if (!TestsContentProviderBase.this.autoRefresh) {
					file.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		Assert.assertEquals("File existence", false, file.exists());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testChangeFileContentRemote() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");

		final IFile file = parent.getFile("File1");
		Assert.assertFalse("File should not exist", file.exists());

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sfr = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
				sfr.addFile("File1", TestsContentProviderBase.this.options, monitor);
				if (!TestsContentProviderBase.this.autoRefresh) {
					file.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		Assert.assertTrue("File should exist", file.exists());

		assertContentsEqual(file, "Hello");

		Assert.assertEquals("Wrong remote content", "Hello", new String(getRemoteFile().getContent(), "UTF-8"));

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
				IStatus stat = sf.validateEdit(null);

				Assert.assertTrue("ValidateEdit should have returned OK", stat.isOK());

				try {
					file.setContents(new ByteArrayInputStream("NewString".getBytes("UTF-8")), IResource.KEEP_HISTORY,
							new NullProgressMonitor());
					sf.synchronizeContentWithRemote(SyncDirection.OUTGOING, TestsContentProviderBase.this.options, monitor);
					if (getRemoteFile().getStore() instanceof RemoteStore) {
						((RemoteStore) getRemoteFile().getStore()).serialize(monitor);
					}

					if (!TestsContentProviderBase.this.autoRefresh) {
						file.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
					}

				} catch (UnsupportedEncodingException e) {
					// $JL-EXC$
					throw new RuntimeException(e.getMessage());
				}

			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		assertContentsEqual(file, "NewString");

		Assert.assertEquals("Wrong remote content", "NewString", new String(getRemoteFile().getContent(), "UTF-8"));

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
				sf.synchronizeContentWithRemote(SyncDirection.OUTGOING, TestsContentProviderBase.this.options, monitor);
			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
				sf.remove(TestsContentProviderBase.this.options, null);
				if (!TestsContentProviderBase.this.autoRefresh) {
					file.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		Assert.assertFalse("File should not exist", file.exists());

	}

	/**
	 * TODO should this call synchronizeStateWithRemoate?
	 * 
	 * @throws Exception
	 */
	@Test
	public void testChangeFileStateRemote() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");

		final IFile file = parent.getFile("File1");
		Assert.assertFalse("File should not exist", file.exists());

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sfr = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
				sfr.addFile("File1", TestsContentProviderBase.this.options, monitor);
				if (!TestsContentProviderBase.this.autoRefresh) {
					file.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		Assert.assertTrue("File should exist", file.exists());

		ISemanticFile sfile = (ISemanticFile) file.getAdapter(ISemanticFile.class);

		if (!sfile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_LOCKING_SUPPORTED, null).isLockingSupported()) {
			return;
		}

		Assert.assertEquals("Wrong lock state", false, sfile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_LOCKED, null).isLocked());

		getRemoteFile().setLocked(true);

		Assert.assertEquals("Wrong lock state", false, sfile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_LOCKED, null).isLocked());

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);

				sf.synchronizeContentWithRemote(SyncDirection.INCOMING, ISemanticFileSystem.NONE, monitor);

			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		Assert.assertEquals("Wrong lock state", true, sfile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_LOCKED, null).isLocked());

		getRemoteFile().setLocked(false);

		Assert.assertEquals("Wrong lock state", true, sfile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_LOCKED, null).isLocked());

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);

				sf.synchronizeContentWithRemote(SyncDirection.INCOMING, ISemanticFileSystem.NONE, monitor);

			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		Assert.assertEquals("Wrong lock state", false, sfile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_LOCKED, null).isLocked());

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
				sf.remove(TestsContentProviderBase.this.options, null);
				if (!TestsContentProviderBase.this.autoRefresh) {
					file.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		Assert.assertFalse("File should not exist", file.exists());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateAndDeleteRemote() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");

		final ISemanticFolder sf = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					sf.createFileRemotely("NewFromClient", new ByteArrayInputStream("RemoteContent".getBytes("UTF-8")), null,
							TestsContentProviderBase.this.options, monitor);
					sf.createResourceRemotely("NewFromClient2", null, TestsContentProviderBase.this.options, monitor);
					if (!TestsContentProviderBase.this.autoRefresh) {
						sf.getAdaptedContainer().refreshLocal(0, monitor);
					}

				} catch (UnsupportedEncodingException e) {
					// $JL-EXC$
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		Assert.assertTrue("Child should be available", sf.hasResource("NewFromClient"));

		final ISemanticResource res = sf.getResource("NewFromClient");
		final ISemanticResource res2 = sf.getResource("NewFromClient2");

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				res.deleteRemotely(TestsContentProviderBase.this.options, monitor);
				res2.deleteRemotely(TestsContentProviderBase.this.options, monitor);
				if (!TestsContentProviderBase.this.autoRefresh) {
					sf.getAdaptedContainer().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			}
		};

		workspace.run(runnable, new NullProgressMonitor());

		Assert.assertFalse("Child should not be available", sf.hasResource("NewFromClient"));

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSetReadOnly() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");
		final IFile file = parent.getFile("File1");
		final ISemanticFolder sf = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sfile = sf.addFile("File1", TestsContentProviderBase.this.options, monitor);

				Assert.assertTrue("File should be read-only", sfile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_READ_ONLY, null)
						.isReadOnly());

				ISemanticFileStore store = (ISemanticFileStore) EFS.getStore(file.getLocationURI());
				IFileInfo info = new FileInfo();
				info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, false);
				store.putInfo(info, EFS.SET_ATTRIBUTES, null);

				Assert.assertFalse("File should not be read-only",
						sfile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_READ_ONLY, null).isReadOnly());

			}
		};
		workspace.run(runnable, new NullProgressMonitor());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testValidateEdit() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");
		IFile file = parent.getFile("File1");

		final ISemanticFolder sf = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
		final IStatus[] result = new IStatus[1];
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sfile = sf.addFile("File1", TestsContentProviderBase.this.options, monitor);
				result[0] = sfile.validateEdit(monitor);
				sfile.remove(TestsContentProviderBase.this.options, monitor);
			}
		};

		workspace.run(runnable, new NullProgressMonitor());
		IStatus stat = result[0];
		Assert.assertTrue("ValidateEdit should be ok", stat.getSeverity() == IStatus.OK);

		file = parent.getFile("File2");
		ISemanticFile sfile = (ISemanticFile) file.getAdapter(ISemanticFile.class);
		stat = sfile.validateRemoteDelete(null);
		Assert.assertFalse("ValidateEdit should not be ok", stat.getSeverity() == IStatus.OK);

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testValidateRemoteDelete() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");
		IFile file = parent.getFile("File1");

		final ISemanticFolder sf = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
		final IStatus[] result = new IStatus[1];
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sfile = sf.addFile("File1", TestsContentProviderBase.this.options, monitor);
				result[0] = sfile.validateRemoteDelete(monitor);
				sfile.remove(TestsContentProviderBase.this.options, monitor);
			}
		};

		workspace.run(runnable, new NullProgressMonitor());
		IStatus stat = result[0];
		Assert.assertTrue("ValidateRemoteDelete should be ok", stat.getSeverity() == IStatus.OK);

		file = parent.getFile("File2");

		ISemanticFile sfile = (ISemanticFile) file.getAdapter(ISemanticFile.class);
		stat = sfile.validateRemoteDelete(null);
		Assert.assertFalse("ValidateRemoteDelete should not be ok", stat.getSeverity() == IStatus.OK);

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testValidateSave() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");
		IFile file = parent.getFile("File1");

		final ISemanticFolder sf = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
		final IStatus[] result = new IStatus[1];
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sfile = sf.addFile("File1", TestsContentProviderBase.this.options, monitor);
				sfile.validateEdit(null);
				result[0] = sfile.validateSave();
				sfile.remove(TestsContentProviderBase.this.options, monitor);
			}
		};

		workspace.run(runnable, new NullProgressMonitor());
		IStatus stat = result[0];
		Assert.assertTrue("ValidateSave should be ok", stat.getSeverity() == IStatus.OK);

		file = parent.getFile("File2");

		ISemanticFile sfile = (ISemanticFile) file.getAdapter(ISemanticFile.class);
		stat = sfile.validateSave();
		Assert.assertFalse("ValidateSave should not be ok", stat.getSeverity() == IStatus.OK);

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSemanticFileStoreToUri() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");

		URI uri = EFS.getStore(parent.getLocationURI()).toURI();

		Assert.assertEquals("URIs should be equal", uri, parent.getLocationURI());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConcurrentStoreCreation() throws Exception {
		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");

		ISemanticFileStore parentStore1 = (ISemanticFileStore) EFS.getStore(parent.getLocationURI());
		ISemanticFileStore parentStore2 = (ISemanticFileStore) EFS.getStore(parent.getLocationURI());

		Assert.assertFalse(parentStore1.isExists());
		Assert.assertFalse(parentStore2.isExists());

		parentStore1.mkdir(0, null);

		Assert.assertTrue(parentStore1.isExists());
		Assert.assertTrue(parentStore2.isExists());

		IFile file = parent.getFile("File1");

		ISemanticFileStore fileStore1 = (ISemanticFileStore) EFS.getStore(file.getLocationURI());
		ISemanticFileStore fileStore2 = (ISemanticFileStore) EFS.getStore(file.getLocationURI());

		Assert.assertFalse(fileStore1.isExists());
		Assert.assertFalse(fileStore2.isExists());

		Util.safeClose(fileStore1.openOutputStream(0, null));

		Assert.assertTrue(fileStore1.isExists());
		Assert.assertTrue(fileStore2.isExists());

	}

}
