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
package org.eclipse.core.internal.resources.semantic.cacheservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import org.eclipse.core.resources.semantic.spi.ICacheUpdateCallback;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

class CachingOutputStream extends OutputStream {

	private final CacheService cacheService;
	private final ITemporaryContentHandle fileHandle;
	private final ICacheUpdateCallback callback;
	private final boolean appendMode;

	protected CachingOutputStream(CacheService service, ITemporaryContentHandle tempHandle, boolean append, ICacheUpdateCallback callback) {

		this.cacheService = service;
		this.fileHandle = tempHandle;
		this.appendMode = append;
		this.callback = callback;

	}

	@Override
	@SuppressWarnings("boxing")
	public void close() throws IOException {
		InputStream stream = null;
		try {

			this.cacheService.addFromTempHandle(this.fileHandle, System.currentTimeMillis());

			IPath path = this.fileHandle.getKey();

			stream = this.cacheService.getContent(path);
			long cacheTimestamp = this.cacheService.getContentTimestamp(path);
			long appendPosition = this.fileHandle.getAppendPosition();
			long skipped = 0l;

			if (appendPosition > 0) {
				skipped = stream.skip(appendPosition);
			}

			if (skipped < appendPosition) {
				throw new IOException(MessageFormat.format(Messages.CachingOutputStream_CouldNotSkip_XMSG, appendPosition, skipped));
			}

			this.callback.cacheUpdated(stream, cacheTimestamp, this.appendMode);

		} catch (CoreException e) {
			// $JL-EXC$ ignore
			throw new IOException(e.getMessage());
		} finally {
			Util.safeClose(stream);
		}
	}

	@Override
	public void flush() throws IOException {
		this.fileHandle.flush();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		this.fileHandle.write(b, off, len);
	}

	@Override
	public void write(byte[] b) throws IOException {
		this.fileHandle.write(b);
	}

	@Override
	public void write(int b) throws IOException {
		this.fileHandle.write(b);
	}
}
