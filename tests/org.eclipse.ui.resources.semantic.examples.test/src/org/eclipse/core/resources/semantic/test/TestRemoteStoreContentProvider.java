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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticProject;
import org.eclipse.core.resources.semantic.examples.providers.RemoteStoreContentProvider;
import org.eclipse.core.resources.semantic.examples.remote.RemoteFile;
import org.eclipse.core.resources.semantic.examples.remote.RemoteFolder;
import org.eclipse.core.resources.semantic.examples.remote.RemoteStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.RepositoryProvider;
import org.junit.After;
import org.junit.Before;

/**
 *
 */
public class TestRemoteStoreContentProvider extends TestsContentProviderBase {

	/**
	 * 
	 */
	public TestRemoteStoreContentProvider() {
		super(false, "TestRemoteStoreContentProvider", RemoteStoreContentProvider.class.getName());
	}

	@Override
	public RemoteFile getRemoteFile() {
		RemoteStore store = (RemoteStore) testProject.getAdapter(RemoteStore.class);
		return (RemoteFile) store.getItemByPath(new Path("Folder1/File1"));
	}

	@Override
	@Before
	public void beforeMethod() throws Exception {

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(this.projectName);

		if (project.exists()) {
			throw new IllegalStateException("Project exists");
		}

		IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IProjectDescription description = workspace.newProjectDescription(TestRemoteStoreContentProvider.this.projectName);

				try {
					description
							.setLocationURI(new URI(ISemanticFileSystem.SCHEME + ":/" + TestRemoteStoreContentProvider.this.projectName));
				} catch (URISyntaxException e) {
					// really not likely, though
					throw new RuntimeException(e);
				}
				project.create(description, monitor);
				project.open(monitor);

				// for SFS, we map this to the team provider
				RepositoryProvider.map(project, ISemanticFileSystem.SFS_REPOSITORY_PROVIDER);

				ISemanticProject spr = (ISemanticProject) project.getAdapter(ISemanticProject.class);

				RemoteStore store = (RemoteStore) project.getAdapter(RemoteStore.class);
				store.reset();
				RemoteFolder f1 = store.getRootFolder().addFolder("Folder1");
				f1.addFolder("Folder11");

				try {
					f1.addFile("File1", "Hello".getBytes("UTF-8"), store.newTime());
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}

				store.serialize(monitor);

				Map<QualifiedName, String> properties = new HashMap<QualifiedName, String>();
				properties.put(TEMPLATE_PROP, "World");

				spr.addFolder("root", TestRemoteStoreContentProvider.this.providerName, properties,
						TestRemoteStoreContentProvider.this.options, monitor);

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

	@Override
	@After
	public void afterMethod() throws Exception {

		RemoteStore store = (RemoteStore) this.testProject.getAdapter(RemoteStore.class);
		store.reset();
		store.serialize(null);

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
}
