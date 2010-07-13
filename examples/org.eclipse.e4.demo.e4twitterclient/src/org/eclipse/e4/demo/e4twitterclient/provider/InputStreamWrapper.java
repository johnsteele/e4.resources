/*******************************************************************************
 * Copyright (c) 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eduard Bartsch (SAP AG) - initial API and implementation
 *    Mathias Kinzler (SAP AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.e4twitterclient.provider;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpMethod;

class InputStreamWrapper extends InputStream {

	private InputStream is;
	private HttpMethod httpMethod;

	protected InputStreamWrapper(HttpMethod method, InputStream is) {
		this.httpMethod = method;
		this.is = is;
	}

	@Override
	public void close() throws IOException {
		try {
			this.is.close();
		} finally {
			this.httpMethod.releaseConnection();
		}
	}

	@Override
	public int read() throws IOException {
		return this.is.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return this.is.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return this.is.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		return this.is.skip(n);
	}

	@Override
	public int available() throws IOException {
		return this.is.available();
	}

	@Override
	public synchronized void mark(int readlimit) {
		this.is.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		this.is.reset();
	}

	@Override
	public boolean markSupported() {
		return this.is.markSupported();
	}

}
