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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.examples.remote.RemoteFolder;
import org.eclipse.core.resources.semantic.examples.remote.RemoteStoreTransient;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
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
public class TestsDefaultContentProvider extends TestsContentProviderUtil {
	IWorkspace workspace = ResourcesPlugin.getWorkspace();

	private static final String UTF_8_CHARSET = "utf-8";
	private static final String TEST = "test";
	private static final String TEST2 = "test2";

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
		super(withAutoRefresh, "TestDefaultContentProvider", null);
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
	@Override
	@Before
	public void beforeMethod() throws Exception {

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

		workspace.run(myRunnable, project, IWorkspace.AVOID_UPDATE, null);

		this.testProject = project;

	}

	/**
	 * Deletes the test project and resets the remote repository
	 * 
	 * @throws Exception
	 */
	@Override
	@After
	public void afterMethod() throws Exception {

		RemoteStoreTransient store = (RemoteStoreTransient) this.testProject.getAdapter(RemoteStoreTransient.class);
		store.reset();

		this.testProject = null;

		final IProject project = workspace.getRoot().getProject(this.projectName);
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				project.delete(true, monitor);
				if (!TestsDefaultContentProvider.this.autoRefresh) {
					project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			}
		};

		workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());

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
		workspace.run(runnable, this.testProject, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
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
		workspace.run(runnable, this.testProject.getParent(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
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
		workspace.run(runnable, this.testProject, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
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
		workspace.run(runnable, TestsDefaultContentProvider.this.testProject, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddRemoteFileAndMove() throws Exception {
		final IFolder parent = TestsDefaultContentProvider.this.testProject.getFolder("someFolder");
		final IFolder folder = parent.getFolder("someChild1");
		final IFolder targetFolder = parent.getFolder("someChild2");
		parent.create(true, false, null);
		folder.create(true, false, null);
		targetFolder.create(true, false, null);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					ISemanticFolder sFolder = (ISemanticFolder) folder.getAdapter(ISemanticFolder.class);

					File testfile = createTestFile("test.txt");
					boolean created = testfile.createNewFile();
					if (!created) {
						new FileOutputStream(testfile).close();
					}

					writeContentsToFile(testfile, "Hello World", "UTF-8");

					ISemanticFile sfile = sFolder.addFile("SomeFile", createURI4File(testfile), TestsDefaultContentProvider.this.options,
							monitor);
					if (TestsDefaultContentProvider.this.options == ISemanticFileSystem.SUPPRESS_REFRESH) {
						testProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					}

					IFile file = sfile.getAdaptedFile();

					IFile targetFile = targetFolder.getFile(file.getName());

					file.move(targetFolder.getFile(file.getName()).getFullPath(), false, monitor);

					Assert.assertEquals("File existence", false, file.exists());
					Assert.assertEquals("File existence", true, targetFile.exists());

					assertContentsEqual(targetFile, "Hello World");

				} catch (URISyntaxException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		};
		workspace.run(runnable, parent, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
	}

	@Test(expected = SemanticResourceException.class)
	public void testAddRemoteFileAndMoveWithWrongSchedulingRule() throws Exception {
		final IFolder folder = TestsDefaultContentProvider.this.testProject.getFolder("someFolder/someChild1");

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					ISemanticFolder sFolder = (ISemanticFolder) folder.getAdapter(ISemanticFolder.class);

					File testfile = createTestFile("test.txt");
					boolean created = testfile.createNewFile();
					if (!created) {
						new FileOutputStream(testfile).close();
					}

					writeContentsToFile(testfile, "Hello World", "UTF-8");

					ISemanticFile sfile = sFolder.addFile("SomeFile", createURI4File(testfile), TestsDefaultContentProvider.this.options,
							monitor);
					if (TestsDefaultContentProvider.this.options == ISemanticFileSystem.SUPPRESS_REFRESH) {
						testProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					}

					IFile file = sfile.getAdaptedFile();

					IFolder targetFolder = TestsDefaultContentProvider.this.testProject.getFolder("someFolder/someChild2");
					targetFolder.create(false, false, monitor);

					IFile targetFile = targetFolder.getFile(file.getName());

					file.move(targetFolder.getFile(file.getName()).getFullPath(), false, monitor);

					Assert.assertEquals("File existence", false, file.exists());
					Assert.assertEquals("File existence", true, targetFile.exists());

					assertContentsEqual(targetFile, "Hello World");

				} catch (URISyntaxException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		};
		workspace.run(runnable, folder, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test(expected = CoreException.class)
	public void testAddRemoteFileAndMoveToExistingLocation() throws Exception {
		final IFolder folder = TestsDefaultContentProvider.this.testProject.getFolder("someFolder");

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					ISemanticFolder sFolder = (ISemanticFolder) folder.getAdapter(ISemanticFolder.class);

					File testfile = createTestFile("test.txt");
					boolean created = testfile.createNewFile();
					if (!created) {
						new FileOutputStream(testfile).close();
					}

					writeContentsToFile(testfile, "Hello World", "UTF-8");

					ISemanticFile sfile = sFolder.addFile("SomeFile", createURI4File(testfile), TestsDefaultContentProvider.this.options,
							monitor);
					if (TestsDefaultContentProvider.this.options == ISemanticFileSystem.SUPPRESS_REFRESH) {
						testProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					}

					IFile file = sfile.getAdaptedFile();

					IFolder targetFolder = TestsDefaultContentProvider.this.testProject.getFolder("target");
					targetFolder.create(false, false, monitor);

					IFile targetFile = targetFolder.getFile(file.getName());

					Assert.assertEquals("File existence", false, targetFile.exists());
					ByteArrayInputStream is;
					try {
						is = new ByteArrayInputStream("Hello World".getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(e);
					}
					targetFile.create(is, false, monitor);
					Util.safeClose(targetFile.getContents());
					Assert.assertEquals("File existence", true, targetFile.exists());

					file.move(targetFolder.getFile(file.getName()).getFullPath(), false, monitor);

					Assert.assertTrue("file.move() should have failed.", false);
				} catch (URISyntaxException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		};
		workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
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
		ISchedulingRule rule = workspace.getRuleFactory().createRule(testProject.getFolder("someFolder"));
		workspace.run(runnable, rule, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	@Ignore
	// TODO fails due to some problem in
	// RESTContentProvider.openInputStreamInternal (called upon a folder)
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
		workspace.run(runnable, this.testProject, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
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
					// $JL-EXC$ expected
				}
				try {
					sf.addFolder("testFolder", TestsDefaultContentProvider.this.options, monitor);
					Assert.fail("Should have failed");
				} catch (CoreException e) {
					// $JL-EXC$ expected
				}

				try {
					sf.addResource("testResource", TestsDefaultContentProvider.this.options, monitor);
					Assert.fail("Should have failed");
				} catch (CoreException e) {
					// $JL-EXC$ expected
				}

				try {
					sf.createFileRemotely("newRemoteFile", new ByteArrayInputStream(new byte[0]), null,
							TestsDefaultContentProvider.this.options, monitor);
					Assert.fail("Should have failed");
				} catch (CoreException e) {
					// $JL-EXC$ expected
				}

				try {
					sf.createResourceRemotely("newRemoteResource", null, TestsDefaultContentProvider.this.options, monitor);
					Assert.fail("Should have failed");
				} catch (CoreException e) {
					// $JL-EXC$ expected
				}

				final ISemanticFile sFile = (ISemanticFile) file.getAdapter(ISemanticFile.class);

				try {
					sFile.deleteRemotely(TestsDefaultContentProvider.this.options, monitor);
					Assert.fail("Should have failed");
				} catch (CoreException e) {
					// $JL-EXC$ expected
				}

				try {
					sFile.revertChanges(TestsDefaultContentProvider.this.options, monitor);
					Assert.fail("Should have failed");
				} catch (CoreException e) {
					// $JL-EXC$ expected
				}
			}
		};

		workspace.run(runnable, this.testProject, 0, new NullProgressMonitor());

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
					// $JL-EXC$ expected
				}

			}
		};

		workspace.run(runnable, new NullProgressMonitor());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTemplateMapping() throws Exception {

		// we have a mapping to PlainTestContentProvider which does not allow to
		// create local files
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

		workspace.run(runnable, workspace.getRuleFactory().refreshRule(dcpfolder), 0, new NullProgressMonitor());

	}

	/**
	 * TODO create some SPI test class
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFileStoreBehaviorAfterRemove() throws Exception {

		IFile file = TestsDefaultContentProvider.this.testProject.getFile("someFolder/SomeFileX");

		((IFolder) file.getParent()).create(false, true, null);

		file.create(new ByteArrayInputStream("".getBytes()), false, null);

		ISemanticFileStore store = (ISemanticFileStore) EFS.getStore(file.getLocationURI());
		ISemanticFileStore store2 = (ISemanticFileStore) EFS.getStore(file.getLocationURI());
		IPath beforePath = store.getPath();
		store.remove(null);
		IPath afterPath = store.getPath();
		IPath afterPath2 = store2.getPath();
		Assert.assertEquals("Path should be the same after removal of semantic file store", beforePath, afterPath);
		Assert.assertEquals("Path should be the same after removal of semantic file store", beforePath, afterPath2);

		try {
			store.getPersistentProperties();
			Assert.assertTrue("GetPersistentProperties should have failed.", false);
		} catch (CoreException e) {
			// ignore
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFileStoreBehaviorAfterRemove2() throws Exception {

		IFile file = TestsDefaultContentProvider.this.testProject.getFile("someFolder/SomeFileX");

		((IFolder) file.getParent()).create(false, true, null);

		file.create(new ByteArrayInputStream("".getBytes()), false, null);

		ISemanticFileStore store = (ISemanticFileStore) EFS.getStore(file.getLocationURI());
		ISemanticFileStore parentStore = (ISemanticFileStore) EFS.getStore(file.getParent().getLocationURI());
		IPath beforePath = store.getPath();
		IPath beforePath2 = parentStore.getPath();
		parentStore.remove(null);
		IPath afterPath = store.getPath();
		IPath afterPath2 = parentStore.getPath();
		Assert.assertEquals("Path should be the same after removal of semantic file store", beforePath, afterPath);
		Assert.assertEquals("Path should be the same after removal of semantic file store", beforePath2, afterPath2);
		Assert.assertTrue("Folder should not exist", !parentStore.isExists());
		Assert.assertTrue("Child file should not exist as well", !store.isExists());

		try {
			store.getPersistentProperties();
			Assert.assertTrue("GetPersistentProperties should have failed.", false);
		} catch (CoreException e) {
			// ignore
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddFileWithURL() throws Exception {
		final IFolder folder = TestsDefaultContentProvider.this.testProject.getFolder("someFolder");

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sFolder = (ISemanticFolder) folder.getAdapter(ISemanticFolder.class);
				try {
					File testfile = createTestFile("test.txt");
					boolean created = testfile.createNewFile();
					if (!created) {
						new FileOutputStream(testfile).close();
					}

					ISemanticFile sfile = sFolder.addFile("SomeFile", createURI4File(testfile), TestsDefaultContentProvider.this.options,
							monitor);
					if (TestsDefaultContentProvider.this.options == ISemanticFileSystem.SUPPRESS_REFRESH) {
						testProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					}

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

		workspace.run(runnable, testProject, 0, new NullProgressMonitor());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddFileWithWrongURL() throws Exception {
		final IFolder folder = TestsDefaultContentProvider.this.testProject.getFolder("someFolder");

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sFolder = (ISemanticFolder) folder.getAdapter(ISemanticFolder.class);

				if (sFolder.getAdaptedContainer().getFile(new Path("SomeFile")).exists()) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, "resource already exists"));
				}

				try {
					sFolder.addFile("SomeFile", new URI("x:y"), TestsDefaultContentProvider.this.options, monitor);
					Assert.assertTrue("addFile() should have failed", false);

				} catch (CoreException e) {
					// catch and continue
					e.getMessage();
				} catch (URISyntaxException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				}

			}
		};

		workspace.run(runnable, new NullProgressMonitor());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddFileWithNonExistingURL() throws Exception {
		final IFolder folder = TestsDefaultContentProvider.this.testProject.getFolder("someFolder");

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sFolder = (ISemanticFolder) folder.getAdapter(ISemanticFolder.class);

				if (sFolder.getAdaptedContainer().getFile(new Path("SomeFile")).exists()) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, "resource already exists"));
				}

				try {
					File testfile = createTestFile("test.txt");
					if (!testfile.delete()) {
						throw new IOException("Cann't delete file " + testfile);
					}

					sFolder.addFile("SomeFile", createURI4File(testfile), TestsDefaultContentProvider.this.options, monitor);
					Assert.assertTrue("addFile() should have failed", false);

				} catch (CoreException e) {
					// catch and continue
					e.getMessage();
				} catch (URISyntaxException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				}

			}
		};

		workspace.run(runnable, new NullProgressMonitor());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSynchronize() throws Exception {
		final IFolder folder = TestsDefaultContentProvider.this.testProject.getFolder("someFolder");

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sFolder = (ISemanticFolder) folder.getAdapter(ISemanticFolder.class);
				try {
					File testfile = createTestFile("test.txt");

					writeContentsToFile(testfile, TEST, UTF_8_CHARSET);

					ISemanticFile sfile = sFolder.addFile("SomeFileZ", createURI4File(testfile), TestsDefaultContentProvider.this.options,
							monitor);
					if (TestsDefaultContentProvider.this.options == ISemanticFileSystem.SUPPRESS_REFRESH) {
						testProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					}

					assertContentsEqual(sfile.getAdaptedFile(), TEST);

					Assert.assertTrue("Resource should exist " + sfile.getAdaptedResource().getLocationURI(), sfile.getAdaptedResource()
							.exists());

					writeContentsToFile(testfile, TEST2, UTF_8_CHARSET);

					sfile.synchronizeContentWithRemote(SyncDirection.INCOMING, TestsDefaultContentProvider.this.options, monitor);

					assertContentsEqual(sfile.getAdaptedFile(), TEST2);

					if (!testfile.delete()) {
						throw new IOException("Cann't delete file " + testfile);
					}

					try {
						sfile.synchronizeContentWithRemote(SyncDirection.INCOMING, TestsDefaultContentProvider.this.options, monitor);
					} catch (CoreException e) {
						// ignore and continue
						e.getMessage();
					}

					try {
						Util.safeClose(sfile.getAdaptedFile().getContents());
					} catch (CoreException e) {
						// ignore and continue
						e.getMessage();
					}

					writeContentsToFile(testfile, TEST, UTF_8_CHARSET);

					sfile.synchronizeContentWithRemote(SyncDirection.INCOMING, TestsDefaultContentProvider.this.options, monitor);

					assertContentsEqual(sfile.getAdaptedFile(), TEST);

				} catch (URISyntaxException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		};

		workspace.run(runnable, this.testProject, 0, new NullProgressMonitor());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRuleFactory() throws Exception {
		final IFolder folder = TestsDefaultContentProvider.this.testProject.getFolder("someFolder");
		final IFolder subfolder = folder.getFolder("someSubfolder");
		final IFolder file = subfolder.getFolder("someFile");
		IResourceRuleFactory factory = workspace.getRuleFactory();

		Assert.assertEquals(testProject, factory.modifyRule(testProject));
		Assert.assertEquals(folder, factory.modifyRule(folder));
		Assert.assertEquals(subfolder, factory.modifyRule(subfolder));
		Assert.assertEquals(file, factory.modifyRule(file));

		Assert.assertEquals(testProject, factory.refreshRule(testProject));
		Assert.assertEquals(testProject, factory.refreshRule(folder));
		Assert.assertEquals(folder, factory.refreshRule(subfolder));
		Assert.assertEquals(subfolder, factory.refreshRule(file));

		Assert.assertEquals(testProject, factory.deleteRule(testProject));
		Assert.assertEquals(testProject, factory.deleteRule(folder));
		Assert.assertEquals(folder, factory.deleteRule(subfolder));
		Assert.assertEquals(subfolder, factory.deleteRule(file));

		Assert.assertEquals(testProject, factory.createRule(testProject));
		Assert.assertEquals(testProject, factory.createRule(folder));
		Assert.assertEquals(folder, factory.createRule(subfolder));
		Assert.assertEquals(subfolder, factory.createRule(file));
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testImplicitFolderCreation() throws Exception {
		final IFolder folder = TestsDefaultContentProvider.this.testProject.getFolder("someFolder");
		final IFolder subfolder = folder.getFolder("someSubfolder");

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sFolder = (ISemanticFolder) subfolder.getAdapter(ISemanticFolder.class);

				File testfile = createTestFile("test.txt");

				try {
					writeContentsToFile(testfile, TEST, UTF_8_CHARSET);
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				}

				try {
					sFolder.addFile("SomeFile", createURI4File(testfile), TestsDefaultContentProvider.this.options, monitor);
				} catch (URISyntaxException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		};

		workspace.run(runnable, workspace.getRuleFactory().refreshRule(subfolder), 0, new NullProgressMonitor());
	}
}
