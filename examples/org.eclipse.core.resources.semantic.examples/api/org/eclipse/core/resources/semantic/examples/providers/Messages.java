package org.eclipse.core.resources.semantic.examples.providers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.core.resources.semantic.examples.providers.messages"; //$NON-NLS-1$
	public static String RemoteStoreContentProvider_Canceld_XMSG;
	public static String RemoteStoreContentProvider_CannotCreateFileChild_XMSG;
	public static String RemoteStoreContentProvider_CannotCreateProject_XMSG;
	public static String RemoteStoreContentProvider_Confirm_XGRP;
	public static String RemoteStoreContentProvider_ConfirmCreate_XMSG;
	public static String RemoteStoreContentProvider_ConfirmEdit_XGRP;
	public static String RemoteStoreContentProvider_ConfirmRemoteDelete_XMSG;
	public static String RemoteStoreContentProvider_FoldersNoRevert_XMSG;
	public static String RemoteStoreContentProvider_ParentIsFile_XMSG;
	public static String RemoteStoreContentProvider_RemoteItemNotFound_XMSG;
	public static String RemoteStoreContentProvider_RemoteNotFile_XMSG;
	public static String RemoteStoreContentProvider_RemoteNotFound_XMSG;
	public static String RemoteStoreContentProvider_Syncing_XMSG;
	public static String RemoteStoreContentProvider_URIError_XMSG;
	public static String RemoteStoreContentProvider_ValidateEdit_XGRP;
	public static String SampleCompositeResourceContentProvider_CannotDeletePartOfCompositeResource_XMSG;
	public static String SampleCompositeResourceContentProvider_NotSupported_XMSG;
	public static String SampleCompositeResourceContentProvider_RemoteURINotSet_XMSG;
	public static String SampleCompositeResourceContentProvider_SomeResourcesAreCheckedOut_XMSG;
	public static String SampleRESTReadonlyContentProvider_MethodNotSupported_XMSG;
	public static String SampleRESTReadonlyContentProvider_MethodResult_XMSG;
	public static String SampleRESTReadonlyContentProvider_RemoteUriNotSet_XMSG;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
