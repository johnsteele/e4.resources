/*******************************************************************************
 * Copyright (c) 2009 SAP AG. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Eduard Bartsch (SAP AG) - initial API and implementation
 * Mathias Kinzler (SAP AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources.semantic.examples;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Reusable code for {@link IHandler} implementations
 * 
 */
abstract class HandlerUtilities implements IHandler {

	public boolean isEnabled() {
		return true;
	}

	public boolean isHandled() {
		return true;
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {
		// ignore
	}

	public void addHandlerListener(IHandlerListener handlerListener) {
		// ignore
	}

	public void dispose() {
		// nothing to dispose
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected <T> T getSelectedObject(ExecutionEvent evt, Class T, boolean adapt) {

		IEvaluationContext ctx = (IEvaluationContext) evt.getApplicationContext();
		ISelection selection = (ISelection) ctx.getVariable("selection"); //$NON-NLS-1$
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}
		IStructuredSelection ssel = (IStructuredSelection) selection;
		if (ssel.size() != 1) {
			return null;
		}
		if (!(ssel.getFirstElement() instanceof IResource)) {
			return null;
		}
		IResource r = (IResource) ssel.getFirstElement();
		if (adapt) {
			Object adapted = r.getAdapter(T);
			try {
				return (T) adapted;
			} catch (ClassCastException e1) {
				// $JL-EXC$ ignore
				return null;
			}
		}
		try {
			return (T) r;
		} catch (ClassCastException e1) {
			// $JL-EXC$ ignore
			return null;
		}

	}

	protected void showPopup(final String title, final String message) {

		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				MessageDialog.openInformation(null, title, message);
			}
		});

	}

	protected void submit(Shell shell, IRunnableWithProgress runnable) throws InterruptedException, InvocationTargetException {
		new ProgressMonitorDialog(shell).run(true, true, runnable);
	}
}
