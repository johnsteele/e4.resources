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
package org.eclipse.core.internal.resources.semantic.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

/**
 * Interface for tracing Semantic File System.
 * 
 * @since 4.0
 * @noextend This class is not intended to be extended by clients.
 */

public interface ISemanticFileSystemTrace {

	/**
	 * Writes an Exception trace to the given location if the location is
	 * enabled.
	 * 
	 * @param location
	 *            the trace location
	 * @param traceException
	 *            the Exception to trace
	 */
	public void trace(TraceLocation location, CoreException traceException);

	/**
	 * Writes a Status trace to the given location if the location is enabled.
	 * 
	 * @param location
	 *            the trace location
	 * @param traceStatus
	 *            the status
	 */
	public void trace(TraceLocation location, IStatus traceStatus);

}
