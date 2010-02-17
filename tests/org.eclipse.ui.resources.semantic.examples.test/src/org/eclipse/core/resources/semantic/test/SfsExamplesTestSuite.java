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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * All SFS tests
 */
@RunWith(Suite.class)
@Suite.SuiteClasses( { TestsRestContentProvider.class, //
		TestsSampleCompositeResourceProvider.class,//
		TestsSynchronization.class,//
		TestsRepositoryProvider.class,//
		TestsMemoryCachingProvider.class,//
		TestsSFSUi.class })
public class SfsExamplesTestSuite {
	// the suite
}
