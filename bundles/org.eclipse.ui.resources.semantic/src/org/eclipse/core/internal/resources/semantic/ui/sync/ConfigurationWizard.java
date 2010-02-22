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
package org.eclipse.core.internal.resources.semantic.ui.sync;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.ui.IConfigurationWizard;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeManager;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizeView;
import org.eclipse.ui.IWorkbench;

/**
 * Configures the team synchronization support
 */
public class ConfigurationWizard extends Wizard implements IConfigurationWizard {

	private ISemanticFileSystem mySfs;

	@Override
	public void addPages() {
		try {
			// TODO 0.1: dependency injection
			this.mySfs = (ISemanticFileSystem) EFS.getFileSystem(ISemanticFileSystem.SCHEME);
		} catch (CoreException e) {
			// $JL-EXC$ ignore here and deal with it in the performFinish
			this.mySfs = null;
		}
		addPage(new ConfigurationPage(this.mySfs));
	}

	@Override
	public boolean performFinish() {

		if (this.mySfs == null) {
			return false;
		}

		ConfigurationPage page = (ConfigurationPage) getPage(ConfigurationPage.NAME);

		ISynchronizeParticipant participant = new SemanticSubscriberParticipant(this.mySfs, page.isThreeWaySelected);
		ISynchronizeManager manager = TeamUI.getSynchronizeManager();
		manager.addSynchronizeParticipants(new ISynchronizeParticipant[] { participant });
		ISynchronizeView view = manager.showSynchronizeViewInActivePage();
		view.display(participant);
		return true;
	}

	public void init(IWorkbench workbench, IProject project) {

		// this is not being called, apparently
	}

}
