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
package org.eclipse.core.resources.semantic.examples.remote;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.resources.semantic.ISemanticProject;
import org.eclipse.core.resources.semantic.examples.SemanticResourcesPluginExamples;
import org.eclipse.core.resources.semantic.examples.providers.RemoteStoreContentProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.QualifiedName;

/**
 * Adapts a project to a remote store
 */
public class RemoteStoreAdapter implements IAdapterFactory {

	private final static QualifiedName STORE = new QualifiedName(SemanticResourcesPluginExamples.PLUGIN_ID, "TransientStore"); //$NON-NLS-1$

	@SuppressWarnings("unchecked")
	public Object getAdapter(Object adaptableObject, Class adapterType) {

		if (adaptableObject != null && adaptableObject instanceof IContainer && adapterType.equals(RemoteStore.class)) {
			IContainer resource = ((IContainer) adaptableObject);

			ISemanticFolder sfolder = (ISemanticFolder) resource.getAdapter(ISemanticFolder.class);
			if (sfolder != null) {

				try {

					ISemanticProject sProject = (ISemanticProject) resource.getProject().getAdapter(ISemanticProject.class);
					String remoteProjectName = sProject.getPersistentProperty(RemoteStoreContentProvider.USE_PROJECT);
					if (remoteProjectName != null) {
						IProject remoteProject = ResourcesPlugin.getWorkspace().getRoot().getProject(remoteProjectName);
						if (remoteProject.isAccessible()) {
							RemoteStore store = new RemoteStore(remoteProject);
							try {
								store.deserialize();
								return store;
							} catch (CoreException e) {
								// $JL-EXC$
								SemanticResourcesPluginExamples.getDefault().getLog().log(e.getStatus());
							}
						}
						return null;
					}
				} catch (CoreException e) {
					// $JL-EXC$ ignore
				}
			}

			RemoteStore newStore = new RemoteStore(resource);

			try {
				newStore.deserialize();
			} catch (CoreException e) {
				// $JL-EXC$
				SemanticResourcesPluginExamples.getDefault().getLog().log(e.getStatus());
			}

			return newStore;
		}

		if (adaptableObject != null && adaptableObject instanceof IContainer && adapterType.equals(RemoteStoreTransient.class)) {

			IContainer resource = ((IContainer) adaptableObject);

			try {
				RemoteStoreTransient store = (RemoteStoreTransient) resource.getSessionProperty(STORE);
				if (store != null) {
					return store;
				}
			} catch (CoreException e) {
				// $JL-EXC$ ignore here
			}

			RemoteStoreTransient newStore = new RemoteStoreTransient(resource);
			try {
				resource.setSessionProperty(STORE, newStore);
			} catch (CoreException e) {
				// $JL-EXC$
				SemanticResourcesPluginExamples.getDefault().getLog().log(e.getStatus());
			}

			return newStore;
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public Class[] getAdapterList() {
		return new Class[] { RemoteStore.class, RemoteStoreTransient.class };
	}

}
