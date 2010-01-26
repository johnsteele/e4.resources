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

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Label provider
 */
public class SFSBrowserTreeLabelProvider implements ITableLabelProvider {

	private final static Image FOLDER = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
	private final static Image FILE = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
	private final static Image NONEXIST = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE_DISABLED);

	/**
	 * Sets up tree columns.
	 * <p>
	 * Conveniently located here to keep column semantics in one place
	 * 
	 * @param tv
	 *            the tree viewer
	 */
	public void configureTreeColumns(final TreeViewer tv) {

		final Tree tree = tv.getTree();

		TreeColumn c1 = new TreeColumn(tree, SWT.NONE);
		c1.setWidth(450);
		c1.setText(Messages.SFSBrowserTreeLabelProvider_Name_XCOL);

		tree.setHeaderVisible(true);

		tree.addMouseListener(new MouseAdapter() {

			public void mouseDoubleClick(MouseEvent e) {
				TreeItem item = tree.getItem(new Point(e.x, e.y));
				boolean expanded = tv.getExpandedState(item.getData());
				if (expanded) {
					tv.collapseToLevel(item.getData(), 1);
				} else {
					tv.expandToLevel(item.getData(), 1);
				}
			}
		});

	}

	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == 0) {
			SFSBrowserTreeObject obj = (SFSBrowserTreeObject) element;
			if (!obj.getInfo().exists()) {
				return NONEXIST;
			}
			if (obj.getInfo().isDirectory()) {
				return FOLDER;
			}
			return FILE;
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {

		SFSBrowserTreeObject object = (SFSBrowserTreeObject) element;
		if (columnIndex == 0) {
			return object.getPath().lastSegment();
		}
		return null;
	}

	public void addListener(ILabelProviderListener listener) {
		// nothing
	}

	public void dispose() {
		// nothing
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// nothing
	}

}
