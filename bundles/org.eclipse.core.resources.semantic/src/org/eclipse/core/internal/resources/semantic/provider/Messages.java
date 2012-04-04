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
package org.eclipse.core.internal.resources.semantic.provider;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.core.internal.resources.semantic.provider.messages"; //$NON-NLS-1$
	public static String ContentProvider_NotSupported_XMSG;
	public static String DefaultContentProvider_CacheFillError_XMSG;
	public static String DefaultContentProvider_NoRemoteEdit_XMSG;
	public static String DefaultContentProvider_NotSupported_XMSG;
	public static String DefaultContentProvider_RemotURINotSet_XMSG;
	public static String DefaultContentProvider_UnknownHostError_XMSG;
	public static String DefaultContentProvider_ValidateEditResult_XGRP;
	public static String InvalidContentProvider_InvalidContentProviderIDforPath_XMSG;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		//
	}
}
