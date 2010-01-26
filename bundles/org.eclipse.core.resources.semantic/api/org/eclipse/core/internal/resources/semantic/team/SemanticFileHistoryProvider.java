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
package org.eclipse.core.internal.resources.semantic.team;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticFileHistoryProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.provider.FileHistory;
import org.eclipse.team.core.history.provider.FileHistoryProvider;
import org.eclipse.team.ui.history.IHistoryPageSource;

/**
 * History Provider for Semantic Files
 * 
 */
public class SemanticFileHistoryProvider extends FileHistoryProvider implements IAdaptable {

	private final ISemanticFileSystem sfs;

	/**
	 * @param actSfs
	 *            the SFS instance
	 */
	public SemanticFileHistoryProvider(ISemanticFileSystem actSfs) {
		this.sfs = actSfs;
	}

	private final static IFileHistory EMPTY_HISTORY = new FileHistory() {

		public IFileRevision[] getTargets(IFileRevision revision) {
			return new IFileRevision[0];
		}

		public IFileRevision[] getFileRevisions() {
			return new IFileRevision[0];
		}

		public IFileRevision getFileRevision(String id) {
			return null;
		}

		public IFileRevision[] getContributors(IFileRevision revision) {
			return new IFileRevision[0];
		}
	};

	public IFileHistory getFileHistoryFor(IResource resource, int flags, IProgressMonitor monitor) {
		IFileStore store;
		try {
			store = EFS.getStore(resource.getLocationURI());
		} catch (CoreException e) {
			// $JL-EXC$ log and return empty history
			this.sfs.getLog().log(e);
			return SemanticFileHistoryProvider.EMPTY_HISTORY;
		}
		return getFileHistoryFor(store, flags, monitor);
	}

	public IFileHistory getFileHistoryFor(IFileStore store, int flags, IProgressMonitor monitor) {
		try {
			if (store instanceof ISemanticFileStore) {
				ISemanticFileStore sstore = (ISemanticFileStore) store;
				ISemanticContentProvider provider = sstore.getEffectiveContentProvider();
				ISemanticFileHistoryProvider hist = (ISemanticFileHistoryProvider) provider.getAdapter(ISemanticFileHistoryProvider.class);
				if (hist != null) {
					return hist.getHistoryFor(sstore, flags, monitor);
				}
			}
		} catch (CoreException e) {
			// $JL-EXC$ just log
			IFileSystem fs = store.getFileSystem();
			if (fs instanceof ISemanticFileSystem) {
				((ISemanticFileSystem) fs).getLog().log(e);
			}
		}
		return SemanticFileHistoryProvider.EMPTY_HISTORY;
	}

	public IFileRevision getWorkspaceFileRevision(IResource resource) {

		IFileStore store;
		try {
			store = EFS.getStore(resource.getLocationURI());
		} catch (CoreException e1) {
			// $JL-EXC$ log and return null
			this.sfs.getLog().log(e1);
			return null;
		}

		try {
			if (store instanceof ISemanticFileStore) {
				ISemanticFileStore sstore = (ISemanticFileStore) store;
				ISemanticContentProvider provider = sstore.getEffectiveContentProvider();
				ISemanticFileHistoryProvider hist = (ISemanticFileHistoryProvider) provider.getAdapter(ISemanticFileHistoryProvider.class);
				if (hist != null) {
					return hist.getWorkspaceFileRevision(sstore);
				}
			}
		} catch (CoreException e) {
			// $JL-EXC$ just log
			IFileSystem fs = store.getFileSystem();
			if (fs instanceof ISemanticFileSystem) {
				((ISemanticFileSystem) fs).getLog().log(e);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IHistoryPageSource.class)) {
			return new SemanticHistoryPageSource();
		}
		return null;
	}

}
