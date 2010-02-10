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
package org.eclipse.core.internal.resources.semantic.cacheservice;

import java.io.InputStream;

/**
 * Called after successful cache updates
 * 
 */
public interface ICacheUpdateCallback {
	/**
	 * Notifies about successful cache updates
	 * 
	 * @param newContent
	 *            the content of the cache
	 * @param cacheTimestamp
	 *            the cache timestamp
	 * @param append
	 *            if <code>true</code>, the content only represents the appended
	 *            portion of the cache
	 */
	public void cacheUpdated(InputStream newContent, long cacheTimestamp, boolean append);
}
