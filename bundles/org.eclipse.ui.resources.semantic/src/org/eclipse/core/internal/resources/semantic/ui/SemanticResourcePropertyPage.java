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
package org.eclipse.core.internal.resources.semantic.ui;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.resources.semantic.ISemanticProject;
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.resources.semantic.ISemanticResourceInfo;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Semantic Resources Property page
 * 
 */
public class SemanticResourcePropertyPage extends PropertyPage {

	@Override
	protected Control createContents(Composite parent) {

		Tree propsTree = new Tree(parent, SWT.NONE);

		TreeColumn key = new TreeColumn(propsTree, SWT.NONE);
		key.setWidth(220);
		key.setText(Messages.SemanticResourcePropertyPage_Key_XGRP);
		TreeColumn value = new TreeColumn(propsTree, SWT.NONE);
		value.setWidth(300);
		value.setText(Messages.SemanticResourcePropertyPage_Value_XGRP);

		ISemanticResource node = (ISemanticResource) this.getElement().getAdapter(ISemanticResource.class);
		IProject project = node.getAdaptedResource().getProject();

		TreeItem infoRoot = new TreeItem(propsTree, SWT.NONE);

		try {
			infoRoot.setText(0, Messages.SemanticResourcePropertyPage_SFSAttr_XFLD);
			ISemanticFileStore sfs = (ISemanticFileStore) EFS.getStore(node.getAdaptedResource().getLocationURI());

			TreeItem childItem = new TreeItem(infoRoot, SWT.NONE);

			childItem.setText(new String[] { Messages.SemanticResourcePropertyPage_EffContentProv_XFLD,
					sfs.getEffectiveContentProvider().getClass().getName() });

		} catch (Exception e) {
			// $JL-EXC$
			infoRoot.setText(1, Messages.SemanticResourcePropertyPage_Exception_XMSG);
		}

		addResourceInfo(node, propsTree);

		if (node instanceof ISemanticProject) {
			// nothing over folder
		} else if (node instanceof ISemanticFolder) {
			TreeItem folderRoot = new TreeItem(propsTree, SWT.NONE);
			folderRoot.setText(0, Messages.SemanticResourcePropertyPage_SFolderAttr_XFLD);
			TreeItem it = new TreeItem(folderRoot, SWT.NONE);
			it.setText(new String[] { Messages.SemanticResourcePropertyPage_Project_XFLD, project.getName() });

			IPath path = node.getAdaptedResource().getProjectRelativePath();
			it = new TreeItem(folderRoot, SWT.NONE);
			it.setText(new String[] { Messages.SemanticResourcePropertyPage_Path_XFLD, path.toPortableString() });

			ISemanticFolder folder = (ISemanticFolder) node;

			it = new TreeItem(folderRoot, SWT.NONE);
			try {
				it.setText(new String[] { Messages.SemanticResourcePropertyPage_ContentProviderId_XFLD, folder.getContentProviderID() });
			} catch (CoreException e) {
				// $JL-EXC$
				it.setText(new String[] { Messages.SemanticResourcePropertyPage_ContentProviderId_XFLD,
						Messages.SemanticResourcePropertyPage_Exception_XMSG });
			}
		} else if (node instanceof ISemanticFile) {
			TreeItem fileRoot = new TreeItem(propsTree, SWT.NONE);
			fileRoot.setText(0, Messages.SemanticResourcePropertyPage_SFileAttributes_XFLD);
			TreeItem it = new TreeItem(fileRoot, SWT.NONE);
			it.setText(new String[] { Messages.SemanticResourcePropertyPage_Project_XFLD, project.getName() });

			IPath path = node.getAdaptedResource().getProjectRelativePath();
			it = new TreeItem(fileRoot, SWT.NONE);
			it.setText(new String[] { Messages.SemanticResourcePropertyPage_Path_XFLD, path.toPortableString() });

		}

		propsTree.setLinesVisible(true);
		propsTree.setHeaderVisible(true);

		for (TreeItem item : propsTree.getItems()) {
			item.setExpanded(true);
		}

		return propsTree;
	}

