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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticProject;
import org.eclipse.core.resources.semantic.examples.remote.RemoteFolder;
import org.eclipse.core.resources.semantic.examples.remote.RemoteStoreTransient;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.RepositoryProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Some basic functionality for (almost) all SFS tests
 * <p>
 * Initializes the trace
 * 
 */
public abstract class TestsContentProviderUtil {

	public static final QualifiedName TEMPLATE_PROP = new QualifiedName(TestPlugin.PLUGIN_ID, "Hello");
	public static final QualifiedName DUMMY_PROP = new QualifiedName(TestPlugin.PLUGIN_ID, "Dummy1");
	public static final QualifiedName DUMMY_PROP2 = new QualifiedName(TestPlugin.PLUGIN_ID, "Dummy2");

	public final String projectName;
	public final String providerName;

	public IProject testProject;
	public final int options;
	public final boolean autoRefresh;

	/**
	 * The constructor
	 * 
	 * @param withAutoRefresh
	 *            if auto refresh should be used
	 * @param projectName
	 *            the project name
	 * @param providerName
	 *            the id of the content provider
	 */
	public TestsContentProviderUtil(boolean withAutoRefresh, String projectName, String providerName) {
		this.projectName = projectName;
		this.providerName = providerName;
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
		initTrace();
	}

	/**
	 * Initializes tracing
	 */
	public static void initTrace() {
		// currently not used
	}

	/**
	 * Resets tracing
	 */
	public static void resetTrace() {
		// currently not used
	}

	/**
	 * Resets the trace locations
	 * 
	 * @throws Exception
	 */
	public static void afterClass() throws Exception {
		// reset traces
		resetTrace();
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
				IProjectDescription description = workspace.newProjectDescription(TestsContentProviderUtil.this.projectName);

				try {
					description.setLocationURI(new URI(ISemanticFileSystem.SCHEME + ":/" + TestsContentProviderUtil.this.projectName));
				} catch (URISyntaxException e) {
					// really not likely, though
					throw new RuntimeException(e);
				}
				project.create(description, monitor);
				project.open(monitor);

				RemoteStoreTransient store = (RemoteStoreTransient) project.getAdapter(RemoteStoreTransient.class);
				RemoteFolder f1 = store.getRootFolder().addFolder("Folder1");
				f1.addFolder("Folder11");
				try {
					f1.addFile("File1", "Hello World".getBytes("UTF-8"), System.currentTimeMillis());
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}

				// for SFS, we map this to the team provider
				RepositoryProvider.map(project, ISemanticFileSystem.SFS_REPOSITORY_PROVIDER);

				ISemanticProject spr = (ISemanticProject) project.getAdapter(ISemanticProject.class);

				Map<QualifiedName, String> properties = new HashMap<QualifiedName, String>();
				properties.put(TEMPLATE_PROP, "World");

				spr.addFolder("root", TestsContentProviderUtil.this.providerName, properties, TestsContentProviderUtil.this.options,
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

	/**
	 * Deletes the test project and resets the remote repository
	 * 
	 * @throws Exception
	 */
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

	public void assertContentsEqual(IFile file, String test) {
		InputStream is = null;
		try {
			is = file.getContents();
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			Assert.assertEquals("Wrong content", test, new String(buffer, "UTF-8"));

		} catch (Exception e) {
			// $JL-EXC$
			Assert.fail("Exception getting file content: " + e.getMessage());
		} finally {
			Util.safeClose(is);
		}
	}

	public void assertContentsNotEqual(IFile file, String test) {
		InputStream is = null;
		try {
			is = file.getContents();
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			if (new String(buffer, "UTF-8").equals(test)) {
				Assert.fail("Contents should differ");
			}

		} catch (Exception e) {
			// $JL-EXC$
			Assert.fail("Exception getting file content: " + e.getMessage());
		} finally {
			Util.safeClose(is);
		}
	}

	public File createTestFile(String fileName) {
		String tmpdir = System.getProperty("java.io.tmpdir");
		File file = new File(tmpdir, fileName);
		file.deleteOnExit();
		return file;
	}

	public URI createURI4File(File file) throws URISyntaxException {
		String filepath = file.getAbsolutePath().replace('\\', '/');

		// Handle differences between Windows and UNIX
		if (!filepath.startsWith("/")) {
			filepath = "/" + filepath;
		}

		return new URI("file", "", filepath, null);
	}

	public void writeContentsToFile(File testfile, String contents, String encoding) throws FileNotFoundException, IOException,
			UnsupportedEncodingException {
		OutputStream os = new FileOutputStream(testfile);

		try {
			os.write(contents.getBytes(encoding));
		} finally {
			Util.safeClose(os);
		}
	}
}
