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
package org.eclipse.core.resources.semantic.examples.webdav;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;

public class WebDAVUtil {
	private static final String LOCK_TOKEN_HEADER = "Lock-Token"; //$NON-NLS-1$
	private static final String DEPTH_HEADER = "Depth"; //$NON-NLS-1$

	private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$

	private static final String propfindRequestXML = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>" //$NON-NLS-1$
			+ "<DAV:propfind xmlns:DAV=\"DAV:\">" //$NON-NLS-1$
			+ "<DAV:prop><DAV:displayname/><DAV:getcontenttype/><DAV:getlastmodified/><DAV:resourcetype/>" //$NON-NLS-1$
			+ "<DAV:getetag/><DAV:supportedlock/><DAV:lockdiscovery/></DAV:prop>" + //$NON-NLS-1$
			"</DAV:propfind>"; //$NON-NLS-1$

	private static final byte[] buffer = new byte[8192];

	private static DefaultHttpClient httpClient = new DefaultHttpClient();

	public static class WebDAVNode {
		public IPath path;
		public String contentType;
		public String etag;
		public long lastModified;
		public boolean isFolder;
		public boolean supportsLocking;
		public String lockToken;
		public final ArrayList<WebDAVNode> children = new ArrayList<WebDAVUtil.WebDAVNode>();
	}

	/**
	 * Call-back interface for setting the timestamp and content type.
	 * 
	 */
	public interface IWebDAVCallback {
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

		/**
		 * 
		 * @param value
		 */
		public void setETag(String value);

	}

	public interface InputStreamProvider {
		public InputStream getInputStream() throws IOException;
	}

