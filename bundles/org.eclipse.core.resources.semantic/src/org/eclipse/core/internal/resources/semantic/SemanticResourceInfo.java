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
package org.eclipse.core.internal.resources.semantic;

import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticResourceInfo;
import org.eclipse.core.resources.semantic.spi.ISemanticSpiResourceInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;

/**
 * The {@link ISemanticResourceInfo} implementation.
 * 
 */
public class SemanticResourceInfo implements ISemanticResourceInfo {

	private final ISemanticSpiResourceInfo info;
	private final boolean isLocalOnly;
	private final int options;

	SemanticResourceInfo(int options, ISemanticSpiResourceInfo info, boolean isLocalOnly) {
		this.options = options;
		this.isLocalOnly = isLocalOnly;
		this.info = info;
	}

	public boolean isLocalOnly() {
		assertCorrectOption(ISemanticFileSystem.RESOURCE_INFO_LOCAL_ONLY);
		return this.isLocalOnly;
	}

	public String getRemoteURIString() {
		return this.info.getRemoteUriString();
	}

	/**
	 * @throws CoreException
	 */
	public String getContentType() throws CoreException {
		return this.info.getContentType();
	}

	public boolean isReadOnly() {
		return this.info.isReadOnly();
	}

	public boolean isLocked() {
		return this.info.isLocked();
	}

	/**
	 * @throws CoreException
	 */
	public boolean isLockingSupported() throws CoreException {
		return this.info.isLockingSupported();
	}

	public boolean existsRemotely() {
		return this.info.existsRemotely();
	}

	@SuppressWarnings("boxing")
	private void assertCorrectOption(int option) {

		boolean ok = this.options == ISemanticFileSystem.NONE || ((this.options & option) > 0);
		if (!ok) {
			throw new IllegalArgumentException(NLS.bind(Messages.SemanticResourceInfo_OptionNotSpecified_XMSG, option));
		}

	}

}
