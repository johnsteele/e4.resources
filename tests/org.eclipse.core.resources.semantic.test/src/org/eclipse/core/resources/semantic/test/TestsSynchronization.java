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

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.resources.semantic.test.provider.CachingTestContentProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.IConfigurationWizard;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests synchronization
 */
@SuppressWarnings("restriction")
public class TestsSynchronization extends TestsContentProviderUtil {
	/**
	 * Constructor
	 */
	public TestsSynchronization() {
		super(true, "SFSTestsSynchronization", CachingTestContentProvider.class.getName());
	}

	/**
	 * @throws CoreException
	 */
	@Test
	public void testSynchronizationWizard() throws CoreException {

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint =
			registry.getExtensionPoint("org.eclipse.team.ui.synchronizeWizards");
		for (IExtension extension : extensionPoint.getExtensions()) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				if ("wizard".equals(element.getName())) {
					final Object test = element.createExecutableExtension("class");

					Assert.assertNotNull("Wizard should not be null", test);

					IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

						public void run(IProgressMonitor monitor) throws CoreException {

							// we make a file so that there is some input
							ISemanticFolder sf = (ISemanticFolder) TestsSynchronization.this.testProject.getFolder("root").getFolder(
							"Folder1").getAdapter(ISemanticFolder.class);
							sf.addFile("File1", ISemanticFileSystem.NONE, monitor);
							// make sure to suppress the "switch perspective"-popup
							String nonePerspective = "org.eclipse.team.ui.sync_view_perspective_none";
							String perspectiveId = "org.eclipse.team.ui.syncview_default_perspective";
							TeamUIPlugin.getPlugin().getPreferenceStore().setValue(perspectiveId, nonePerspective);

							IConfigurationWizard cw = (IConfigurationWizard) test;

							WizardDialog wd = new WizardDialog(Display.getDefault().getActiveShell(), cw);
							wd.setBlockOnOpen(false);
							wd.open();
							cw.performFinish();
							wd.close();


						}
					};

					ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

					return;

				}
			}
		}

		Assert.fail("Should have been executed");
	}

}