	/**
	 * Should only be called from unit tests.
	 * 
	 * @param userName
	 * @param password
	 */
	public static void setGlobalCredentialsForTest(String userName, String password) {
		Credentials defaultcreds = new UsernamePasswordCredentials(userName, password);
		httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, defaultcreds);
	}

	public static void sendData(String remoteURI, final InputStreamProvider data, final IProgressMonitor monitor) throws IOException {
		HttpPut putMethod = new HttpPut(remoteURI);

		try {
			installCredentialsProvider(httpClient);

			HttpEntity requestEntity = new InputStreamEntity(data.getInputStream(), -1);

			putMethod.setEntity(requestEntity);

			HttpResponse response = httpClient.execute(putMethod);

			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode < HttpStatus.SC_OK || statusCode > HttpStatus.SC_RESET_CONTENT) {
				throw new IOException(response.getStatusLine().getReasonPhrase());
			}
		} finally {
			// TODO cleanup
		}
	}

	public static final void transferStreams(InputStream source, OutputStream destination, IProgressMonitor monitor) throws IOException {
		IProgressMonitor actMonitor = Util.monitorFor(monitor);
		try {
			synchronized (buffer) {
				while (true) {
					int bytesRead = -1;
					bytesRead = source.read(buffer);
					if (bytesRead == -1)
						break;
					destination.write(buffer, 0, bytesRead);
					actMonitor.worked(1);
				}
			}
		} finally {
			Util.safeClose(source);
		}
	}

	public static boolean checkExistence(String remoteURI, boolean bFolder, IProgressMonitor monitor) {
		boolean existsRemotely = false;

		if (bFolder) {
			try {
				WebDAVUtil.executePropfindRequest(remoteURI, 0, monitor);
				existsRemotely = true;
			} catch (IOException e) {
				// $JL-EXC$ ignore and simply return false here
			}
		} else {
			try {
				InputStream is = WebDAVUtil.openInputStream(remoteURI, null);
				existsRemotely = is != null;
				Util.safeClose(is);
			} catch (IOException e) {
				// $JL-EXC$ ignore and simply return false here
			}
		}
		return existsRemotely;
	}

	public static InputStream openInputStream(String remoteURI, IWebDAVCallback setter) throws IOException {
		HttpPut getMethod = new HttpPut(remoteURI);

		installCredentialsProvider(httpClient);
		boolean releaseConnectionOnException = true;
		InputStream is;

		try {
			HttpResponse response = httpClient.execute(getMethod);

			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_OK) {
				throw new IOException(response.getStatusLine().getReasonPhrase());
			}

			if (setter != null) {
				Header timestampHeader = response.getFirstHeader("Last-Modified"); //$NON-NLS-1$
				if (timestampHeader != null) {
					String timestampString = timestampHeader.getValue();
					try {
						setter.setTimestamp(parseDateTime(timestampString));
					} catch (ParseException e) {
						throw new IOException(e.getMessage());
					}
				} else {
					Header dateHeader = response.getFirstHeader("Date"); //$NON-NLS-1$
					if (dateHeader != null) {
						String dateString = dateHeader.getValue();
						try {
							setter.setTimestamp(parseDateTime(dateString));
						} catch (ParseException e) {
							throw new IOException(e.getMessage());
						}
					}
				}

				Header contentTypeHeader = response.getFirstHeader("Content-Type"); //$NON-NLS-1$
				if (contentTypeHeader != null) {
					setter.setContentType(contentTypeHeader.getValue());
				}

				Header eTagHeader = response.getFirstHeader("ETag"); //$NON-NLS-1$
				if (eTagHeader != null) {
					setter.setETag(eTagHeader.getValue());
				}
			}

			is = response.getEntity().getContent();

			releaseConnectionOnException = false;
		} finally {
			if (releaseConnectionOnException) {
				// TODO cleanup
			}
		}
		return new InputStreamWrapper(getMethod, is);
	}

	/**
	 * 
	 * @param uri
	 *            uri that points to WebDAV resource
	 * @param monitor
	 * @return <code>true</code> in case of folder and <code>false</code> in
	 *         case of file
	 * @throws IOException
	 */
	public static boolean checkWebDAVURL(URI uri, IProgressMonitor monitor) throws IOException {
		MultistatusType multistatus = WebDAVUtil.executePropfindRequest(uri.toString(), 0, monitor);

		WebDAVNode node = convertResponseToNodeTree(uri, multistatus, monitor);

		if (node != null) {
			return node.isFolder;
		}
		// TODO extern
		throw new IOException("No Node"); //$NON-NLS-1$
	}

	public static WebDAVNode retrieveRemoteState(URI rootURI, IProgressMonitor monitor) throws IOException {
		MultistatusType multistatus = WebDAVUtil.executePropfindRequest(rootURI.toString(), -1, monitor);

		return convertResponseToNodeTree(rootURI, multistatus, monitor);
	}

	public static MultistatusType executePropfindRequest(String uriString, int depth, IProgressMonitor monitor) throws IOException {

		PropfindMethod propfindMethod = new PropfindMethod(URI.create(uriString));

		propfindMethod.setEntity(new HttpEntity() {

			public boolean isRepeatable() {
				return true;
			}

			public Header getContentType() {
				return new BasicHeader("content-type", "text/xml; charset=\"utf-8\""); //$NON-NLS-1$ //$NON-NLS-2$
			}

			public long getContentLength() {
				try {
					return propfindRequestXML.getBytes(UTF_8).length;
				} catch (UnsupportedEncodingException e) {
					return -1;
				}
			}

			public boolean isChunked() {
				return false;
			}

			public Header getContentEncoding() {
				return null;
			}

			public InputStream getContent() throws IllegalStateException {
				return null;
			}

			public void writeTo(OutputStream outstream) throws IOException {
				outstream.write(propfindRequestXML.getBytes(UTF_8));
			}

			public boolean isStreaming() {
				return false;
			}

			@Deprecated
			public void consumeContent() {
				//
			}
		});

		installCredentialsProvider(httpClient);

		try {
			if (depth == 0) {
				propfindMethod.addHeader(DEPTH_HEADER, "0"); //$NON-NLS-1$
			} else if (depth == 1) {
				propfindMethod.addHeader(DEPTH_HEADER, "1"); //$NON-NLS-1$				
			}
			HttpResponse response = httpClient.execute(propfindMethod);

			int statusCode = response.getStatusLine().getStatusCode();

			monitor.worked(1);

			if (statusCode == HttpStatus.SC_NOT_FOUND) {
				throw new WebDAVResourceNotFoundException(response.getStatusLine().getReasonPhrase());
			} else if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_MULTI_STATUS) {
				throw new IOException(response.getStatusLine().getReasonPhrase());
			}

			// System.out.println(propfindMethod.getResponseBodyAsString());

			return new PropfindResponseReader().loadMultitatusTypeFromResponse(response.getEntity().getContent(), monitor);
		} finally {
			// TODO cleanup
		}
	}

	public static void installCredentialsProvider(DefaultHttpClient httpMethod) {
		httpMethod.setCredentialsProvider(new CredentialsProvider() {

			public void setCredentials(AuthScope authscope, Credentials credentials) {
				//
			}

			public Credentials getCredentials(final AuthScope authscope) {
				class UIOperation implements Runnable {
					public Credentials credentials;

					public void run() {
						String message = ""; //$NON-NLS-1$
						if (authscope.getRealm() != null) {
							message = authscope.getRealm();
						}
						credentials = UserCredentialsDialog.askForCredentials(authscope.getHost() + ":" + authscope.getPort(), message); //$NON-NLS-1$
					}
				}

				UIOperation uio = new UIOperation();
				if (Display.getCurrent() != null) {
					uio.run();
				} else {
					Display.getDefault().syncExec(uio);
				}

				if (uio.credentials != null) {
					return uio.credentials;
				}
				return null;
			}

			public void clear() {
				//
			}
		});
	}

	public static IPath calculateRelativePath(URI rootURI, String href) throws URISyntaxException {
		String relativePath;
		URI webdavResourceURI = new URI(href);

		if (webdavResourceURI.isAbsolute()) {
			URI relativeURI = rootURI.relativize(webdavResourceURI);

			relativePath = relativeURI.toString();
		} else {
			String rootPath = rootURI.getPath();

			if (href.startsWith(rootPath)) {
				relativePath = href.substring(rootPath.length());
				if (relativePath.startsWith("/")) { //$NON-NLS-1$
					relativePath = relativePath.substring(1);
				}
			} else {
				// TODO unclear what to do when href is not under root path
				throw new URISyntaxException(href, "in not prefixed by root path"); //$NON-NLS-1$
			}
		}
		return new Path(relativePath);
	}

	public static WebDAVNode convertResponseToNodeTree(URI rootURI, MultistatusType multistatus, IProgressMonitor monitor)
			throws IOException {
		HashMap<IPath, WebDAVNode> nodes = new HashMap<IPath, WebDAVNode>();
		WebDAVNode rootNode = null;

		for (ResponseType response : multistatus.getResponse()) {
			try {
				WebDAVNode node = new WebDAVNode();

				node.path = WebDAVUtil.calculateRelativePath(rootURI, response.getHref());

				nodes.put(node.path, node);

				for (PropstatType propstat : response.getPropstat()) {
					if (propstat.getStatus().contains("200")) { //$NON-NLS-1$
						PropType prop = propstat.getProp();

						if (prop.getLastmodified() != null) {
							try {
								node.lastModified = parseDateTime(prop.getLastmodified());
							} catch (ParseException e) {
								throw new IOException(e.getMessage());
							}
						}
						node.contentType = prop.getContentType();
						node.isFolder = prop.getIsFolder();
						node.etag = prop.getETag();
						node.supportsLocking = prop.getSupportsLocking();
						node.lockToken = prop.getLockToken();
					}
				}
			} catch (URISyntaxException e) {
				throw new IOException(e.getMessage());
			}
		}

		for (WebDAVNode node : nodes.values()) {
			if (node.path.segmentCount() == 0) {
				rootNode = node;
			} else {
				IPath parentPath = node.path.removeLastSegments(1);

				WebDAVNode parent = nodes.get(parentPath);

				if (parent != null) {
					parent.children.add(node);
				} else {
					// TODO broken hierarchy
				}
			}
		}

		return rootNode;
	}

	private static long parseDateTime(String lastmodified) throws ParseException {
		NumberFormat nf = NumberFormat.getIntegerInstance(Locale.ENGLISH);

		lastmodified = lastmodified.substring(5);

		String[] parts = lastmodified.split(" "); //$NON-NLS-1$
		int year = nf.parse(parts[2]).intValue();
		int month = decodeMonths(parts[1]);
		int date = nf.parse(parts[0]).intValue();
		String[] timeparts = parts[3].split(":"); //$NON-NLS-1$
		int hourOfDay = nf.parse(timeparts[0]).intValue();
		int minute = nf.parse(timeparts[1]).intValue();
		int second = nf.parse(timeparts[2]).intValue();

		GregorianCalendar cal = new GregorianCalendar(year, month, date, hourOfDay, minute, second);

		return cal.getTimeInMillis();
	}

	private static int decodeMonths(String string) {
		if (string.equals("Jan")) { //$NON-NLS-1$
			return 1;
		} else if (string.equals("Feb")) { //$NON-NLS-1$
			return 2;
		} else if (string.equals("Mar")) { //$NON-NLS-1$
			return 3;
		} else if (string.equals("Apr")) { //$NON-NLS-1$
			return 4;
		} else if (string.equals("May")) { //$NON-NLS-1$
			return 5;
		} else if (string.equals("Jun")) { //$NON-NLS-1$
			return 6;
		} else if (string.equals("Jul")) { //$NON-NLS-1$
			return 7;
		} else if (string.equals("Aug")) { //$NON-NLS-1$
			return 8;
		} else if (string.equals("Sep")) { //$NON-NLS-1$
			return 9;
		} else if (string.equals("Oct")) { //$NON-NLS-1$
			return 10;
		} else if (string.equals("Nov")) { //$NON-NLS-1$
			return 11;
		} else if (string.equals("Dec")) { //$NON-NLS-1$
			return 12;
		}
		return 0;
	}

	/**
	 * 
	 * @param remoteURI
	 * @param monitor
	 * @return lock token
	 * @throws IOException
	 */
	public static String sendLockRequest(String remoteURI, final IProgressMonitor monitor) throws IOException {
		LockMethod lockMethod = new LockMethod(URI.create(remoteURI));

		try {
			installCredentialsProvider(httpClient);

			String requestBody = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:lockinfo xmlns:D='DAV:'>" //$NON-NLS-1$
					+ "<D:lockscope><D:exclusive/></D:lockscope><D:locktype><D:write/></D:locktype></D:lockinfo>"; //$NON-NLS-1$ 
			final String finalRequestBody = requestBody;

			final InputStreamProvider data = new InputStreamProvider() {
				public InputStream getInputStream() throws IOException {
					return new ByteArrayInputStream(finalRequestBody.getBytes(UTF_8));
				}
			};

			BasicHttpEntity requestEntity = new BasicHttpEntity();

			requestEntity.setContent(data.getInputStream());

			lockMethod.setEntity(requestEntity);

			HttpResponse response = httpClient.execute(lockMethod);

			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED) {
				throw new IOException(response.getStatusLine().getReasonPhrase());
			}

			Header header = response.getFirstHeader(LOCK_TOKEN_HEADER);

			if (header != null) {
				return header.getValue();
			}
		} finally {
			// TODO cleanup
		}
		return null;
	}

	/**
	 * 
	 * @param remoteURI
	 * @param monitor
	 * @throws IOException
	 */
	public static void sendUnlockRequest(String remoteURI, String lockToken, final IProgressMonitor monitor) throws IOException {
		UnlockMethod unlockMethod = new UnlockMethod(URI.create(remoteURI));

		try {
			installCredentialsProvider(httpClient);

			unlockMethod.addHeader(LOCK_TOKEN_HEADER, lockToken);

			HttpResponse response = httpClient.execute(unlockMethod);

			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_NO_CONTENT && statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CONFLICT) {
				throw new IOException(response.getStatusLine().getReasonPhrase());
			}
		} finally {
			// TODO cleanup
		}
	}

	private static class PropfindMethod extends HttpEntityEnclosingRequestBase {

		/**
		 * Constructor specifying a URI string.
		 * 
		 * @param uri
		 *            either an absolute or relative URI
		 */
		public PropfindMethod(URI uri) {
			super();
			setURI(uri);
		}

		@Override
		public String getMethod() {
			return "PROPFIND"; //$NON-NLS-1$
		}

	}

	private static class LockMethod extends HttpEntityEnclosingRequestBase {

		/**
		 * Constructor specifying a URI string.
		 * 
		 * @param uri
		 *            either an absolute or relative URI
		 */
		public LockMethod(URI uri) {
			super();
			setURI(uri);
		}

		@Override
		public String getMethod() {
			return "LOCK"; //$NON-NLS-1$
		}

	}

	protected static class UnlockMethod extends HttpEntityEnclosingRequestBase {

		/**
		 * Constructor specifying a URI string.
		 * 
		 * @param uri
		 *            either an absolute or relative URI
		 */
		public UnlockMethod(URI uri) {
			super();
			setURI(uri);
		}

		@Override
		public String getMethod() {
			return "UNLOCK"; //$NON-NLS-1$
		}

	}

}
