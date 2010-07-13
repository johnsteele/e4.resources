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
package org.eclipse.core.internal.resources.semantic.ui.actions;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.internal.resources.semantic.ui.NavigateToContentViewHandler;
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Show Remote History
 * 
 */
public class ShowSFSBrowserViewAction extends ActionBase {

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

		super.selectionChanged(action, selection);

		action.setEnabled(getSelection().size() == 1 && checkSelectionSemanticResource());
	}

	public void run(IAction action) {

		ISemanticResource res = (ISemanticResource) getSelection().getFirstElement();

		ICommandService srv = (ICommandService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ICommandService.class);
		Command cmd = srv.getCommand(NavigateToContentViewHandler.NAV_COMMAND_ID);
		IHandlerService hsrv = (IHandlerService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IHandlerService.class);
		try {
			IParameter parameter = cmd.getParameter(NavigateToContentViewHandler.NAV_PATH_PARAMETER_ID);
			Parameterization parm = new Parameterization(parameter, res.getAdaptedResource().getFullPath().toString());
			ParameterizedCommand pcmd = new ParameterizedCommand(cmd, new Parameterization[] {parm});
			cmd.executeWithChecks(hsrv.createExecutionEvent(pcmd, null));
		} catch (Exception e) {
			MessageDialog.openError(getShell(), Messages.ShowSFSBrowserViewAction_CouldNotOpenView_XMSG, e.getMessage());
		}

	}
}
