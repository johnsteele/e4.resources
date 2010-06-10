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
package org.eclipse.core.internal.resources.semantic.provider;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

import org.eclipse.core.internal.resources.semantic.SemanticResourcesPlugin;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.spi.ContentProvider;
import org.eclipse.core.resources.semantic.spi.DefaultMinimalSemanticResourceRuleFactory;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore.ResourceType;
import org.eclipse.core.resources.semantic.spi.ISemanticResourceRuleFactory;
import org.eclipse.core.resources.semantic.spi.ISemanticSpiResourceInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * This content provider is instantiated when the proper content provider can
 * not be created.
 * <p>
 * It throws an exception for all methods.
 * 
 */
public class InvalidContentProvider extends ContentProvider {

	private CoreException createException(ISemanticFileStore semanticFileStore) {
		return new SemanticResourceException(SemanticResourceStatusCode.UNKNOWN_CONTENT_PROVIDER_ID, semanticFileStore.getPath(), NLS.bind(
				Messages.InvalidContentProvider_InvalidContentProviderIDforPath_XMSG, this.getRootStore().getContentProviderID(),
				semanticFileStore.getPath()));
	}

	public void revertChanges(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException {
		throw createException(semanticFileStore);
	}

	public void addFileFromRemoteByURI(ISemanticFileStore childStore, String name, URI uri, IProgressMonitor monitor) throws CoreException {
		throw createException(childStore);
	}

	public void addFolderFromRemoteByURI(ISemanticFileStore childStore, String name, URI uri, IProgressMonitor monitor)
			throws CoreException {
		throw createException(childStore);
	}

	public String getURIString(ISemanticFileStore childStore) throws CoreException {
		throw createException(childStore);
	}

	public void setURIString(ISemanticFileStore semanticFileStore, URI uri, IProgressMonitor monitor) throws CoreException {
		throw createException(semanticFileStore);
	}

	public ISemanticSpiResourceInfo fetchResourceInfo(ISemanticFileStore semanticFileStore, int options, IProgressMonitor monitor)
			throws CoreException {

		throw createException(semanticFileStore);
	}

	public void setReadOnly(ISemanticFileStore childStore, boolean readonly, IProgressMonitor monitor) throws CoreException {
		throw createException(childStore);
	}

	public void removeResource(ISemanticFileStore childStore, IProgressMonitor monitor) throws CoreException {
		throw createException(childStore);
	}

	public void synchronizeContentWithRemote(ISemanticFileStore semanticFileStore, SyncDirection direction, IProgressMonitor monitor,
			MultiStatus status) {
		CoreException e = createException(semanticFileStore);

		status.add(new Status(IStatus.ERROR, SemanticResourcesPlugin.PLUGIN_ID, e.getMessage(), e));
	}

	public IStatus validateEdit(ISemanticFileStore[] stores, Object shell) {
		CoreException e = createException(stores[0]);

		return new Status(IStatus.ERROR, SemanticResourcesPlugin.PLUGIN_ID, e.getMessage(), e);
	}

	public IStatus validateSave(ISemanticFileStore childStore) {
		return validateEdit(new ISemanticFileStore[] {childStore}, null);
	}

	@Override
	public ISemanticResourceRuleFactory getRuleFactory() {
		return new DefaultMinimalSemanticResourceRuleFactory(this.getRootStore());
	}

	// not supported stuff

	public void addResource(ISemanticFileStore parentStore, String name, ResourceType resourceType, IProgressMonitor monitor)
			throws CoreException {
		throw createException(parentStore);
	}

	public void addResource(ISemanticFileStore parentStore, String name, ResourceType resourceType, String contentProviderID,
			Map<QualifiedName, String> properties) throws CoreException {
		throw createException(parentStore);
	}

	public InputStream openInputStream(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException {
		throw createException(semanticFileStore);
	}

	public OutputStream openOutputStream(ISemanticFileStore semanticFileStore, int options, IProgressMonitor monitor) throws CoreException {
		throw createException(semanticFileStore);
	}

	public long getResourceTimestamp(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException {
		throw createException(semanticFileStore);
	}

	public void setResourceTimestamp(ISemanticFileStore semanticFileStore, long timestamp, IProgressMonitor monitor) throws CoreException {
		throw createException(semanticFileStore);
	}

}
