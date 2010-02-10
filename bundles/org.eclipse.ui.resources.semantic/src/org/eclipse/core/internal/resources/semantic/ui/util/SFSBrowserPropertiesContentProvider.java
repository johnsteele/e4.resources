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
package org.eclipse.core.internal.resources.semantic.ui.util;

import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Properties Content Provider
 */
public class SFSBrowserPropertiesContentProvider implements ITreeContentProvider {

	private SFSBrowserTreeObject myInput;

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.myInput = (SFSBrowserTreeObject) newInput;
	}

	public void dispose() {
		// nothing
	}

	public Object[] getElements(Object inputElement) {

		SFSBrowserTreeObject object = (SFSBrowserTreeObject) inputElement;
		if (object == null) {
			return null;
		}
		// virtual nodes for persistent and session properties
		return new String[] { Messages.SFSBrowserPropertiesContentProvider_PersistentProperties_XCOL,
				Messages.SFSBrowserPropertiesContentProvider_SessionProperties_XCOL };

	}

	public boolean hasChildren(Object element) {

		if (Messages.SFSBrowserPropertiesContentProvider_PersistentProperties_XCOL.equals(element)) {
			// virtual node for persistent properties
			try {
				return !((ISemanticFileStore) this.myInput.getStore()).getPersistentProperties().isEmpty();
			} catch (CoreException e) {
				return false;
			}
		} else if (Messages.SFSBrowserPropertiesContentProvider_SessionProperties_XCOL.equals(element)) {
			// virtual node for session properties
			try {
				return !((ISemanticFileStore) this.myInput.getStore()).getSessionProperties().isEmpty();
			} catch (CoreException e) {
				return false;
			}
		}
		return false;
	}

	public Object getParent(Object element) {
		return null;
	}

	public Object[] getChildren(Object parentElement) {
		if (Messages.SFSBrowserPropertiesContentProvider_PersistentProperties_XCOL.equals(parentElement)) {
			// virtual node for persistent properties
			try {
				return ((ISemanticFileStore) this.myInput.getStore()).getPersistentProperties().entrySet().toArray();
			} catch (CoreException e) {
				return null;
			}
		} else if (Messages.SFSBrowserPropertiesContentProvider_SessionProperties_XCOL.equals(parentElement)) {
			// virtual node for session properties
			try {
				return ((ISemanticFileStore) this.myInput.getStore()).getSessionProperties().entrySet().toArray();
			} catch (CoreException e) {
				return null;
			}
		}
		return null;
	}
}
