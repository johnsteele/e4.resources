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
package org.eclipse.core.internal.resources.semantic.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * The activator class controls the plug-in life cycle
 */
public class SemanticResourcesUIPlugin extends Plugin {

	/** The plug-in ID */
	public static final String PLUGIN_ID = "org.eclipse.ui.resources.semantic"; //$NON-NLS-1$

	/**
	 * Handle an error. The error is logged. If <code>show</code> is
	 * <code>true</code> the error is shown to the user.
	 * 
	 * @param message
	 *            a localized message
	 * @param throwable
	 * @param show
	 */
	public static void handleError(String message, Throwable throwable, boolean show) {
		if (throwable instanceof CoreException) {
			IStatus causeStatus = ((CoreException) throwable).getStatus();
			IStatus statusToShow;
			if (causeStatus.isMultiStatus()) {
				statusToShow = causeStatus;
			} else {
				MultiStatus status = new MultiStatus(PLUGIN_ID, 0, message, throwable);
				status.add(((CoreException) throwable).getStatus());
				statusToShow = status;
			}
			int style = StatusManager.LOG;
			if (show)
				style |= StatusManager.SHOW;
			StatusManager.getManager().handle(statusToShow, style);
		} else {
			IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message, throwable);
			int style = StatusManager.LOG;
			if (show)
				style |= StatusManager.SHOW;
			StatusManager.getManager().handle(status, style);
		}
	}

	public static void handleError(IStatus status, boolean show) {
		int style = StatusManager.LOG;
		if (show)
			style |= StatusManager.SHOW;
		StatusManager.getManager().handle(status, style);
	}

}
