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

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import org.eclipse.core.resources.semantic.ISemanticProperties;
import org.eclipse.core.resources.semantic.ISemanticResourceInfo;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;

interface ISemanticFileStoreInternal extends ISemanticProperties {

	//
	// ISemanticFolder methods
	//

	public ISemanticFileStoreInternal addResourceFromRemote(String name, IProgressMonitor monitor) throws CoreException;

	public ISemanticFileStoreInternal createResourceRemotely(String name, Object context, IProgressMonitor monitor) throws CoreException;

	public void addFileFromRemote(String name, IProgressMonitor monitor) throws CoreException;

	public void addFolderFromRemote(String name, IProgressMonitor monitor) throws CoreException;

	public void addFileFromRemoteByURI(String name, URI uri, IProgressMonitor monitor) throws CoreException;

	public void addFolderFromRemoteByURI(String name, URI uri, IProgressMonitor monitor) throws CoreException;

	public void addResource(String name, boolean asFolder, String contentProviderID, Map<QualifiedName, String> properties,
			IProgressMonitor monitor) throws CoreException;

	public void createFileRemotely(String name, InputStream source, Object context, IProgressMonitor monitor) throws CoreException;

	public boolean hasResource(String name);

	public ISemanticFileStoreInternal getChildResource(String name);

	public IStatus validateRemoteCreate(String name, Object shell);

	public String getContentProviderID();

	public IPath[] findURI(URI uri, IProgressMonitor monitor) throws CoreException;

	//
	// ISemanticFile methods
	//

	public IStatus validateEdit(Object shell);

	public IStatus validateSave();

	//
	// ISemanticResource methods
	//

	public IStatus validateRemoteDelete(Object shell);

	public IStatus validateRemove(int option, IProgressMonitor monitor);

	public void deleteRemotely(IProgressMonitor monitor) throws CoreException;

	public void removeFromWorkspace(IProgressMonitor monitor) throws CoreException;

	public String getRemoteURIString() throws CoreException;

	public void synchronizeContentWithRemote(SyncDirection direction, IProgressMonitor monitor) throws CoreException;

	public void revertChanges(IProgressMonitor monitor) throws CoreException;

	public ISemanticResourceInfo fetchResourceInfo(int options, IProgressMonitor monitor) throws CoreException;

	public IStatus lockResource(IProgressMonitor monitor) throws CoreException;

	public IStatus unlockResource(IProgressMonitor monitor) throws CoreException;

	public int getType();

	public void setRemoteURI(URI uri, IProgressMonitor monitor) throws CoreException;

}
