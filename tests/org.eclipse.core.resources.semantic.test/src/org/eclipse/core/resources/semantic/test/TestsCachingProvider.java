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
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import junit.framework.Assert;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.examples.remote.RemoteFile;
import org.eclipse.core.resources.semantic.examples.remote.RemoteStoreTransient;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.resources.semantic.test.provider.CachingTestContentProvider;
import org.eclipse.core.resources.semantic.test.provider.CachingTestContentProviderBase;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

/**
 * Caching Content provider tests
 * 
 */
public class TestsCachingProvider extends TestsContentProviderBase {

	private RemoteFile file1;

	@Override
	public void afterMethod() throws Exception {
		super.afterMethod();
		this.file1 = null;
	}

	@Override
	public void beforeMethod() throws Exception {
		super.beforeMethod();
		RemoteStoreTransient store = (RemoteStoreTransient) this.testProject.getAdapter(RemoteStoreTransient.class);
		this.file1 = (RemoteFile) store.getItemByPath(new Path("Folder1/File1"));

	}

	@Override
	public RemoteFile getRemoteFile() {
		return this.file1;
	}

	/**
	 * No-argument construcator
	 */
	public TestsCachingProvider() {
		super(false, "CachingTests", CachingTestContentProvider.class.getName());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddFileFromRemoteDeleteAndAddAgain() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");
		Assert.assertEquals("Folder existence", false, parent.exists());

		final IFile file = parent.getFile("File1");
		Assert.assertEquals("File existence", false, file.exists());

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sfr = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
				ISemanticFile sfile = sfr.addFile("File1", TestsCachingProvider.this.options, monitor);
				Assert.assertTrue(sfile.getAdaptedFile().equals(file));
				file.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
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
		ResourcesPlugin.getWorkspace().run(runnable, this.testProject, 0, new NullProgressMonitor());

		Assert.assertEquals("File existence", true, file.exists());
		Assert.assertEquals("Folder existence", true, parent.exists());

		// we keep a file stream so that deletion goes wrong
		InputStream is = file.getContents();

		try {

			runnable = new IWorkspaceRunnable() {

				/**
				 * @throws CoreException
				 */
				public void run(IProgressMonitor monitor) throws CoreException {
					ISemanticFolder sfr = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
					ISemanticFile sFile = (ISemanticFile) file.getAdapter(ISemanticFile.class);
					try {
						sFile.remove(TestsCachingProvider.this.options, monitor);
						sfr.getAdaptedResource().refreshLocal(IResource.DEPTH_ONE, monitor);
					} catch (CoreException e) {
						// $JL-EXC$
						Assert.fail("Remove failure");
					}

				}
			};

			ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

			Assert.assertEquals("File exsistence", false, file.exists());

			runnable = new IWorkspaceRunnable() {

				public void run(IProgressMonitor monitor) throws CoreException {
					ISemanticFolder sfr = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
					// should work even if cache is deleted logically only
					ISemanticFile sfile = sfr.addFile("File1", TestsCachingProvider.this.options, monitor);
					// TODO verify that other operations fail
					// (write/read/timestamp)
					Assert.assertTrue(sfile.getAdaptedFile().equals(file));
					file.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
					InputStream is1 = null;
					try {
						is1 = sfile.getAdaptedFile().getContents();
						try {
							Assert.assertTrue("Too few bytes available", is1.available() > 0);
						} catch (IOException e) {
							// $JL-EXC$
							Assert.fail(e.getMessage());
						}
					} finally {
						Util.safeClose(is1);
					}
				}
			};

			try {
				// re-adding should not fail even if the input stream is still
				// open
				ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

			} catch (CoreException e) {
				// $JL-EXC$ expected

				Assert.fail("Should not fail");
			}
		} finally {
			Util.safeClose(is);
		}

		Assert.assertEquals("File existence", true, file.exists());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWriteThrough() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");

		final IFile file = parent.getFile("File1");
		Assert.assertFalse("File should not exist", file.exists());

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sfr = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
				sfr.addFile("File1", ISemanticFileSystem.NONE, monitor);
				Assert.assertEquals("File existence", true, file.exists());
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		assertContentsEqual(file, "Hello");
		Assert.assertEquals("Wrong remote content", "Hello", new String(this.file1.getContent(), "UTF-8"));

		// setcontent without remote update
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

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		assertContentsEqual(file, "NewString");
		Assert.assertEquals("Wrong remote content", "Hello", new String(this.file1.getContent(), "UTF-8"));

		// setcontent with remote update
		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
					sf.setSessionProperty(CachingTestContentProviderBase.WRITE_THROUGH, "");

					IStatus stat = sf.validateEdit(null);

					Assert.assertTrue("ValidateEdit should have returned OK", stat.isOK());

					file.setContents(new ByteArrayInputStream("ThirdString".getBytes("UTF-8")), IResource.KEEP_HISTORY, monitor);

					sf.setSessionProperty(CachingTestContentProviderBase.WRITE_THROUGH, null);

				} catch (UnsupportedEncodingException e) {
					// $JL-EXC$
					Assert.fail(e.getMessage());
				}
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		assertContentsEqual(file, "ThirdString");
		Assert.assertEquals("Wrong remote content", "ThirdString", new String(this.file1.getContent(), "UTF-8"));

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
				sf.remove(ISemanticFileSystem.NONE, null);
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		Assert.assertEquals("File existence", false, file.exists());

	}

	@Test
	public void testBeforeCacheUpdate() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");

		final IFile file = parent.getFile("File1");
		Assert.assertFalse("File should not exist", file.exists());

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sfr = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
				sfr.addFile("File1", ISemanticFileSystem.NONE, monitor);
				Assert.assertEquals("File existence", true, file.exists());
				ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
				sf.setSessionProperty(CachingTestContentProviderBase.USE_BEFORE_CACHE_UPDATE, "x");
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		assertContentsEqual(file, "Hello");
		Assert.assertEquals("Wrong remote content", "Hello", new String(this.file1.getContent(), "UTF-8"));

		// setcontent without remote update
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

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		assertContentsEqual(file, "NewString");
		Assert.assertEquals("Wrong remote content", "Hello", new String(this.file1.getContent(), "UTF-8"));

		// setcontent with remote update
		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
					sf.setSessionProperty(CachingTestContentProviderBase.WRITE_THROUGH, "");

					IStatus stat = sf.validateEdit(null);

					Assert.assertTrue("ValidateEdit should have returned OK", stat.isOK());

					file.setContents(new ByteArrayInputStream("ThirdString".getBytes("UTF-8")), IResource.KEEP_HISTORY, monitor);

					sf.setSessionProperty(CachingTestContentProviderBase.WRITE_THROUGH, null);

				} catch (UnsupportedEncodingException e) {
					// $JL-EXC$
					Assert.fail(e.getMessage());
				}
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		assertContentsEqual(file, "ThirdString");
		Assert.assertEquals("Wrong remote content", "ThirdString", new String(this.file1.getContent(), "UTF-8"));

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
				sf.remove(ISemanticFileSystem.NONE, null);
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		Assert.assertEquals("File existence", false, file.exists());

	}

	@Test
	public void testExceptionInBeforeCacheUpdate() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");

		final IFile file = parent.getFile("File1");
		Assert.assertFalse("File should not exist", file.exists());

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sfr = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
				sfr.addFile("File1", ISemanticFileSystem.NONE, monitor);
				Assert.assertEquals("File existence", true, file.exists());
				ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
				sf.setSessionProperty(CachingTestContentProviderBase.USE_BEFORE_CACHE_UPDATE, "x");
				sf.setSessionProperty(CachingTestContentProviderBase.EXCEPTION_IN_BEFORE_CACHE_UPDATE, "x");
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		assertContentsEqual(file, "Hello");
		Assert.assertEquals("Wrong remote content", "Hello", new String(this.file1.getContent(), "UTF-8"));

		// setcontent without remote update
		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
					IStatus stat = sf.validateEdit(null);

					Assert.assertTrue("ValidateEdit should have returned OK", stat.isOK());

					file.setContents(new ByteArrayInputStream("NewString".getBytes("UTF-8")), IResource.KEEP_HISTORY, monitor);
					Assert.fail("Should throw an exception");
				} catch (CoreException e) {
					// OK
				} catch (UnsupportedEncodingException e) {
					// $JL-EXC$
					Assert.fail(e.getMessage());
				}
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		// cached file content must remain unchanged
		assertContentsEqual(file, "Hello");
		Assert.assertEquals("Wrong remote content", "Hello", new String(this.file1.getContent(), "UTF-8"));

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
				sf.remove(ISemanticFileSystem.NONE, null);
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		Assert.assertEquals("File existence", false, file.exists());

	}

	@Test
	public void testReturnFalseInBeforeCacheUpdate() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");

		final IFile file = parent.getFile("File1");
		Assert.assertFalse("File should not exist", file.exists());

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sfr = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
				sfr.addFile("File1", ISemanticFileSystem.NONE, monitor);
				Assert.assertEquals("File existence", true, file.exists());
				ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
				sf.setSessionProperty(CachingTestContentProviderBase.USE_BEFORE_CACHE_UPDATE, "x");
				sf.setSessionProperty(CachingTestContentProviderBase.RETURN_FALSE_IN_BEFORE_CACHE_UPDATE, "x");
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		assertContentsEqual(file, "Hello");
		Assert.assertEquals("Wrong remote content", "Hello", new String(this.file1.getContent(), "UTF-8"));

		// setcontent without remote update
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

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		// cached file content must remain unchanged
		assertContentsEqual(file, "Hello");
		Assert.assertEquals("Wrong remote content", "Hello", new String(this.file1.getContent(), "UTF-8"));

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
				sf.remove(ISemanticFileSystem.NONE, null);
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		Assert.assertEquals("File existence", false, file.exists());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLocalFileSupport() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");

		final IFile file = parent.getFile("File1");
		Assert.assertFalse("File should not exist", file.exists());

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sfr = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
				ISemanticFile sFile = sfr.addFile("File1", ISemanticFileSystem.NONE, monitor);

				File localFile = EFS.getStore(sFile.getAdaptedFile().getLocationURI()).toLocalFile(EFS.CACHE, monitor);
				Assert.assertNotNull("Cached Local File should not be null", localFile);
				Assert.assertTrue("Cached file should be in Eclipse file cache", localFile.getPath()
						.contains("org.eclipse.core.filesystem"));

				localFile = EFS.getStore(sFile.getAdaptedFile().getLocationURI()).toLocalFile(EFS.NONE, monitor);
				Assert.assertNotNull("Uncached Local File should not be null", localFile);
				Assert.assertTrue("Uncached file should be in SFS cache",
						localFile.getPath().contains("org.eclipse.core.resources.semantic"));
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf = (ISemanticFile) file.getAdapter(ISemanticFile.class);
				sf.remove(ISemanticFileSystem.NONE, null);
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		Assert.assertEquals("File existence", false, file.exists());

	}

	/**
	 * TODO move this to the base class to test all content provider after
	 * refactoring of tests wrt obtaining the remote file
	 * 
	 * @throws Exception
	 */
	@Test
	public void testParallelReadAndWrite() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");

		final IFile file = parent.getFile("File1");
		Assert.assertFalse("File should not exist", file.exists());

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sfr = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
				sfr.addFile("File1", ISemanticFileSystem.NONE, monitor);
				try {
					Util.transferStreams(new ByteArrayInputStream("New Remote".getBytes("UTF-8")), file1.getOutputStream(false), monitor);
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				InputStream is = null;
				try {
					is = file.getContents();
					assertContentsEqual(file, "Hello");

					ISemanticFile sFile = (ISemanticFile) file.getAdapter(ISemanticFile.class);
					sFile.synchronizeContentWithRemote(SyncDirection.INCOMING, ISemanticFileSystem.NONE, monitor);
					// we are still holding the file input stream, so we are
					// seeing the out dated content
					assertContentsEqual(file, "New Remote");
					// drop the input stream
					Util.safeClose(is);

					// TODO now we need a refresh!
					file.refreshLocal(IResource.DEPTH_INFINITE, monitor);

					assertContentsEqual(file, "New Remote");

				} finally {
					Util.safeClose(is);
				}

			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

	}

	/**
	 * 
	 * TODO move this to the base class to test all content provider after
	 * refactoring of tests wrt obtaining the remote file
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAppendContentsToOpenFile() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");

		final IFile file = parent.getFile("File1");

		final ISemanticFile sfile = (ISemanticFile) file.getAdapter(ISemanticFile.class);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder folder = (ISemanticFolder) file.getParent().getAdapter(ISemanticFolder.class);
				folder.addFile(file.getName(), ISemanticFileSystem.NONE, monitor);
				assertContentsEqual(file, "Hello");
				sfile.validateEdit(null);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// $JL-EXC$
				}
				file.appendContents(new ByteArrayInputStream(" appended".getBytes()), EFS.NONE, monitor);
				// TODO does not work with plain provider; fix after integration
				// of other tests
				assertContentsEqual(file, "Hello appended");
				InputStream is = null;
				try {
					is = file.getContents();
					assertContentsEqual(file, "Hello appended");
					file.appendContents(new ByteArrayInputStream(" again".getBytes()), EFS.NONE, monitor);
					assertContentsEqual(file, "Hello appended again");

					// TODO this should also work with an opened file
					sfile.synchronizeContentWithRemote(SyncDirection.OUTGOING, ISemanticFileSystem.NONE, monitor);
					Assert.assertEquals("Wrong remote content", new String(file1.getContent()), "Hello appended again");
				} finally {
					Util.safeClose(is);
				}
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

	}
}
