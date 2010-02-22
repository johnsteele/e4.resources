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
package org.eclipse.core.resources.semantic.test.provider;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.internal.resources.semantic.SemanticResourcesPlugin;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.spi.ContentProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.ISemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.SemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore.ResourceType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

/**
 * Nothing overridden in order to test the base class methods
 * 
 */
public class NullContentProvider extends ContentProvider {

	private final IStatus error = new Status(IStatus.ERROR, SemanticResourcesPlugin.PLUGIN_ID, "Not Supported");

	public ISemanticSpiResourceInfo fetchResourceInfo(ISemanticFileStore semanticFileStore, int options, IProgressMonitor monitor) {
		return new SemanticSpiResourceInfo(options, false, false, false, true, null, null);
	}

	public long getResourceTimestamp(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) {
		return 0;
	}

	public InputStream openInputStream(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException {
		throw new CoreException(this.error);
	}

	public OutputStream openOutputStream(ISemanticFileStore semanticFileStore, int options, IProgressMonitor monitor) throws CoreException {
		throw new CoreException(this.error);
	}

	public void removeResource(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) {
		// do nothing
	}

	public void revertChanges(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException {
		throw new CoreException(this.error);
	}

	public void setReadOnly(ISemanticFileStore semanticFileStore, boolean readonly, IProgressMonitor monitor) throws CoreException {
		throw new CoreException(this.error);
	}

	public void setResourceTimestamp(ISemanticFileStore semanticFileStore, long timestamp, IProgressMonitor monitor) throws CoreException {
		throw new CoreException(this.error);
	}

	public void synchronizeContentWithRemote(ISemanticFileStore semanticFileStore, SyncDirection direction, IProgressMonitor monitor,
			MultiStatus status) {
		status.add(this.error);
	}

	public IStatus validateEdit(ISemanticFileStore[] semanticFileStore, Object shell) {
		return this.error;
	}

	public IStatus validateSave(ISemanticFileStore semanticFileStore) {
		return this.error;
	}

	public void addResource(ISemanticFileStore parentStore, String name, ResourceType resourceType, IProgressMonitor monitor)
			throws CoreException {
		throw new CoreException(this.error);
	}

}
