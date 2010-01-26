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
package org.eclipse.core.internal.resources.semantic.util;

import org.eclipse.core.internal.resources.semantic.SemanticResourcesSharedPlugin;
import org.eclipse.core.runtime.Platform;

/**
 * Trace locations for the Semantic File System
 * 
 * @since 4.0
 * 
 */
public enum TraceLocation {
	/** Core */
	CORE("Core", "/debug/core"), //$NON-NLS-1$ //$NON-NLS-2$
	/** Core Verbose */
	CORE_VERBOSE("Core", "/debug/core/verbose"), //$NON-NLS-1$ //$NON-NLS-2$
	/** Cache Store */
	CACHESTORE("CacheFileStore", "/debug/cacheFileStore"), //$NON-NLS-1$ //$NON-NLS-2$
	/** Debug (default entry) */
	DEBUG("Debug", "/debug"), //$NON-NLS-1$ //$NON-NLS-2$
	/** Resource Rule Factory */
	RULEFACTORY("RuleFactory", "/debug/rulefactory"), //$NON-NLS-1$ //$NON-NLS-2$
	/** Synchronization */
	SYNC("Synchronization", "/debug/synchronization"), //$NON-NLS-1$ //$NON-NLS-2$
	/** Team provider */
	TEAM("TeamProvider", "/debug/teamprovider"), //$NON-NLS-1$ //$NON-NLS-2$
	/** Content provider */
	CONTENTPROVIDER("ContentProvider", "/debug/contentprovider"); //$NON-NLS-1$ //$NON-NLS-2$

	private final String name;
	private final String path;
	private boolean active = false;

	private TraceLocation(String name, String path) {
		this.name = name;
		this.path = path;
	}

	/**
	 * 
	 * @return <code>true</code> if this location is active
	 */
	public boolean isActive() {
		return this.active;
	}

	/**
	 * 
	 * @return a human-readable name (not localized)
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 
	 * @return the path
	 */
	public String getPath() {
		return this.path;
	}

	/**
	 * Initializes all {@link TraceLocation}s with the platform debug options
	 */
	public static void init() {
		TraceLocation[] values = values();
		for (TraceLocation location : values) {
			location.active = Boolean.TRUE.toString().equals(
					Platform.getDebugOption(SemanticResourcesSharedPlugin.PLUGIN_ID + location.path));
		}
	}

	/**
	 * Sets the "active" flag for this location
	 * 
	 * @param active
	 *            the "active" flag
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
}
