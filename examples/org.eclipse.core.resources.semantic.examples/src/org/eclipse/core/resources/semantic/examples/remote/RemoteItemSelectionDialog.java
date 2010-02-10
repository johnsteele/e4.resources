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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.semantic.examples.remote.RemoteItem.Type;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * For selecting a remote item
 * 
 */
public class RemoteItemSelectionDialog extends Dialog {

	private TableViewer tv;
	private final RemoteItem[] itemsToSelect;
	private List<RemoteItem> selected = new ArrayList<RemoteItem>();
	private final boolean multiSelect;

	private final static class ContentProvider implements ITreeContentProvider {

		private RemoteItem[] myContent;

		ContentProvider(RemoteItem[] content) {
			this.myContent = content;
		}

		public Object[] getChildren(Object parentElement) {
			return null;
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return false;
		}

		public Object[] getElements(Object inputElement) {
			return this.myContent;
		}

		public void dispose() {
			// noting

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// no, we're immutable
		}

	}

	private final static class LabelProvider implements ITableLabelProvider {

		LabelProvider() {
			// nothing
		}

		public Image getColumnImage(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0:

				if (((RemoteItem) element).getType() == Type.FOLDER) {
					return RemoteStoreEditor.FOLDERIMAGE;
				}
				return RemoteStoreEditor.FILEIMAGE;

			default:
				return null;
			}

		}

		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return ((RemoteItem) element).getName();

			default:
				return null;
			}
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

	/**
	 * 
	 * @param parentShell
	 *            the shell
	 * @param items
	 *            the items from which to select
	 * @param multiSelect
	 *            true for multiple selection
	 */
	public RemoteItemSelectionDialog(Shell parentShell, RemoteItem[] items, boolean multiSelect) {
		super(parentShell);
		this.itemsToSelect = items;
		this.multiSelect = multiSelect;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (this.multiSelect) {
			newShell.setText(Messages.RemoteItemSelectionDialog_SelectItems_XGRP);
		} else {
			newShell.setText(Messages.RemoteItemSelectionDialog_SelectItem_XGRP);
		}

	}

	/**
	 * @return the result
	 */
	public List<RemoteItem> getSelectedItems() {
		return this.selected;
	}

	protected Control createDialogArea(Composite parent) {

		Composite myArea = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(myArea);

		myArea.setLayout(new GridLayout(1, false));

		if (this.multiSelect) {
			this.tv = new TableViewer(myArea);
		} else {
			this.tv = new TableViewer(myArea, SWT.SINGLE);
		}

		Table table = this.tv.getTable();

		TableColumn c1 = new TableColumn(table, SWT.NONE);
		c1.setText(Messages.RemoteItemSelectionDialog_Name_XFLD);
		c1.setWidth(200);

		this.tv.setContentProvider(new ContentProvider(this.itemsToSelect));
		this.tv.setLabelProvider(new LabelProvider());
		this.tv.setInput(new Object());

		return myArea;

	}

	@SuppressWarnings("unchecked")
	protected void okPressed() {
		IStructuredSelection sel = (IStructuredSelection) this.tv.getSelection();
		this.selected.clear();
		this.selected.addAll(sel.toList());

		super.okPressed();
	}

}
