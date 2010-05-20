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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

public class PropertiesLabelProvider extends BaseLabelProvider implements ITableLabelProvider, IFontProvider {

	public PropertiesLabelProvider() {
		// nothing
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		switch (columnIndex) {
			case 0 :
				if (element instanceof String)
					return (String) element;
				if (element instanceof KeyValuePair) {
					return String.valueOf(((KeyValuePair) element).getKey());
				}
				break;
			case 1 :
				if (element instanceof KeyValuePair) {
					return String.valueOf(((KeyValuePair) element).getValue());
				}
				break;
			default :
				return null;
		}
		return null;
	}

	public Font getFont(Object element) {
		if (element instanceof String) {
			return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
		}
		return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().get(JFaceResources.DEFAULT_FONT);
	}
}
