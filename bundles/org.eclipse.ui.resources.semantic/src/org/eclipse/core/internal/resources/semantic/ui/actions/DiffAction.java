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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.resources.semantic.spi.ISemanticFileHistoryProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.SemanticResourceVariant;
import org.eclipse.core.resources.semantic.spi.SemanticResourceVariantComparator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.ui.synchronize.SyncInfoCompareInput;

/**
 * Compare with remote version
 * 
 */
public class DiffAction extends ActionBase {

	public void selectionChanged(IAction action, ISelection selection) {

		super.selectionChanged(action, selection);
		// only single non-local object can be used to show the difference view
		action.setEnabled(getSelection().size() == 1 && checkSelectionNonLocalOnly()
				&& getSelection().getFirstElement() instanceof ISemanticFile);

	}

	public void run(IAction action) {

		IRunnableWithProgress outerRunnable = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

				try {
					ISemanticFile file = (ISemanticFile) getSelection().getFirstElement();
					ISemanticFileStore sfs = (ISemanticFileStore) EFS.getStore(file.getAdaptedFile().getLocationURI());
					ISemanticFileHistoryProvider fhp = (ISemanticFileHistoryProvider) sfs.getEffectiveContentProvider().getAdapter(
							ISemanticFileHistoryProvider.class);

					if (fhp == null) {
						Display.getDefault().syncExec(new Runnable() {

							public void run() {
								MessageDialog.openInformation(getShell(), Messages.DiffAction_CompareAction_XGRP,
										Messages.DiffAction_NoHistory_XMSG);
							}

						});
						return;
					}

					IFileRevision[] revs = fhp.getResourceVariants(sfs, monitor);
					if (revs == null || revs[1] == null) {
						Display.getDefault().syncExec(new Runnable() {

							public void run() {
								MessageDialog.openInformation(getShell(), Messages.DiffAction_CompareAction_XGRP,
										Messages.DiffAction_NoHistory_XMSG);
							}

						});
						return;
					}
					IFileRevision hist = fhp.getWorkspaceFileRevision(sfs);

					IResourceVariant[] var = new IResourceVariant[] { new SemanticResourceVariant(hist, sfs),
							new SemanticResourceVariant(revs[1], sfs) };

					SyncInfo syncinfo = new SyncInfo(file.getAdaptedFile(), var[0], var[1], new SemanticResourceVariantComparator(
							var[0] != null));

					syncinfo.init();

					SyncInfoCompareInput input = new SyncInfoCompareInput("", syncinfo); //$NON-NLS-1$
					input.getCompareConfiguration().setLeftEditable(false);

					CompareUI.openCompareEditor(input);

				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}

			}

		};

		run(outerRunnable);

	}
}
