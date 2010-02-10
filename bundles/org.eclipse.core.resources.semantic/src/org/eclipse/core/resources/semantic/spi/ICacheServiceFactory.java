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

import org.eclipse.core.runtime.CoreException;

/**
 * Used to obtain a cache service instance
 * 
 * @noimplement Clients must use one of the implementations in this package
 * @noextend Clients must use one of the implementations in this package
 */
public interface ICacheServiceFactory {
	/**
	 * @return the cache service instance
	 * @throws CoreException
	 *             upon failure
	 */
	public ICacheService getCacheService() throws CoreException;
}
