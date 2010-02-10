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
package org.eclipse.core.internal.resources.semantic.cacheservice;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.core.internal.resources.semantic.cacheservice.messages"; //$NON-NLS-1$
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		//
	}

	public static String CachingOutputStream_CouldNotSkip_XMSG;
	public static String FileHandleFactory_CacheFileNotDeleted_XMSG;
	public static String FileHandleFactory_FileHandleFactory_FileHandleFactory_SaveError_XMSG;
	public static String FileHandleFactory_FileHandleFactory_LoadError_XMSG;
	public static String FileHandleFactory_TempFileNotRenamed_XMSG;
	public static String TemporaryFileHandle_OsCloseErrorOnCommit_XMSG;
	public static String TemporaryFileHandle_TimstampSetOnCommit_XMSG;
}
