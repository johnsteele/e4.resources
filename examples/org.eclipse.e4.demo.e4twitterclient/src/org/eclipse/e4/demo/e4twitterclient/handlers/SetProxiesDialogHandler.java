/*******************************************************************************
 * Copyright (c) 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eduard Bartsch (SAP AG) - initial API and implementation
 *    Mathias Kinzler (SAP AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.e4twitterclient.handlers;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

@SuppressWarnings("restriction")
public class SetProxiesDialogHandler {

	private final static class SetProxiesWizard extends Wizard {

		SetProxiesWizard(IEclipseContext context) {
			// nothing to do
		}

		@Override
		public void addPages() {
			addPage(new SetProxiesPage());
		}

		@Override
		public boolean performFinish() {
			return true;
		}
	}

	@Execute
	public void execute(Shell shell, IEclipseContext context) {
		SetProxiesWizard wizard = new SetProxiesWizard(context);

		new WizardDialog(shell, wizard).open();
	}

}
