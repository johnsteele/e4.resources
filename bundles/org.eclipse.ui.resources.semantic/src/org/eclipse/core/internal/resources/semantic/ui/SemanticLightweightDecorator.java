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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.resources.semantic.ISemanticResourceInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Decorates Semantic Resources
 * 
 */
public class SemanticLightweightDecorator implements ILightweightLabelDecorator {
	private static final ImageDescriptor EDIT_IMAGE;
	private static final ImageDescriptor LOCAL_IMAGE;
	private static final ImageDescriptor LOCKED_IMAGE;

	// Need to keep our own list of listeners
	private ListenerList listeners = new ListenerList();

	static {
		EDIT_IMAGE = AbstractUIPlugin.imageDescriptorFromPlugin(SemanticResourcesUIPlugin.PLUGIN_ID, "$nl$/icons/edit.gif"); //$NON-NLS-1$
		LOCAL_IMAGE = AbstractUIPlugin.imageDescriptorFromPlugin(SemanticResourcesUIPlugin.PLUGIN_ID, "$nl$/icons/local.gif"); //$NON-NLS-1$
		LOCKED_IMAGE = AbstractUIPlugin.imageDescriptorFromPlugin(SemanticResourcesUIPlugin.PLUGIN_ID, "$nl$/icons/lock.gif"); //$NON-NLS-1$
	}

	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IResource) {
			ISemanticResource sresource = (ISemanticResource) ((IResource) element).getAdapter(ISemanticResource.class);

			if (sresource != null && sresource instanceof ISemanticFile) {
				try {
					int options = ISemanticFileSystem.RESOURCE_INFO_LOCAL_ONLY | ISemanticFileSystem.RESOURCE_INFO_LOCKED
							| ISemanticFileSystem.RESOURCE_INFO_READ_ONLY;
					if (sresource.getAdaptedResource().exists()) {
						ISemanticResourceInfo attrs = sresource.fetchResourceInfo(options, null);
						if (attrs.isLocalOnly()) {
							decoration.addOverlay(SemanticLightweightDecorator.LOCAL_IMAGE, IDecoration.BOTTOM_RIGHT);
						} else if (attrs.isLocked()) {
							decoration.addOverlay(SemanticLightweightDecorator.LOCKED_IMAGE, IDecoration.BOTTOM_RIGHT);
						} else if (!attrs.isReadOnly()) {
							decoration.addOverlay(SemanticLightweightDecorator.EDIT_IMAGE, IDecoration.BOTTOM_RIGHT);
						}
					}
				} catch (CoreException e) {
					// $JL-EXC$ ignore
				}

			}
		}

	}

	public void addListener(ILabelProviderListener listener) {
		this.listeners.add(listener);
	}

	public void dispose() {
		// nothing to dispose
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		this.listeners.remove(listener);
	}

}
