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
package org.eclipse.core.internal.resources.semantic.ui.util;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.core.internal.resources.semantic.ui.util.messages"; //$NON-NLS-1$
	public static String SFSBrowserPropertiesContentProvider_PersistentProperties_XCOL;
	public static String SFSBrowserPropertiesContentProvider_SessionProperties_XCOL;
	public static String SFSBrowserPropertiesLabelProvider_Key_XCOL;
	public static String SFSBrowserPropertiesLabelProvider_Value_XCOL;
	public static String SFSBrowserTreeLabelProvider_Name_XCOL;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		//
	}
}
