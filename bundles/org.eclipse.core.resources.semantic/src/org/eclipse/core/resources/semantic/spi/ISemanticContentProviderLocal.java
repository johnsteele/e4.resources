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

import java.io.File;

import org.eclipse.core.runtime.CoreException;

/**
 * TODO 0.1: javadoc
 * 
 * @since 4.0
 * 
 */
public interface ISemanticContentProviderLocal {

	/**
	 * Retrieves the file store for the cache.
	 * 
	 * 
	 * @param semanticFileStore
	 *            the resource handle
	 * @return the file store for caching the content
	 * @throws CoreException
	 *             in case of failure
	 */
	public File toLocalFile(ISemanticFileStore semanticFileStore) throws CoreException;

}
