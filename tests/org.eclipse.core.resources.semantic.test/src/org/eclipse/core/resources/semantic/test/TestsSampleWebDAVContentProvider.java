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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.examples.providers.SampleWebDAVContentProvider;
import org.eclipse.core.resources.semantic.examples.webdav.WebDAVUtil;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.junit.Test;

public class TestsSampleWebDAVContentProvider extends TestsContentProviderUtil {

	protected static final String WEBDAV_TEST_URL = "http://localhost:2080/testWebDAV";

	IWorkspace workspace = ResourcesPlugin.getWorkspace();

	public TestsSampleWebDAVContentProvider() {
		this(true);
	}

	TestsSampleWebDAVContentProvider(boolean withAutoRefresh) {
		super(withAutoRefresh, "TestsSampleWebDAVContentProvider", SampleWebDAVContentProvider.class.getName());
	}

	TestsSampleWebDAVContentProvider(boolean withAutoRefresh, String projectName, String providerName) {
		super(withAutoRefresh, projectName, providerName);
	}

	@Test
	public void testAddWebDAVFolder() throws Exception {
		final IFolder folder = this.testProject.getFolder("root");
		final int[] counters = new int[3];

		IResourceChangeListener listener = new IResourceChangeListener() {

			public void resourceChanged(IResourceChangeEvent event) {
				IResourceDelta delta = event.getDelta();

				if (delta != null) {
					IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
						public boolean visit(IResourceDelta delta1) {
							switch (delta1.getKind()) {
								case IResourceDelta.ADDED :
									// handle added resource
									counters[0]++;
									break;
								case IResourceDelta.REMOVED :
									// handle removed resource
									counters[1]++;
									break;
								case IResourceDelta.CHANGED :
									// handle changed resource
									counters[2]++;
									break;
							}
							return true;
						}
					};

					try {
						delta.accept(visitor);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};

		workspace.addResourceChangeListener(listener);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(final IProgressMonitor monitor) throws CoreException {

				ISemanticFolder sFolder = (ISemanticFolder) folder.getAdapter(ISemanticFolder.class);
				try {
					sFolder.addFolder("test", new URI(WEBDAV_TEST_URL), options, monitor);

					folder.accept(new IResourceVisitor() {

						public boolean visit(IResource resource) {
							if (resource instanceof IFile) {
								IFile file = (IFile) resource;

								try {
									Util.safeClose(file.getContents());
									monitor.worked(1);
								} catch (CoreException e) {
									// ignore
								} catch (Exception e) {
									Assert.assertTrue("should not fail", false);
								}
							}
							return true;
						}
					});
				} catch (URISyntaxException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				}
			}
		};

		workspace.run(runnable, testProject, 0, new NullProgressMonitor());

		Assert.assertTrue(folder.getFolder("test").exists());

		Assert.assertTrue("Added", counters[0] > 0);
		Assert.assertTrue("Deleted", counters[1] == 0);
		Assert.assertTrue("Changed", counters[2] > 0);

		for (int j = 0; j < counters.length; j++) {
			counters[j] = 0;
		}

		IWorkspaceRunnable runnable2 = new IWorkspaceRunnable() {

			public void run(final IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sFolder = (ISemanticFolder) folder.getAdapter(ISemanticFolder.class);
				sFolder.synchronizeContentWithRemote(SyncDirection.INCOMING, options, monitor);
			}
		};

		workspace.run(runnable2, testProject, 0, new NullProgressMonitor());

		validateAndResetCounters(counters);

		IWorkspaceRunnable runnable3 = new IWorkspaceRunnable() {

			public void run(final IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sFolder = (ISemanticFolder) folder.getFolder("test").getAdapter(ISemanticFolder.class);
				sFolder.synchronizeContentWithRemote(SyncDirection.INCOMING, options, monitor);
			}
		};

		workspace.run(runnable3, testProject, 0, new NullProgressMonitor());

		validateAndResetCounters(counters);

		IWorkspaceRunnable runnable4 = new IWorkspaceRunnable() {

			public void run(final IProgressMonitor monitor) throws CoreException {
				ISemanticFolder sFolder = (ISemanticFolder) folder.getFolder("test").getFolder("src").getAdapter(ISemanticFolder.class);
				sFolder.synchronizeContentWithRemote(SyncDirection.INCOMING, options, monitor);
			}
		};

		workspace.run(runnable4, testProject, 0, new NullProgressMonitor());

		validateAndResetCounters(counters);

		workspace.removeResourceChangeListener(listener);
	}

	@Test
	public void testWebDAVAsLinkedFolder() throws Exception {
		final String linkedProjectName = "testWebDAVAsLinkedFolder";

		final IProject project = workspace.getRoot().getProject(linkedProjectName);

		if (project.exists()) {
			throw new IllegalStateException("Project exists");
		}

		IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IProjectDescription description = workspace.newProjectDescription(linkedProjectName);

				project.create(description, monitor);
				project.open(monitor);
			}
		};

