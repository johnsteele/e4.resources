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
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

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

			return store.validateEdit(shell);
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
}
