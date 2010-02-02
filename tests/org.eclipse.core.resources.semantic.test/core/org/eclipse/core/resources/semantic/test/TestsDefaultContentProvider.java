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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.Assert;

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
import org.eclipse.core.resources.semantic.examples.remote.RemoteFolder;
import org.eclipse.core.resources.semantic.examples.remote.RemoteStoreTransient;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.RepositoryProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests the default content provider
 */
public class TestsDefaultContentProvider {

	static final QualifiedName TEMPLATE_PROP = new QualifiedName(TestPlugin.PLUGIN_ID, "Hello");

	final String projectName;
	IProject testProject;
	final int options;
	final boolean autoRefresh;

	/**
	 * The default constructor
	 */
	public TestsDefaultContentProvider() {
		this(true);
	}

	/**
	 * The constructor with auto-refresh flag
	 * 
	 * @param withAutoRefresh
	 */
	TestsDefaultContentProvider(boolean withAutoRefresh) {
		this.projectName = "TestDefaultContentProvider";
		if (withAutoRefresh) {
			this.options = ISemanticFileSystem.NONE;
			this.autoRefresh = true;
		} else {
			this.options = ISemanticFileSystem.SUPPRESS_REFRESH;
			this.autoRefresh = false;
		}
	}

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
	@AfterClass
	public static void afterClass() throws Exception {

		TestsContentProviderUtil.resetTrace();
	}

