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
package org.eclipse.core.resources.semantic;

/**
 * 
 * @since 4.0
 * 
 */
public enum SyncDirection {
	/**
	 * Update the workspace version with the remote version
	 */
	INCOMING,
	/**
	 * Update the remote version with the workspace version
	 */
	OUTGOING,
	/**
	 * Two-way update (the content provider decides which direction to use)
	 */
	BOTH
}
