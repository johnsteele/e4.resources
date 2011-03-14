/*******************************************************************************
 * Copyright (c) 2011 SAP AG.
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

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.resources.semantic.examples.remote.RemoteFile;
import org.eclipse.core.resources.semantic.examples.remote.RemoteStoreTransient;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.resources.semantic.test.provider.CachingTestContentProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

/**
 * Caching Content provider tests that use autorefresh
 * 
 */
public class TestsCachingProvider2 extends TestsContentProviderBase {

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
	public TestsCachingProvider2() {
		super(true, "CachingTests", CachingTestContentProvider.class.getName());
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
				ISemanticFile sfile = sfr.addFile("File1", TestsCachingProvider2.this.options, monitor);
				Assert.assertTrue(sfile.getAdaptedFile().equals(file));
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
					ISemanticFile sFile = (ISemanticFile) file.getAdapter(ISemanticFile.class);
					try {
						sFile.remove(TestsCachingProvider2.this.options, monitor);
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
					ISemanticFile sfile = sfr.addFile("File1", TestsCachingProvider2.this.options, monitor);
					// TODO verify that other operations fail
					// (write/read/timestamp)
					Assert.assertTrue(sfile.getAdaptedFile().equals(file));
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

}
