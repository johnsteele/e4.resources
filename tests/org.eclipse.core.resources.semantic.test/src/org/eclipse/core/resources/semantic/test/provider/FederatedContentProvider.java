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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.internal.resources.semantic.SemanticResourcesPlugin;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.spi.ContentProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.ISemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.SemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore.ResourceType;
import org.eclipse.core.resources.semantic.test.TestPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;

/**
 * Tests federation
 * 
 */
public class FederatedContentProvider extends ContentProvider {

	private final static QualifiedName READONLY = new QualifiedName(TestPlugin.PLUGIN_ID, "ReadOnly");
	private final static QualifiedName TIMESTAMP = new QualifiedName(TestPlugin.PLUGIN_ID, "Timestamp");

	public void addResource(ISemanticFileStore parentStore, String name, ResourceType resourceType, IProgressMonitor monitor)
			throws CoreException {
		ISemanticFileStore newChild;

		switch (resourceType) {

			case FILE_TYPE :
				parentStore.addChildFile(name);
				newChild = (ISemanticFileStore) parentStore.getChild(name);
				setTimestamp(newChild, System.currentTimeMillis());
				setReadOnly(newChild, true, monitor);
				break;
			case FOLDER_TYPE :
				parentStore.addChildFolder(name);
				newChild = (ISemanticFileStore) parentStore.getChild(name);
				setTimestamp(newChild, System.currentTimeMillis());
				setReadOnly(newChild, true, monitor);
				break;
			default :
				throw new CoreException(new Status(IStatus.ERROR, SemanticResourcesPlugin.PLUGIN_ID, "Can not add resource of type "
						+ resourceType.name()));
		}
	}

	public ISemanticSpiResourceInfo fetchResourceInfo(ISemanticFileStore semanticFileStore, int options, IProgressMonitor monitor)
			throws CoreException {
		return new SemanticSpiResourceInfo(options, false, false, semanticFileStore.getPersistentProperty(READONLY) != null, true, null,
				null);
	}

	public long getResourceTimestamp(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException {
		return getTimestampe(semanticFileStore);
	}

	public InputStream openInputStream(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) {
		return new ByteArrayInputStream(new byte[0]);
	}

	public OutputStream openOutputStream(ISemanticFileStore childStore, int options, IProgressMonitor monitor) {
		return new OutputStream() {

			@Override
			public void write(int b) {
				// we don't need this

			}

			@Override
			public void close() throws IOException {
				// we don't need this
				super.close();
			}

		};
	}

	public void removeResource(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) throws CoreException {
		semanticFileStore.remove(monitor);

	}

	public void revertChanges(ISemanticFileStore semanticFileStore, IProgressMonitor monitor) {
		throw new RuntimeException("Not supported");

	}

	public void setReadOnly(ISemanticFileStore semanticFileStore, boolean readonly, IProgressMonitor monitor) throws CoreException {
		if (readonly) {
			semanticFileStore.setPersistentProperty(READONLY, "");
		} else {
			semanticFileStore.setPersistentProperty(READONLY, null);
		}

	}

	public void setResourceTimestamp(ISemanticFileStore semanticFileStore, long timestamp, IProgressMonitor monitor) throws CoreException {
		setTimestamp(semanticFileStore, timestamp);

	}

	public void synchronizeContentWithRemote(ISemanticFileStore semanticFileStore, SyncDirection direction, IProgressMonitor monitor,
			MultiStatus status) {
		throw new RuntimeException("Not supported");
	}

	public IStatus validateEdit(ISemanticFileStore[] stores, Object shell) {
		MultiStatus multi = new MultiStatus(TestPlugin.PLUGIN_ID, IStatus.OK, "Validate Edit Result", null);
		for (ISemanticFileStore store : stores) {
			try {
				store.setPersistentProperty(READONLY, null);
			} catch (CoreException e) {
				multi.add(e.getStatus());
			}
		}
		return multi;
	}

	public IStatus validateSave(ISemanticFileStore semanticFileStore) {
		return new Status(IStatus.OK, TestPlugin.PLUGIN_ID, "");
	}

	private void setTimestamp(ISemanticFileStore store, long currentTimeMillis) throws CoreException {
		store.setPersistentProperty(TIMESTAMP, Long.toString(currentTimeMillis));
	}

	private long getTimestampe(ISemanticFileStore store) throws CoreException {
		String test = store.getPersistentProperty(TIMESTAMP);
		if (test == null) {
			return 0;
		}
		try {
			return Long.parseLong(test);
		} catch (NumberFormatException e) {
			// $JL-EXC$
			return 0;
		}
	}

}
