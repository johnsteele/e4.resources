/*******************************************************************************
 * Copyright (c) 2010 SAP AG.
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

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.internal.resources.semantic.ui.util.SFSBrowserTreeObject;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Semantic Resources Property page
 * 
 */
public class SemanticResourcePropertyPage extends PropertyPage {

	public SemanticResourcePropertyPage() {
		// hide the set default and apply buttons
		noDefaultAndApplyButton();
	}

	@Override
	protected Control createContents(Composite parent) {

		ISemanticResource node = (ISemanticResource) this.getElement().getAdapter(ISemanticResource.class);

		TreeViewer tv = new TreeViewer(parent);

		PropertiesContentProvider.initTree(tv, 400);

		tv.setLabelProvider(new PropertiesLabelProvider());
		tv.setContentProvider(new PropertiesContentProvider());

		try {
			SFSBrowserTreeObject ob = new SFSBrowserTreeObject(EFS.getFileSystem(ISemanticFileSystem.SCHEME), node.getAdaptedResource()
					.getFullPath());

			tv.setInput(ob);
		} catch (CoreException e) {
			// we don't use "show" here, as the error popup would be hidden
			// behind the properties page
			SemanticResourcesUIPlugin.handleError(e.getMessage(), e, false);
		}
		tv.expandAll();

		return tv.getTree();
	}
}
