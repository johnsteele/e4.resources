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
package org.eclipse.core.resources.semantic.examples;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.resources.semantic.examples.remote.RemoteFolder;
import org.eclipse.core.resources.semantic.examples.remote.RemoteItem;
import org.eclipse.core.resources.semantic.examples.remote.RemoteItemSelectionDialog;
import org.eclipse.core.resources.semantic.examples.remote.RemoteStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

/**
 * Handles the "Add from remote" command
 * 
 */
public class HandleAddFromRemote extends HandlerUtilities {

	public Object execute(ExecutionEvent event) throws ExecutionException {

		ISemanticFolder sFolder = getSelectedObject(event, ISemanticFolder.class, true);
		if (sFolder == null) {
			showPopup(Messages.HandleAddFromRemote_Error_XGRP, Messages.HandleAddFromRemote_NotSemantic_XMSG);
			return null;
		}

		final IContainer folder = sFolder.getAdaptedContainer();

		try {
			while (sFolder != null && sFolder.getContentProviderID() == null) {
				IContainer parent = sFolder.getAdaptedContainer().getParent();
				if (parent == null) {
					break;
				}
				sFolder = (ISemanticFolder) parent.getAdapter(ISemanticFolder.class);
			}
		} catch (CoreException e) {
			throw new ExecutionException(e.getMessage(), e);
		}

		if (sFolder == null) {
			showPopup(Messages.HandleAddFromRemote_Info_XGRP, Messages.HandleAddFromRemote_NoRemote_XMSG);
			return null;
		}

		int templateFolderSegments = sFolder.getAdaptedContainer().getFullPath().segmentCount();

		RemoteStore store = (RemoteStore) folder.getProject().getAdapter(RemoteStore.class);
		if (store == null) {
			showPopup(Messages.HandleAddFromRemote_Info_XGRP, Messages.HandleAddFromRemote_NoRemote_XMSG);
			return null;
		}
		RemoteItem item = store.getItemByPath(folder.getFullPath().removeFirstSegments(templateFolderSegments));

		try {
			IResource[] members = folder.members();
			List<String> memberNames = new ArrayList<String>();
			for (IResource member : members) {
				memberNames.add(member.getName());
			}
			List<RemoteItem> children = ((RemoteFolder) item).getChildren();

			final List<RemoteItem> addable = new ArrayList<RemoteItem>();

			for (RemoteItem childItem : children) {
				if (!memberNames.contains(childItem.getName())) {
					addable.add(childItem);
				}
			}

			if (addable.isEmpty()) {
				showPopup(Messages.HandleAddFromRemote_Info_XGRP, Messages.HandleAddFromRemote_NoRemote_XMSG);
				return null;
			}

			final RemoteItemSelectionDialog dialog = new RemoteItemSelectionDialog(Display.getCurrent().getActiveShell(), addable
					.toArray(new RemoteItem[0]), true);
			int result = dialog.open();
			if (result != Window.OK) {
				return null;
			}

			try {
				submit(Display.getCurrent().getActiveShell(), new IRunnableWithProgress() {

					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

							public void run(IProgressMonitor actMonitor) throws CoreException {
								ISemanticFolder sfolder = (ISemanticFolder) folder.getAdapter(ISemanticFolder.class);
								for (RemoteItem addItem : dialog.getSelectedItems()) {
									sfolder.addResource(addItem.getName(), ISemanticFileSystem.NONE, actMonitor);
								}

							}
						};

						try {
							ResourcesPlugin.getWorkspace().run(runnable, monitor);
						} catch (CoreException e) {
							throw new InvocationTargetException(e);
						}

					}
				});
			} catch (InvocationTargetException e) {
				throw new ExecutionException(e.getMessage(), e);
			} catch (InterruptedException e) {
				// $JL-EXC$ ignore here
			}

		} catch (CoreException e) {
			throw new ExecutionException(e.getMessage(), e);
		}

		return null;
	}

}
