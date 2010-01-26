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
 * The SPI part of the Resource information.
 * <p>
 * In order to allow for performance optimization, instances of this interface
 * are always constructed using a bit mask specifying the requested attributes
 * (see
 * {@link ISemanticContentProvider#fetchResourceInfo(ISemanticFileStore, int, org.eclipse.core.runtime.IProgressMonitor)}.
 * <p>
 * If a given attribute was not requested when obtaining an instance of this
 * interface, a {@link RuntimeException} will be thrown.
 * 
 * @since 4.0
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * 
 */
public interface ISemanticSpiResourceInfo {

	/**
	 * Returns the read-only flag for a resource.
	 * <p>
	 * Content providers typically need to "prepare" their resources in some way
	 * or another before they can be safely modified. This flag enables them to
	 * react on modification attempts.
	 * <p>
	 * As long as this method returns <code>true</code>, any attempt to modify
	 * the (workspace) resource will trigger a call to
	 * {@link ISemanticContentProvider#validateEdit(ISemanticFileStore[], Object)}
	 * . The content provider can then perform the necessary steps (like
	 * checking time stamps, locking, interaction with the user...) and either
	 * allow modification or signal failure by returning a failure status in the
	 * validation method.
	 * <p>
	 * After successful completion of the validation method, this should return
	 * <code>false</code> in order to avoid repeated calls to the validation
	 * method.
	 * <p>
	 * As soon as validation is required again, the read-only flag should be set
	 * to <code>true</true>, for example after unlocking the resource, 
	 * so that subsequent modification attempts trigger the validation method again.
	 * 
	 * @return <code>true</code> if the resource is read-only
	 * 
	 */
	public boolean isReadOnly();

	/**
	 * See the corresponding <code>ISemanticResourceInfo</code> method
	 * 
	 * @return <code>true</code> if locking is supported for this resource
	 */
	public boolean isLockingSupported();

	/**
	 * See the corresponding <code>ISemanticResourceInfo</code> method
	 * 
	 * @return <code>true</code> if the resource is locked, <code>false</code>
	 *         otherwise (even if locking is not supported)
	 * 
	 */
	public boolean isLocked();

	/**
	 * See the corresponding <code>ISemanticResourceInfo</code> method
	 * 
	 * @return the URI as String
	 */
	public String getRemoteUriString();

	/**
	 * See the corresponding <code>ISemanticResourceInfo</code> method
	 * 
	 * @return <code>true</code> if the resource exists in the remote repository
	 */
	public boolean existsRemotely();

	/**
	 * See the corresponding <code>ISemanticResourceInfo</code> method
	 * 
	 * @return content type string or <code>null</code>
	 */
	public String getContentType();

}
