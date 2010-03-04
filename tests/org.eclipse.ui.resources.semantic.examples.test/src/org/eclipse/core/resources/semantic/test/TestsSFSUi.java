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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.internal.resources.semantic.Util;
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
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.resources.semantic.examples.remote.RemoteFile;
import org.eclipse.core.resources.semantic.examples.remote.RemoteFolder;
import org.eclipse.core.resources.semantic.examples.remote.RemoteStoreTransient;
import org.eclipse.core.resources.semantic.test.provider.CachingTestContentProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.ui.history.IHistoryView;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.fileSystem.FileSystemContributor;
import org.eclipse.ui.internal.ide.filesystem.FileSystemConfiguration;
import org.eclipse.ui.internal.ide.filesystem.FileSystemSupportRegistry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * SFS UI tests
 * 
 */
@SuppressWarnings("restriction")
public class TestsSFSUi extends TestsContentProviderUtil {

	RemoteFile file1;

	/**
	 * Constructor
	 */
	public TestsSFSUi() {
		super(true, "TestsSemanticFileSystemUI", CachingTestContentProvider.class.getName());
	}

	@Override
	@Before
	public void beforeMethod() throws Exception {

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		this.testProject = workspace.getRoot().getProject(this.projectName);

		if (this.testProject.exists()) {
			throw new IllegalStateException("Project exists");
		}

		IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IProjectDescription description = workspace.newProjectDescription(TestsSFSUi.this.projectName);

				try {
					description.setLocationURI(new URI(ISemanticFileSystem.SCHEME + ":/" + TestsSFSUi.this.projectName));
				} catch (URISyntaxException e) {
					// really not likely, though
					throw new RuntimeException(e);
				}
				TestsSFSUi.this.testProject.create(description, monitor);
				TestsSFSUi.this.testProject.open(monitor);

				RemoteStoreTransient store = (RemoteStoreTransient) TestsSFSUi.this.testProject.getAdapter(RemoteStoreTransient.class);
				RemoteFolder f1 = store.getRootFolder().addFolder("Folder1");
				try {
					TestsSFSUi.this.file1 = f1.addFile("File1", "Hello".getBytes("UTF-8"), store.newTime());
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}

				f1.addFolder("Folder11");

				// for SFS, we map this to the team provider
				RepositoryProvider.map(TestsSFSUi.this.testProject, ISemanticFileSystem.SFS_REPOSITORY_PROVIDER);

				ISemanticProject spr = (ISemanticProject) TestsSFSUi.this.testProject.getAdapter(ISemanticProject.class);

				Map<QualifiedName, String> properties = new HashMap<QualifiedName, String>();
				properties.put(TEMPLATE_PROP, "World");

				spr.addFolder("root", TestsSFSUi.this.providerName, properties, TestsSFSUi.this.options, monitor);

				TestsSFSUi.this.testProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);

			}

		};

		workspace.run(myRunnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);

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

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(this.projectName);

		this.testProject = null;

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				project.delete(true, monitor);

				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

	}

	/**
	 * 
	 * @throws CoreException
	 */
	@Test
	public void testPropertiesPage() throws CoreException {

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder f1 = (ISemanticFolder) TestsSFSUi.this.testProject.getFolder("root").getFolder("Folder1").getAdapter(
						ISemanticFolder.class);
				f1.addFile("File1", ISemanticFileSystem.NONE, monitor);
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, null);

		Shell shell = new Shell(Display.getCurrent());

		PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(shell, this.testProject,
				"org.eclipse.core.resources.semantic.propertyPage", null, null);
		dialog.setBlockOnOpen(false);
		dialog.open();
		dialog.close();

		IFolder folder = this.testProject.getFolder("root").getFolder("Folder1");

		dialog = PreferencesUtil.createPropertyDialogOn(shell, folder, "org.eclipse.core.resources.semantic.propertyPage", null, null);
		dialog.setBlockOnOpen(false);
		dialog.open();
		dialog.close();

		IFile file = folder.getFile("File1");

		dialog = PreferencesUtil.createPropertyDialogOn(shell, file, "org.eclipse.core.resources.semantic.propertyPage", null, null);
		dialog.setBlockOnOpen(false);
		dialog.open();
		dialog.close();

	}

	/**
	 * 
	 */
	@Test
	public void testPreferencePage() {

		Shell shell = new Shell(Display.getCurrent());

		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(shell, "org.eclipse.core.resources.semantic.preferencePage",
				null, null);
		dialog.setBlockOnOpen(false);
		dialog.open();
		dialog.close();

	}

	/**
	 * 
	 * @throws CoreException
	 */
	@Test
	public void testBrowser() throws CoreException {

		IViewPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
				"org.eclipse.core.resources.semantic.resourceView");
		Assert.assertNotNull("View should not be null", part);

	}

	/**
	 * 
	 * @throws CoreException
	 */
	@Test
	public void testContributor() throws CoreException {

		FileSystemContributor contributor = null;
		FileSystemConfiguration[] configs = FileSystemSupportRegistry.getInstance().getConfigurations();
		for (FileSystemConfiguration config : configs) {
			if (config.getContributor().getClass().getName().equals(
					"org.eclipse.core.internal.resources.semantic.ui.SemanticFileSystemContributor")) {
				contributor = config.getContributor();
			}
		}
		if (contributor == null) {
			Assert.fail("Contributor not found");
			return;
		}

		URI test1 = contributor.getURI("/test/Uri");

		Assert.assertEquals("URI result", "semanticfs:/test/Uri", test1.toString());

		test1 = contributor.getURI("semanticfs:\\test/Uri2");

		Assert.assertEquals("URI result", "semanticfs:/test/Uri2", test1.toString());

		URI test = contributor.browseFileSystem("semanticfs:/" + this.testProject.getName() + "/" + "root", null);

		Assert.assertEquals("URI result", "semanticfs:/TestsSemanticFileSystemUI/root", test.toString());

	}

	// Actions

	/**
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testLockUnlockActions() throws Exception {

		// org.eclipse.core.internal.resources.semantic.ui.UnlockAction
		// org.eclipse.core.internal.resources.semantic.ui.LockAction

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder f1 = (ISemanticFolder) TestsSFSUi.this.testProject.getFolder("root").getFolder("Folder1").getAdapter(
						ISemanticFolder.class);
				f1.addFile("File1", ISemanticFileSystem.NONE, monitor);
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, null);

		ISemanticFile sFile = (ISemanticFile) this.testProject.getFile(new Path("root/Folder1/File1")).getAdapter(ISemanticFile.class);

		Assert.assertEquals("Lock state", false, sFile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_LOCKED, null).isLocked());

		runCommandByAction("LockCommand", sFile);

		Assert.assertEquals("Lock state", true, sFile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_LOCKED, null).isLocked());

		runCommandByAction("UnlockCommand", sFile);

		Assert.assertEquals("Lock state", false, sFile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_LOCKED, null).isLocked());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testRemoveAction() throws Exception {

		// org.eclipse.core.internal.resources.semantic.ui.RemoveAction

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder f1 = (ISemanticFolder) TestsSFSUi.this.testProject.getFolder("root").getFolder("Folder1").getAdapter(
						ISemanticFolder.class);
				f1.addFile("File1", ISemanticFileSystem.NONE, monitor);
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, null);

		ISemanticFile sFile = (ISemanticFile) this.testProject.getFile(new Path("root/Folder1/File1")).getAdapter(ISemanticFile.class);

		Assert.assertEquals("File existence", true, sFile.getAdaptedFile().exists());

		runCommandByAction("RemoveCommand", sFile);

		Assert.assertEquals("File existence", false, sFile.getAdaptedFile().exists());
		Assert.assertEquals("Remote File existence", true, sFile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_EXISTS_REMOTELY, null)
				.existsRemotely());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testHistoryAction() throws Exception {

		// org.eclipse.core.internal.resources.semantic.ui.HistoryAction

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder f1 = (ISemanticFolder) TestsSFSUi.this.testProject.getFolder("root").getFolder("Folder1").getAdapter(
						ISemanticFolder.class);
				f1.addFile("File1", ISemanticFileSystem.NONE, monitor);
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, null);

		ISemanticFile sFile = (ISemanticFile) this.testProject.getFile(new Path("root/Folder1/File1")).getAdapter(ISemanticFile.class);

		// make sure to hide the history view
		IViewPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(IHistoryView.VIEW_ID);

		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(part);

		final List<String> partIds = new ArrayList<String>();

		IPartListener2 listener = new IPartListener2() {

			public void partVisible(IWorkbenchPartReference partRef) {
				// nothing

			}

			public void partOpened(IWorkbenchPartReference partRef) {
				partIds.add(partRef.getId());

			}

			public void partInputChanged(IWorkbenchPartReference partRef) {
				// nothing

			}

			public void partHidden(IWorkbenchPartReference partRef) {
				// nothing

			}

			public void partDeactivated(IWorkbenchPartReference partRef) {
				// nothing

			}

			public void partClosed(IWorkbenchPartReference partRef) {
				// nothing

			}

			public void partBroughtToTop(IWorkbenchPartReference partRef) {
				// nothing

			}

			public void partActivated(IWorkbenchPartReference partRef) {
				// nothing

			}
		};

		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(listener);

		runCommandByAction("RemoteHistoryCommand", sFile);

		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().removePartListener(listener);

		Assert.assertTrue("History part should have been opened", partIds.contains(IHistoryView.VIEW_ID));

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	@Ignore
	// currently not working in less than millisecond timestamp environment
	public void testSynchronizeAction() throws Exception {

		// org.eclipse.core.internal.resources.semantic.ui.SynchronizeAction

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder f1 = (ISemanticFolder) TestsSFSUi.this.testProject.getFolder("root").getFolder("Folder1").getAdapter(
						ISemanticFolder.class);
				f1.addFile("File1", ISemanticFileSystem.NONE, monitor);
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, null);

		final ISemanticFile sFile = (ISemanticFile) this.testProject.getFile(new Path("root/Folder1/File1"))
				.getAdapter(ISemanticFile.class);

		assertContentsEqual(sFile.getAdaptedFile(), "Hello");

		// sync outbound

		runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {

				IStatus stat = sFile.validateEdit(null);

				Assert.assertTrue("Validate Edit should be ok", stat.isOK());

				try {
					sFile.getAdaptedFile().setContents(new ByteArrayInputStream("Test".getBytes("UTF-8")), IResource.NONE, null);
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}

			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, null);

		assertContentsEqual(sFile.getAdaptedFile(), "Test");

		runCommandByAction("SynchronizeCommand", sFile);

		Assert.assertEquals("Remote content", "Test", new String(this.file1.getContent(), "UTF-8"));

		// sync inbound

		byte[] bytes = "NewRemote".getBytes("UTF-8");
		OutputStream os = null;
		try {
			os = this.file1.getOutputStream(false);
			os.write(bytes, 0, bytes.length);
		} finally {
			Util.safeClose(os);
		}

		Assert.assertEquals("Remote content", "NewRemote", new String(this.file1.getContent(), "UTF-8"));

		runCommandByAction("SynchronizeCommand", sFile);

		assertContentsEqual(sFile.getAdaptedFile(), "NewRemote");

	}

	/**
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testDeleteAction() throws Exception {

		// org.eclipse.core.internal.resources.semantic.ui.DeleteAction

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder f1 = (ISemanticFolder) TestsSFSUi.this.testProject.getFolder("root").getFolder("Folder1").getAdapter(
						ISemanticFolder.class);
				f1.addFile("File1", ISemanticFileSystem.NONE, monitor);
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, null);

		ISemanticFile sFile = (ISemanticFile) this.testProject.getFile(new Path("root/Folder1/File1")).getAdapter(ISemanticFile.class);

		Assert.assertEquals("File existence", true, sFile.getAdaptedFile().exists());

		runCommandByAction("DeleteCommand", sFile);

		Assert.assertEquals("File existence", false, sFile.getAdaptedFile().exists());
		Assert.assertEquals("Remote File existence", false, sFile
				.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_EXISTS_REMOTELY, null).existsRemotely());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testDiffAction() throws Exception {

		// org.eclipse.core.internal.resources.semantic.ui.DeleteAction

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder f1 = (ISemanticFolder) TestsSFSUi.this.testProject.getFolder("root").getFolder("Folder1").getAdapter(
						ISemanticFolder.class);
				f1.addFile("File1", ISemanticFileSystem.NONE, monitor);
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, null);

		ISemanticFile sFile = (ISemanticFile) this.testProject.getFile(new Path("root/Folder1/File1")).getAdapter(ISemanticFile.class);

		Assert.assertEquals("File existence", true, sFile.getAdaptedFile().exists());

		runCommandByAction("DiffCommand", sFile);

		boolean finished = false;
		int counter = 0;

		while (!finished) {
			for (int i = 0; i < 10; i++) {
				Thread.sleep(100);
				while (Display.getCurrent().readAndDispatch()) {
					// do nothing
				}
			}

			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			for (IWorkbenchWindow iWorkbenchWindow : windows) {
				IWorkbenchPage[] pages = iWorkbenchWindow.getPages();

				for (IWorkbenchPage iWorkbenchPage : pages) {
					IEditorReference[] eRefs = iWorkbenchPage.findEditors(null, "org.eclipse.compare.CompareEditor",
							IWorkbenchPage.MATCH_ID);
					for (IEditorReference iEditorReference : eRefs) {
						IEditorInput eInput = iEditorReference.getEditorInput();

						if (eInput instanceof CompareEditorInput) {
							if (((CompareEditorInput) eInput).getCompareResult() != null) {
								finished = true;
							}
						}
					}
				}
			}
			counter++;

			if (counter > 10) {
				Assert.assertTrue("DiffEditor still not ready", false);
				break;
			}
		}

		Assert.assertEquals("File existence", true, sFile.getAdaptedFile().exists());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testEditRevertActions() throws Exception {

		// org.eclipse.core.internal.resources.semantic.ui.EditAction
		// org.eclipse.core.internal.resources.semantic.ui.RevertAction

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				ISemanticFolder f1 = (ISemanticFolder) TestsSFSUi.this.testProject.getFolder("root").getFolder("Folder1").getAdapter(
						ISemanticFolder.class);
				f1.addFile("File1", ISemanticFileSystem.NONE, monitor);
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, null);

		ISemanticFile sFile = (ISemanticFile) this.testProject.getFile(new Path("root/Folder1/File1")).getAdapter(ISemanticFile.class);

		Assert.assertEquals("Read-only state", true, sFile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_READ_ONLY, null)
				.isReadOnly());

		runCommandByAction("OpenForEditCommand", sFile);

		Assert.assertEquals("Read-only state", false, sFile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_READ_ONLY, null)
				.isReadOnly());

		runCommandByAction("RevertCommand", sFile);

		Assert.assertEquals("Read-only state", true, sFile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_READ_ONLY, null)
				.isReadOnly());

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUnmapAction() throws Exception {

		// org.eclipse.core.internal.resources.semantic.ui.UnmapAction

		ISemanticProject project = (ISemanticProject) this.testProject.getAdapter(ISemanticProject.class);

		Assert.assertNotNull("Mapping", RepositoryProvider.getProvider(this.testProject, ISemanticFileSystem.SFS_REPOSITORY_PROVIDER));

		runCommandByAction("UnshareCommand", project);

		Assert.assertNull("Mapping", RepositoryProvider.getProvider(this.testProject, ISemanticFileSystem.SFS_REPOSITORY_PROVIDER));

	}

	private void runCommandByAction(final String actionName, final ISemanticResource resource) throws Exception {

		// make sure project explorer is selected
		IViewPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
				"org.eclipse.ui.navigator.ProjectExplorer"); // Project
		// Explorer
		// view
		// id is
		// hard-coded
		// for
		// 3.4
		// compatibility

		ICommandService csrv = (ICommandService) part.getSite().getService(ICommandService.class);
		Command commandToRun = csrv.getCommand("org.eclipse.core.resources.semantic.ui." + actionName);

		if (commandToRun == null || !commandToRun.isDefined()) {
			throw new RuntimeException("Command not found for " + actionName);
		}

		IHandlerService srv = (IHandlerService) part.getSite().getService(IHandlerService.class);
		IEvaluationContext ctx = srv.createContextSnapshot(true);
		ctx.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, new StructuredSelection(resource));
		ctx.addVariable(ISources.ACTIVE_MENU_SELECTION_NAME, new StructuredSelection(resource));

		ExecutionEvent event = new ExecutionEvent(commandToRun, new HashMap<String, String>(), resource, ctx);
		commandToRun.executeWithChecks(event);

	}

}
