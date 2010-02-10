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
import org.eclipse.core.internal.resources.semantic.ui.SemanticResourcesUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.ui.IConfigurationWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Maps a project to the Semantic Repository Provider
 * 
 */
public class RepositoryConfigurationWizard extends Wizard implements IConfigurationWizard {

	private IProject project;
	private ISemanticFileSystem mySfs;

	public boolean performFinish() {
		if (this.mySfs == null) {
			IStatus error = new Status(IStatus.ERROR, SemanticResourcesUIPlugin.PLUGIN_ID,
					Messages.RepositoryConfigurationWizard_SFSNotInitialized_XMSG);
			ErrorDialog.openError(getShell(), Messages.RepositoryConfigurationWizard_SFSError_XGRP, null, error);
			return false;
		}
		try {
			RepositoryProvider.map(this.project, ISemanticFileSystem.SFS_REPOSITORY_PROVIDER);
			return true;
		} catch (TeamException e) {
			this.mySfs.getLog().log(e);
			return false;
		}
	}

	public void init(IWorkbench workbench, IProject actProject) {
		this.project = actProject;
		try {
			this.mySfs = (ISemanticFileSystem) EFS.getFileSystem(ISemanticFileSystem.SCHEME);
		} catch (CoreException e) {
			// $JL-EXC$ ignore here; deal with it in the performFinish method
			// TODO 0.1: dependency injection
		}
	}

}
