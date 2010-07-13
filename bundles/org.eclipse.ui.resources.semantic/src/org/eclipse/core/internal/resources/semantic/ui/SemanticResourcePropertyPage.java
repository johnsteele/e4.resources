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
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
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

		final TreeViewer tv = new TreeViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

		PropertiesContentProvider.initTree(tv, 400);

		tv.setLabelProvider(new PropertiesLabelProvider());
		tv.setContentProvider(new PropertiesContentProvider());

		tv.getTree().addMenuDetectListener(new MenuDetectListener() {

			public void menuDetected(MenuDetectEvent e) {
				buildMenu(tv.getTree(), e);
			}
		});

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

	void buildMenu(Tree tree, MenuDetectEvent e) {
		Menu menu = tree.getMenu();
		if (menu != null) {
			menu.dispose();
		}

		final TreeItem[] selected = tree.getSelection();
		if (selected.length == 1) {
			TreeItem item = selected[0];
			if (item.getData() instanceof KeyValuePair) {
				menu = new Menu(tree);
				tree.setMenu(menu);

				final KeyValuePair kvp = (KeyValuePair) item.getData();
				MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
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
							clip.setContents(
									new String[] {kvp.getKey() + "\t" + kvp.getValue()}, new Transfer[] {TextTransfer.getInstance()}); //$NON-NLS-1$
						} finally {
							if (clip != null) {
								clip.dispose();
							}
						}
					}

				});
			}
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
