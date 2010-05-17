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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class SemanticResourcesPluginExamples extends Plugin {

	/** Plug-in ID */
	public static final String PLUGIN_ID = "org.eclipse.core.resources.semantic.examples"; //$NON-NLS-1$
	/** Example project nature */
	public static final String EXAPMLE_NATURE = "org.eclipse.core.resources.semantic.examples.nature"; //$NON-NLS-1$

	// The shared instance
	private static SemanticResourcesPluginExamples plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		if (PlatformUI.isWorkbenchRunning()) {
			IMenuService service = (IMenuService) PlatformUI.getWorkbench().getService(IMenuService.class);
			service.addContributionFactory(new ExamplesContribution());
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static SemanticResourcesPluginExamples getDefault() {
		return plugin;
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
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message, throwable);
		int style = StatusManager.LOG;
		if (show)
			style |= StatusManager.SHOW;
		StatusManager.getManager().handle(status, style);
	}

}
