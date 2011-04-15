/*******************************************************************************
 * Copyright (c) 2011 SAP AG.
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
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.internal.resources.semantic.ui.team.SemanticHistoryPage;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.semantic.examples.remote.RemoteFile;
import org.eclipse.core.resources.semantic.examples.remote.RemoteItem;
import org.eclipse.core.resources.semantic.spi.FileCacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.ICacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderLocal;
import org.eclipse.core.resources.semantic.spi.ISemanticFileHistoryProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.SemanticFileCache;
import org.eclipse.core.resources.semantic.spi.SemanticRevisionStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.ITag;
import org.eclipse.team.ui.history.IHistoryPageSource;
import org.eclipse.ui.part.Page;

/**
 * File System Caching
 * 
 */
public class CachingTestContentProviderWithHistoryPage extends CachingTestContentProviderBase implements ISemanticContentProviderLocal {

	/**
	 * @throws CoreException
	 */
	@Override
	public ICacheServiceFactory getCacheServiceFactory() throws CoreException {
		return new FileCacheServiceFactory();
	}

	@Override
	@SuppressWarnings({"rawtypes"})
	public Object getAdapter(Class adapter) {
		if (IHistoryPageSource.class == adapter) {
			return new IHistoryPageSource() {
				@SuppressWarnings("restriction")
				public Page createPage(Object object) {
					return new SemanticHistoryPage((IResource) object);
				}

				public boolean canShowHistoryFor(Object object) {
					return true;
				}
			};
		}
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
							return "RemoteVersion";
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

						public ITag[] getBranches() {
							return new ITag[0];
						}
					};

					return new IFileRevision[] {null, remote};
				}

			};
		}
		return super.getAdapter(adapter);
	}

	public File toLocalFile(ISemanticFileStore semanticFileStore) throws CoreException {

		File cacheFile = new File(SemanticFileCache.getCache().getCacheDir(), semanticFileStore.getPath().toString());
		return cacheFile;
	}

}