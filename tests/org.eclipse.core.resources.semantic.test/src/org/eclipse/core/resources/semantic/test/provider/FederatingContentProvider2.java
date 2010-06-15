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
package org.eclipse.core.resources.semantic.test.provider;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.spi.ContentProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderFederation2;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore.ResourceType;
import org.eclipse.core.resources.semantic.spi.ISemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.SemanticSpiResourceInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

/**
 * Tests federation
 * 
 */
public class FederatingContentProvider2 extends ContentProvider implements ISemanticContentProviderFederation2 {

	private static final Map<String, String> pathTemplateMap = new HashMap<String, String>();

	static {
		pathTemplateMap.put("B", FederatedContentProvider.class.getName());
		// TODO use another content provider
		pathTemplateMap.put("C", "org.eclipse.core.resources.semantic.provider.DefaultContentProvider");

		// invalid ID
		pathTemplateMap.put("D", "invalid ID");

		// nested federating provider
		pathTemplateMap.put("E", FederatingContentProvider3.class.getName());

	}

	public FederatedProviderInfo getFederatedProviderInfoForPath(IPath path) {
		IPath checkPath = path.removeFirstSegments(getRootStore().getPath().segmentCount());

		if (checkPath.segmentCount() < 2) {
			return null;
		}
		if (checkPath.segment(0).equals("A")) {
			if (pathTemplateMap.get(checkPath.segment(1)) != null) {
				return new FederatedProviderInfo(pathTemplateMap.get(checkPath.segment(1)), 2);
			}
		}
		return null;
	}

	public void addResource(ISemanticFileStore parentStore, String name, ResourceType resourceType, IProgressMonitor monitor)
			throws CoreException {
		switch (resourceType) {
			case FILE_TYPE :
				parentStore.addChildResource(name, false, null, null);
				break;

			default :
				throw new RuntimeException("Not supported");
		}

	}

	public ISemanticSpiResourceInfo fetchResourceInfo(ISemanticFileStore semanticFileStore, int options, IProgressMonitor monitor) {
		return new SemanticSpiResourceInfo(options, false, false, false, false, null, null);
	}

	public long getResourceTimestamp(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) {
		throw new RuntimeException("Not supported");
	}

	public InputStream openInputStream(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) {
		throw new RuntimeException("Not supported");
	}

	public OutputStream openOutputStream(ISemanticFileStore childStore, int options, IProgressMonitor monitor) throws CoreException {
		throw new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, childStore.getPath(),
				"can not open output stream for this resource"); //$NON-NLS-1$
	}

	public void removeResource(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) {
		// simply ignore here (would otherwise result in problems during cleanup
		// of test

	}

	public void revertChanges(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) {
		throw new RuntimeException("Not supported");

	}

	public void setReadOnly(ISemanticFileStore semanticFileStore, boolean readonly, IProgressMonitor monitor) {
		throw new RuntimeException("Not supported");

	}

	public void setResourceTimestamp(ISemanticFileStore semanticFileStore, long timestamp, IProgressMonitor monitor) {
		throw new RuntimeException("Not supported");

	}

	public void synchronizeContentWithRemote(ISemanticFileStore semanticFileStore, SyncDirection direction, IProgressMonitor monitor,
			MultiStatus status) {
		throw new RuntimeException("Not supported");

	}

	public IStatus validateEdit(ISemanticFileStore[] semanticFileStores, Object shell) {
		throw new RuntimeException("Not supported");
	}

	public IStatus validateSave(ISemanticFileStore semanticFileStore) {
		throw new RuntimeException("Not supported");
	}

}
