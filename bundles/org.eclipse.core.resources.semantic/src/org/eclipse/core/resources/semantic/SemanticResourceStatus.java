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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * The {@link ISemanticResourceStatus} implementation.
 */
final class SemanticResourceStatus extends Status implements ISemanticResourceStatus {

	private final IPath path;

	private SemanticResourceStatus(int type, SemanticResourceStatusCode code, IPath path, String message, Throwable exception) {

		super(type, SemanticResourceStatusCode.PLUGIN_ID, code.toInt(), message, exception);
		if (message == null && exception != null) {
			setMessage(exception.getMessage());
		}
		this.path = path;
	}

	/**
	 * The constructor
	 * 
	 * @param code
	 *            the status code
	 * @param path
	 *            the path to the resource
	 * @param message
	 *            the message
	 * @param exception
	 *            the causing exception, or <code>null</code>
	 */
	SemanticResourceStatus(SemanticResourceStatusCode code, IPath path, String message, Throwable exception) {
		this(getSeverity(code), code, path, message, exception);
	}

	public IPath getPath() {
		return this.path;
	}

	protected static int getSeverity(SemanticResourceStatusCode scode) {
		int code = scode.toInt();
		return code == 0 ? 0 : 1 << (code % 100 / 33);
	}

	// for debug only
	private String getTypeName() {
		switch (getSeverity()) {
		case IStatus.OK:
			return "OK"; //$NON-NLS-1$
		case IStatus.ERROR:
			return "ERROR"; //$NON-NLS-1$
		case IStatus.INFO:
			return "INFO"; //$NON-NLS-1$
		case IStatus.WARNING:
			return "WARNING"; //$NON-NLS-1$
		default:
			return String.valueOf(getSeverity());
		}
	}

	// for debug only
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[type: "); //$NON-NLS-1$
		sb.append(getTypeName());
		sb.append("], [path: "); //$NON-NLS-1$
		sb.append(getPath());
		sb.append("], [message: "); //$NON-NLS-1$
		sb.append(getMessage());
		sb.append("], [plugin: "); //$NON-NLS-1$
		sb.append(getPlugin());
		sb.append("], [exception: "); //$NON-NLS-1$
		sb.append(getException());
		sb.append("]\n"); //$NON-NLS-1$
		return sb.toString();
	}

}
