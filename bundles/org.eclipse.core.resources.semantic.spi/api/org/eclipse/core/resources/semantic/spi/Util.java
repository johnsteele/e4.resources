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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Contains utility methods for use by content providers
 */
public class Util {

	/**
	 * Singleton buffer created to avoid buffer creations in the transferStreams
	 * method. Used as an optimization, based on the assumption that multiple
	 * writes won't happen in a given instance of FileStore.
	 */
	private static final byte[] buffer = new byte[8192];
	private static final String EMPTY = ""; //$NON-NLS-1$

	/**
	 * Creates a progress monitor.
	 * 
	 * @param monitor
	 *            the parent monitor (may be null)
	 * @return a progress monitor
	 */
	public static IProgressMonitor monitorFor(IProgressMonitor monitor) {
		return monitor == null ? new NullProgressMonitor() : monitor;
	}

	/**
	 * Transfers the contents of an input stream to an output stream, using a
	 * large buffer.
	 * 
	 * @param source
	 *            The input stream to transfer
	 * @param destination
	 *            The destination stream of the transfer
	 * @param monitor
	 *            A progress monitor. The monitor is assumed to have already
	 *            done beginWork with one unit of work allocated per buffer load
	 *            of contents to be transferred.
	 * @throws CoreException
	 *             in case of failure
	 */
	public static final void transferStreams(InputStream source, OutputStream destination, IProgressMonitor monitor) throws CoreException {
		IProgressMonitor actMonitor = Util.monitorFor(monitor);
		try {
			/*
			 * Note: although synchronizing on the buffer is thread-safe, it may
			 * result in slower performance in the future if we want to allow
			 * concurrent writes.
			 */
			synchronized (buffer) {
				while (true) {
					int bytesRead = -1;
					try {
						bytesRead = source.read(buffer);
					} catch (IOException e) {
						throw new SemanticResourceException(SemanticResourceStatusCode.UTIL_BYTE_TRANSER, new Path(EMPTY), Messages.Util_TransferRead_XMSG, e);
					}
					if (bytesRead == -1)
						break;
					try {
						destination.write(buffer, 0, bytesRead);
					} catch (IOException e) {
						throw new SemanticResourceException(SemanticResourceStatusCode.UTIL_BYTE_TRANSER, new Path(EMPTY), Messages.Util_TransferWrite_XMSG, e);
					}
					actMonitor.worked(1);
				}
			}
		} finally {
			Util.safeClose(source);
			Util.safeClose(destination);
		}
	}

	/**
	 * Closes a stream and ignores any resulting exception.
	 * 
	 * @param in
	 *            the stream
	 * 
	 */
	public static void safeClose(InputStream in) {
		try {
			if (in != null)
				in.close();
		} catch (IOException e) {
			// $JL-EXC$ ignore
		}
	}

	/**
	 * Closes a stream and ignores any resulting exception.
	 * 
	 * @param out
	 *            the stream
	 * 
	 */
	public static void safeClose(OutputStream out) {
		try {
			if (out != null)
				out.close();
		} catch (IOException e) {
			// $JL-EXC$ ignore
		}
	}

	/**
	 * Creates a sub-monitor.
	 * 
	 * @param monitor
	 *            the parent monitor
	 * @param ticks
	 *            the number of ticks
	 * @return the sub-monitor
	 */
	public static IProgressMonitor subMonitorFor(IProgressMonitor monitor, int ticks) {
		if (monitor == null)
			return new NullProgressMonitor();
		if (monitor instanceof NullProgressMonitor)
			return monitor;
		return new SubProgressMonitor(monitor, ticks);
	}

}
