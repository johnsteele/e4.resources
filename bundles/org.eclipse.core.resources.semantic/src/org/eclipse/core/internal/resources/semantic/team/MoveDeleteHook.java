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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.resources.semantic.ISemanticProject;
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Intercepts move and delete operations on resources that are under team
 * control
 * 
 */
public class MoveDeleteHook implements IMoveDeleteHook {

	private final ISemanticFileSystem sfs;

	/**
	 * @param actSfs
	 *            the SFS
	 */
	public MoveDeleteHook(ISemanticFileSystem actSfs) {
		this.sfs = actSfs;
	}

	private boolean deleteResource(IResourceTree tree, IResource resource, int updateFlags, final IProgressMonitor monitor) {

		ISemanticResource sresource = (ISemanticResource) resource.getAdapter(ISemanticResource.class);
		if (sresource != null) {
			try {

				// we check if a content provider does not allow local deletion
				// if this is implemented properly, we shall only expect
				// deletions
				// of this resource and its children by the content provider;
				// this
				// avoids problems with the resource management updates later on
				// (notably,
				// alias deletion)
				if (!sresource.validateRemove(ISemanticFileSystem.VALIDATE_REMOVE_RAISE_ERROR_ON_SCOPE_EXTENSION, monitor).isOK()) {
					monitor.setCanceled(true);
					OperationCanceledException ex = new OperationCanceledException();
					throw ex;
				}

				sresource.remove(ISemanticFileSystem.SUPPRESS_REFRESH | ISemanticFileSystem.INTERNAL_DELETE_PROJECT, monitor);

				if (resource.getType() == IResource.FILE) {
					tree.deletedFile((IFile) resource);
				} else if (resource.getType() == IResource.FOLDER) {
					tree.deletedFolder((IFolder) resource);
				} else if (resource.getType() == IResource.PROJECT) {
					tree.deletedProject((IProject) resource);
				}

				return true;

			} catch (CoreException e) {
				this.sfs.getLog().log(e);
				monitor.setCanceled(true);
				OperationCanceledException ex = new OperationCanceledException();
				ex.initCause(e);
				throw ex;
			}
		}
		return false;
	}

	public boolean deleteFile(IResourceTree tree, IFile file, int updateFlags, IProgressMonitor monitor) {
		return deleteResource(tree, file, updateFlags, monitor);
	}

	public boolean deleteFolder(IResourceTree tree, IFolder folder, int updateFlags, IProgressMonitor monitor) {
		return deleteResource(tree, folder, updateFlags, monitor);
	}

	public boolean deleteProject(IResourceTree tree, IProject project, int updateFlags, IProgressMonitor monitor) {
		return deleteResource(tree, project, updateFlags, monitor);
	}

	public boolean moveFile(IResourceTree tree, IFile source, IFile destination, int updateFlags, IProgressMonitor monitor) {
		ISemanticFile sFile = (ISemanticFile) source.getAdapter(ISemanticFile.class);
		if (sFile == null) {
			return false;
		}
		boolean localOnly;

		try {
			localOnly = sFile.fetchResourceInfo(ISemanticFileSystem.RESOURCE_INFO_LOCAL_ONLY, monitor).isLocalOnly();
		} catch (CoreException e) {
			monitor.setCanceled(true);
			OperationCanceledException ex = new OperationCanceledException(e.getMessage());
			ex.initCause(e);
			throw ex;
		}

		if (!localOnly) {
			// TODO 0.1: no file move for the time being
			SemanticResourceException ex = new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, source
					.getFullPath(), Messages.MoveDeleteHook_MoveRenameNotSupported_XMSG);
			this.sfs.getLog().log(ex);
			// the message is not transported to the client anyway, so we leave
			// it empty
			throw new OperationCanceledException();
		}
		return false;
	}

	public boolean moveFolder(IResourceTree tree, IFolder source, IFolder destination, int updateFlags, IProgressMonitor monitor) {

		ISemanticFolder sFolder = (ISemanticFolder) source.getAdapter(ISemanticFolder.class);
		if (sFolder == null) {
			return false;
		}

		// TODO 0.1: no folder move for the time being
		SemanticResourceException ex = new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, source.getFullPath(),
				Messages.MoveDeleteHook_MoveRenameNotSupported_XMSG);
		this.sfs.getLog().log(ex);
		// the message is not transported to the client anyway, so we leave it
		// empty
		throw new OperationCanceledException();

	}

	public boolean moveProject(IResourceTree tree, IProject source, IProjectDescription description, int updateFlags,
			IProgressMonitor monitor) {
		// TODO 0.1: no project move for the time being
		if (source.getAdapter(ISemanticProject.class) != null) {
			SemanticResourceException ex = new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, source
					.getFullPath(), Messages.MoveDeleteHook_MoveRenameNotSupported_XMSG);
			this.sfs.getLog().log(ex);
			// the message is not transported to the client anyway, so we leave
			// it empty
			throw new OperationCanceledException();
		}
		return false;
	}

}
