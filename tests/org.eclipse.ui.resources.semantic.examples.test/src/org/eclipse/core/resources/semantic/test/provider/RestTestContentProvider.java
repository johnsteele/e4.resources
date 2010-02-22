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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.examples.SemanticResourcesPluginExamples;
import org.eclipse.core.resources.semantic.examples.providers.RESTUtil;
import org.eclipse.core.resources.semantic.examples.providers.SampleRESTReadonlyContentProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.ISemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.SemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore.ResourceType;
import org.eclipse.core.resources.semantic.test.TestPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * REST content provider
 * 
 */
public class RestTestContentProvider extends SampleRESTReadonlyContentProvider {

	@Override
	public ISemanticSpiResourceInfo fetchResourceInfo(
			ISemanticFileStore semanticFileStore, int options,
			IProgressMonitor monitor) throws CoreException {
		String uriString;
		if (SemanticSpiResourceInfo.isOptionRequested(
				ISemanticFileSystem.RESOURCE_INFO_URI_STRING, options)) {
			uriString = this.getURIStringInternal(semanticFileStore);
		} else {
			uriString = null;
		}

		boolean existsRemotely = false;
		if (SemanticSpiResourceInfo.isOptionRequested(
				ISemanticFileSystem.RESOURCE_INFO_EXISTS_REMOTELY, options)) {

			String remoteURI = getURIString(semanticFileStore);

			if (remoteURI == null) {
				throw new SemanticResourceException(
						SemanticResourceStatusCode.REMOTE_URI_NOT_FOUND,
						semanticFileStore.getPath(), NLS.bind(
								"Remote URI is not set for file {0}",
								semanticFileStore.getPath().toString()));
			}

			try {
				InputStream is = RESTUtil.openInputStream(remoteURI, null);
				existsRemotely = is != null;
				Util.safeClose(is);
			} catch (IOException e) {
				// $JL-EXC$ ignore and simply return false here
			}

		}

		boolean isReadOnly = isReadOnlyInternal(semanticFileStore);
		return new SemanticSpiResourceInfo(options, false, false, isReadOnly,
				existsRemotely, uriString, this
						.getContentTypeInternal(semanticFileStore));
	}

	@Override
	public void revertChanges(ISemanticFileStore semanticFileStore,
			IProgressMonitor monitor) throws CoreException {

		MultiStatus status = new MultiStatus(TestPlugin.PLUGIN_ID, IStatus.OK,
				NLS.bind("Revert Change Result for {0}", semanticFileStore
						.getPath().toString()), null);
		dropCache(semanticFileStore, monitor, new IDropCacheVisitor() {

			public boolean shouldDrop(ISemanticFileStore store) {
				// TODO only files
				return !store.isLocalOnly();
			}
		}, status);

		if (!status.isOK()) {
			throw new CoreException(status);
		}

		fillCache(semanticFileStore, monitor, status);

		if (!status.isOK()) {
			throw new CoreException(status);
		}
		// this can only happen on files, no recursion
		this.setReadOnly(semanticFileStore, true, monitor);
	}

	@Override
	public void synchronizeContentWithRemote(
			ISemanticFileStore semanticFileStore, SyncDirection direction,
			IProgressMonitor monitor, MultiStatus status) {

		if (semanticFileStore.getType() == ISemanticFileStore.FILE) {
			if (semanticFileStore.isLocalOnly()) {
				return;
			}

			URI uri;
			try {
				uri = new URI(getURIString(semanticFileStore));
			} catch (URISyntaxException e) {
				// $JL-EXC$ ignore
				status.add(new Status(IStatus.ERROR,
						SemanticResourcesPluginExamples.PLUGIN_ID, e
								.getMessage()));
				return;
			} catch (CoreException e) {
				status.add(e.getStatus());
				return;
			}

			boolean syncIn = false;
			boolean syncOut = false;
			if (direction == SyncDirection.BOTH) {

				long remoteTimestamp;
				try {
					remoteTimestamp = uri.toURL().openConnection()
							.getLastModified();
				} catch (MalformedURLException e) {
					// $JL-EXC$ ignore
					status.add(new Status(IStatus.ERROR,
							SemanticResourcesPluginExamples.PLUGIN_ID, e
									.getMessage()));
					return;
				} catch (IOException e) {
					// $JL-EXC$ ignore
					status.add(new Status(IStatus.ERROR,
							SemanticResourcesPluginExamples.PLUGIN_ID, e
									.getMessage()));
					return;
				}
				long localTimestamp;
				try {
					localTimestamp = getCacheService().getContentTimestamp(
							semanticFileStore.getPath());
				} catch (CoreException e) {
					status.add(e.getStatus());
					return;
				}
				syncIn = remoteTimestamp > localTimestamp;
				syncOut = localTimestamp > remoteTimestamp;
			}

			if (direction == SyncDirection.INCOMING || syncIn) {
				this.dropCache(semanticFileStore, monitor,
						this.deleteAllVisitor, status);
				this.fillCache(semanticFileStore, monitor, status);
			}
			if (direction == SyncDirection.OUTGOING || syncOut) {
				try {
					File file = new File(uri);
					Util.transferStreams(getCacheService().getContent(
							semanticFileStore.getPath()), new FileOutputStream(
							file), monitor);
					file.setLastModified(getCacheService().getContentTimestamp(
							semanticFileStore.getPath()));
				} catch (CoreException e) {
					status.add(e.getStatus());
				} catch (IOException e) {
					// $JL-EXC$ ignore
					status.add(new Status(IStatus.ERROR,
							SemanticResourcesPluginExamples.PLUGIN_ID, e
									.getMessage()));
					return;
				}
			}

		} else {
			IFileStore[] childStores;
			try {
				childStores = semanticFileStore.childStores(EFS.NONE, monitor);
			} catch (CoreException e) {
				status.add(e.getStatus());
				return;
			}
			for (IFileStore store : childStores) {
				if (store instanceof ISemanticFileStore) {
					synchronizeContentWithRemote((ISemanticFileStore) store,
							direction, monitor, status);
				}
			}
		}
	}

	//
	// not supported
	//

	@Override
	public void addResource(ISemanticFileStore childStore, String name,
			ResourceType resourceType, IProgressMonitor monitor)
			throws CoreException {
		switch (resourceType) {
		case FOLDER_TYPE:
			// create internal data
			childStore.addChildFolder(name);

			break;

		default:
			throw new SemanticResourceException(
					SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, childStore
							.getPath(), "Not supported");
		}
	}

	@Override
	public IStatus validateEdit(ISemanticFileStore[] stores, Object shell) {
		for (ISemanticFileStore store : stores) {
			try {
				setReadOnlyInternal(store, false);
			} catch (CoreException e) {
				return e.getStatus();
			}
		}
		return new Status(IStatus.OK,
				SemanticResourcesPluginExamples.PLUGIN_ID, null);
	}

	@Override
	public IStatus validateSave(ISemanticFileStore semanticFileStore) {
		return validateEdit(new ISemanticFileStore[] { semanticFileStore },
				null);
	}

}
