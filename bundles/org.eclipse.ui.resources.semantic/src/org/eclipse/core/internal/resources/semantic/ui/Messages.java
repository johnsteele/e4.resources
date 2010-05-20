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
package org.eclipse.core.internal.resources.semantic.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.core.internal.resources.semantic.ui.messages"; //$NON-NLS-1$
	public static String BrowseSFSDialog_Browser_XGRP;
	public static String BrowseSFSDialog_Path_XFLD;
	public static String PropertiesContentProvider_ContentType_XFLD;
	public static String PropertiesContentProvider_EffectiveContentProvider_XFLD;
	public static String PropertiesContentProvider_ExceptionGettingChildren_XMSG;
	public static String PropertiesContentProvider_ExceptionGettingValue_XMSG;
	public static String PropertiesContentProvider_ExistsRemotely_XFLD;
	public static String PropertiesContentProvider_Key_XGRP;
	public static String PropertiesContentProvider_LastModified_XFLD;
	public static String PropertiesContentProvider_LocalOnly_XFLD;
	public static String PropertiesContentProvider_Locked_XFLD;
	public static String PropertiesContentProvider_LockingSupported_XFLD;
	public static String PropertiesContentProvider_PersistentProps_XGRP;
	public static String PropertiesContentProvider_ProviderID_XFLD;
	public static String PropertiesContentProvider_ReadOnly_XFLD;
	public static String PropertiesContentProvider_RemoteUri_XFLD;
	public static String PropertiesContentProvider_ResInfoAttributes_XGRP;
	public static String PropertiesContentProvider_SessionProps_XGRP;
	public static String PropertiesContentProvider_Value_XGRP;
	public static String SemanticFileSystemPreferencePage_Confirm_XGRP;
	public static String SemanticFileSystemPreferencePage_Confirm_XMSG;
	public static String SemanticFileSystemPreferencePage_CouldNotDelete_XMSG;
	public static String SemanticFileSystemPreferencePage_Delete_XBUT;
	public static String SemanticFileSystemPreferencePage_DeletingFile_XMSG;
	public static String SemanticFileSystemPreferencePage_Exception_GetPath_XMSG;
	public static String SemanticFileSystemPreferencePage_PathToCache_XFLD;
	public static String SemanticFileSystemPreferencePage_PathToDb_XFLD;
	public static String SemanticFileSystemPreferencePage_Restart_XGRP;
	public static String SemanticFileSystemPreferencePage_Restart_XMSG;
	public static String SFSBrowserActionProvider_AutoRefresh_XBUT;
	public static String SFSBrowserActionProvider_ConfirmDelete_XGRP;
	public static String SFSBrowserActionProvider_ConfirmDelete_XMSG;
	public static String SFSBrowserActionProvider_Delete_XMIT;
	public static String SFSBrowserActionProvider_DontAskAgain_XMSG;
	public static String SFSBrowserActionProvider_OpenInTextEditor_XMIT;
	public static String SFSBrowserActionProvider_Refesh_XBUT;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		//
	}
}
