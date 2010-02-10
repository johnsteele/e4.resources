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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * This interface must be implemented by content providers that support file
 * locking.
 * <p>
 * Content providers may support locking for some or all resources. If a content
 * provider supports locking for a particular resource, the method
 * {@link ISemanticSpiResourceInfo#isLockingSupported()} must return
 * <code>true</code> for this resource.
 * 
 * @since 4.0
 * 
 */
public interface ISemanticContentProviderLocking {

	/**
	 * Locks a resource.
	 * 
	 * @param semanticFileStore
	 *            the resource handle
	 * @param monitor
	 *            may be null
	 * @return {@link IStatus#OK} if locking was successful or if the resource
	 *         was already locked before this call, any other status upon
	 *         failure, {@link IStatus#CANCEL} if locking is not supported
	 * @throws CoreException
	 *             in case of failure
	 * 
	 */
	public IStatus lockResource(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException;

	/**
	 * Unlocks a resource.
	 * 
	 * @param semanticFileStore
	 *            the resource handle
	 * @param monitor
	 *            may be null
	 * @return {@link IStatus#OK} if unlocking was successful or if the resource
	 *         was already unlocked before this call, any other status upon
	 *         failure, {@link IStatus#CANCEL} if locking is not supported
	 * @throws CoreException
	 *             in case of failure
	 * 
	 */
	public IStatus unlockResource(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException;

}
