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
package org.eclipse.core.internal.resources.semantic;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.core.internal.resources.semantic.messages"; //$NON-NLS-1$
	public static String SemanticFileStore_AddChildFile_XMSG;
	public static String SemanticFileStore_AddChildFolder_XMSG;
	public static String SemanticFileStore_AddContentProviderRootFile_XMSG;
	public static String SemanticFileStore_AddContentProviderRootFolder_XMSG;
	public static String SemanticFileStore_AddFileRemote_XMSG;
	public static String SemanticFileStore_AddFileRemoteURI_XMSG;
	public static String SemanticFileStore_AddFolderRemote_XMSG;
	public static String SemanticFileStore_AddLocalChild_XMSG;
	public static String SemanticFileStore_AddResourceRemote_XMSG;
	public static String SemanticFileStore_AppendingInfo_XMSG;
	public static String SemanticFileStore_CreateFileRemote_XMSG;
	public static String SemanticFileStore_CreateResourceRemtoe_XMSG;
	public static String SemanticFileStore_DeleteResourceRemote_XMSG;
	public static String SemanticFileStore_FederatingContentProviderReturnedInvalidRootNodePosition;
	public static String SemanticFileStore_FederatingContentProviderReturnedNull;
	public static String SemanticFileStore_IntefaceNotImplemented_XMSG;
	public static String SemanticFileStore_InvalidURISyntax_XMSG;
	public static String SemanticFileStore_Locking_XMSG;
	public static String SemanticFileStore_MkDir_XMSG;
	public static String SemanticFileStore_MkDirOnFile_XMSG;
	public static String SemanticFileStore_NotWritable_XMSG;
	public static String SemanticFileStore_OpeningInfo_XMSG;
	public static String SemanticFileStore_OpeningInputInfo_XMSG;
	public static String SemanticFileStore_OpenInputOnlyOnFiles_XMSG;
	public static String SemanticFileStore_OpenOutputNotOnFolders_XMSG;
	public static String SemanticFileStore_RemoveResourceRemote_XMSG;
	public static String SemanticFileStore_RemovingResource_XMSG;
	public static String SemanticFileStore_ResourceWithPathExists_XMSG;
	public static String SemanticFileStore_Revert_XMSG;
	public static String SemanticFileStore_SettingURI_XMSG;
	public static String SemanticFileStore_ShallowMkDirFailed_XMSG;
	public static String SemanticFileStore_SyncContent_XGRP;
	public static String SemanticFileStore_SynchContent_XMSG;
	public static String SemanticFileStore_Unlocking_XMSG;
	public static String SemanticFileStore_UpdateFileInfo_XMSG;
	public static String SemanticFileStore_ValidateEdit_XMSG;
	public static String SemanticFileStore_ValidateRemoteCreate_XMSG;
	public static String SemanticFileStore_ValidateRemoteDelete_XMSG;
	public static String SemanticFileStore_ValidateRemove_XMSG;
	public static String SemanticFileStore_ValidateSave_XMSG;
	public static String SemanticFileSystem_NotInitialized_XMSG;
	public static String SemanticFileSystem_SFSInitError_XMSG;
	public static String SemanticFileSystem_SFSUpdateError_XMSG;
	public static String SemanticFileSystemCore_TemplateIdNotFound_XMSG;
	public static String SemanticProperties_StoreNotAccessible_XMSG;
	public static String SemanticResourceAdapterImpl_CalledOutsideRule_XMSG;
	public static String SemanticResourceAdapterImpl_JobNoRule_XMSG;
	public static String SemanticResourceAdapterImpl_NoSemanticStore_XMSG;
	public static String SemanticResourceAdapterImpl_NullFile_XMSG;
	public static String SemanticResourceAdapterImpl_OperationNotCoveredByRule_XMSG;
	public static String SemanticResourceAdapterImpl_ProjectNotAccessible_XMSG;
	public static String SemanticResourceAdapterImpl_ProjectNotMapped_XMSG;
	public static String SemanticResourceAdapterImpl_RuleNoResource_XMSG;
	public static String SemanticResourceAdapterImpl_UnknownRuleType_XMSG;
	public static String SemanticResourceInfo_OptionNotSpecified_XMSG;
	public static String SemanticResourcesPlugin_UserHomeAsCache_XMSG;
	public static String Util_LocalNameNoCirconflex_XMSG;
	public static String Util_QualifierNoCirconflex_XMSG;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		//
	}
}
