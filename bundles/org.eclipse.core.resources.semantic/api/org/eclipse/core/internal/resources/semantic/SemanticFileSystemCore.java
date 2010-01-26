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

import java.util.HashMap;

import org.eclipse.core.internal.resources.semantic.spi.SemanticResourcesSpiPlugin;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.osgi.util.NLS;

/**
 * The core part of the Semantic File System.
 * <p>
 * This mainly deals with the extension point registration
 * 
 * 
 */
public class SemanticFileSystemCore implements IRegistryChangeListener {

	private static final String PI_CONTENT_PROVIDER = "contentProvider"; //$NON-NLS-1$
	private final static String EMPTY = ""; //$NON-NLS-1$

	private static final SemanticFileSystemCore INSTANCE = new SemanticFileSystemCore();

	private HashMap<String, IConfigurationElement> contentProviders;

	/**
	 * Returns the singleton instance of this class.
	 * 
	 * @return The singleton instance.
	 */
	public static SemanticFileSystemCore getInstance() {
		return SemanticFileSystemCore.INSTANCE;
	}

	private SemanticFileSystemCore() {
		super();
		RegistryFactory.getRegistry().addRegistryChangeListener(this);
	}

	public void registryChanged(IRegistryChangeEvent event) {

		IExtensionDelta[] changes = event.getExtensionDeltas(SemanticResourcesSpiPlugin.PLUGIN_ID,
				SemanticFileSystemCore.PI_CONTENT_PROVIDER);
		if (changes.length == 0)
			return;
		synchronized (this) {
			// let the registry be rebuilt lazily
			this.contentProviders = null;
		}
	}

	/**
	 * 
	 * Returns the provider with lazy instantiation
	 * 
	 * @param contentProviderID
	 *            the content provider id
	 * 
	 * @return the content provider factory corresponding to the content
	 *         provider ID
	 */
	public ISemanticContentProviderFactory getContentProviderFactory(final String contentProviderID) {

		return new ISemanticContentProviderFactory() {

			public ISemanticContentProvider createContentProvider() throws CoreException {

				for (int i = 0; i < 3; i++) {
					try {
						return toProvider(getConfigurationElement());
					} catch (InvalidRegistryObjectException e) {
						// $JL-EXC$ try again
					}
				}

				return toProvider(getConfigurationElement());

			}

			private IConfigurationElement getConfigurationElement() throws CoreException {
				HashMap<String, IConfigurationElement> registry = getContentProviderRegistry();

				IConfigurationElement element = registry.get(contentProviderID);
				if (element == null) {
					throw new SemanticResourceException(SemanticResourceStatusCode.UNKNOWN_CONTENT_PROVIDER_ID, new Path(
							SemanticFileSystemCore.EMPTY), NLS.bind(Messages.SemanticFileSystemCore_TemplateIdNotFound_XMSG,
							contentProviderID));
				}
				return element;
			}

			private ISemanticContentProvider toProvider(IConfigurationElement element) throws CoreException {

				ISemanticContentProvider provider = (ISemanticContentProvider) element.createExecutableExtension("class"); //$NON-NLS-1$

				return provider;
			}
		};

	}

	/**
	 * Returns the fully initialized content provider registry
	 * 
	 * @return The content provider registry
	 */
	synchronized HashMap<String, IConfigurationElement> getContentProviderRegistry() {
		if (this.contentProviders == null) {

			this.contentProviders = new HashMap<String, IConfigurationElement>();

			IExtensionPoint extensionPoint = RegistryFactory.getRegistry().getExtensionPoint(SemanticResourcesSpiPlugin.PLUGIN_ID,
					SemanticFileSystemCore.PI_CONTENT_PROVIDER);
			IExtension[] exts = extensionPoint.getExtensions();
			for (int i = 0; i < exts.length; i++) {
				IConfigurationElement[] elements = exts[i].getConfigurationElements();
				for (int j = 0; j < elements.length; j++) {
					if ("contentProvider".equals(elements[j].getName())) { //$NON-NLS-1$
						String contentProviderID = elements[j].getAttribute("id"); //$NON-NLS-1$
						if (contentProviderID != null) {
							this.contentProviders.put(contentProviderID, elements[j]);
						}
					}
				}
			}

		}
		return this.contentProviders;
	}

}
