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
import java.io.UnsupportedEncodingException;

import junit.framework.Assert;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.spi.ISemanticFileHistoryProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.test.provider.MemoryCachingTestContentProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.ui.history.HistoryPageSource;
import org.eclipse.team.ui.history.IHistoryView;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

/**
 * Tests memory caching
 * 
 */
public class TestsMemoryCachingProvider extends TestsContentProviderBase {
	/**
	 * Constructor
	 */
	public TestsMemoryCachingProvider() {
		super(true, "MemoryCachingTests", MemoryCachingTestContentProvider.class.getName());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testHistoryProvider() throws Exception {

		final IFolder root = this.testProject.getFolder("root");
		final IFolder parent = root.getFolder("Folder1");
		final IFile file = parent.getFile("File1");
		final ISemanticFolder sfolder = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
		final ISemanticFile sfile = (ISemanticFile) file.getAdapter(ISemanticFile.class);

		final ISemanticFileStore sfs = (ISemanticFileStore) EFS.getStore(file.getLocationURI());
		final ISemanticFileHistoryProvider provider = (ISemanticFileHistoryProvider) sfs.getEffectiveContentProvider().getAdapter(
				ISemanticFileHistoryProvider.class);

		if (provider != null) {
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

				public void run(IProgressMonitor monitor) throws CoreException {

					IViewPart histView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
							IHistoryView.VIEW_ID);

					((IHistoryView) histView).showHistoryFor(file);

					sfolder.addFile("File1", TestsMemoryCachingProvider.this.options, monitor);
					parent.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					IFileHistory hist = provider.getHistoryFor(sfs, EFS.NONE, monitor);
					Assert.assertNotNull("Should return a history", hist);
					Assert.assertTrue("Should have one history entry", hist.getFileRevisions().length == 1);

					sfile.validateEdit(null);

					try {
						file.setContents(new ByteArrayInputStream("New".getBytes("UTF-8")), EFS.NONE, monitor);
					} catch (UnsupportedEncodingException e) {
						// $JL-EXC$
						throw new RuntimeException(e.getMessage());
					}
					sfile.synchronizeContentWithRemote(SyncDirection.OUTGOING,
							TestsMemoryCachingProvider.this.options, monitor);
					file.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);

					hist = provider.getHistoryFor(sfs, EFS.NONE, monitor);
					Assert.assertTrue("Should have two history entries", hist.getFileRevisions().length == 2);

					RepositoryProvider rProvivder = RepositoryProvider.getProvider(TestsMemoryCachingProvider.this.testProject);

					IFileHistory rHist = rProvivder.getFileHistoryProvider().getFileHistoryFor(sfs, 0, monitor);
					Assert.assertTrue("Should have two history entries", rHist.getFileRevisions().length == 2);

					rHist = rProvivder.getFileHistoryProvider().getFileHistoryFor(file, 0, monitor);
					Assert.assertTrue("Should have two history entries", rHist.getFileRevisions().length == 2);

					IFileRevision wrev = rProvivder.getFileHistoryProvider().getWorkspaceFileRevision(file);
					Assert.assertNotNull("Workspace version should not be null", wrev);

					boolean canShow = HistoryPageSource.getHistoryPageSource(file).canShowHistoryFor(file);

					Assert.assertTrue("Should be able to show history view", canShow);

					IViewPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
							IHistoryView.VIEW_ID);

					Assert.assertNotNull("History page part should not be null", part);

					IViewReference ref = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(
							IHistoryView.VIEW_ID);

					Assert.assertNotNull("View reference must not be null", ref);

				}
			};

			ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

			runnable = new IWorkspaceRunnable() {

				public void run(IProgressMonitor monitor) throws CoreException {
					sfolder.remove(TestsMemoryCachingProvider.this.options, monitor);
					file.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			};

			ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

			Assert.assertEquals("File existence", false, file.exists());

		} else {
			Assert.fail("No Histoy provider");
		}
	}

}
