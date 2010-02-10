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
package org.eclipse.core.resources.semantic.spi;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.core.resources.semantic.spi.messages"; //$NON-NLS-1$
	public static String CachingContentProvider_DeletingCache_XMSG;
	public static String CachingContentProvider_FillCache_XGRP;
	public static String CachingContentProvider_TimestampNotInCache_XMSG;
	public static String SemanticSpiResourceInfo_OptionNotRequested_XMSG;
	public static String Util_TransferRead_XMSG;
	public static String Util_TransferWrite_XMSG;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		//
	}
}
