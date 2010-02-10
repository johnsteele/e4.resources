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
package org.eclipse.core.internal.resources.semantic.ui.actions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.core.internal.resources.semantic.ui.actions.messages"; //$NON-NLS-1$
	public static String ActionBase_ActionCancelded_XGRP;
	public static String ActionBase_ActionCanceled_XMSG;
	public static String DeleteAction_Deleting_XMSG;
	public static String DiffAction_CompareAction_XGRP;
	public static String DiffAction_NoHistory_XMSG;
	public static String EditAction_Editing_XMSG;
	public static String LockAction_LockingRes_XMSG;
	public static String RemoveAction_ConfirmResourceRemoval_XGRP;
	public static String RemoveAction_DoYouWantToRemove_XMSG;
	public static String RemoveAction_Remove_XGRP;
	public static String RemoveAction_Removing_XMSG;
	public static String RevertAction_Reverting_XMSG;
	public static String SynchronizeAction_Synchronizing_XMSG;
	public static String UnlockAction_Unlocking_XMSG;
	public static String UnmapAction_Unmap_XGRP;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		//
	}
}
