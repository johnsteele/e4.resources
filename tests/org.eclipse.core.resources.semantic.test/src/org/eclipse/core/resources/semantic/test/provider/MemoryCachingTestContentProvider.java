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
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.semantic.examples.remote.RemoteFile;
import org.eclipse.core.resources.semantic.examples.remote.RemoteItem;
import org.eclipse.core.resources.semantic.spi.ICacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.ISemanticFileHistoryProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.MemoryCacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.SemanticRevisionStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.ITag;

/**
 * Memory caching
 * 
 */
public class MemoryCachingTestContentProvider extends CachingTestContentProviderBase {

	/**
	 * @throws CoreException
	 */
	@Override
	public ICacheServiceFactory getCacheServiceFactory() throws CoreException {
		return new MemoryCacheServiceFactory();
	}

	@Override
	@SuppressWarnings({"rawtypes"})
	public Object getAdapter(Class adapter) {
		if (ISemanticFileHistoryProvider.class == adapter) {
			return new ISemanticFileHistoryProvider() {

				public IFileRevision getWorkspaceFileRevision(ISemanticFileStore store) {
					RemoteItem item = getStore().getItemByPath(store.getPath().removeFirstSegments(2));
					if (item == null || item.getType() != RemoteItem.Type.FILE) {
						return null;
					}
					RemoteFile file = (RemoteFile) item;
					return file.getCurrentRevision(store);
				}

				public IFileHistory getHistoryFor(ISemanticFileStore store, int options, IProgressMonitor monitor) {
					RemoteItem item = getStore().getItemByPath(store.getPath().removeFirstSegments(2));
					if (item == null || item.getType() != RemoteItem.Type.FILE) {
						return null;
					}
					RemoteFile file = (RemoteFile) item;
					return file.getHistory(store);
				}

				public IFileRevision[] getResourceVariants(final ISemanticFileStore semanticFileStore, IProgressMonitor monitor) {

					RemoteItem item = getStore().getItemByPath(semanticFileStore.getPath().removeFirstSegments(2));

					if (!(item instanceof RemoteFile)) {
						return null;
					}

					final RemoteFile file = (RemoteFile) item;

					IFileRevision remote = new IFileRevision() {

						public IFileRevision withAllProperties(IProgressMonitor monitor1) {
							return this;
						}

						public boolean isPropertyMissing() {
							return false;
						}

						public URI getURI() {
							try {
								return new URI(file.getPath().toString());
							} catch (URISyntaxException e) {
								// $JL-EXC$ ignore
								throw new RuntimeException("Uri can not be created");
							}
						}

						public long getTimestamp() {
							return file.getTimestamp();
						}

						public ITag[] getTags() {
							return null;
						}

						public IStorage getStorage(IProgressMonitor monitor1) throws CoreException {
							SemanticRevisionStorage storage = new SemanticRevisionStorage(semanticFileStore);
							storage.setContents(new ByteArrayInputStream(file.getContent()), monitor1);
							return storage;
						}

						public String getName() {
							return file.getName();
						}

						public String getContentIdentifier() {
							return null;
						}

						public String getComment() {
							return null;
						}

						public String getAuthor() {
							return null;
						}

						public boolean exists() {
							return true;
						}
					};

					return new IFileRevision[] {null, remote};
				}

			};
		}
		return super.getAdapter(adapter);
	}

}
