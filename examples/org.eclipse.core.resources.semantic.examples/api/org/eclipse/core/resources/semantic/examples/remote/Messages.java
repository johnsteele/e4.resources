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
package org.eclipse.core.resources.semantic.examples.remote;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.core.resources.semantic.examples.remote.messages"; //$NON-NLS-1$
	public static String RemoteFile_Current_XGRP;
	public static String RemoteFile_FilesOnlyUnderFolders_XMSG;
	public static String RemoteFile_Version_XGRP;
	public static String RemoteFolder_ChildAlreadyExists_XMSG;
	public static String RemoteItem_ItemsUnderFolderOrRoot_XMSG;
	public static String RemoteItemSelectionDialog_Name_XFLD;
	public static String RemoteItemSelectionDialog_SelectItem_XGRP;
	public static String RemoteItemSelectionDialog_SelectItems_XGRP;
	public static String RemoteStoreEditor_ChangeContent_XMEN;
	public static String RemoteStoreEditor_ChildName_XFLD;
	public static String RemoteStoreEditor_Content_XFLD;
	public static String RemoteStoreEditor_CreateChild_XGRP;
	public static String RemoteStoreEditor_CreateFile_XMEN;
	public static String RemoteStoreEditor_CreateFolder_XMEN;
	public static String RemoteStoreEditor_Delete_XMEN;
	public static String RemoteStoreEditor_EnterContent_XGRP;
	public static String RemoteStoreEditor_Error_XGRP;
	public static String RemoteStoreEditor_Name_XCOL;
	public static String RemoteStoreEditor_NameInUse_XMSG;
	public static String RemoteStoreEditor_NewContent_XFLD;
	public static String RemoteStoreEditor_NewRootFile_XBUT;
	public static String RemoteStoreEditor_NewRootFolder_XBUT;
	public static String RemoteStoreEditor_Timestamp_XFLD;
	public static String RemoteStoreEditor_UpdateTime_XBUT;
	public static String RemoteStoreEditor_Upload_XMEN;
	public static String RemoteStoreEditor_WrongFile_XMSG;
	public static String RemoteStoreEditor_WrongInput_XMSG;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		//
	}
}
