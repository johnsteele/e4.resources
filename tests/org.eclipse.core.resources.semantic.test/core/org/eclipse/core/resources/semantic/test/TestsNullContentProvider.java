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

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.resources.semantic.ISemanticResourceInfo;
import org.eclipse.core.resources.semantic.test.provider.NullContentProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the content provider base class
 * 
 */
public class TestsNullContentProvider extends TestsContentProviderUtil {

	/**
	 * Constructor
	 */
	public TestsNullContentProvider(){
		super(true, "NullContentProvider", NullContentProvider.class.getName());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNonLockingNonLocal() throws Exception {

		final ISemanticFolder sf = (ISemanticFolder) this.testProject.getFolder("root").getAdapter(ISemanticFolder.class);
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					ISemanticResourceInfo inf = sf.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_LOCKING_SUPPORTED, monitor);
					Assert.assertFalse("Locking should not be supported", inf.isLockingSupported());
				} catch (CoreException e1) {
					// $JL-EXC$ expected
				}

				IStatus stat = sf.lockResource(TestsNullContentProvider.this.options, monitor);
				Assert.assertFalse("Locking should have failed", stat.isOK());

				stat = sf.unlockResource(TestsNullContentProvider.this.options, monitor);
				Assert.assertFalse("Unlocking should have failed", stat.isOK());

				try {
					TestsNullContentProvider.this.testProject.getFolder("root").getFile("New").create(
							new ByteArrayInputStream(new byte[] { 1, 2, 3 }), true, monitor);
					Assert.fail("Should have failed");
				} catch (CoreException e) {
					// $JL-EXC$ expected
				}

			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());

	}

}
