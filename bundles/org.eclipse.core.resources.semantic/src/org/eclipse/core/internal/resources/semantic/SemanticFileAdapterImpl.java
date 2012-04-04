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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;

/**
 * The {@link ISemanticFile} implementation.
 * 
 */
public class SemanticFileAdapterImpl extends SemanticResourceAdapterImpl implements ISemanticFile {

	private final IFile file;

	SemanticFileAdapterImpl(IFile file, ISemanticFileSystem fileSystem) {
		super(file, fileSystem);
		this.file = file;
	}

	public IFile getAdaptedFile() {
		return this.file;
	}

	public IStatus validateEdit(Object shell) {
		try {
			checkCurrentRule(RuleType.VALIDATE_EDIT);
			ISemanticFileStoreInternal store = getOwnStore();

			IStatus result = store.validateEdit(shell);

			if (result.isOK()) {
				refreshLocalIfNeeded(RuleType.MODIFY, getRuleForType(RuleType.MODIFY, this.file), 0, null);
			}

			return result;
		} catch (CoreException e) {
			if (SfsTraceLocation.TEAM.isActive()) {
				SfsTraceLocation.getTrace().trace(SfsTraceLocation.TEAM.getLocation(), e.getMessage(), e);
			}
			return e.getStatus();
		}

	}

	public IStatus validateSave() {
		try {

			ISemanticFileStoreInternal store = getOwnStore();

			return store.validateSave();
		} catch (CoreException e) {
			if (SfsTraceLocation.TEAM.isActive()) {
				SfsTraceLocation.getTrace().trace(SfsTraceLocation.TEAM.getLocation(), e.getMessage(), e);
			}
			return e.getStatus();
		}

	}

	public void revertChanges(int options, IProgressMonitor monitor) throws CoreException {
		checkCurrentRule(RuleType.REFRESH);

		ISemanticFileStoreInternal store = getOwnStore();

		store.revertChanges(monitor);

		refreshLocalIfNeeded(RuleType.MODIFY, getRuleForType(RuleType.MODIFY, this.file), options, monitor);

	}

	/**
	 * @param targetFolder
	 * @param targetName
	 * @param monitor
	 *            a progress monitor, or <code>null</code> if progress reporting
	 *            is not desired
	 * @return true if resource move is supported
	 * @throws CoreException
	 * @since 0.6.0
	 */
	public boolean supportsMoveInternal(ISemanticFolder targetFolder, String targetName, IProgressMonitor monitor) throws CoreException {
		ISemanticFileStoreInternal store = getOwnStore();
		ISemanticFileStore targetParent = (ISemanticFileStore) getStoreForResource(targetFolder.getAdaptedResource());

		return store.supportsMove(targetParent, targetName, monitor);
	}

	/**
	 * Moves the current file to the target folder using the specified name
	 * 
	 * @param targetFolder
	 *            must exist
	 * @param targetName
	 * @param options
	 * @param monitor
	 *            a progress monitor, or <code>null</code> if progress reporting
	 *            is not desired
	 * @throws CoreException
	 *             in case of operation failure, e.g. the target file already
	 *             exists, content can not be copied.
	 * 
	 * @see IFile#move(org.eclipse.core.runtime.IPath, int, IProgressMonitor)
	 * 
	 * @since 0.6.0
	 */
	public void moveToInternal(ISemanticFolder targetFolder, String targetName, int options, IProgressMonitor monitor) throws CoreException {
		IResource destination = targetFolder.getAdaptedContainer().getFile(new Path(targetName));

		checkCurrentMoveRule(destination);

		ISemanticFileStoreInternal store = getOwnStore();

		ISemanticFileStore targetParent = (ISemanticFileStore) getStoreForResource(targetFolder.getAdaptedResource());

		store.moveTo(targetParent, targetName, monitor);
	}
}
