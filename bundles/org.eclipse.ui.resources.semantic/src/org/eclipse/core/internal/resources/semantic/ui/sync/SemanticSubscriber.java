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
package org.eclipse.core.internal.resources.semantic.ui.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.spi.ISemanticFileHistoryProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.SemanticResourceVariant;
import org.eclipse.core.resources.semantic.spi.SemanticResourceVariantComparator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.subscribers.ISubscriberChangeEvent;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.subscribers.SubscriberChangeEvent;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;

/**
 * The Semantic File System Subscriber
 * 
 */
public class SemanticSubscriber extends Subscriber {

	final Map<IPath, SyncInfo> outOfSync = new HashMap<IPath, SyncInfo>();
	private final boolean threeWay;

	/**
	 * @param threeWay
	 *            <code>true</code> if three-way is supported
	 */
	public SemanticSubscriber(boolean threeWay) {
		this.threeWay = threeWay;
	}

	@Override
	public String getName() {
		return Messages.SemanticSubscriber_SfsSubsriberName_XGRP;
	}

	@Override
	public IResourceVariantComparator getResourceComparator() {
		return new SemanticResourceVariantComparator(this.threeWay);
	}

	@Override
	public synchronized SyncInfo getSyncInfo(IResource resource) throws TeamException {

		SyncInfo syncinfo;
		try {
			IFileStore store = EFS.getStore(resource.getLocationURI());
			if (store instanceof ISemanticFileStore) {
				ISemanticFileStore sfs = (ISemanticFileStore) store;

				ISemanticFileHistoryProvider fhp = (ISemanticFileHistoryProvider) sfs.getEffectiveContentProvider().getAdapter(
						ISemanticFileHistoryProvider.class);
				if (fhp != null) {
					IFileRevision[] revs = fhp.getResourceVariants(sfs, null);
					if (revs != null) {
						IResourceVariant[] var = toVariants(revs, sfs);
						syncinfo = new SyncInfo(resource, var[0], var[1], getResourceComparator());

						syncinfo.init();

						if (!SyncInfo.isInSync(syncinfo.getKind())) {
							this.outOfSync.put(resource.getFullPath(), syncinfo);
						} else {
							this.outOfSync.remove(resource.getFullPath());
						}

						return syncinfo;
					}
				}
			}
		} catch (CoreException e) {
			TeamException ex = new TeamException(e.getStatus());
			throw ex;
		}
		return null;
	}

	/**
	 * Utility method to convert {@link IFileRevision}s to
	 * {@link IResourceVariant}s
	 * 
	 * @param revs
	 *            file revisions
	 * @param store
	 *            the semantic file store
	 * @return the resource variants
	 */
	public static IResourceVariant[] toVariants(IFileRevision[] revs, ISemanticFileStore store) {

		IResourceVariant[] result = new IResourceVariant[revs.length];
		for (int i = 0; i < revs.length; i++) {
			IFileRevision rev = revs[i];
			if (rev != null) {
				result[i] = new SemanticResourceVariant(rev, store);
			}
		}
		return result;
	}

	@Override
	public boolean isSupervised(IResource resource) {
		return true;
	}

	@Override
	public IResource[] members(IResource resource) throws TeamException {
		if (resource instanceof IContainer) {
			try {
				return ((IContainer) resource).members();
			} catch (CoreException e) {
				throw new TeamException(e.getMessage(), e);
			}
		}
		return new IResource[0];
	}

	@Override
	public synchronized void refresh(IResource[] resources, int depth, final IProgressMonitor monitor) throws TeamException {

		final List<ISubscriberChangeEvent> events = new ArrayList<ISubscriberChangeEvent>();

		for (IResource res : resources) {
			try {
				res.accept(new IResourceVisitor() {

					public boolean visit(IResource resource) {

						SyncInfo info = SemanticSubscriber.this.outOfSync.get(resource.getFullPath());
						if (info != null) {
							// TODO 0.1: optimize refresh on team sync
							// we could be a bit more smart here provided we can
							// get the information
							// whether the remote resource or resources are
							// still the "same" as the info.getRemote()
							// and info.getBase() (the latter in case of
							// three-way compare)
							events.add(new SubscriberChangeEvent(SemanticSubscriber.this, 1, resource));
						}
						return true;
					}
				});
			} catch (CoreException e) {
				// $JL-EXC$
				throw new TeamException(e.getStatus());
			}
		}

		fireTeamResourceChange(events.toArray(new ISubscriberChangeEvent[0]));

	}

	@Override
	public IResource[] roots() {
		ArrayList<IResource> rootList = new ArrayList<IResource>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

		for (IProject iProject : projects) {
			RepositoryProvider provider = RepositoryProvider.getProvider(iProject, ISemanticFileSystem.SFS_REPOSITORY_PROVIDER);

			if (provider != null) {
				rootList.add(iProject);
			}
		}
		return rootList.toArray(new IResource[] {});
	}

}
