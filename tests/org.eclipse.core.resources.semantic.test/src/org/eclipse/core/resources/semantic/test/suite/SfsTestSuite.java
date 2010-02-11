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
package org.eclipse.core.resources.semantic.test.suite;

import org.eclipse.core.resources.semantic.test.TestCacheService;
import org.eclipse.core.resources.semantic.test.TestsCachingProvider;
import org.eclipse.core.resources.semantic.test.TestsDefaultContentProvider;
import org.eclipse.core.resources.semantic.test.TestsFederatingProvider;
import org.eclipse.core.resources.semantic.test.TestsMemoryCachingProvider;
import org.eclipse.core.resources.semantic.test.TestsNullContentProvider;
import org.eclipse.core.resources.semantic.test.TestsPlainProvider;
import org.eclipse.core.resources.semantic.test.TestsRepositoryProvider;
import org.eclipse.core.resources.semantic.test.TestsRestContentProvider;
import org.eclipse.core.resources.semantic.test.TestsSFSUi;
import org.eclipse.core.resources.semantic.test.TestsSampleCompositeResourceProvider;
import org.eclipse.core.resources.semantic.test.TestsSynchronization;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * All SFS tests
 */
@RunWith(Suite.class)
@Suite.SuiteClasses( { TestsDefaultContentProvider.class, //
	TestsPlainProvider.class, //
	TestsRestContentProvider.class,//
	TestsCachingProvider.class,//
	TestsFederatingProvider.class,//
	TestsMemoryCachingProvider.class,//
	TestsNullContentProvider.class,//
	TestsRepositoryProvider.class,//
		TestsSampleCompositeResourceProvider.class,//
	TestsSFSUi.class,//
	TestsSynchronization.class,//
	TestCacheService.class

})
public class SfsTestSuite {
	// the suite
}