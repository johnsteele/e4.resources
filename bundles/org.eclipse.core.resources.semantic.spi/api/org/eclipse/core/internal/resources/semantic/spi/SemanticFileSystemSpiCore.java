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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.RegistryFactory;

/**
 * Deals with the registry.
 * 
 */
public class SemanticFileSystemSpiCore implements IRegistryChangeListener {

	private static final String PI_FOLDER_MAPPING = "pathContentProviderMapping"; //$NON-NLS-1$

	private static final SemanticFileSystemSpiCore INSTANCE = new SemanticFileSystemSpiCore();

	private HashMap<IPath, String> pathMappings;

	/**
	 * Returns the singleton instance of this class.
	 * 
	 * @return The singleton instance.
	 */
	public static SemanticFileSystemSpiCore getInstance() {
		return SemanticFileSystemSpiCore.INSTANCE;
	}

	private SemanticFileSystemSpiCore() {
		super();
		RegistryFactory.getRegistry().addRegistryChangeListener(this);
	}

	public void registryChanged(IRegistryChangeEvent event) {

		if (SfsSpiTraceLocation.CORE.isActive()) {
			SfsSpiTraceLocation.getTrace().traceEntry(SfsSpiTraceLocation.CORE.getLocation());
		}

		IExtensionDelta[] mappingChanges = event.getExtensionDeltas(SemanticResourcesSpiPlugin.PLUGIN_ID,
				SemanticFileSystemSpiCore.PI_FOLDER_MAPPING);
		if (mappingChanges.length == 0) {
			return;
		}

		synchronized (this) {

			if (mappingChanges.length > 0) {
				// let the registry be rebuilt lazily
				this.pathMappings = null;
			}
		}
	}

	/**
	 * @param path
	 *            the path
	 * @return the mapped content provider ID, or <code>null</code>
	 */
	public String getFolderTemplateMapping(IPath path) {

		if (SfsSpiTraceLocation.CORE.isActive()) {
			SfsSpiTraceLocation.getTrace().traceEntry(SfsSpiTraceLocation.CORE.getLocation(), path.toString());
		}

		String result = getFolderMappingRegistry().get(path);

		if (SfsSpiTraceLocation.CORE.isActive()) {
			SfsSpiTraceLocation.getTrace().traceExit(SfsSpiTraceLocation.CORE.getLocation(), result);
		}

		return result;
	}

	private synchronized Map<IPath, String> getFolderMappingRegistry() {

		if (this.pathMappings == null) {
			this.pathMappings = new HashMap<IPath, String>();

			IExtensionPoint point = RegistryFactory.getRegistry().getExtensionPoint(SemanticResourcesSpiPlugin.PLUGIN_ID,
					SemanticFileSystemSpiCore.PI_FOLDER_MAPPING);
			IExtension[] extensions = point.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement[] elements = extensions[i].getConfigurationElements();
				for (int j = 0; j < elements.length; j++) {
					if ("mapping".equals(elements[j].getName())) { //$NON-NLS-1$
						String contentProviderID = elements[j].getAttribute("contentProviderId"); //$NON-NLS-1$
						String path = elements[j].getAttribute("path"); //$NON-NLS-1$

						if (contentProviderID != null && path != null) {
							IPath relPath = new Path(path).makeRelative();
							this.pathMappings.put(relPath, contentProviderID);

						}
					}
				}
			}
		}
		return this.pathMappings;
	}
}