	private void addResourceInfo(ISemanticResource resource, Tree propsTable) {

		TreeItem infoRoot = new TreeItem(propsTable, SWT.NONE);

		try {
			infoRoot.setText(0, Messages.SemanticResourcePropertyPage_SRInfo_XFLD);
			int options = ISemanticFileSystem.RESOURCE_INFO_LOCAL_ONLY | ISemanticFileSystem.RESOURCE_INFO_LOCKED
					| ISemanticFileSystem.RESOURCE_INFO_READ_ONLY;
			ISemanticResourceInfo info = resource.fetchResourceInfo(options, null);

			TreeItem childItem = new TreeItem(infoRoot, SWT.NONE);
			childItem.setText(new String[] { Messages.SemanticResourcePropertyPage_ReadOnly_XFLD, "" + info.isReadOnly() }); //$NON-NLS-1$

			if (resource instanceof ISemanticFile) {
				childItem = new TreeItem(infoRoot, SWT.NONE);
				childItem.setText(new String[] { Messages.SemanticResourcePropertyPage_LocalOnly_XFLD, "" + info.isLocalOnly() }); //$NON-NLS-1$
			}

			childItem = new TreeItem(infoRoot, SWT.NONE);
			childItem.setText(new String[] { Messages.SemanticResourcePropertyPage_Locked_XFLD, "" + info.isLocked() }); //$NON-NLS-1$

			infoRoot.setExpanded(true);

		} catch (Exception e) {
			// $JL-EXC$
			infoRoot.setText(1, Messages.SemanticResourcePropertyPage_Exception_XMSG);
		}

		TreeItem storeRoot = new TreeItem(propsTable, SWT.NONE);
		storeRoot.setText(0, Messages.SemanticResourcePropertyPage_SemResAttr_XFLD);

		try {

			TreeItem childItem = new TreeItem(storeRoot, SWT.NONE);
			String typeText = null;

			if (resource instanceof ISemanticFile) {
				typeText = Messages.SemanticResourcePropertyPage_File_XFLD;
			}
			if (resource instanceof ISemanticFolder) {
				typeText = Messages.SemanticResourcePropertyPage_Folder_XFLD;
			}
			if (resource instanceof ISemanticProject) {
				typeText = Messages.SemanticResourcePropertyPage_Project_XFLD;
			}
			if (typeText == null) {
				typeText = ""; //$NON-NLS-1$
			}

			childItem.setText(new String[] { Messages.SemanticResourcePropertyPage_Type_XFLD, typeText });

		} catch (Exception e) {
			// $JL-EXC$
			storeRoot.setText(1, Messages.SemanticResourcePropertyPage_Exception_XMSG);
			return;
		}
		{
			Map<QualifiedName, String> persistent;
			TreeItem persistentRoot = new TreeItem(storeRoot, SWT.NONE);
			persistentRoot.setText(0, Messages.SemanticResourcePropertyPage_PersistentProps_XFLD);

			try {
				persistent = resource.getPersistentProperties();
			} catch (CoreException e) {
				// $JL-EXC$
				persistentRoot.setText(1, Messages.SemanticResourcePropertyPage_Exception_XMSG);
				persistent = null;
			}
			if (persistent != null) {
				if (!persistent.isEmpty()) {

					for (Entry<QualifiedName, String> entry : persistent.entrySet()) {
						TreeItem childItem = new TreeItem(persistentRoot, SWT.NONE);
						childItem.setText(new String[] { entry.getKey().toString(), entry.getValue() });
					}

					persistentRoot.setExpanded(true);
				} else {
					persistentRoot.setText(1, Messages.SemanticResourcePropertyPage_NoEntries_XMSG);
				}
			}
		}
		{
			Map<QualifiedName, Object> transientAtts;
			TreeItem transientRoot = new TreeItem(storeRoot, SWT.NONE);
			transientRoot.setText(0, Messages.SemanticResourcePropertyPage_SessionProp_XFLD);

			try {
				transientAtts = resource.getSessionProperties();
			} catch (CoreException e) {
				// $JL-EXC$
				transientRoot.setText(1, Messages.SemanticResourcePropertyPage_Exception_XMSG);
				transientAtts = null;
			}
			if (transientAtts != null) {
				if (!transientAtts.isEmpty()) {

					for (Entry<QualifiedName, Object> entry : transientAtts.entrySet()) {
						TreeItem childItem = new TreeItem(transientRoot, SWT.NONE);
						childItem.setText(new String[] { entry.getKey().toString(), String.valueOf(entry.getValue()) });
					}

					transientRoot.setExpanded(true);
				} else {
					transientRoot.setText(1, Messages.SemanticResourcePropertyPage_NoEntries_XMSG);
				}
			}
		}

	}
}
