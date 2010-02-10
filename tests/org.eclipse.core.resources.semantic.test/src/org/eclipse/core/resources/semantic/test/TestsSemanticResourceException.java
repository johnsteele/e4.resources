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
package org.eclipse.core.resources.semantic.test;

import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.internal.resources.semantic.SemanticResourcesPlugin;
import org.eclipse.core.internal.resources.semantic.SfsTraceLocation;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.junit.Test;

/**
 * Checks the stack traces
 * 
 */
public class TestsSemanticResourceException {
	/**
	 * 
	 */
	@Test
	public void testStackTraceNoRootCause() {

		String methodName = "testStackTraceNoRootCause";

		SemanticResourceException plain = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, null, null);
		showTrace("Path and message are null", plain);

		SemanticResourceException path = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, new Path("Some/path"), null);
		showTrace("Path is not null and message are null", path);

		SemanticResourceException message = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, null,
				methodName);
		showTrace("Path is null and message is not null", message);

		SemanticResourceException both = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, new Path("Some/path"),
				methodName);
		showTrace("Path is not null and message is not null", both);

	}

	/**
	 * 
	 */
	@Test
	public void testStackTraceWithSimpleRootCause() {

		String methodName = "testStackTraceWithSimpleRootCause";

		RuntimeException rootCause = new RuntimeException("I'm the root cause");

		SemanticResourceException plain = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, null, null, rootCause);
		showTrace("Path and message are null", plain);

		SemanticResourceException path = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, new Path("Some/path"),
				null, rootCause);
		showTrace("Path is not null and message are null", path);

		SemanticResourceException message = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, null,
				methodName, rootCause);
		showTrace("Path is null and message is not null", message);

		SemanticResourceException both = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, new Path("Some/path"),
				methodName, rootCause);
		showTrace("Path is not null and message is not null", both);

	}

	/**
	 * 
	 */
	@Test
	public void testStackTraceWithNestedRootCause() {

		String methodName = "testStackTraceWithNestedRootCause";

		IOException ioe = new IOException("I'm the root root cause");
		RuntimeException rootCause = new RuntimeException("I'm the root cause", ioe);

		SemanticResourceException plain = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, null, null, rootCause);
		showTrace("Path and message are null", plain);

		SemanticResourceException path = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, new Path("Some/path"),
				null, rootCause);
		showTrace("Path is not null and message are null", path);

		SemanticResourceException message = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, null,
				methodName, rootCause);
		showTrace("Path is null and message is not null", message);

		SemanticResourceException both = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, new Path("Some/path"),
				methodName, rootCause);
		showTrace("Path is not null and message is not null", both);

	}

	/**
	 * 
	 */
	@Test
	public void testStackTraceWithCoreExceptionRootCause() {

		String methodName = "testStackTraceWithCoreExceptionRootCause";

		IOException ioe = new IOException("I'm the root root cause");
		IStatus status = new Status(IStatus.WARNING, "pluginId", "A Message", ioe);
		CoreException rootCause = new CoreException(status);

		SemanticResourceException plain = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, null, null, rootCause);
		showTrace("Path and message are null", plain);

		SemanticResourceException path = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, new Path("Some/path"),
				null, rootCause);
		showTrace("Path is not null and message are null", path);

		SemanticResourceException message = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, null,
				methodName, rootCause);
		showTrace("Path is null and message is not null", message);

		SemanticResourceException both = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, new Path("Some/path"),
				methodName, rootCause);
		showTrace("Path is not null and message is not null", both);

	}

	/**
	 * 
	 */
	@Test
	public void testStackTraceWithMultiStatusCoreExceptionRootCause() {

		String methodName = "testStackTraceWithMultiStatusCoreExceptionRootCause";

		IOException ioe = new IOException("I'm the root root cause");
		MultiStatus status = new MultiStatus("plgin", IStatus.OK, "I'm the multi status", null);

		status.add(new Status(IStatus.WARNING, "pluginId", "First child no RootCause", null));
		status.add(new Status(IStatus.WARNING, "pluginId", "Second child with RootCause", ioe));

		CoreException rootCause = new CoreException(status);

		SemanticResourceException plain = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, null, null, rootCause);
		showTrace("Path and message are null", plain);

		SemanticResourceException path = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, new Path("Some/path"),
				null, rootCause);
		showTrace("Path is not null and message are null", path);

		SemanticResourceException message = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, null,
				methodName, rootCause);
		showTrace("Path is null and message is not null", message);

		SemanticResourceException both = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, new Path("Some/path"),
				methodName, rootCause);
		showTrace("Path is not null and message is not null", both);

	}

	/**
	 * 
	 */
	@Test
	public void testStackTraceWithSemanticExceptionRootCause() {

		String methodName = "testStackTraceWithSemanticExceptionRootCause";

		IOException ioe = new IOException("I'm the root root cause");
		IStatus status = new Status(IStatus.WARNING, "pluginId", "A Message", ioe);
		CoreException coreRoot = new CoreException(status);
		SemanticResourceException rootCause = new SemanticResourceException(SemanticResourceStatusCode.FILECACHE_ERROR_WRITING_METADATA,
				null, "I'm the semantic cause", coreRoot);

		SemanticResourceException plain = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, null, null, rootCause);
		showTrace("Path and message are null", plain);

		SemanticResourceException path = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, new Path("Some/path"),
				null, rootCause);
		showTrace("Path is not null and message are null", path);

		SemanticResourceException message = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, null,
				methodName, rootCause);
		showTrace("Path is null and message is not null", message);

		SemanticResourceException both = new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, new Path("Some/path"),
				methodName, rootCause);
		showTrace("Path is not null and message is not null", both);

	}

	private void showTrace(String text, SemanticResourceException plain) {
//		System.out.println("************************************************************************************************");
//		System.out.println("* " + text);
//		System.out.println("************************************************************************************************");
//		plain.printStackTrace(System.out);

		//logDefault(plain);
		//logWithStack(plain);
		//trace(plain.getStatus());
		logForSFS(plain, text);

	}

	void logDefault(CoreException x) {

		ILog log = Platform.getLog(Platform.getBundle(SemanticResourcesPlugin.PLUGIN_ID));
		if (log != null) {
			log.log(x.getStatus());
		}
	}

	void logWithStack(CoreException x) {

		ILog log = Platform.getLog(Platform.getBundle(SemanticResourcesPlugin.PLUGIN_ID));
		if (log != null) {
			IStatus dummyStatus = new Status(x.getStatus().getSeverity(), x.getStatus().getPlugin(), x.getStatus().getCode(), x.getStatus()
					.getMessage(), x);
			log.log(dummyStatus);
		}

	}

	void logForSFS(CoreException x, String text) {

		try {
			ISemanticFileSystem sfs = (ISemanticFileSystem) EFS.getFileSystem(ISemanticFileSystem.SCHEME);
			IStatus textStatus = new Status(IStatus.INFO, TestPlugin.PLUGIN_ID, text);
			sfs.getLog().log(new CoreException(textStatus));
			sfs.getLog().log(x);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}

	}

	void trace(CoreException x) {

		try {
			if (SfsTraceLocation.CORE.isActive()) {
				SfsTraceLocation.getTrace().trace(SfsTraceLocation.CORE.getLocation(), x.getMessage(), x);
			}
		} catch (Exception e) {
			// $JL-EXC$ //$JL-SYS_OUT_ERR$
			x.printStackTrace(System.out);
		}
	}

	void trace(IStatus stat) {

		try {
			if (SfsTraceLocation.CORE.isActive()) {
				SfsTraceLocation.getTrace().trace(SfsTraceLocation.CORE.getLocation(), stat.getMessage());
			}
		} catch (Exception e) {
			// $JL-EXC$ //$JL-SYS_OUT_ERR$
			printChildren(stat, System.out);
		}
	}

	// taken from PrintStackUtil
	private void printChildren(IStatus status, PrintStream output) {
		IStatus[] children = status.getChildren();
		if (children == null || children.length == 0) {
			return;
		}
		for (IStatus element : children) {
			output.println("Contains: " + element.getMessage()); //$NON-NLS-1$
			Throwable exception = element.getException();
			if (exception != null) {
				exception.printStackTrace(output);
			}
			printChildren(element, output);
		}
	}

}
