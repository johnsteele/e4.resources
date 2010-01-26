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

import org.eclipse.core.internal.resources.semantic.util.TraceLocation;
import org.eclipse.core.runtime.Plugin;

/**
 * 
 * The Shared Semantic File System Plugin
 * 
 */
public class SemanticResourcesSharedPlugin extends Plugin {
	/**
	 * This plugin's ID
	 */
	public static final String PLUGIN_ID = "org.eclipse.core.resources.semantic.shared"; //$NON-NLS-1$

	/**
	 * No-argument constructor
	 */
	public SemanticResourcesSharedPlugin() {
		TraceLocation.init();
	}

}
