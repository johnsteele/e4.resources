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

import java.text.MessageFormat;

import org.eclipse.core.resources.semantic.ISemanticFileSystem;

/**
 * The {@link ISemanticSpiResourceInfo} implementation
 * 
 * @since 4.0
 * 
 * @noextend This class is not intended to be extended by clients.
 * 
 */
public class SemanticSpiResourceInfo implements ISemanticSpiResourceInfo {
	/**
	 * Check if an option constant is requested by an options parameter
	 * 
	 * @param option
	 *            the option to check
	 * @param options
	 *            the options parameter
	 * @return <code>true</code> if the option is requested by the parameter
	 */
	public static boolean isOptionRequested(int option, int options) {
		return options == ISemanticFileSystem.NONE || ((options & option) > 0);
	}

	private final boolean isLocked;
	private final boolean isLockingSupported;
	private final boolean isReadOnly;
	private final boolean existsRemotely;
	private final String uriString;
	private final int options;
	private final String contentType;

	/**
	 * Constructs the {@link ISemanticSpiResourceInfo} instance.
	 * <p>
	 * In order to allow for performance optimizations, only the attributes
	 * corresponding to the requested option or options need to be filled. If a
	 * certain option was not requested, the parameter should be set to some
	 * default value (unless obtaining the actual parameter value comes at no or
	 * almost no performance cost); the implementation will throw a
	 * RuntimeException if an attribute is accessed which was not requested in
	 * the options.
	 * <p>
	 * Use {@link SemanticSpiResourceInfo#isOptionRequested(int, int)} to check
	 * whether a given option was requested.
	 * 
	 * @param options
	 *            the options provided when this was created
	 * @param isLocked
	 *            <code>true</code> if the resource is locked
	 * @param isLockingSupported
	 *            <code>true</code> if locking is supported for the resource
	 * @param isReadOnly
	 *            <code>true</code> if the resource is read-only
	 * @param existsRemotely
	 *            <code>true</code> if the resource exists in the remote
	 *            repository
	 * @param uriString
	 *            the URI string (may be <code>null</code>)
	 * @param contentType
	 *            the content type string (may be <code>null</code>)
	 */
	public SemanticSpiResourceInfo(int options, boolean isLocked, boolean isLockingSupported, boolean isReadOnly, boolean existsRemotely,
			String uriString, String contentType) {
		this.options = options;
		this.isLocked = isLocked;
		this.isLockingSupported = isLockingSupported;
		this.isReadOnly = isReadOnly;
		this.existsRemotely = existsRemotely;
		this.uriString = uriString;
		this.contentType = contentType;
	}

	public boolean isLocked() {
		assertCorrectOption(ISemanticFileSystem.RESOURCE_INFO_LOCKED);
		return this.isLocked;
	}

	public boolean isLockingSupported() {
		assertCorrectOption(ISemanticFileSystem.RESOURCE_INFO_LOCKING_SUPPORTED);
		return this.isLockingSupported;
	}

	public boolean isReadOnly() {
		assertCorrectOption(ISemanticFileSystem.RESOURCE_INFO_READ_ONLY);
		return this.isReadOnly;
	}

	public String getRemoteUriString() {
		assertCorrectOption(ISemanticFileSystem.RESOURCE_INFO_URI_STRING);
		return this.uriString;
	}

	public String getContentType() {
		assertCorrectOption(ISemanticFileSystem.RESOURCE_INFO_CONTENT_TYPE);
		return this.contentType;
	}

	public boolean existsRemotely() {
		assertCorrectOption(ISemanticFileSystem.RESOURCE_INFO_EXISTS_REMOTELY);
		return this.existsRemotely;
	}

	@SuppressWarnings("boxing")
	private void assertCorrectOption(int option) {

		if (!isOptionRequested(option, this.options)) {
			throw new IllegalArgumentException(MessageFormat.format(Messages.SemanticSpiResourceInfo_OptionNotRequested_XMSG, option));
		}

	}

}
