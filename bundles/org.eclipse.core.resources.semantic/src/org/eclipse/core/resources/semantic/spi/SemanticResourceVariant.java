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
package org.eclipse.core.resources.semantic.spi;

import java.io.UnsupportedEncodingException;

import org.eclipse.core.internal.resources.semantic.SemanticResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.variants.CachedResourceVariant;

/**
 * A {@link CachedResourceVariant} implemenation for team synchronization
 * support
 * 
 */
public class SemanticResourceVariant extends CachedResourceVariant {

	private final ISemanticFileStore myStore;
	private IFileRevision myRevision;

	/**
	 * @param revision
	 *            the wrapped file revision
	 * @param store
	 *            the semantic file store
	 */
	public SemanticResourceVariant(IFileRevision revision, ISemanticFileStore store) {
		this.myRevision = revision;
		this.myStore = store;
	}

	@Override
	protected void fetchContents(IProgressMonitor monitor) throws TeamException {
		try {
			setContents(this.myRevision.getStorage(monitor).getContents(), monitor);
		} catch (CoreException e) {
			TeamException ex = new TeamException(e.getStatus());
			throw ex;
		}

	}

	@Override
	protected String getCacheId() {
		return SemanticResourcesPlugin.PLUGIN_ID;
	}

	@Override
	protected String getCachePath() {
		return this.myStore.getPath().append(this.myRevision.getContentIdentifier()).toString();
	}

	public byte[] asBytes() {
		try {
			return Long.toString(this.myRevision.getTimestamp()).getBytes("UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			// $JL-EXC$ should really not happen
			return null;
		}
	}

	public String getContentIdentifier() {
		return this.myRevision.getContentIdentifier();
	}

	public String getName() {
		return this.myStore.getName();
	}

	public boolean isContainer() {
		return this.myStore.getType() != ISemanticFileStore.FILE;
	}

}
