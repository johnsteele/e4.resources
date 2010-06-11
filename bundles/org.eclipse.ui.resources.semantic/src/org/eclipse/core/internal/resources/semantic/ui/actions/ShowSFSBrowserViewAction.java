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

import org.eclipse.core.internal.resources.semantic.ui.SFSNavigator;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

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

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		// ((ISemanticFile) getSelection().getFirstElement()).getAdaptedFile();

		try {
			SFSNavigator view = (SFSNavigator) page.findView(SFSNavigator.VIEW_ID);
			if (view == null) {
				page.showView(SFSNavigator.VIEW_ID);
				view = (SFSNavigator) page.findView(SFSNavigator.VIEW_ID);
			}

			page.activate(view);

			// TODO locate the selected file store in viewer
		} catch (PartInitException e) {
			// Ignore
		}
	}
}
