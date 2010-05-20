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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class PropertySection extends AbstractPropertySection {

	TreeViewer tv;

	public PropertySection() {
		// nothing
	}

	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		tv = new TreeViewer(parent);
		PropertiesContentProvider.initTree(tv, 400);

		tv.setLabelProvider(new PropertiesLabelProvider());
		tv.setContentProvider(new PropertiesContentProvider());
	}

	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		IStructuredSelection ssel = (IStructuredSelection) selection;
		// just show nothing in case of multiple selection
		if (ssel.size() == 1) {
			tv.setInput(ssel.getFirstElement());
			tv.expandAll();
		} else {
			tv.setInput(null);
		}
	}

	@Override
	public boolean shouldUseExtraSpace() {
		// we are the first and only section
		return true;
	}

}
