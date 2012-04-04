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
package org.eclipse.core.internal.resources.semantic.ui.team;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.team.ui.history.HistoryPageSource;
import org.eclipse.team.ui.history.IHistoryPageSource;
import org.eclipse.ui.part.Page;

/**
 * The "Team History Page" provider for Semantic Resources
 * 
 */
public class SemanticHistoryPageSource extends HistoryPageSource {

	public boolean canShowHistoryFor(Object object) {
		if (object instanceof IResource) {
			IResource resource = (IResource) object;
			try {
				IFileStore store = EFS.getStore(resource.getLocationURI());
				if (store instanceof ISemanticFileStore) {
					ISemanticFileStore sstore = (ISemanticFileStore) store;
					ISemanticContentProvider provider = sstore.getEffectiveContentProvider();
					IHistoryPageSource hps = (IHistoryPageSource) provider.getAdapter(IHistoryPageSource.class);
					if (hps != null) {
						return hps.canShowHistoryFor(object);
					}
					hps = (IHistoryPageSource) Platform.getAdapterManager().getAdapter(provider, IHistoryPageSource.class);
					if (hps != null) {
						return hps.canShowHistoryFor(object);
					}
				}
			} catch (CoreException e) {
				// $JL-EXC$ ignore and fallback to default behavior
			}
			ISemanticResource res = (ISemanticResource) ((IResource) object).getAdapter(ISemanticResource.class);

			return res != null;
		}
		return false;
	}

	public Page createPage(Object object) {
		IResource resource = (IResource) object;
		try {
			IFileStore store = EFS.getStore(resource.getLocationURI());
			if (store instanceof ISemanticFileStore) {
				ISemanticFileStore sstore = (ISemanticFileStore) store;
				ISemanticContentProvider provider = sstore.getEffectiveContentProvider();
				IHistoryPageSource hps = (IHistoryPageSource) provider.getAdapter(IHistoryPageSource.class);
				if (hps != null) {
					return hps.createPage(object);
				}
				hps = (IHistoryPageSource) Platform.getAdapterManager().getAdapter(provider, IHistoryPageSource.class);
				if (hps != null) {
					return hps.createPage(object);
				}
			}
		} catch (CoreException e) {
			// $JL-EXC$ ignore and provide the standard page
		}
		return new SemanticHistoryPage(resource);
	}

}
