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

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.ui.IConfigurationWizard;
import org.eclipse.ui.PlatformUI;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests Repository provider stuff
 * 
 */
public class TestsRepositoryProvider {
	/**
	 * 
	 * @throws CoreException
	 */
	@Test
	public void testRepositoryConfigurationWizard() throws CoreException {

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint =
			registry.getExtensionPoint("org.eclipse.team.ui.configurationWizards");
		for (IExtension extension : extensionPoint.getExtensions()) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				if ("wizard".equals(element.getName())) {
					final Object test = element.createExecutableExtension("class");

					Assert.assertNotNull("Wizard should not be null", test);

					IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

						public void run(IProgressMonitor monitor) throws CoreException {
							IConfigurationWizard cw = (IConfigurationWizard) test;
							IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("RepositoryProviderWizardTest");
							project.create(monitor);
							project.open(monitor);

							RepositoryProvider test = RepositoryProvider.getProvider(project, ISemanticFileSystem.SFS_REPOSITORY_PROVIDER);

							Assert.assertNull("Repository provider should not be mapped", test);

							cw.init(PlatformUI.getWorkbench(), project);
							cw.performFinish();

							test = RepositoryProvider.getProvider(project, ISemanticFileSystem.SFS_REPOSITORY_PROVIDER);

							Assert.assertNotNull("Repository provider should be mapped", test);

							project.close(monitor);
							project.delete(false, monitor);

						}
					};

					ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

					return;


				}
			}
		}

		Assert.fail("Should have been executed");
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPathToDb() throws Exception {
		ISemanticFileSystem sfs = (ISemanticFileSystem) EFS.getFileSystem(ISemanticFileSystem.SCHEME);
		Assert.assertNotNull("Path to db should not be null", sfs.getPathToDb());
	}

}
