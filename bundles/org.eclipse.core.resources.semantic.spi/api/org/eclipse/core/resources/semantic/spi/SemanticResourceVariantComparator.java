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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;

/**
 * Resource variant comparator for the Semantic File System
 * <p>
 * Uses timestamp information for comparing two versions
 * 
 */
public class SemanticResourceVariantComparator implements IResourceVariantComparator {

	private final boolean threeWay;

	/**
	 * @param threeWay
	 *            if three-way merge is supported
	 */
	public SemanticResourceVariantComparator(boolean threeWay) {
		this.threeWay = threeWay;
	}

	public boolean compare(IResource local, IResourceVariant remote) {

		if (!(local instanceof IFile)) {
			return true;
		}

		try {
			String filestamp = Long.toString(((IFile) local).getLocalTimeStamp());
			String remoteStamp = new String(remote.asBytes(), "UTF-8"); //$NON-NLS-1$
			return remoteStamp.equals(filestamp);
		} catch (UnsupportedEncodingException e) {
			// $JL-EXC$ should really not happen
			return false;
		}

	}

	public boolean compare(IResourceVariant base, IResourceVariant remote) {
		try {
			String baseStamp = new String(base.asBytes(), "UTF-8"); //$NON-NLS-1$
			String remoteStamp = new String(base.asBytes(), "UTF-8"); //$NON-NLS-1$
			return baseStamp.equals(remoteStamp);
		} catch (UnsupportedEncodingException e) {
			// $JL-EXC$ should really not happen
			return false;
		}
	}

	public boolean isThreeWay() {
		return this.threeWay;
	}

}