		workspace.run(myRunnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);

		final IFolder folder = project.getFolder("test");

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				URI uri = null;
				try {
					uri = new URI("semanticfs", null, "/webdav/test", "type=folder;create=true;provider="
							+ SampleWebDAVContentProvider.class.getName() + ";uri=" + WEBDAV_TEST_URL, null);

					folder.createLink(uri, 0, monitor);

					ISemanticFolder sFolder = (ISemanticFolder) folder.getAdapter(ISemanticFolder.class);

					Assert.assertEquals(SampleWebDAVContentProvider.class.getName(), sFolder.getContentProviderID());

					sFolder.synchronizeContentWithRemote(SyncDirection.INCOMING, options, monitor);

					IResource[] members = folder.members();

					Assert.assertTrue(members.length > 0);

					IFile file = folder.getFile("test.txt");

					ResourceAttributes attributes = file.getResourceAttributes();
					attributes.setReadOnly(false);
					file.setResourceAttributes(attributes);

					file.setContents(new ByteArrayInputStream("xxx".getBytes("UTF-8")), true, false, monitor);

					sFolder.synchronizeContentWithRemote(SyncDirection.OUTGOING, options, monitor);

					ISemanticFile sFile = (ISemanticFile) file.getAdapter(ISemanticFile.class);

					IStatus status = sFile.lockResource(options, monitor);

					Assert.assertTrue(status.isOK());

					status = sFile.unlockResource(options, monitor);

					Assert.assertTrue(status.isOK());

					try {
						boolean isFolder = WebDAVUtil.checkWebDAVURL(new URI(WEBDAV_TEST_URL), monitor);

						Assert.assertTrue(isFolder);

						isFolder = WebDAVUtil.checkWebDAVURL(new URI(WEBDAV_TEST_URL + "/test.txt"), monitor);

						Assert.assertFalse(isFolder);
					} catch (IOException e) {
						throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
					} catch (URISyntaxException e) {
						throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
					}

					try {
						WebDAVUtil.checkWebDAVURL(new URI(WEBDAV_TEST_URL + "/xxx.xxx"), monitor);

						Assert.assertTrue("Should have failed", false);
					} catch (IOException e) {
						// ignore
					} catch (URISyntaxException e) {
						throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
					}

				} catch (URISyntaxException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				} catch (UnsupportedEncodingException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				} finally {
					cleanupSFS(uri, monitor);
				}
			}
		};

		workspace.run(runnable, workspace.getRuleFactory().refreshRule(folder), 0, new NullProgressMonitor());

	}

	@Test
	public void testWebDAVAsLinkedFile() throws Exception {
		final String linkedProjectName = "testWebDAVAsLinkedFile";

		final IProject project = workspace.getRoot().getProject(linkedProjectName);

		if (project.exists()) {
			throw new IllegalStateException("Project exists");
		}

		IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IProjectDescription description = workspace.newProjectDescription(linkedProjectName);

				project.create(description, monitor);
				project.open(monitor);
			}
		};

		workspace.run(myRunnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);

		final IFile file = project.getFile("test.txt");

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				URI uri = null;
				try {
					uri = new URI("semanticfs", null, "/webdav/test.txt", "type=file;create=true;provider="
							+ SampleWebDAVContentProvider.class.getName() + ";uri=" + WEBDAV_TEST_URL + "/test.txt", null);

					file.createLink(uri, 0, monitor);

					ISemanticFile sFile = (ISemanticFile) file.getAdapter(ISemanticFile.class);

					Assert.assertEquals(SampleWebDAVContentProvider.class.getName(), sFile.getContentProviderID());

					sFile.synchronizeContentWithRemote(SyncDirection.INCOMING, options, monitor);

					sFile.synchronizeContentWithRemote(SyncDirection.OUTGOING, options, monitor);

				} catch (URISyntaxException e) {
					throw new CoreException(new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, e.getMessage(), e));
				} finally {
					cleanupSFS(uri, monitor);
				}
			}
		};

		workspace.run(runnable, workspace.getRuleFactory().refreshRule(file), 0, new NullProgressMonitor());

	}

	void cleanupSFS(URI uri, IProgressMonitor monitor) throws CoreException {
		if (uri != null) {
			IFileStore store = EFS.getStore(uri);

			if (!store.fetchInfo(EFS.NONE, monitor).exists()) {
				store.delete(options, monitor);
			}
		}
	}

	private void validateAndResetCounters(final int[] counters) {
		Assert.assertTrue("Added", counters[0] == 0);
		Assert.assertTrue("Deleted", counters[1] == 0);
		Assert.assertTrue("Changed", counters[2] == 0);

		for (int j = 0; j < counters.length; j++) {
			counters[j] = 0;
		}
	}
}
