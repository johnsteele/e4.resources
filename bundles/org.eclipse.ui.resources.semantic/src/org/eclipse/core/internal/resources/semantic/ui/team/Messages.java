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
package org.eclipse.core.internal.resources.semantic.ui.team;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.core.internal.resources.semantic.ui.team.messages"; //$NON-NLS-1$
	public static String RepositoryConfigurationWizard_SFSError_XGRP;
	public static String RepositoryConfigurationWizard_SFSNotInitialized_XMSG;
	public static String SemanticHistoryPage_Comment_XCOL;
	public static String SemanticHistoryPage_Compare_XMIT;
	public static String SemanticHistoryPage_Revision_XCOL;
	public static String SemanticHistoryPage_SelectCommonAncestor_XGRP;
	public static String SemanticHistoryPage_SemHistPage_XGRP;
	public static String SemanticHistoryPage_ThreeWayCompare_XMIT;
	public static String SemanticHistoryPage_Timestamp_XCOL;
	public static String SemanticHistoryPage_User_XCOL;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		//
	}
}
