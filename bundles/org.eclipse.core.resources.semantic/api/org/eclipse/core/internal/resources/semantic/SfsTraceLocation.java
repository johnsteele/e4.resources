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
package org.eclipse.core.internal.resources.semantic;

import org.eclipse.core.internal.resources.semantic.util.ITraceLocation;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugTrace;

/**
 * Trace locations for the Semantic File System implementation
 * 
 * @since 4.0
 * 
 */
public enum SfsTraceLocation implements ITraceLocation {
	/** Core */
	CORE("/debug/core"), //$NON-NLS-1$
	/** Core DB */
	CORE_DB("/debug/core/database"), //$NON-NLS-1$ 
	/** Core Verbose */
	CORE_VERBOSE("/debug/core/verbose"), //$NON-NLS-1$ 
	/** Debug (default entry) */
	DEBUG("/debug"), //$NON-NLS-1$ 
	/** Resource Rule Factory */
	RULEFACTORY("/debug/rulefactory"), //$NON-NLS-1$ 
	/**
	 * Team provider
	 * <p>
	 * Used to trace Exceptions during validateEdit/validateSave
	 **/
	TEAM("/debug/teamprovider"), //$NON-NLS-1$ 
	/** Content provider */
	CONTENTPROVIDER("/debug/contentprovider"); //$NON-NLS-1$ 

	/**
	 * Initialize the locations
	 * 
	 * @param options
	 * @param pluginIsDebugging
	 */
	public static void initializeFromOptions(DebugOptions options, boolean pluginIsDebugging) {

		// we evaluate the plug-in switch
		if (pluginIsDebugging) {
			myTrace = options.newDebugTrace(SemanticResourcesPlugin.PLUGIN_ID);
			for (SfsTraceLocation loc : values()) {
				boolean active = options.getBooleanOption(loc.getFullPath(), false);
				loc.setActive(active);
			}
		} else {
			// if the plug-in switch is off, we don't set the trace instance 
			// to null to avoid problems with possibly running trace calls
			for (SfsTraceLocation loc : values()) {
				loc.setActive(false);
			}
		}
	}

	private final String location;
	private final String fullPath;

	private boolean active = false;
	private static DebugTrace myTrace;

	private SfsTraceLocation(String path) {
		this.fullPath = SemanticResourcesPlugin.PLUGIN_ID + path;
		this.location = path;
	}

	/**
	 * Convenience method
	 * 
	 * @return the debug trace (may be null)
	 * 
	 **/
	public static DebugTrace getTrace() {
		return SfsTraceLocation.myTrace;
	}

	/**
	 * 
	 * @return <code>true</code> if this location is active
	 */
	public boolean isActive() {
		return this.active;
	}

	/**
	 * @return the full path
	 */
	public String getFullPath() {
		return this.fullPath;
	}

	public String getLocation() {
		return this.location;
	}

	/**
	 * Sets the "active" flag for this location.
	 * <p>
	 * Used by the initializer
	 * 
	 * @param active
	 *            the "active" flag
	 */
	private void setActive(boolean active) {
		this.active = active;
	}

}
