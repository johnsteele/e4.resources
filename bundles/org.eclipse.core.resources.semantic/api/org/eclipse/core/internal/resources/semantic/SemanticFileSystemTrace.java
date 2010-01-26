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

import java.io.PrintStream;

import org.eclipse.core.internal.resources.semantic.util.ISemanticFileSystemTrace;
import org.eclipse.core.internal.resources.semantic.util.TraceLocation;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;

/**
 * The Semantic File System Trace implementation
 * 
 * @since 4.0
 * @noextend This class is not intended to be extended by clients.
 */
class SemanticFileSystemTrace implements ISemanticFileSystemTrace {

	/**
	 * Writes an Exception trace to the given location if the location is
	 * enabled.
	 * 
	 * @param location
	 *            the trace location
	 * @param traceException
	 *            the Exception to trace
	 */
	public void trace(TraceLocation location, CoreException traceException) {

		if (!location.isActive()) {
			return;
		}

		ILog log = Platform.getLog(Platform.getBundle(SemanticResourcesPlugin.PLUGIN_ID));
		if (log != null) {
			log.log(traceException.getStatus());
		} else {
			traceException.printStackTrace();
		}

	}

	/**
	 * Writes a Status trace to the given location if the location is enabled.
	 * 
	 * @param location
	 *            the trace location
	 * @param traceStatus
	 *            the status
	 */
	public void trace(TraceLocation location, IStatus traceStatus) {

		if (!location.isActive()) {
			return;
		}

		ILog log = Platform.getLog(Platform.getBundle(SemanticResourcesPlugin.PLUGIN_ID));
		if (log != null) {
			log.log(traceStatus);
		} else {
			//$JL-SYS_OUT_ERR$
			printChildren(traceStatus, System.out);
		}
	}

	// taken from PrintStackUtil
	private void printChildren(IStatus status, PrintStream output) {
		// TODO 0.1: check trace output if we also need to write the root message
		IStatus[] children = status.getChildren();
		if (children == null || children.length == 0)
			return;
		for (int i = 0; i < children.length; i++) {
			output.println("Contains: " + children[i].getMessage()); //$NON-NLS-1$
			Throwable exception = children[i].getException();
			if (exception != null)
				exception.printStackTrace(output);
			printChildren(children[i], output);
		}
	}

}
