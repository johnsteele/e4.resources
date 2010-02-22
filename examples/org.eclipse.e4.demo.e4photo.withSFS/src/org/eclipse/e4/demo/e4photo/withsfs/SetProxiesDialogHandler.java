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
package org.eclipse.e4.demo.e4photo.withsfs;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

public class SetProxiesDialogHandler {

	private final static class SetProxiesWizard extends Wizard {
		/**
		 * 
		 * @param folder
		 *            the folder
		 */
		SetProxiesWizard(IEclipseContext context) {
		}

		public void addPages() {
			addPage(new SetProxiesPage());
		}

		public boolean performFinish() {
			return true;
		}
	}

	public void execute(Shell shell, IEclipseContext context) {
		SetProxiesWizard wizard = new SetProxiesWizard(context);

		new WizardDialog(shell, wizard).open();
	}

}
