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
package org.eclipse.core.internal.resources.semantic;

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.resources.semantic.ISemanticProject;
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * Adapts {@link IResource}s to {@link ISemanticResource}s
 * 
 */
public class SemanticFileSystemAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("unchecked")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof IResource) {
			if (isSemanticStoreScheme((IResource) adaptableObject)) {
				ISemanticFileSystem sfs;
				try {
					sfs = (ISemanticFileSystem) EFS.getFileSystem(ISemanticFileSystem.SCHEME);
				} catch (CoreException e) {
					// $JL-EXC$
					// TODO 0.1: dependency injection of ISemanticFileSystem and
					// error logging there
					return null;
				}
				if (adaptableObject instanceof IFile) {
					if (adapterType == ISemanticFile.class || adapterType == ISemanticResource.class) {
						return new SemanticFileAdapterImpl((IFile) adaptableObject, sfs);
					}
				} else if (adaptableObject instanceof IFolder) {
					if (adapterType == ISemanticFolder.class || adapterType == ISemanticResource.class) {
						return new SemanticFolderAdapterImpl((IContainer) adaptableObject, sfs);
					}
				} else if (adaptableObject instanceof IProject) {
					if (adapterType == ISemanticProject.class || adapterType == ISemanticFolder.class
							|| adapterType == ISemanticResource.class) {
						return new SemanticProjectAdapterImpl((IProject) adaptableObject, sfs);
					}
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Class[] getAdapterList() {
		return new Class[] { ISemanticFolder.class, ISemanticFile.class, ISemanticResource.class, ISemanticProject.class };
	}

	private boolean isSemanticStoreScheme(IResource resource) {
		URI uri = resource.getLocationURI();

		return uri != null && ISemanticFileSystem.SCHEME.equals(uri.getScheme());

	}

}
