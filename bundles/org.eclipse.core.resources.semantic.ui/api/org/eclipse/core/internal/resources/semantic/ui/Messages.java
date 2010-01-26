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
	public static String BrowseSFSDialog_Hide_XCKL;
	public static String BrowseSFSDialog_Refresh_XBUT;
	public static String BrowseSFSDialog_RefreshError_XGRP;
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
	public static String SemanticResourcePropertyPage_ContentProviderId_XFLD;
	public static String SemanticResourcePropertyPage_EffContentProv_XFLD;
	public static String SemanticResourcePropertyPage_Exception_XMSG;
	public static String SemanticResourcePropertyPage_File_XFLD;
	public static String SemanticResourcePropertyPage_Folder_XFLD;
	public static String SemanticResourcePropertyPage_Key_XGRP;
	public static String SemanticResourcePropertyPage_LocalOnly_XFLD;
	public static String SemanticResourcePropertyPage_Locked_XFLD;
	public static String SemanticResourcePropertyPage_NoEntries_XMSG;
	public static String SemanticResourcePropertyPage_Path_XFLD;
	public static String SemanticResourcePropertyPage_PersistentProps_XFLD;
	public static String SemanticResourcePropertyPage_Project_XFLD;
	public static String SemanticResourcePropertyPage_ReadOnly_XFLD;
	public static String SemanticResourcePropertyPage_SemResAttr_XFLD;
	public static String SemanticResourcePropertyPage_SessionProp_XFLD;
	public static String SemanticResourcePropertyPage_SFileAttributes_XFLD;
	public static String SemanticResourcePropertyPage_SFolderAttr_XFLD;
	public static String SemanticResourcePropertyPage_SFSAttr_XFLD;
	public static String SemanticResourcePropertyPage_SRInfo_XFLD;
	public static String SemanticResourcePropertyPage_Type_XFLD;
	public static String SemanticResourcePropertyPage_Value_XGRP;
	public static String SemanticResourcesView_AutoRefresh_XCKL;
	public static String SemanticResourcesView_Exists_XFLD;
	public static String SemanticResourcesView_Path_XFLD;
	public static String SemanticResourcesView_PropertiesOf_XGRP;
	public static String SemanticResourcesView_RefreshJob_XGRP;
	public static String SemanticResourcesView_RefreshRate_XTOL;
	public static String SemanticResourcesView_Timestamp_XFLD;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		//
	}
}
