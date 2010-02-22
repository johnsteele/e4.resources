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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.examples.remote.SemanticResourcesPluginExamplesCore;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore.ResourceType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

/**
 *
 */
public class SampleWSDLXSDContentProvider extends SampleRESTReadonlyContentProvider {

	@Override
	public void addFileFromRemoteByURI(ISemanticFileStore parentStore, String name, URI uri, IProgressMonitor monitor) throws CoreException {
		super.addFileFromRemoteByURI(parentStore, name, uri, monitor);
		this.addDependentFiles((ISemanticFileStore) parentStore.getChild(name), uri.toString(), monitor);
	}

	/**
	 * Adds the dependent files
	 * 
	 * @param store
	 *            the store
	 * @param uri
	 *            the URI
	 * @param monitor
	 *            may be null
	 * @throws CoreException
	 *             on failure
	 */
	public void addDependentFiles(ISemanticFileStore store, String uri, IProgressMonitor monitor) throws CoreException {
		InputStream is = store.openInputStream(EFS.NONE, monitor);
		List<String> uris;

		try {
			StringBuffer buf = RESTUtil.readStreamIntoStringBuffer(is, "UTF-8"); //$NON-NLS-1$

			uris = this.findRelativeURLs(buf);

		} catch (IOException e) {
			// $JL-EXC$ ignore
			throw new CoreException(new Status(IStatus.ERROR, SemanticResourcesPluginExamplesCore.PLUGIN_ID, e.getMessage(), e));
		} finally {
			Util.safeClose(is);
		}

		String rootURI = uri.substring(0, uri.lastIndexOf("/")); //$NON-NLS-1$

		for (String uriString : uris) {
			if (uriString.contains(":")) { //$NON-NLS-1$
				// this is an absolute URI; ignore
				continue;
			}

			if (uriString.contains("?")) { //$NON-NLS-1$
				// this is an invalid file name; ignore
				continue;
			}

			if (uriString.startsWith("/")) { //$NON-NLS-1$
				// this is an URI that is not relative to the document; ignore
				continue;
			}

			String[] parts = uriString.split("/"); //$NON-NLS-1$

			addDependentFiles((ISemanticFileStore) store.getParent(), rootURI, parts, 0, monitor);
		}
	}

	private void addDependentFiles(ISemanticFileStore parentStore, String rootURI, String[] parts, int index, IProgressMonitor monitor)
			throws CoreException {
		addChildrenHierarchy(parentStore, rootURI, parts, index, monitor);
	}

	private void addChildrenHierarchy(ISemanticFileStore childStore, String rootURI, String[] parts, int index, IProgressMonitor monitor)
			throws CoreException {

		IFileStore child = childStore.getChild(parts[index]);

		if (parts.length > index + 1) {
			if (!child.fetchInfo().exists()) {
				this.addResource(childStore, parts[index], ResourceType.FOLDER_TYPE, monitor);
			}

			child = childStore.getChild(parts[index]);

			addChildrenHierarchy((ISemanticFileStore) child, rootURI + "/" + parts[index], parts, index + 1, monitor); //$NON-NLS-1$
		} else {
			// create a file
			if (!child.fetchInfo().exists()) {
				super.addFileFromRemoteByURI(childStore, parts[index], URI.create(rootURI + "/" + parts[index]), monitor); //$NON-NLS-1$

				this.addDependentFiles((ISemanticFileStore) childStore.getChild(parts[index]), rootURI + "/" + parts[index], monitor); //$NON-NLS-1$
			}
		}
	}

	private List<String> findRelativeURLs(StringBuffer buf) {
		List<String> uris = new ArrayList<String>();

		if (buf.indexOf("<wsdl:") < 0) { //$NON-NLS-1$
			Pattern pattern = Pattern.compile("(<import)(.*)( schemaLocation=\")([^\"]+)"); //$NON-NLS-1$
			Matcher matcher = pattern.matcher(buf);
			while (matcher.find()) {
				String relativeURLString = matcher.group(4);
				uris.add(relativeURLString);
			}

			pattern = Pattern.compile("(<import schemaLocation=\")([^\"]+)"); //$NON-NLS-1$
			matcher = pattern.matcher(buf);
			while (matcher.find()) {
				String relativeURLString = matcher.group(2);
				uris.add(relativeURLString);
			}
		} else {
			Pattern pattern = Pattern.compile("(<xsd:import)(.*)( schemaLocation=\")([^\"]+)"); //$NON-NLS-1$
			Matcher matcher = pattern.matcher(buf);
			while (matcher.find()) {
				String relativeURLString = matcher.group(4);
				uris.add(relativeURLString);
			}
			pattern = Pattern.compile("(<xsd:import schemaLocation=\")([^\"]+)"); //$NON-NLS-1$
			matcher = pattern.matcher(buf);
			while (matcher.find()) {
				String relativeURLString = matcher.group(2);
				uris.add(relativeURLString);
			}
		}
		return uris;
	}

	@Override
	public void synchronizeContentWithRemote(ISemanticFileStore semanticFileStore, SyncDirection direction, IProgressMonitor monitor,
			MultiStatus status) {
		super.synchronizeContentWithRemote(semanticFileStore, direction, monitor, status);

		if (semanticFileStore.getType() == ISemanticFileStore.FILE && !semanticFileStore.isLocalOnly()) {
			try {
				String uriString = this.getURIStringInternal(semanticFileStore);
				this.addDependentFiles(semanticFileStore, uriString, monitor);
			} catch (CoreException e) {
				status.add(e.getStatus());
			}
		}
	}

}
