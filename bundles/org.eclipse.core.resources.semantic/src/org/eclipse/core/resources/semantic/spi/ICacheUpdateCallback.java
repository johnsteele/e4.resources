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
package org.eclipse.core.resources.semantic.spi;

import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;

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
	 * @throws CoreException
	 */
	public void cacheUpdated(InputStream newContent, long cacheTimestamp, boolean append) throws CoreException;

	/**
	 * Notifies that the cache is about to be updated
	 * 
	 * @param newContent
	 *            the content to be added to the cache
	 * @param cacheTimestamp
	 *            the cache timestamp
	 * @param append
	 *            if <code>true</code>, the content only represents the appended
	 *            portion of the cache
	 * @throws CoreException
	 * 
	 * @since 0.5
	 */
	public void beforeCacheUpdate(InputStream newContent, long cacheTimestamp, boolean append) throws CoreException;
}
