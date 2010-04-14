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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.Assert;

import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.spi.FileCacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.ICacheService;
import org.eclipse.core.resources.semantic.spi.ICacheUpdateCallback;
import org.eclipse.core.resources.semantic.spi.MemoryCacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.SemanticFileCache;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.junit.Test;

/**
 * 
 */
public class TestCacheService {

	/**
	 * @throws Exception
	 */
	@Test
	public void testCacheService() throws Exception {
		ICacheService service = new FileCacheServiceFactory().getCacheService();

		IPath path = new Path("/test/file.txt");
		String content = "test";
		InputStream input = new ByteArrayInputStream(content.getBytes("UTF-8"));

		writeToCache(service, path, input);

		String content2 = "test2";
		InputStream input2 = new ByteArrayInputStream(content2.getBytes("UTF-8"));

		writeToCache(service, path, input2);

		readFromCache(service, path, content2);

		String content3 = "test3";
		writeToCacheViaOutputStream(service, path, content3);

		readFromCache(service, path, content3);

		String content4 = "appended";
		InputStream input4 = new ByteArrayInputStream(content4.getBytes("UTF-8"));
		appendToCache(service, path, input4);

		readFromCache(service, path, content3 + content4);

		IPath path2 = new Path("/test/file2.txt");

		writeToCache(service, path2, input2);

		removeFromCache(service, path2);

		manipulateTimestamp(service, path);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testFileSystemCompaction() throws Exception {
		ICacheService service = new FileCacheServiceFactory().getCacheService();

		IPath path = new Path("/test2/file.txt");
		String content = "test";
		InputStream input = new ByteArrayInputStream(content.getBytes("UTF-8"));

		appendToCache(service, path, input);

		removeFromCache(service, path);

		File cacheFile = new File(SemanticFileCache.getCache().getCacheDir(), path.toString());
		Assert.assertTrue(!cacheFile.exists());
		Assert.assertTrue(!cacheFile.getParentFile().exists());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testContentDeletion() throws Exception {
		ICacheService service = new FileCacheServiceFactory().getCacheService();

		IPath path = new Path("/test2/file.txt");
		String content = "test";
		InputStream input = new ByteArrayInputStream(content.getBytes("UTF-8"));

		File cacheFile = new File(SemanticFileCache.getCache().getCacheDir(), path.toString());

		appendToCache(service, path, input);

		// this will open an input stream that will prevent a file from being
		// deleted
		InputStream is = service.getContent(path);

		try {
			removeFromCache(service, path);
		} catch (CoreException e) {
			// $JL-EXC$ ignore exception

			// the cache file must still be there but hasContent must report
			// false
			Assert.assertTrue(!service.hasContent(path));
			Assert.assertTrue(cacheFile.exists());
		} finally {
			// this will close the stream so that a file can be deleted
			Util.safeClose(is);
		}

		// the cache file must be removed during check method and both checks
		// return false
		Assert.assertTrue(!service.hasContent(path));
		Assert.assertTrue(!cacheFile.exists());
		Assert.assertTrue(!cacheFile.getParentFile().exists());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testRecursiveContentDeletion() throws Exception {
		ICacheService service = new FileCacheServiceFactory().getCacheService();

		IPath rootPath = new Path("/testrecursive/testroot/");

		IPath path = rootPath.append("/test2/file.txt");
		String content = "test";
		InputStream input = new ByteArrayInputStream(content.getBytes("UTF-8"));

		File cacheFile = new File(SemanticFileCache.getCache().getCacheDir(), path.toString());

		IPath path2 = rootPath.append("/test2/test3/file.txt");
		String content2 = "test";
		InputStream input2 = new ByteArrayInputStream(content2.getBytes("UTF-8"));
		File cacheFile2 = new File(SemanticFileCache.getCache().getCacheDir(), path.toString());

		appendToCache(service, path, input);

		appendToCache(service, path2, input2);

		removeFromCacheRecursive(service, rootPath);

		Assert.assertTrue(!service.hasContent(path));
		Assert.assertTrue(!service.hasContent(path2));
		Assert.assertTrue(!cacheFile.exists());
		Assert.assertTrue(!cacheFile.getParentFile().exists());
		Assert.assertTrue(!cacheFile2.exists());
		Assert.assertTrue(!cacheFile2.getParentFile().exists());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testRecursiveContentDeletion2() throws Exception {
		ICacheService service = new FileCacheServiceFactory().getCacheService();

		IPath rootPath = new Path("/testrecursive/testroot/");

		IPath path = rootPath.append("/test2/file.txt");
		String content = "test";
		InputStream input = new ByteArrayInputStream(content.getBytes("UTF-8"));

		File cacheFile = new File(SemanticFileCache.getCache().getCacheDir(), path.toString());

		IPath path2 = rootPath.append("/test2/test3/file.txt");
		String content2 = "test";
		InputStream input2 = new ByteArrayInputStream(content2.getBytes("UTF-8"));
		File cacheFile2 = new File(SemanticFileCache.getCache().getCacheDir(), path.toString());

		appendToCache(service, path, input);

		appendToCache(service, path2, input2);

		// this will open an input stream that will prevent a file from being
		// deleted
		InputStream is = service.getContent(path);

		try {
			removeFromCacheRecursive(service, rootPath);
		} finally {
			Util.safeClose(is);
		}

		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			Assert.assertTrue("File must still be present", cacheFile.exists());
			Assert.assertTrue("Parent folder must still be present", cacheFile.getParentFile().exists());
		}

		// the cache file must be removed during check method and both checks
		// return false
		Assert.assertTrue(!service.hasContent(path));
		Assert.assertTrue(!service.hasContent(path2));
		Assert.assertTrue(!cacheFile.exists());
		Assert.assertTrue(!cacheFile.getParentFile().exists());
		Assert.assertTrue(!cacheFile2.exists());
		Assert.assertTrue(!cacheFile2.getParentFile().exists());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testRecursiveContentDeletion3() throws Exception {
		ICacheService service = new MemoryCacheServiceFactory().getCacheService();

		IPath rootPath = new Path("/testrecursive/testroot/");

		IPath path = rootPath.append("/test2/file.txt");
		String content = "test";
		InputStream input = new ByteArrayInputStream(content.getBytes("UTF-8"));

		IPath path2 = rootPath.append("/test2/test3/file.txt");
		String content2 = "test";
		InputStream input2 = new ByteArrayInputStream(content2.getBytes("UTF-8"));

		appendToCache(service, path, input);

		appendToCache(service, path2, input2);

		removeFromCacheRecursive(service, rootPath);

		Assert.assertTrue(!service.hasContent(path));
		Assert.assertTrue(!service.hasContent(path2));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testRecursiveContentDeletion4() throws Exception {
		ICacheService service = new MemoryCacheServiceFactory().getCacheService();

		IPath rootPath = new Path("/testrecursive/testroot/");

		IPath path = rootPath.append("/test2/file.txt");
		String content = "test";
		InputStream input = new ByteArrayInputStream(content.getBytes("UTF-8"));

		IPath path2 = rootPath.append("/test2/test3/file.txt");
		String content2 = "test";
		InputStream input2 = new ByteArrayInputStream(content2.getBytes("UTF-8"));

		appendToCache(service, path, input);

		appendToCache(service, path2, input2);

		removeFromCacheRecursive(service, path);

		Assert.assertTrue(!service.hasContent(path));
		Assert.assertTrue(service.hasContent(path2));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testConcurrentAccess() throws Exception {
		ICacheService service = new FileCacheServiceFactory().getCacheService();

		IPath path = new Path("/test2/file.txt");
		String content = "test";
		InputStream input = new ByteArrayInputStream(content.getBytes("UTF-8"));

		writeToCache(service, path, input);

		IPath path1 = new Path("/test2/file1.txt");
		String content1 = "test1";
		InputStream input1 = new ByteArrayInputStream(content1.getBytes("UTF-8"));

		writeToCache(service, path1, input1);

		// this will open an input stream that will prevent a file from being
		// deleted
		InputStream is = service.getContent(path);
		InputStream is1 = service.getContent(path1);

		try {
			writeToCache(service, path, new ByteArrayInputStream("test2".getBytes("UTF-8")));
			writeToCache(service, path1, new ByteArrayInputStream("test2".getBytes("UTF-8")));

			readFromCache(service, path, "test2");
			readFromCache(service, path1, "test2");

			// this will open an input stream that will prevent an alternative
			// file from being
			// deleted
			InputStream is2 = service.getContent(path);
			InputStream is3 = service.getContent(path1);

			try {
				writeToCache(service, path, new ByteArrayInputStream("test3".getBytes("UTF-8")));
				writeToCache(service, path1, new ByteArrayInputStream("test3".getBytes("UTF-8")));

				readFromCache(service, path, "test3");
				readFromCache(service, path1, "test3");
			} finally {
				// this will close the stream so that an alternative file can be
				// deleted
				Util.safeClose(is2);
				Util.safeClose(is3);
			}
		} catch (CoreException e) {
			// $JL-EXC$ ignore exception
			Assert.fail("Should not fail even if file open");

		} finally {
			// this will close the stream so that a file can be deleted
			Util.safeClose(is);
			Util.safeClose(is1);
		}

		File cacheFile = new File(SemanticFileCache.getCache().getCacheDir(), path.toString());

		Assert.assertTrue(cacheFile.exists());

		// this should remove the alternate files and go back to original
		writeToCache(service, path, new ByteArrayInputStream("test4".getBytes("UTF-8")));

		Assert.assertTrue(cacheFile.exists());

		Assert.assertTrue(service.hasContent(path));
	}

	/**
	 * @param service
	 * @param path
	 * @throws CoreException
	 */
	public void manipulateTimestamp(ICacheService service, IPath path) throws CoreException {
		// TODO removed due to problems with less than millisecond accuracy
		// long timestamp = 1234567890123L;
		//
		// service.setContentTimestamp(path, timestamp);
		//
		// Assert.assertEquals(timestamp, service.getContentTimestamp(path));
	}

	/**
	 * @param service
	 * @param path
	 * @param input
	 * @throws CoreException
	 */
	public void writeToCache(ICacheService service, IPath path, InputStream input) throws CoreException {

		service.addContent(path, input, ISemanticFileSystem.NONE, null);

		Assert.assertTrue(service.hasContent(path));
	}

	/**
	 * @param service
	 * @param path
	 * @param input
	 * @throws CoreException
	 */
	public void appendToCache(ICacheService service, IPath path, InputStream input) throws CoreException {

		service.addContent(path, input, ISemanticFileSystem.CONTENT_APPEND, null);

		Assert.assertTrue(service.hasContent(path));
	}

	/**
	 * @param service
	 * @param path
	 * @throws CoreException
	 */
	public void removeFromCache(ICacheService service, IPath path) throws CoreException {

		service.removeContent(path, null);

		Assert.assertTrue(!service.hasContent(path));
	}

	/**
	 * @param service
	 * @param path
	 * @throws CoreException
	 */
	public void removeFromCacheRecursive(ICacheService service, IPath path) throws CoreException {
		service.removeContentRecursive(path, null);
	}

	/**
	 * @param service
	 * @param path
	 * @param content
	 * @throws CoreException
	 */
	public void writeToCacheViaOutputStream(ICacheService service, IPath path, String content) throws CoreException {

		final ByteArrayOutputStream remoteOs = new ByteArrayOutputStream();

		ICacheUpdateCallback callback = new ICacheUpdateCallback() {

			public void cacheUpdated(InputStream newContent, long cacheTimestamp, boolean append) {
				try {
					Util.transferStreams(newContent, remoteOs, null);
				} catch (CoreException e) {
					// $JL-EXC$ ignore here
				}

			}
		};

		OutputStream os = service.wrapOutputStream(path, false, callback, null);

		try {
			os.write(content.getBytes());

			os.close();

			compareByteArrayWithContent(content, remoteOs.toByteArray());
		} catch (IOException e) {
			// $JL-EXC$
			throw new CoreException(new Status(IStatus.ERROR, "test", 0, content, e));
		}

	}

	/**
	 * @param service
	 * @param path
	 * @param content
	 * @throws CoreException
	 */
	public void readFromCache(ICacheService service, IPath path, String content) throws CoreException {
		InputStream is = null;

		try {
			is = service.getContent(path);

			compareInputStreamWithContent(content, is);

		} catch (IOException e) {
			// $JL-EXC$
			throw new CoreException(new Status(IStatus.ERROR, "test", 0, content, e));
		} finally {
			Util.safeClose(is);
		}

	}

	private void compareInputStreamWithContent(String content, InputStream is) throws IOException {
		byte b[] = new byte[content.length() + 2];
		int size = is.read(b);

		Assert.assertEquals(content.length(), size);

		for (int i = 0; i < size; i++) {
			byte c = b[i];
			Assert.assertTrue("Content is different", c == content.charAt(i));
		}
	}

	private void compareByteArrayWithContent(String content, byte[] b) {
		Assert.assertEquals(content.length(), b.length);

		for (int i = 0; i < b.length; i++) {
			byte c = b[i];
			Assert.assertTrue("Content is different", c == content.charAt(i));
		}
	}
}
