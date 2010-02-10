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

/**
 * This is an interface that provides methods called by framework to initialize
 * a content provider instance.
 * 
 * @since 4.0
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 *              Clients should subclass {@link ContentProvider} or one of its
 *              subclasses instead.
 * 
 */
public interface ISemanticContentProviderBase {

	/**
	 * Called by the Semantic File System once per instance during content
	 * provider initialization.
	 * <p>
	 * This is implemented by the {@link ContentProvider} base class.
	 * 
	 * @param store
	 *            the root store
	 */
	public void setRootStore(ISemanticFileStore store);

	/**
	 * @return the root store of this provider
	 */
	public ISemanticFileStore getRootStore();

}
