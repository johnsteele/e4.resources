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
package org.eclipse.core.internal.resources.semantic.ui.actions;

import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * Show Remote History
 * 
 */
public class HistoryAction extends ActionBase {

	public void selectionChanged(IAction action, ISelection selection) {

		super.selectionChanged(action, selection);
		// only single non-local object can show history
		action.setEnabled(getSelection().size() == 1 && checkSelectionNonLocalOnly());
	}

	public void run(IAction action) {

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		TeamUI.showHistoryFor(page, ((ISemanticFile) getSelection().getFirstElement()).getAdaptedFile(), null);

	}
}
