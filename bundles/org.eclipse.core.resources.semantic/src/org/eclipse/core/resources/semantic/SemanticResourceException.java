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

import java.io.PrintStream;
import java.io.PrintWriter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;

/**
 * 
 * TODO 0.1: javadoc
 * 
 * @since 4.0
 * @noextend This class is not intended to be extended by clients.
 */
public class SemanticResourceException extends CoreException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor with causing Exception
	 * 
	 * @param code
	 *            status code
	 * @param path
	 *            the path of the resource
	 * @param message
	 *            the message
	 * @param exception
	 *            the causing Exception
	 */
	public SemanticResourceException(SemanticResourceStatusCode code, IPath path, String message, Throwable exception) {
		super(new SemanticResourceStatus(code, path, message, exception));
	}

	/**
	 * Constructor without causing Exception
	 * 
	 * @param code
	 *            status code
	 * @param path
	 *            the path of the resource
	 * @param message
	 *            the message
	 */
	public SemanticResourceException(SemanticResourceStatusCode code, IPath path, String message) {
		super(new SemanticResourceStatus(code, path, message, null));
	}

	public void printStackTrace(PrintStream output) {
		synchronized (output) {
			IStatus status = getStatus();
			if (status.getException() != null) {
				String path = "(" + ((SemanticResourceStatus) status).getPath() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				output.print(getClass().getName() + path + "[" + status.getCode() + "]: "); //$NON-NLS-1$ //$NON-NLS-2$
				status.getException().printStackTrace(output);
			} else {
				super.printStackTrace(output);
			}
		}
	}

	public void printStackTrace(PrintWriter output) {
		synchronized (output) {
			IStatus status = getStatus();
			if (status.getException() != null) {
				String path = "(" + ((SemanticResourceStatus) status).getPath() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				output.print(getClass().getName() + path + "[" + status.getCode() + "]: "); //$NON-NLS-1$ //$NON-NLS-2$
				status.getException().printStackTrace(output);
			} else {
				super.printStackTrace(output);
			}
		}
	}

}
