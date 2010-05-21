/*******************************************************************************
 * Copyright (c) 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eduard Bartsch (SAP AG) - initial API and implementation
 *    Mathias Kinzler (SAP AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources.semantic.examples.webdav;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.core.resources.semantic.examples.webdav.messages"; //$NON-NLS-1$
	public static String UserCredentialsDialog_ConnectTo_XFLD;
	public static String UserCredentialsDialog_Password_XFLD;
	public static String UserCredentialsDialog_PasswordRequired;
	public static String UserCredentialsDialog_UserName_XFLD;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
