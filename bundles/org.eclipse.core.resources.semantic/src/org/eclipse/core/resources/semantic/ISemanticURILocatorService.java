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

import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * TODO 0.1: javadoc
 */
public interface ISemanticURILocatorService {

	/**
	 * Returns paths of all resources for the given URI
	 * 
	 * @param uri
	 *            the URI
	 * @return path array
	 * @throws CoreException
	 *             upon failure
	 */
	public IPath[] locateURI(URI uri) throws CoreException;

	/**
	 * Returns paths of all resources for the given URI that are filtered by the
	 * given path
	 * 
	 * @param uri
	 *            the URI
	 * @param rootpath
	 *            the path
	 * @return path array
	 * @throws CoreException
	 *             upon failure
	 */
	public IPath[] locateURI(URI uri, IPath rootpath) throws CoreException;

}
