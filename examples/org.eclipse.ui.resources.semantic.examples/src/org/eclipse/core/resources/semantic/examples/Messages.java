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
package org.eclipse.core.resources.semantic.examples;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.core.resources.semantic.examples.messages"; //$NON-NLS-1$
	public static String AddDemoRESTResourcePage_AddRestResource_XGRP;
	public static String AddDemoRESTResourcePage_FileExists_XMSG;
	public static String AddDemoRESTResourcePage_Folder_XFLD;
	public static String AddDemoRESTResourcePage_Provide_URL_XMSG;
	public static String AddDemoRESTResourcePage_ProvideFileName_XMSG;
	public static String AddDemoRESTResourcePage_ResName_XFLD;
	public static String AddDemoRESTResourcePage_ResUrl_XFLD;
	public static String AddFileOrFolderFromRemotePage_Browse_XBUT;
	public static String AddFileOrFolderFromRemotePage_ChildName_XFLD;
	public static String AddFileOrFolderFromRemotePage_Deep_XRBL;
	public static String AddFileOrFolderFromRemotePage_File_XRBL;
	public static String AddFileOrFolderFromRemotePage_Folder_XRBL;
	public static String AddFileOrFolderFromRemotePage_LocalFileOrFolder_XGRP;
	public static String AddFileOrFolderFromRemotePage_LocalPath_XFLD;
	public static String AddFileOrFolderFromRemotePage_NameInUse_XMSG;
	public static String AddFileOrFolderFromRemotePage_NameMissing_XMSG;
	public static String AddFileOrFolderFromRemotePage_NotFile_XMSG;
	public static String AddFileOrFolderFromRemotePage_NotFolder_XMSG;
	public static String AddFileOrFolderFromRemotePage_NotFoundAtPath_XMSG;
	public static String AddFileOrFolderFromRemotePage_ParentFolder_XFLD;
	public static String AddFileOrFolderFromRemotePage_PathMssing_XMSG;
	public static String AddFileOrFolderFromRemotePage_Select_XMSG;
	public static String AddWebDAVResourceCheckURLPage_ChecksIgnoreMessage;
	public static String AddWebDAVResourceCheckURLPage_FileLabel;
	public static String AddWebDAVResourceCheckURLPage_FolderLabel;
	public static String AddWebDAVResourceCheckURLPage_IgnoreButton;
	public static String AddWebDAVResourceCheckURLPage_InvalidUrlMessage;
	public static String AddWebDAVResourceCheckURLPage_InvalidURLWithCause;
	public static String AddWebDAVResourceCheckURLPage_MustBeFileOrFolderMessage;
	public static String AddWebDAVResourceCheckURLPage_NoURLMessage;
	public static String AddWebDAVResourceCheckURLPage_PageTitle;
	public static String AddWebDAVResourceCheckURLPage_RetrieveContentAfterFinish;
	public static String AddWebDAVResourceCheckURLPage_URLLabel;
	public static String AddWebDAVResourceNameAndURLPage_BrowseButton;
	public static String AddWebDAVResourceNameAndURLPage_ChildExistsMessage;
	public static String AddWebDAVResourceNameAndURLPage_ChooseParentFolderMessage;
	public static String AddWebDAVResourceNameAndURLPage_ChooseURLMessage;
	public static String AddWebDAVResourceNameAndURLPage_LinksForbiddenMessage;
	public static String AddWebDAVResourceNameAndURLPage_MustProviderChildMessage;
	public static String AddWebDAVResourceNameAndURLPage_NameLabel;
	public static String AddWebDAVResourceNameAndURLPage_OverwriteChildMessage;
	public static String AddWebDAVResourceNameAndURLPage_OverwriteLabel;
	public static String AddWebDAVResourceNameAndURLPage_PageTitle;
	public static String AddWebDAVResourceNameAndURLPage_ParentIsSemanticMessage;
	public static String AddWebDAVResourceNameAndURLPage_ParentLabel;
	public static String AddWebDAVResourceNameAndURLPage_ParentNotSemanticMessage;
	public static String AddWebDAVResourceNameAndURLPage_SelectFolderMessage;
	public static String AddWebDAVResourceNameAndURLPage_StoreExistsMessage;
	public static String AddWebDAVResourceNameAndURLPage_StoreOverwriteMessage;
	public static String AddWebDAVResourceNameAndURLPage_WebDAVUrlLabel;
	public static String AddWebDAVResourceWizard_ActionAbortedMessage;
	public static String AddWebDAVResourceWizard_AlreadyExistsMessaage;
	public static String AddWebDAVResourceWizard_CreationFailedMessage;
	public static String AddWebDAVResourceWizard_RetrieveWebDAVContent;
	public static String AddWebDAVResourceWizard_WizardTitle;
	public static String CreateDemoProjectPage_Browse_XBUT;
	public static String CreateDemoProjectPage_CreateProject_XGRP;
	public static String CreateDemoProjectPage_DirRequired_XMSG;
	public static String CreateDemoProjectPage_ProjectExists_XMSG;
	public static String CreateDemoProjectPage_ProjectName_XFLD;
	public static String CreateDemoProjectPage_TempDir_XFLD;
	public static String CreateDemoProjectPage_UseSeparateProject_XRBL;
	public static String CreateRemotelyPage_FileContent_XGRP;
	public static String CreateRemotelyPage_TextContent_XFLD;
	public static String HandleAddFileFromRemote_Adding_XMSG;
	public static String HandleAddFileFromRemote_AddingResource_XMSG;
	public static String HandleAddFileFromRemote_AddLocalResource_XGRP;
	public static String HandleAddFileFromRemote_Interrupted_XMSG;
	public static String HandleAddFileFromRemote_Refreshing_XMSG;
	public static String HandleAddFromRemote_Error_XGRP;
	public static String HandleAddFromRemote_Info_XGRP;
	public static String HandleAddFromRemote_NoRemote_XMSG;
	public static String HandleAddFromRemote_NotSemantic_XMSG;
	public static String HandleCreateRemotely_CreateRemotely_XGRP;
	public static String NewDemoRESTResourceWizard_Error_XGRP;
	public static String SelectScenarioPage_CompositeProvider_XFLD;
	public static String SelectScenarioPage_DefaultProviderScenario_XFLD;
	public static String SelectScenarioPage_RemoteStoreScenario_XFLD;
	public static String SelectScenarioPage_SelectScenarios_XMSG;
	public static String SelectScenarioPage_WebServiceScenario_XFLD;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		//
	}
}
