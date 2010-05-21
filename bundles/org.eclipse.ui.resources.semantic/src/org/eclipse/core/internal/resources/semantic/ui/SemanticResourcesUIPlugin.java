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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class SemanticResourcesUIPlugin extends Plugin {

	/** The plug-in ID */
	public static final String PLUGIN_ID = "org.eclipse.ui.resources.semantic"; //$NON-NLS-1$
	/** The refresh icon to be obtained from the {@link ImageRegistry} */
	public static final String IMG_REFRESH = "refresh"; //$NON-NLS-1$

	private static SemanticResourcesUIPlugin INSTANCE;

	private ImageRegistry imageRegistry;

	public static SemanticResourcesUIPlugin getInstance() {
		return INSTANCE;
	}

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

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		INSTANCE = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		INSTANCE = null;

		if (imageRegistry != null)
			imageRegistry.dispose();
		imageRegistry = null;
	}

	public ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {

			if (Display.getCurrent() != null) {
				imageRegistry = new ImageRegistry(Display.getCurrent());
			}

			if (PlatformUI.isWorkbenchRunning()) {
				imageRegistry = new ImageRegistry(PlatformUI.getWorkbench().getDisplay());
			}
			imageRegistry.put(IMG_REFRESH, ImageDescriptor.createFromURL(getBundle().getEntry("icons/refresh.gif"))); //$NON-NLS-1$
		}
		return imageRegistry;
	}

}
