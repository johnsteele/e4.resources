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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import junit.framework.Assert;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.resources.semantic.examples.providers.SampleCompositeResourceContentProvider;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

/**
 *
 */
public class TestsSampleCompositeResourceProvider extends TestsContentProviderUtil {

	/**
	 */
	public TestsSampleCompositeResourceProvider() {
		super(false, "SampleCompositeResourceProviderTests", SampleCompositeResourceContentProvider.class.getName());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddRemoveFileByURI() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");
		Assert.assertEquals("Folder existence", false, parent.exists());

		final String testFileName = "test.txt";
		final String test_enFileName = "test_en.txt";
		final String test2FileName = "test2.txt";

		final IFile testFile = parent.getFile(testFileName);
		Assert.assertEquals("File existence", false, testFile.exists());

		final IFile test_enFile = parent.getFile(test_enFileName);
		Assert.assertEquals("File existence", false, testFile.exists());

		final IFile test2File = parent.getFile(test2FileName);
		Assert.assertEquals("File existence", false, testFile.exists());

		final URI testFileURI = createTempFile(testFileName, "test content 1");
		final URI test_enFileURI = createTempFile(test_enFileName, "test content 2");
		final URI test2FileURI = createTempFile(test2FileName, "test content 3");

		addFile(parent, testFileName, testFile, testFileURI);
		addFile(parent, test_enFileName, test_enFile, test_enFileURI);
		addFile(parent, test2FileName, test2File, test2FileURI);

		Assert.assertEquals("Folder existence", true, parent.exists());

		Assert.assertEquals("File existence", true, testFile.exists());
		Assert.assertEquals("File existence", true, test_enFile.exists());
		Assert.assertEquals("File existence", true, test2File.exists());

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf = (ISemanticFile) testFile.getAdapter(ISemanticFile.class);
				ISemanticFile sf_en = (ISemanticFile) test_enFile.getAdapter(ISemanticFile.class);
				ISemanticFile sf2 = (ISemanticFile) test2File.getAdapter(ISemanticFile.class);

				Assert.assertTrue("Should be read-only", sf.getAdaptedFile().isReadOnly());
				Assert.assertTrue("Should be read-only", sf_en.getAdaptedFile().isReadOnly());
				Assert.assertTrue("Should be read-only", sf2.getAdaptedFile().isReadOnly());

				IStatus stat = sf.validateEdit(null);
				Assert.assertTrue("ValidateEdit should have returned OK", stat.isOK());

				Assert.assertTrue("Should be writable", !sf.getAdaptedFile().isReadOnly());
				Assert.assertTrue("Should be writable", !sf_en.getAdaptedFile().isReadOnly());
				Assert.assertTrue("Should be still read-only", sf2.getAdaptedFile().isReadOnly());
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf = (ISemanticFile) testFile.getAdapter(ISemanticFile.class);
				ISemanticFile sf_en = (ISemanticFile) test_enFile.getAdapter(ISemanticFile.class);
				ISemanticFile sf2 = (ISemanticFile) test2File.getAdapter(ISemanticFile.class);

				IStatus status;

				status = sf.validateRemove(ISemanticFileSystem.NONE, monitor);
				Assert.assertTrue("ValidateRemove should have returned not OK", !status.isOK());
				status = sf_en.validateRemove(ISemanticFileSystem.NONE, monitor);
				Assert.assertTrue("ValidateRemove should have returned not OK", !status.isOK());
				status = sf2.validateRemove(ISemanticFileSystem.NONE, monitor);
				Assert.assertTrue("ValidateRemove should have returned OK", status.isOK());

				status = sf.validateRemove(ISemanticFileSystem.VALIDATE_REMOVE_RAISE_ERROR_ON_SCOPE_EXTENSION, monitor);
				Assert.assertTrue("ValidateRemove should have returned not OK", !status.isOK());
				status = sf_en.validateRemove(ISemanticFileSystem.VALIDATE_REMOVE_RAISE_ERROR_ON_SCOPE_EXTENSION, monitor);
				Assert.assertTrue("ValidateRemove should have returned not OK", !status.isOK());
				status = sf2.validateRemove(ISemanticFileSystem.VALIDATE_REMOVE_RAISE_ERROR_ON_SCOPE_EXTENSION, monitor);
				Assert.assertTrue("ValidateRemove should have returned OK", status.isOK());

			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf = (ISemanticFile) testFile.getAdapter(ISemanticFile.class);
				ISemanticFile sf_en = (ISemanticFile) test_enFile.getAdapter(ISemanticFile.class);
				ISemanticFile sf2 = (ISemanticFile) test2File.getAdapter(ISemanticFile.class);

				Assert.assertTrue("Should be writable", !sf.getAdaptedFile().isReadOnly());
				Assert.assertTrue("Should be writable", !sf_en.getAdaptedFile().isReadOnly());
				Assert.assertTrue("Should be read-only", sf2.getAdaptedFile().isReadOnly());

				sf_en.revertChanges(TestsSampleCompositeResourceProvider.this.options, monitor);

				Assert.assertTrue("Should be read-only", sf.getAdaptedFile().isReadOnly());
				Assert.assertTrue("Should be read-only", sf_en.getAdaptedFile().isReadOnly());
				Assert.assertTrue("Should be read-only", sf2.getAdaptedFile().isReadOnly());

				IStatus status;
				status = sf.validateRemove(ISemanticFileSystem.VALIDATE_REMOVE_RAISE_ERROR_ON_SCOPE_EXTENSION, monitor);
				Assert.assertTrue("ValidateRemove should have returned not OK", !status.isOK());
				status = sf_en.validateRemove(ISemanticFileSystem.VALIDATE_REMOVE_RAISE_ERROR_ON_SCOPE_EXTENSION, monitor);
				Assert.assertTrue("ValidateRemove should have returned not OK", !status.isOK());
				status = sf2.validateRemove(ISemanticFileSystem.NONE, monitor);
				Assert.assertTrue("ValidateRemove should have returned OK", status.isOK());

			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf2 = (ISemanticFile) test2File.getAdapter(ISemanticFile.class);

				IStatus status;

				status = sf2.validateEdit(null);
				Assert.assertTrue("ValidateEdit should have returned OK", status.isOK());

				status = sf2.validateRemove(ISemanticFileSystem.NONE, monitor);
				Assert.assertTrue("ValidateRemove should have returned not OK", !status.isOK());

				status = sf2.validateRemove(ISemanticFileSystem.VALIDATE_REMOVE_RAISE_ERROR_ON_SCOPE_EXTENSION, monitor);
				Assert.assertTrue("ValidateRemove should have returned not OK", !status.isOK());

				status = sf2.validateRemove(ISemanticFileSystem.VALIDATE_REMOVE_RAISE_ERROR_ON_SCOPE_EXTENSION
						| ISemanticFileSystem.VALIDATE_REMOVE_IGNORE_RESOURCE_STATE, monitor);
				Assert.assertTrue("ValidateRemove should have returned OK", status.isOK());
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFile sf = (ISemanticFile) testFile.getAdapter(ISemanticFile.class);
				sf.remove(TestsSampleCompositeResourceProvider.this.options, monitor);
				if (!TestsSampleCompositeResourceProvider.this.autoRefresh) {
					Assert.assertEquals("File existence", true, testFile.exists());
					testFile.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
				Assert.assertEquals("File existence", false, testFile.exists());
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

		Assert.assertFalse("File should not exist", testFile.exists());
		Assert.assertFalse("File should not exist", test_enFile.exists());
		Assert.assertTrue("File should exist", test2File.exists());

	}

	/**
	 * @param parent
	 * @param testFileName
	 * @param file
	 * @param testFileURI
	 * @throws CoreException
	 */
	private void addFile(final IFolder parent, final String testFileName, final IFile file, final URI testFileURI) throws CoreException {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sfr = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
				ISemanticFile sfile = sfr.addFile(testFileName, testFileURI, TestsSampleCompositeResourceProvider.this.options, monitor);
				Assert.assertTrue(sfile.getAdaptedFile().equals(file));
				if (!TestsSampleCompositeResourceProvider.this.autoRefresh) {
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

		ResourcesPlugin.getWorkspace().run(runnable, this.testProject, 0, new NullProgressMonitor());

	}

	private URI createTempFile(String name, String content) throws IOException {
		String tmproot = System.getProperty("java.io.tmpdir");

		File tmpdirRoot = new File(tmproot);

		if (!tmpdirRoot.exists()) {
			throw new FileNotFoundException(tmproot);
		}

		File tmpdir = new File(tmpdirRoot, "SFSTestSuite");

		tmpdir.mkdirs();

		if (!tmpdir.exists()) {
			throw new FileNotFoundException(tmpdir.getAbsolutePath());
		}

		File file = new File(tmpdir, name);

		file.delete();

		OutputStream os = new FileOutputStream(file);
		os.write(content.getBytes("UTF-8"));
		os.close();

		file.deleteOnExit();

		return file.toURI();
	}

}
