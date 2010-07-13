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
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class PropertySection extends AbstractPropertySection {

	TreeViewer tv;

	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		tv = new TreeViewer(parent, SWT.MULTI | SWT.FULL_SELECTION);
		PropertiesContentProvider.initTree(tv, 400);

		tv.setLabelProvider(new PropertiesLabelProvider());
		tv.setContentProvider(new PropertiesContentProvider());

		tv.getTree().addMenuDetectListener(new MenuDetectListener() {

			public void menuDetected(MenuDetectEvent e) {
				buildMenu(tv.getTree(), e);
			}
		});
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

	void buildMenu(Tree tree, MenuDetectEvent e) {
		Menu menu = tree.getMenu();
		if (menu != null) {
			menu.dispose();
		}
		menu = new Menu(tree);
		tree.setMenu(menu);

		MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(Messages.PropertySection_Refresh_XMIT);
		menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				refresh();
			}
		});

		final TreeItem[] selected = tree.getSelection();
		if (selected.length == 1 && selected[0].getData() instanceof KeyValuePair) {
			new MenuItem(menu, SWT.SEPARATOR);
			final KeyValuePair kvp = (KeyValuePair) selected[0].getData();
			menuItem = new MenuItem(menu, SWT.PUSH);
			menuItem.setText(Messages.PropertySection_CopyValue_XMIT);
			menuItem.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent evt) {
					Clipboard clip = null;
					try {
						clip = new Clipboard(Display.getCurrent());
						clip.setContents(new String[] {kvp.getValue()}, new Transfer[] {TextTransfer.getInstance()});
					} finally {
						if (clip != null) {
							clip.dispose();
						}
					}
				}

			});

			menuItem = new MenuItem(menu, SWT.PUSH);
			menuItem.setText(Messages.PropertySection_CopyKeyAndValue_XMIT);
			menuItem.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent evt) {
					Clipboard clip = null;
					try {
						clip = new Clipboard(Display.getCurrent());
						clip.setContents(new String[] {kvp.getKey() + "\t" + kvp.getValue()}, new Transfer[] {TextTransfer.getInstance()}); //$NON-NLS-1$
					} finally {
						if (clip != null) {
							clip.dispose();
						}
					}
				}

			});
		} else if (selected.length > 1) {
			final String linebreak = System.getProperty("line.separator"); //$NON-NLS-1$
			menu = new Menu(tree);
			tree.setMenu(menu);
			MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText(Messages.SemanticResourcePropertyPage_CopySelection_XMIT);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent evt) {
					StringBuilder sb = new StringBuilder();
					for (TreeItem actItem : selected) {
						if (actItem.getData() instanceof KeyValuePair) {
							KeyValuePair pair = (KeyValuePair) actItem.getData();
							sb.append(pair.getKey() + "\t" + pair.getValue()); //$NON-NLS-1$
							sb.append(linebreak);
						} else if (actItem.getData() instanceof String) {
							sb.append((String) actItem.getData());
							sb.append(linebreak);
						}
					}
					// drop the last linebreak
					if (sb.length() > linebreak.length()) {
						sb.setLength(sb.length() - linebreak.length());
					}
					Clipboard clip = null;
					try {
						clip = new Clipboard(Display.getCurrent());
						clip.setContents(new String[] {sb.toString()}, new Transfer[] {TextTransfer.getInstance()});
					} finally {
						if (clip != null) {
							clip.dispose();
						}
					}
				}
			});
		}

	}

}
