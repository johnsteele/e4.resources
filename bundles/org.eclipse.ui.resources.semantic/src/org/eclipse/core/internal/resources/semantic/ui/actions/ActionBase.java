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
import java.util.Iterator;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.internal.resources.semantic.ui.SemanticResourcesUIPlugin;
import org.eclipse.core.internal.resources.semantic.util.ISemanticFileSystemLog;
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.resources.semantic.ISemanticResourceInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

abstract class ActionBase implements IObjectActionDelegate {

	private IStructuredSelection mySelection;
	private IWorkbenchPart myActivePart;
	private ISemanticFileSystemLog myLog;

	protected final int options = ISemanticFileSystem.NONE;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.myActivePart = targetPart;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.mySelection = (IStructuredSelection) selection;
		}
	}

	protected Shell getShell() {
		return this.myActivePart.getSite().getShell();
	}

	protected IStructuredSelection getSelection() {
		return this.mySelection;
	}

	@SuppressWarnings({"rawtypes"})
	protected boolean checkSelectionNonLocalOnly() {

		boolean shouldEnable = true;

		// we check for all object that they are semantic resources
		// we also check for at least one non-local resources

		for (Iterator it = getSelection().iterator(); it.hasNext();) {

			Object nextObject = it.next();

			if (!(nextObject instanceof ISemanticResource)) {
				shouldEnable = false;
				break;
			}

			if (nextObject instanceof ISemanticFile) {
				// ignore the folder state
				try {
					ISemanticResourceInfo attrs = ((ISemanticResource) nextObject).fetchResourceInfo(
							ISemanticFileSystem.RESOURCE_INFO_LOCAL_ONLY, null);
					if (attrs.isLocalOnly()) {
						shouldEnable = false;
						break;
					}

				} catch (CoreException e) {
					// $JL-EXC$ ignore here
					log(e);
					// as fall back, enable here
					continue;
				}
			}
		}

		return shouldEnable;
	}

	@SuppressWarnings({"rawtypes"})
	protected boolean checkSelectionSemanticResource() {

		boolean shouldEnable = true;

		// we check for all object that they are semantic resources

		for (Iterator it = getSelection().iterator(); it.hasNext();) {

			Object nextObject = it.next();

			if (!(nextObject instanceof ISemanticResource)) {
				shouldEnable = false;
				break;
			}
		}

		return shouldEnable;
	}

	@SuppressWarnings({"rawtypes"})
	protected boolean checkFilesWithReadOnlyFlagOnly(boolean readOnly) {

		boolean shouldEnable = true;

		// we check for all object that they are read-only semantic resources

		for (Iterator it = getSelection().iterator(); it.hasNext();) {

			Object nextObject = it.next();

			if (!(nextObject instanceof ISemanticFile)) {
				shouldEnable = false;
				break;
			}

			try {
				ISemanticResourceInfo attrs = ((ISemanticResource) nextObject).fetchResourceInfo(
						ISemanticFileSystem.RESOURCE_INFO_READ_ONLY, null);

				if (readOnly != attrs.isReadOnly()) {
					shouldEnable = false;
					break;
				}

			} catch (CoreException e) {
				// $JL-EXC$ ignore here
				log(e);
				// as fall back, enable here
				continue;
			}
		}

		return shouldEnable;
	}

	@SuppressWarnings({"rawtypes"})
	protected boolean checkSelectionLockingSupportedOnly() {

		boolean shouldEnable = true;

		// we check for all object that they are semantic resources and support
		// locking
		// we don't check the lock state intentionally
		for (Iterator it = getSelection().iterator(); it.hasNext();) {

			Object nextObject = it.next();

			if (!(nextObject instanceof ISemanticResource)) {
				shouldEnable = false;
				break;
			}

			try {
				ISemanticResourceInfo attrs = ((ISemanticResource) nextObject).fetchResourceInfo(
						ISemanticFileSystem.RESOURCE_INFO_LOCKING_SUPPORTED, null);

				if (!attrs.isLockingSupported()) {
					shouldEnable = false;
					break;
				}

			} catch (CoreException e) {
				// $JL-EXC$ ignore here
				log(e);
				// as fall back, enable locking here
				continue;
			}
		}

		return shouldEnable;
	}

	protected void run(IRunnableWithProgress runnable) {
		IProgressService srv = (IProgressService) PlatformUI.getWorkbench().getService(IProgressService.class);

		try {
			srv.busyCursorWhile(runnable);
		} catch (InvocationTargetException e) {
			// we don't try to unpack a status from a causing CoreException,
			// since the status code might
			// be CANCEL, which would not be shown in the error dialog below
			IStatus error = new Status(IStatus.ERROR, SemanticResourcesUIPlugin.PLUGIN_ID, e.getCause().getMessage(), e.getCause());
			ErrorDialog.openError(getShell(), Messages.RemoveAction_Remove_XGRP, null, error);
		} catch (InterruptedException e) {
			// $JL-EXC$ ignore here
			MessageDialog.openInformation(getShell(), Messages.ActionBase_ActionCancelded_XGRP, Messages.ActionBase_ActionCanceled_XMSG);
		}
	}

	private synchronized void log(CoreException ex) {

		if (this.myLog == null) {
			try {
				ISemanticFileSystem sfs = (ISemanticFileSystem) EFS.getFileSystem(ISemanticFileSystem.SCHEME);
				this.myLog = sfs.getLog();
				this.myLog.log(ex);
			} catch (CoreException e) {
				// $JL-EXC$
				// TODO 0.1: log and fallback
			}
		} else {
			this.myLog.log(ex);
		}

	}

}
