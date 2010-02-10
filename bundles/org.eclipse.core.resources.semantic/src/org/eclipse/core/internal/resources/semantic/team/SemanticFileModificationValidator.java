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

import org.eclipse.core.internal.resources.semantic.SemanticResourcesPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.resources.team.FileModificationValidationContext;
import org.eclipse.core.resources.team.FileModificationValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

/**
 * File Modification Validator for Semantic Files
 * 
 */
public class SemanticFileModificationValidator extends FileModificationValidator {

	public IStatus validateSave(IFile file) {

		ISemanticFile sFile = (ISemanticFile) file.getAdapter(ISemanticFile.class);

		if (sFile == null) {
			return new Status(IStatus.CANCEL, SemanticResourcesPlugin.PLUGIN_ID, Messages.SemanticFileModificationValidator_CanNotAdaptFile_XMSG);
		}

		return sFile.validateSave();

	}

	public IStatus validateEdit(IFile[] files, FileModificationValidationContext context) {
		MultiStatus finalState = new MultiStatus(SemanticResourcesPlugin.PLUGIN_ID, IStatus.OK, Messages.SemanticFileModificationValidator_ValidateEdit_XGRP,
				null);

		for (IFile iFile : files) {
			ISemanticFile sFile = (ISemanticFile) iFile.getAdapter(ISemanticFile.class);

			if (sFile != null) {
				finalState.add(sFile.validateEdit(context == null ? null : context.getShell()));
			} else {
				finalState.add(new Status(IStatus.CANCEL, SemanticResourcesPlugin.PLUGIN_ID, null));
			}
		}

		return finalState;
	}

}
