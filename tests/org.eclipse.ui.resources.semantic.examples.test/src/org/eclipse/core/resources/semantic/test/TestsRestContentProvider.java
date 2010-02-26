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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

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
import org.eclipse.core.resources.semantic.examples.remote.RemoteFolder;
import org.eclipse.core.resources.semantic.examples.remote.RemoteStoreTransient;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.resources.semantic.test.provider.RestTestContentProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.RepositoryProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test the REST content provider
 * 
 */
public class TestsRestContentProvider {

	final String projectName;
	final String providerName;
	private IProject testProject;
	final int options = ISemanticFileSystem.NONE;

	/**
	 * Initializes the trace locations
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeClass() throws Exception {
		TestsContentProviderUtil.initTrace();
	}

	/**
	 * Resets the trace locations
	 * 
	 * @throws Exception
	 */
	public static void afterClass() throws Exception {
		// reset traces
		TestsContentProviderUtil.resetTrace();
	}

	/**
	 * Setup
	 * 
	 * @throws Exception
	 */
	@Before
	public void beforeMethod() throws Exception {

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(this.projectName);

		if (project.exists()) {
			throw new IllegalStateException("Project exists");
		}

		IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IProjectDescription description = workspace.newProjectDescription(TestsRestContentProvider.this.projectName);

				try {
					description.setLocationURI(new URI(ISemanticFileSystem.SCHEME + ":/" + TestsRestContentProvider.this.projectName));
				} catch (URISyntaxException e) {
					// really not likely, though
					throw new RuntimeException(e);
				}
				project.create(description, monitor);
				project.open(monitor);

				RemoteStoreTransient store = (RemoteStoreTransient) project.getAdapter(RemoteStoreTransient.class);
				RemoteFolder f1 = store.getRootFolder().addFolder("Folder1");
				f1.addFolder("Folder11");

				// for SFS, we map this to the team provider
				RepositoryProvider.map(project, ISemanticFileSystem.SFS_REPOSITORY_PROVIDER);

				ISemanticProject spr = (ISemanticProject) project.getAdapter(ISemanticProject.class);

				spr.addFolder("root", TestsRestContentProvider.this.providerName, null, TestsRestContentProvider.this.options, monitor);

				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			}

		};

		workspace.run(myRunnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);

		this.testProject = project;

	}

	/**
	 * Teardown
	 * 
	 * @throws Exception
	 */
	@After
	public void afterMethod() throws Exception {

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(this.projectName);

		RemoteStoreTransient store = (RemoteStoreTransient) project.getAdapter(RemoteStoreTransient.class);
		store.reset();

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				project.delete(true, monitor);
				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		this.testProject = null;
	}

	/**
	 * Constructor
	 */
	public TestsRestContentProvider() {
		this.projectName = "TestRestContentProvider";
		this.providerName = RestTestContentProvider.class.getName();
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddFileWithUri() throws Exception {

		final String googleuri = "http://www.google.de";
		final String dummyuri = "file:someUri/which/is/long";

		IFolder root = this.testProject.getFolder("root");

		final ISemanticFolder sf = (ISemanticFolder) root.getAdapter(ISemanticFolder.class);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {

				ISemanticFile uriFile;
				try {
					uriFile = sf.addFile("file", new URI(dummyuri), TestsRestContentProvider.this.options, monitor);
					sf.getAdaptedResource().refreshLocal(IResource.DEPTH_INFINITE, monitor);
					ISemanticResourceInfo info = uriFile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_EXISTS_REMOTELY, monitor);
					Assert.assertFalse("Remote existence", info.existsRemotely());
					try {
						Util.safeClose(uriFile.getAdaptedFile().getContents());
						Assert.fail("Should have failed");
					} catch (CoreException e) {
						// $JL-EXC$ expected
					}

					uriFile = sf.addFile("file2", new URI(googleuri), TestsRestContentProvider.this.options, monitor);
					sf.getAdaptedResource().refreshLocal(IResource.DEPTH_INFINITE, monitor);

					info = uriFile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_EXISTS_REMOTELY, monitor);
					// TODO this currently fails in the test environment (no
					// proxy)
					// Assert.assertTrue("Remote existence",
					// info.existsRemotely());

					try {
						Util.safeClose(uriFile.getAdaptedFile().getContents());
					} catch (Exception e) {
						// $JL-EXC$
						// TODO this currently fails in the test environemnt (no
						// proxy)
						// Assert.fail("Should not have failed");
					}
				} catch (URISyntaxException e) {
					// $JL-EXC$
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				}

				String uriString = uriFile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_URI_STRING, monitor).getRemoteURIString();

				Assert.assertEquals(googleuri, uriString);

				try {
					IResource[] resources = sf.findURI(new URI(googleuri), monitor);

					Assert.assertEquals(1, resources.length);

					Assert.assertEquals("file2", resources[0].getName());
				} catch (URISyntaxException e) {
					// $JL-EXC$
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				}

			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddFileWithUriChangeSyncInocmingAndRevert() throws Exception {

		IFolder root = this.testProject.getFolder("root");

		final File file = createTestFile("AFile.txt");
		boolean created = file.createNewFile();
		if (!created) {
			new FileOutputStream(file).close();
		}

		final ISemanticFolder sf = (ISemanticFolder) root.getAdapter(ISemanticFolder.class);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {

				ISemanticFile uriFile;
				try {
					uriFile = sf.addFile("file2", createURI4File(file), TestsRestContentProvider.this.options, monitor);
					sf.getAdaptedResource().getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
					assertContentsEqual(uriFile.getAdaptedFile(), "");

					ISemanticResourceInfo info = uriFile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_EXISTS_REMOTELY, monitor);
					Assert.assertTrue("Remote existence", info.existsRemotely());

					FileOutputStream os = null;
					try {
						os = new FileOutputStream(file);
						os.write("Hello World".getBytes("UTF-8"));
						os.close();
					} catch (Exception e) {
						throw new RuntimeException(e);
					} finally {
						Util.safeClose(os);
					}

					assertContentsEqual(uriFile.getAdaptedFile(), "");

					uriFile.synchronizeContentWithRemote(SyncDirection.INCOMING, TestsRestContentProvider.this.options, monitor);

					sf.getAdaptedResource().getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
					assertContentsEqual(uriFile.getAdaptedFile(), "Hello World");

					long firstTime = uriFile.getAdaptedFile().getLocalTimeStamp();

					try {
						// make sure we get another file timestamp
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					try {
						uriFile.validateEdit(null);
						uriFile.getAdaptedFile().setContents(new ByteArrayInputStream("Another world".getBytes("UTF-8")), IResource.NONE,
								monitor);
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(e);
					}

					assertContentsEqual(uriFile.getAdaptedFile(), "Another world");
					long secondTime = uriFile.getAdaptedFile().getLocalTimeStamp();

					Assert.assertTrue("Timstamps should differ", secondTime > firstTime);

					uriFile.revertChanges(TestsRestContentProvider.this.options, monitor);
					sf.getAdaptedResource().getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
					assertContentsEqual(uriFile.getAdaptedFile(), "Hello World");

					long thirdTime = uriFile.getAdaptedFile().getLocalTimeStamp();

					Assert.assertTrue("Timestamp should be the same", thirdTime == firstTime);

					try {
						// make sure we get another file timestamp
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					try {
						uriFile.validateEdit(null);
						uriFile.getAdaptedFile().setContents(new ByteArrayInputStream("Yet another world".getBytes("UTF-8")),
								IResource.NONE, monitor);
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(e);
					}

					assertContentsEqual(uriFile.getAdaptedFile(), "Yet another world");
					long fourthTime = uriFile.getAdaptedFile().getLocalTimeStamp();

					Assert.assertTrue("Timstamps should differ", fourthTime > secondTime);

				} catch (URISyntaxException e) {
					// $JL-EXC$
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				}

			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddFileWithUriChangeSyncOutgoingAndRevert() throws Exception {

		IFolder root = this.testProject.getFolder("root");

		final File file = createTestFile("AFile.txt");
		boolean created = file.createNewFile();
		if (!created) {
			OutputStream os = new FileOutputStream(file);
			os.write("I'm remote".getBytes("UTF-8"));
			os.close();
		}

		final ISemanticFolder sf = (ISemanticFolder) root.getAdapter(ISemanticFolder.class);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {

				ISemanticFile uriFile;

				try {
					uriFile = sf.addFile("file2", createURI4File(file), TestsRestContentProvider.this.options, monitor);
				} catch (URISyntaxException e2) {
					throw new RuntimeException(e2);
				}
				sf.getAdaptedResource().getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);

				assertContentsEqual(uriFile.getAdaptedFile(), "I'm remote");

				long firstTime = uriFile.getAdaptedFile().getLocalTimeStamp();

				try {
					uriFile.validateEdit(null);
					uriFile.getAdaptedFile()
							.setContents(new ByteArrayInputStream("New content".getBytes("UTF-8")), IResource.NONE, monitor);
				} catch (UnsupportedEncodingException e1) {
					throw new RuntimeException(e1);
				}

				assertContentsEqual(file, "I'm remote");
				assertContentsEqual(uriFile.getAdaptedFile(), "New content");

				long filestamp = file.lastModified();

				Assert.assertEquals("Timestamp should be the same", filestamp, firstTime);

				// folder sync
				sf.synchronizeContentWithRemote(SyncDirection.OUTGOING, TestsRestContentProvider.this.options, monitor);
				sf.getAdaptedResource().getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);

				assertContentsEqual(file, "New content");

				long secondTime = uriFile.getAdaptedFile().getLocalTimeStamp();

				Assert.assertTrue("Timstamps should differ", secondTime > filestamp);

				if (Platform.OS_WIN32.equals(Platform.getOS())) {
					Assert.assertEquals("Timestamp should be the same", file.lastModified(), secondTime);
				} else {
					// non-millisecond accuracy on UNIXes
					long millis = secondTime % 1000;
					Assert.assertEquals("Timestamp should be the same", file.lastModified(), secondTime - millis);
				}
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	@Ignore
	// fix this
	public void testCreateFileRemotely() throws Exception {

		final IFolder root = this.testProject.getFolder("root");

		final File file = createTestFile("ANewFile.txt");
		// boolean created = file.createNewFile();

		if (file.exists()) {
			if (!file.delete()) {
				throw new RuntimeException("Could not prepare test");
			}

		}

		final ISemanticFolder sf = (ISemanticFolder) root.getAdapter(ISemanticFolder.class);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile uriFile;
				try {
					uriFile = sf.createFileRemotely("filenew", new ByteArrayInputStream("Hello World".getBytes()), createURI4File(file),
							TestsRestContentProvider.this.options, monitor);
					uriFile.getAdaptedFile().getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);

				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
				sf.getAdaptedResource().getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				assertContentsEqual(uriFile.getAdaptedFile(), "Hello World");

			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

	}

	void assertContentsEqual(IFile file, String test) {
		InputStream is = null;
		try {
			is = file.getContents();
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			Util.safeClose(is);
			Assert.assertEquals("Wrong content", test, new String(buffer, "UTF-8"));

		} catch (Exception e) {
			// $JL-EXC$
			Assert.fail("Exception getting file content: " + e.getMessage());
		}
	}

	void assertContentsEqual(File file, String test) {
		InputStream is = null;
		try {

			is = new FileInputStream(file);
			byte[] buffer = new byte[is.available()];
			is.read(buffer);
			Util.safeClose(is);
			Assert.assertEquals("Wrong content", test, new String(buffer, "UTF-8"));

		} catch (Exception e) {
			// $JL-EXC$
			Assert.fail("Exception getting file content: " + e.getMessage());
		}
	}

	private File createTestFile(String fileName) {
		String tmpdir = System.getProperty("java.io.tmpdir");
		final File file = new File(tmpdir, fileName);
		return file;
	}

	URI createURI4File(File file) throws URISyntaxException {
		String filepath = file.getAbsolutePath().replace('\\', '/');

		// Handle differences between Windows and UNIX
		if (!filepath.startsWith("/")) {
			filepath = "/" + filepath;
		}

		return new URI("file", "", filepath, null);
	}

}
