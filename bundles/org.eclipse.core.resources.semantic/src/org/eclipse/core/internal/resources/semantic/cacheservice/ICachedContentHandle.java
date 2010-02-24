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

import org.eclipse.core.runtime.CoreException;

/**
 * Represents a cache entry
 * 
 * @since 4.0
 */
public interface ICachedContentHandle {

	/**
	 * 
	 * @return <code>true</code> if content still exists
	 */
	public boolean exists();

	/**
	 */
	public void delete();

	/**
	 * Opens an {@link InputStream} on this handle
	 * 
	 * @return the stream
	 * 
	 * @throws CoreException
	 */
	public InputStream openInputStream() throws CoreException;

}
