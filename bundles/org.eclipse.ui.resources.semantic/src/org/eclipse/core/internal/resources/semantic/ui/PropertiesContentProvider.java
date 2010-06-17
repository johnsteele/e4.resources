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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.core.internal.resources.semantic.SemanticFileStore;
import org.eclipse.core.internal.resources.semantic.ui.util.SFSBrowserTreeObject;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticResourceInfo;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

public class PropertiesContentProvider implements ITreeContentProvider {

	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss::SSS"); //$NON-NLS-1$	
	private SFSBrowserTreeObject treeObject;

	public static void initTree(TreeViewer tv, int width) {
		Tree tree = tv.getTree();
		TreeColumn c1 = new TreeColumn(tree, SWT.NONE);
		c1.setText(Messages.PropertiesContentProvider_Key_XGRP);
		c1.setWidth(width);

		TreeColumn c2 = new TreeColumn(tree, SWT.NONE);
		c2.setText(Messages.PropertiesContentProvider_Value_XGRP);
		c2.setWidth(width);

		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
	}

	public PropertiesContentProvider() {
		// nothing
	}

	public Object[] getElements(Object inputElement) {
		treeObject = (SFSBrowserTreeObject) inputElement;
		ISemanticFileStore sfs = (ISemanticFileStore) treeObject.getStore();

		List<KeyValuePair> props = new ArrayList<KeyValuePair>();

		Date modified = new Date(sfs.fetchInfo().getLastModified());

		props.add(new KeyValuePair(Messages.PropertiesContentProvider_LastModified_XFLD, df.format(modified)));

		String contentProvider;
		try {
			contentProvider = nullToSpace(sfs.getEffectiveContentProvider().getClass().getName());
		} catch (CoreException e) {
			contentProvider = Messages.PropertiesContentProvider_ExceptionGettingValue_XMSG;
		}
		props.add(new KeyValuePair(Messages.PropertiesContentProvider_EffectiveContentProvider_XFLD, contentProvider));
		props.add(new KeyValuePair(Messages.PropertiesContentProvider_ProviderID_XFLD, nullToSpace(sfs.getContentProviderID())));
		String remoteUri;
		try {
			remoteUri = nullToSpace(sfs.getRemoteURIString());
		} catch (CoreException e) {
			remoteUri = Messages.PropertiesContentProvider_ExceptionGettingValue_XMSG;
		}

		props.add(new KeyValuePair(Messages.PropertiesContentProvider_RemoteUri_XFLD, remoteUri));

		List<String> headers = new ArrayList<String>();
		headers.add(Messages.PropertiesContentProvider_ResInfoAttributes_XGRP);
		headers.add(Messages.PropertiesContentProvider_PersistentProps_XGRP);
		headers.add(Messages.PropertiesContentProvider_SessionProps_XGRP);

		Object[] result = new Object[props.size() + headers.size()];
		System.arraycopy(props.toArray(), 0, result, 0, props.size());
		System.arraycopy(headers.toArray(), 0, result, props.size(), headers.size());
		return result;
	}

	private String nullToSpace(String name) {
		if (name == null) {
			return ""; //$NON-NLS-1$
		}
		return name;
	}

	public void dispose() {
		// nothing to dispose
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		treeObject = (SFSBrowserTreeObject) newInput;
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof String) {
			if (Messages.PropertiesContentProvider_PersistentProps_XGRP.equals(parentElement)) {
				ISemanticFileStore store = (ISemanticFileStore) treeObject.getStore();
				Map<QualifiedName, String> props;
				try {
					props = store.getPersistentProperties();
				} catch (CoreException e) {
					return getChildExceptionMap();
				}
				List<KeyValuePair> resultMap = new ArrayList<KeyValuePair>();
				for (Map.Entry<QualifiedName, String> entry : props.entrySet()) {
					resultMap.add(new KeyValuePair(entry.getKey().toString(), entry.getValue()));
				}
				return resultMap.toArray();
			}
			if (Messages.PropertiesContentProvider_SessionProps_XGRP.equals(parentElement)) {
				ISemanticFileStore store = (ISemanticFileStore) treeObject.getStore();
				Map<QualifiedName, Object> props;
				try {
					props = store.getSessionProperties();
				} catch (CoreException e) {
					return getChildExceptionMap();
				}
				List<KeyValuePair> resultMap = new ArrayList<KeyValuePair>();
				for (Map.Entry<QualifiedName, Object> entry : props.entrySet()) {
					resultMap.add(new KeyValuePair(entry.getKey().toString(), String.valueOf(entry.getValue())));
				}
				return resultMap.toArray();
			}
			if (Messages.PropertiesContentProvider_ResInfoAttributes_XGRP.equals(parentElement)) {
				try {
					ISemanticResourceInfo info = ((SemanticFileStore) treeObject.getStore()).fetchResourceInfo(ISemanticFileSystem.NONE,
							null);

					List<KeyValuePair> resultMap = new ArrayList<KeyValuePair>();

					resultMap.add(new KeyValuePair(Messages.PropertiesContentProvider_ReadOnly_XFLD, String.valueOf(info.isReadOnly())));
					resultMap.add(new KeyValuePair(Messages.PropertiesContentProvider_LocalOnly_XFLD, String.valueOf(info.isLocalOnly())));
					resultMap.add(new KeyValuePair(Messages.PropertiesContentProvider_LockingSupported_XFLD, String.valueOf(info
							.isLockingSupported())));
					resultMap.add(new KeyValuePair(Messages.PropertiesContentProvider_Locked_XFLD, String.valueOf(info.isLocked())));
					resultMap.add(new KeyValuePair(Messages.PropertiesContentProvider_ExistsRemotely_XFLD, String.valueOf(info
							.existsRemotely())));
					resultMap
							.add(new KeyValuePair(Messages.PropertiesContentProvider_ContentType_XFLD, nullToSpace(info.getContentType())));

					return resultMap.toArray();

				} catch (Exception e) {
					return getChildExceptionMap();
				}

			}
		}
		return null;
	}

	private Object[] getChildExceptionMap() {
		List<KeyValuePair> result = new ArrayList<KeyValuePair>();
		result.add(new KeyValuePair(Messages.PropertiesContentProvider_ExceptionGettingChildren_XMSG, "")); //$NON-NLS-1$
		return result.toArray();
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return element instanceof String;
	}
}
