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

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Properties Label Provider
 */
public class SFSBrowserPropertiesLabelProvider implements ITableLabelProvider {

	/**
	 * Configure the viewer
	 * <p>
	 * Conveniently located here to keep column semantics in one place
	 * 
	 * @param tv
	 *            the viewer
	 */
	public void configureTreeColumns(final TreeViewer tv) {

		Tree tab = tv.getTree();
		TreeColumn col = new TreeColumn(tab, SWT.NONE);
		col.setText(Messages.SFSBrowserPropertiesLabelProvider_Key_XCOL);
		col.setWidth(200);

		col = new TreeColumn(tab, SWT.NONE);
		col.setText(Messages.SFSBrowserPropertiesLabelProvider_Value_XCOL);
		col.setWidth(600);

		tab.setLinesVisible(true);
		tab.setHeaderVisible(true);

	}

	public void removeListener(ILabelProviderListener listener) {
		// nothing				
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void dispose() {
		// nothing				
	}

	public void addListener(ILabelProviderListener listener) {
		// nothing
	}

	@SuppressWarnings("unchecked")
	public String getColumnText(Object element, int columnIndex) {

		if (element instanceof String) {
			if (columnIndex == 0) {
				return (String) element;
			}
			return null;
		}

		Map.Entry<QualifiedName, Object> entry = (Entry<QualifiedName, Object>) element;
		switch (columnIndex) {
		case 0:
			return entry.getKey().toString();

		case 1:
			return "" + entry.getValue(); //$NON-NLS-1$
		default:
			return null;
		}
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
}
