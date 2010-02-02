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
package org.eclipse.core.internal.resources.semantic.spi;

import org.eclipse.core.internal.resources.semantic.util.ITraceLocation;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugTrace;

/**
 * Trace locations for the Semantic File System SPI
 * 
 * @since 4.0
 * 
 */
public enum SfsSpiTraceLocation implements ITraceLocation {
	/** Core */
	CORE("/debug/core"), //$NON-NLS-1$
	/** Core */
	CACHESERVICE("/debug/cacheservice"), //$NON-NLS-1$
	;

	private final String location;
	private final String fullPath;

	private boolean active = false;
	private static DebugTrace myTrace;

	/**
	 * Initialize the locations
	 * 
	 * @param options
	 * @param pluginIsDebugging
	 */
	public static void initializeFromOptions(DebugOptions options, boolean pluginIsDebugging) {

		// we evaluate the plug-in switch
		if (pluginIsDebugging) {
			myTrace = options.newDebugTrace(SemanticResourcesSpiPlugin.PLUGIN_ID);
			for (SfsSpiTraceLocation loc : values()) {
				boolean active = options.getBooleanOption(loc.getFullPath(), false);
				loc.setActive(active);
			}
		} else {
			// if the plug-in switch is off, we don't set the trace instance 
			// to null to avoid problems with possibly running trace calls
			for (SfsSpiTraceLocation loc : values()) {
				loc.setActive(false);
			}
		}
	}

	private SfsSpiTraceLocation(String path) {
		this.fullPath = SemanticResourcesSpiPlugin.PLUGIN_ID + path;
		this.location = path;
	}

	/**
	 * Convenience method
	 * 
	 * @return the debug trace (may be null)
	 * 
	 **/
	public static DebugTrace getTrace() {
		return SfsSpiTraceLocation.myTrace;
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
	private String getFullPath() {
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
