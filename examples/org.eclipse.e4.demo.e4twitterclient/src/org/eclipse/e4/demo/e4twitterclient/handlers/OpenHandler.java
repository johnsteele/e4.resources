/*******************************************************************************
 * Copyright (c) 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eduard Bartsch (SAP AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.e4twitterclient.handlers;

import javax.inject.Named;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.demo.e4twitterclient.parts.MyPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

@SuppressWarnings("restriction")
public class OpenHandler {

	IProject project;

	@Execute
	public void execute(final IWorkspace workspace, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell, EPartService partService) {
		InputDialog dialog = new InputDialog(shell, "Select Twitter User", "Enter Twitter ID", "", null);

		dialog.open();
		if (dialog.getReturnCode() != Window.OK)
			return;
		final String pattern = dialog.getValue();
		if (pattern.equals(""))
			return;

		MPart part = partService.findPart(MyPart.TWITTERS_PART_ID);

		if (part != null) {
			MyPart myPart = (MyPart) part.getObject();

			myPart.selectUser(pattern);
		}

	}
}
