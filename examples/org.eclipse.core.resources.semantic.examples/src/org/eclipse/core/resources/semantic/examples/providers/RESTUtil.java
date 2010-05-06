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
package org.eclipse.core.resources.semantic.examples.providers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 */
public class RESTUtil {

	private static final Object FILE_SCHEME = "file"; //$NON-NLS-1$

	/**
	 * Call-back interface for setting the timestamp and content type.
	 * 
	 */
	public interface IRESTCallback {
		/**
		 * sets the cache timestamp
		 * 
		 * @param timestamp
		 *            the timestamp reported
		 */
		public void setTimestamp(long timestamp);

		/**
		 * sets the content type
		 * 
		 * @param contentType
		 *            the content type reported
		 */
		public void setContentType(String contentType);

	}

	/**
	 * Opens an input stream for the given URI
	 * 
	 * @param remoteURI
	 *            the URI
	 * @param setter
	 *            the callback setter
	 * @return the input stream
	 * @throws IOException
	 *             upon failure
	 */
	public static InputStream openInputStream(String remoteURI, IRESTCallback setter) throws IOException {
		final URI uri = URI.create(remoteURI);

		URL url = uri.toURL();

		URLConnection conn = url.openConnection();
		if (setter != null) {
			if (conn.getLastModified() != 0) {
				setter.setTimestamp(conn.getLastModified());
			} else {
				setter.setTimestamp(conn.getDate());
			}
			setter.setContentType(conn.getContentType());
		}

		return conn.getInputStream();
	}

	/**
	 * Opens an output stream for a file URI
	 * 
	 * @param remoteURI
	 *            the URI
	 * @return the output stream
	 * @throws IOException
	 *             upon failure
	 */
	public static OutputStream openOutputStream(String remoteURI) throws IOException {
		final URI uri = URI.create(remoteURI);

		if (uri.getScheme().equals(FILE_SCHEME)) {
			File file = new File(uri);

			if (file.exists()) {
				file.delete();
			}

			return new FileOutputStream(file);
		}

		URL url;

		url = uri.toURL();

		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		return conn.getOutputStream();
	}

	/**
	 * @param remoteURI
	 * @param timestamp
	 * @throws IOException
	 */
	public static void setTimestamp(String remoteURI, long timestamp) throws IOException {
		final URI uri = URI.create(remoteURI);

		// do it for files only since timestamp for other URLs is sent on
		// opening output stream
		if (uri.getScheme().equals(FILE_SCHEME)) {
			File file = new File(uri);

			if (file.exists()) {
				file.setLastModified(timestamp);
			}
		}
	}

	/**
	 * Reads a stream into a String buffer
	 * 
	 * @param is
	 *            the stream
	 * @param encoding
	 * @return the String buffer
	 * @throws IOException
	 */
	public static StringBuffer readStreamIntoStringBuffer(InputStream is, String encoding) throws IOException {
		StringBuffer buf = new StringBuffer();
		char[] buffer = new char[4096];
		// $JL-I18N$
		Reader r = new InputStreamReader(is, encoding);

		int len;
		do {
			len = r.read(buffer);
			if (len > 0) {
				buf.append(buffer, 0, len);
			}
		} while (len != -1);

		return buf;
	}

}