	/**
	 * Creates a test project and initializes the remote repository
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
				IProjectDescription description = workspace.newProjectDescription(TestsDefaultContentProvider.this.projectName);

				try {
					description.setLocationURI(new URI(ISemanticFileSystem.SCHEME + ":/" + TestsDefaultContentProvider.this.projectName));
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
				if (!TestsDefaultContentProvider.this.autoRefresh) {
					project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			}

		};

		workspace.run(myRunnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);

		this.testProject = project;

	}

	/**
	 * Deletes the test project and resets the remote repository
	 * 
	 * @throws Exception
	 */
	@After
	public void afterMethod() throws Exception {

		RemoteStoreTransient store = (RemoteStoreTransient) this.testProject.getAdapter(RemoteStoreTransient.class);
		store.reset();

		this.testProject = null;

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(this.projectName);
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				project.delete(true, monitor);
				if (!TestsDefaultContentProvider.this.autoRefresh) {
					project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
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
	public void testAddFileAndDelete() throws Exception {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {

				IFile file = TestsDefaultContentProvider.this.testProject.getFile("SomeFile");
				Assert.assertEquals("File existence", false, file.exists());
				ByteArrayInputStream is;
				try {
					is = new ByteArrayInputStream("Hello World".getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
				file.create(is, false, monitor);
				Util.safeClose(file.getContents());
				Assert.assertEquals("File existence", true, file.exists());

				InputStream stream = null;
				try {
					stream = file.getContents();
					byte[] result = new byte[stream.available()];
					stream.read(result);
					String content = new String(result, "UTF-8");
					Assert.assertEquals("Hello World", content);
				} catch (Exception e) {
					throw new RuntimeException(e);

				} finally {
					Util.safeClose(stream);
				}
				file.delete(false, monitor);

			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, this.testProject, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddFileAndCopy() throws Exception {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {

				IFile file = TestsDefaultContentProvider.this.testProject.getFile("SomeFile");

				Assert.assertEquals("File existence", false, file.exists());
				ByteArrayInputStream is;
				try {
					is = new ByteArrayInputStream("Hello World".getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
				file.create(is, false, monitor);
				Util.safeClose(file.getContents());
				Assert.assertEquals("File existence", true, file.exists());

				file.copy(TestsDefaultContentProvider.this.testProject.getFullPath().append("CopyTarget"), false, monitor);

				InputStream stream = null;
				try {
					stream = file.getContents();
					byte[] result = new byte[stream.available()];
					stream.read(result);
					String content = new String(result, "UTF-8");
					Assert.assertEquals("Hello World", content);
				} catch (Exception e) {
					throw new RuntimeException(e);

				} finally {
					Util.safeClose(stream);
				}
				file.delete(false, monitor);

			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, this.testProject, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddFileAndChangeCharset() throws Exception {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {

				IFile file = TestsDefaultContentProvider.this.testProject.getFile("SomeFile");
				Assert.assertEquals("File existence", false, file.exists());
				ByteArrayInputStream is;
				try {
					is = new ByteArrayInputStream("Hello World".getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
				file.create(is, false, monitor);
				Util.safeClose(file.getContents());
				Assert.assertEquals("File existence", true, file.exists());

				file.setCharset("UTF-8", monitor);
				Assert.assertEquals("Wrong charset", file.getCharset(), "UTF-8");

				file.setCharset("UTF-16", monitor);
				Assert.assertEquals("Wrong charset", file.getCharset(), "UTF-16");

				InputStream stream = null;
				try {
					stream = file.getContents();
					byte[] result = new byte[stream.available()];
					stream.read(result);
					String content = new String(result, "UTF-8");
					Assert.assertEquals("Hello World", content);
				} catch (Exception e) {
					throw new RuntimeException(e);

				} finally {
					Util.safeClose(stream);
				}
				file.delete(false, monitor);

			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, this.testProject, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddFileAndMove() throws Exception {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {

				IFile file = TestsDefaultContentProvider.this.testProject.getFile("SomeFile");
				IFolder targetFolder = TestsDefaultContentProvider.this.testProject.getFolder("target");
				targetFolder.create(false, false, monitor);

				Assert.assertEquals("File existence", false, file.exists());
				ByteArrayInputStream is;
				try {
					is = new ByteArrayInputStream("Hello World".getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
				file.create(is, false, monitor);
				Util.safeClose(file.getContents());
				Assert.assertEquals("File existence", true, file.exists());

				InputStream stream = null;
				try {
					stream = file.getContents();
					byte[] result = new byte[stream.available()];
					stream.read(result);
					String content = new String(result, "UTF-8");
					Assert.assertEquals("Hello World", content);
				} catch (Exception e) {
					throw new RuntimeException(e);

				} finally {
					Util.safeClose(stream);
				}
				file.move(targetFolder.getFile(file.getName()).getFullPath(), false, monitor);
				file.delete(false, monitor);

			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, this.testProject, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddFileAndDeleteFolder() throws Exception {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {

				IFile file = TestsDefaultContentProvider.this.testProject.getFile("someFolder/SomeFile");
				Assert.assertEquals("File existence", false, file.exists());
				ByteArrayInputStream is;
				try {
					is = new ByteArrayInputStream("Hello World".getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}

				IFolder folder = (IFolder) file.getParent();

				folder.create(false, false, monitor);
				file.create(is, false, monitor);
				Util.safeClose(file.getContents());
				Assert.assertEquals("File existence", true, file.exists());

				InputStream stream = null;
				try {
					stream = file.getContents();
					byte[] result = new byte[stream.available()];
					stream.read(result);
					String content = new String(result, "UTF-8");
					Assert.assertEquals("Hello World", content);
				} catch (Exception e) {
					throw new RuntimeException(e);

				} finally {
					Util.safeClose(stream);
				}
				file.getParent().delete(false, monitor);

			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, this.testProject, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	@Ignore
	// TODO fails due to some problem in RESTContentProvider.openInputStreamInternal (called upon a folder)
	public void testAddFileAndMoveFolder() throws Exception {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {

				IFile file = TestsDefaultContentProvider.this.testProject.getFile("someFolder/SomeFile");
				IFolder targetFolder = TestsDefaultContentProvider.this.testProject.getFolder("newTarget");
				targetFolder.create(false, false, monitor);
				Assert.assertEquals("File existence", false, file.exists());
				ByteArrayInputStream is;
				try {
					is = new ByteArrayInputStream("Hello World".getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}

				IFolder folder = (IFolder) file.getParent();

				folder.create(false, false, monitor);
				file.create(is, false, monitor);
				Util.safeClose(file.getContents());
				Assert.assertEquals("File existence", true, file.exists());

				InputStream stream = null;
				try {
					stream = file.getContents();
					byte[] result = new byte[stream.available()];
					stream.read(result);
					String content = new String(result, "UTF-8");
					Assert.assertEquals("Hello World", content);
				} catch (Exception e) {
					throw new RuntimeException(e);

				} finally {
					Util.safeClose(stream);
				}

				if (!TestsDefaultContentProvider.this.autoRefresh) {
					TestsDefaultContentProvider.this.testProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}

				IFolder parent = (IFolder) file.getParent();
				parent.move(targetFolder.getFolder(parent.getName()).getFullPath(), false, monitor);
				targetFolder.delete(false, monitor);

			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, this.testProject, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFailRemoteMethods() throws Exception {

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {

				IFile file = TestsDefaultContentProvider.this.testProject.getFile("SomeOtherFile");
				Assert.assertEquals("File existence", false, file.exists());
				ByteArrayInputStream is;
				try {
					is = new ByteArrayInputStream("Hello World".getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
				file.create(is, false, null);
				Util.safeClose(file.getContents());
				Assert.assertEquals("File existence", true, file.exists());

				ISemanticFolder sf = (ISemanticFolder) TestsDefaultContentProvider.this.testProject.getFolder("testFolder").getAdapter(
						ISemanticFolder.class);

				try {
					sf.addFile("testFile", TestsDefaultContentProvider.this.options, monitor);
					Assert.fail("Should have failed");
				} catch (CoreException e) {
					//$JL-EXC$ expected
				}
				try {
					sf.addFolder("testFolder", TestsDefaultContentProvider.this.options, monitor);
					Assert.fail("Should have failed");
				} catch (CoreException e) {
					//$JL-EXC$ expected
				}

				try {
					sf.addResource("testResource", TestsDefaultContentProvider.this.options, monitor);
					Assert.fail("Should have failed");
				} catch (CoreException e) {
					//$JL-EXC$ expected
				}

				try {
					sf.createFileRemotely("newRemoteFile", new ByteArrayInputStream(new byte[0]), null,
							TestsDefaultContentProvider.this.options, monitor);
					Assert.fail("Should have failed");
				} catch (CoreException e) {
					//$JL-EXC$ expected
				}

				try {
					sf.createResourceRemotely("newRemoteResource", null, TestsDefaultContentProvider.this.options, monitor);
					Assert.fail("Should have failed");
				} catch (CoreException e) {
					//$JL-EXC$ expected
				}

				final ISemanticFile sFile = (ISemanticFile) file.getAdapter(ISemanticFile.class);

				try {
					sFile.deleteRemotely(TestsDefaultContentProvider.this.options, monitor);
					Assert.fail("Should have failed");
				} catch (CoreException e) {
					//$JL-EXC$ expected
				}

				try {
					sFile.revertChanges(TestsDefaultContentProvider.this.options, monitor);
					Assert.fail("Should have failed");
				} catch (CoreException e) {
					//$JL-EXC$ expected
				}
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

	}

	/**
	 * 
	 * @throws CoreException
	 */
	@Test
	public void testMoveProject() throws CoreException {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {

				try {
					TestsDefaultContentProvider.this.testProject.move(new Path("SomeTarget"), false, monitor);
					Assert.fail("Should have failed");
				} catch (OperationCanceledException e) {
					//$JL-EXC$ expected
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
	public void testTemplateMapping() throws Exception {

		// we have a mapping to PlainTestContentProvider which does not allow to create local files
		final IFolder pcpfolder = this.testProject.getFolder("PlainTestContentProvider");
		pcpfolder.create(false, true, null);

		final IFolder dcpfolder = this.testProject.getFolder("SomeFolder");
		dcpfolder.create(false, true, null);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					pcpfolder.getFile("TestChild").create(new ByteArrayInputStream("".getBytes("UTF-8")), false, monitor);
					Assert.fail("Should have failed");
				} catch (Exception e) {
					// $JL-EXC$ expected
				}

				try {
					// this should succeed, though
					dcpfolder.getFile("TestChild").create(new ByteArrayInputStream("".getBytes("UTF-8")), false, monitor);
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}

			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

	}

}
