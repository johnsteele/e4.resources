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
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.eclipse.core.internal.net.ProxyData;
import org.eclipse.core.internal.net.ProxySelector;
import org.eclipse.core.resources.semantic.spi.CachingContentProvider.ICacheTimestampSetter;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.demo.e4twitterclient.dialogs.UserCredentialsDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

@SuppressWarnings("restriction")
public class HTTPClientUtil {
	private static final byte[] buffer = new byte[8192];

	private static HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
	static String userName;

	public interface InputStreamProvider {
		public InputStream getInputStream() throws IOException;
	}

	/**
	 * 
	 * @param host
	 * @param port
	 * @param userName
	 * @param password
	 */
	public static void setGlobalCredentialsForHost(String host, int port, String userName, String password) {
		if (userName != null && password != null) {
			Credentials defaultcreds = new UsernamePasswordCredentials(userName, password);
			httpClient.getState().setCredentials(new AuthScope(host, port), defaultcreds);
			httpClient.getParams().setAuthenticationPreemptive(true);
		} else {
			httpClient.getState().clearCredentials();
			httpClient.getParams().setAuthenticationPreemptive(false);
		}
	}

	/**
	 * 
	 * @param host
	 * @param port
	 */
	public static boolean needGlobalCredentialsForHost(String host, int port) {
		Credentials creds = httpClient.getState().getCredentials(new AuthScope(host, port));

		return creds == null;
	}

	/**
	 * 
	 * @param host
	 * @param port
	 * @param userName
	 * @param password
	 */
	public static void setGlobalCredentialsForProxy(String host, int port, String userName, String password) {
		if (userName != null && password != null) {
			Credentials defaultcreds = new UsernamePasswordCredentials(userName, password);
			httpClient.getState().setProxyCredentials(new AuthScope(host, port), defaultcreds);
		} else {
			httpClient.getState().setProxyCredentials(new AuthScope(host, port), null);
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

	public static InputStream openInputStream(String remoteURI, ICacheTimestampSetter setter) throws IOException {
		GetMethod getMethod = new GetMethod(remoteURI);

		dump(NLS.bind("Requesting content from {0}", remoteURI));

		getMethod.setFollowRedirects(true);

		installProxyData(remoteURI);

		installCredentialsProvider(getMethod);

		askForCredentials(remoteURI);

		boolean releaseConnectionOnException = true;
		InputStream is;

		try {
			int statusCode = httpClient.executeMethod(getMethod);

			if (statusCode != HttpStatus.SC_OK) {
				String message = NLS.bind("Server returned {0} for URL {1}", getMethod.getStatusLine(), remoteURI);
				throw new IOException(message);
			}

			if (setter != null) {
				Header timestampHeader = getMethod.getResponseHeader("Last-Modified"); //$NON-NLS-1$
				if (timestampHeader != null) {
					String timestampString = timestampHeader.getValue();
					try {
						setter.setTimestamp(parseDateTime(timestampString));
					} catch (ParseException e) {
						throw new IOException(e.getMessage());
					}
				} else {
					Header dateHeader = getMethod.getResponseHeader("Date"); //$NON-NLS-1$
					if (dateHeader != null) {
						String dateString = dateHeader.getValue();
						try {
							setter.setTimestamp(parseDateTime(dateString));
						} catch (ParseException e) {
							throw new IOException(e.getMessage());
						}
					}
				}
			}

			is = getMethod.getResponseBodyAsStream();

			releaseConnectionOnException = false;
		} finally {
			if (releaseConnectionOnException) {
				getMethod.releaseConnection();
			}
		}
		return new InputStreamWrapper(getMethod, is);
	}

	private static void dump(String text) {
		System.out.println(text);
	}

	private static void askForCredentials(String remoteURI) throws URIException {
		org.apache.commons.httpclient.URI uri = new org.apache.commons.httpclient.URI(remoteURI, true);
		final String host = uri.getHost();
		final int port = uri.getPort();
		final String message = uri.getPath();

		if (needGlobalCredentialsForHost(host, port)) {

			class UIOperation implements Runnable {
				public UsernamePasswordCredentials credentials;

				public void run() {
					credentials = (UsernamePasswordCredentials) UserCredentialsDialog.askForCredentials(
							host + ":" + (port > 0 ? port : ""), message, userName);
				}
			}

			UIOperation uio = new UIOperation();
			if (Display.getCurrent() != null) {
				uio.run();
			} else {
				Display.getDefault().syncExec(uio);
			}

			if (uio.credentials != null) {
				userName = uio.credentials.getUserName();
				setGlobalCredentialsForHost(uri.getHost(), uri.getPort(), uio.credentials.getUserName(), uio.credentials.getPassword());
			}
		}
	}

	private static void installProxyData(String remoteURI) throws URIException {
		String defaultProvider = ProxySelector.getDefaultProvider();

		if (!defaultProvider.equals("Direct")) {
			ProxyData[] data = ProxySelector.getProxyData(defaultProvider);

			org.apache.commons.httpclient.URI uri = new org.apache.commons.httpclient.URI(remoteURI, true);

			HostConfiguration hostConfiguration = new HostConfiguration();
			hostConfiguration.setProxy(data[0].getHost(), data[0].getPort());
			hostConfiguration.setHost(uri);

			httpClient.setHostConfiguration(hostConfiguration);

			setGlobalCredentialsForProxy(data[0].getHost(), data[0].getPort(), data[0].getUserId(), data[0].getPassword());
		}
	}

	public static void installCredentialsProvider(HttpMethodBase httpMethod) {
		HttpMethodParams params = new HttpMethodParams();

		params.setParameter(CredentialsProvider.PROVIDER, new CredentialsProvider() {

			public Credentials getCredentials(final AuthScheme scheme, final String host, final int port, boolean proxy)
					throws CredentialsNotAvailableException {
				class UIOperation implements Runnable {
					public Credentials credentials;

					public void run() {
						String message = ""; //$NON-NLS-1$
						if (scheme.getRealm() != null) {
							message = scheme.getRealm();
						}
						credentials = UserCredentialsDialog.askForCredentials(host + ":" + port, message, userName); //$NON-NLS-1$
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
				throw new CredentialsNotAvailableException();
			}
		});

		httpMethod.setParams(params);
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

}
