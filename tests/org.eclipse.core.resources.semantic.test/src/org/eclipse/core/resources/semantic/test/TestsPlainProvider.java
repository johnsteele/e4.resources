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

import org.eclipse.core.resources.semantic.test.provider.PlainTestContentProvider;

/**
 * Tests a non-caching content provider
 * 
 */
public class TestsPlainProvider extends TestsContentProviderBase {
	/**
	 * Constructor
	 */
	public TestsPlainProvider() {
		super(true, "TestPlainProvider", PlainTestContentProvider.class.getName());
	}

	/**
	 * Overwritten since it doesn't work in non-caching providers
	 */
	public void testChangeFileContentLocalAndRevert() throws Exception {
		// TODO this doesn't work in a non-caching provider; change the RemoteFile implementation
		// so that it implements revert
	}

}
