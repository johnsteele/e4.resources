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

import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * The Semantic Resources implementation plug-in
 */
public class SemanticResourcesPlugin extends Plugin implements DebugOptionsListener {

	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "org.eclipse.core.resources.semantic"; //$NON-NLS-1$

	private static BundleContext context;

	@Override
	public void start(BundleContext actContext) throws Exception {
		super.start(actContext);
		context = actContext;
		Dictionary<String, String> props = new Hashtable<String, String>(4);
		props.put(DebugOptions.LISTENER_SYMBOLICNAME, context.getBundle().getSymbolicName());
		context.registerService(DebugOptionsListener.class.getName(), this, props);

	}

	@Override
	public void stop(BundleContext actContext) throws Exception {
		super.stop(actContext);
		context = null;
	}

	/**
	 * 
	 * @return the cache location in the local file system
	 */
	public static IPath getCacheLocation() {
		// try to put the cache in the instance location if possible (3.2
		// behavior)
		try {
			if (context != null) {
				ServiceReference[] refs = context.getServiceReferences(Location.class.getName(), Location.INSTANCE_FILTER);
				if (refs != null && refs.length == 1) {
					Location location = (Location) context.getService(refs[0]);
					if (location != null) {
						IPath instancePath = new Path(new File(location.getURL().getFile()).toString());
						context.ungetService(refs[0]);
						return instancePath.append(".metadata/.plugins").append(PLUGIN_ID); //$NON-NLS-1$
					}
				}
			}

		} catch (InvalidSyntaxException e) {
			// $JL-EXC$ ignore and use user.home below
		}
		// just put the cache in the user home directory

		if (SfsTraceLocation.CORE.isActive()) {
			SfsTraceLocation.getTrace().trace(SfsTraceLocation.CORE.getLocation(), Messages.SemanticResourcesPlugin_UserHomeAsCache_XMSG);
		}
		return Path.fromOSString(System.getProperty("user.home")); //$NON-NLS-1$
	}

	public void optionsChanged(DebugOptions options) {

		SfsTraceLocation.initializeFromOptions(options, isDebugging());

	}

}
