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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Tree content provider
 */
public class SFSBrowserTreeContentProvider implements ITreeContentProvider {

	private boolean hideNonExisting;
	private ISemanticFileSystem myFs;

	public Object[] getChildren(Object parentElement) {
		try {
			SFSBrowserTreeObject parent = (SFSBrowserTreeObject) parentElement;
			IPath parentPath = parent.getPath();
			String[] childNames = parent.getStore().childNames(EFS.NONE, null);
			List<SFSBrowserTreeObject> resultList = new ArrayList<SFSBrowserTreeObject>();
			for (int i = 0; i < childNames.length; i++) {
				SFSBrowserTreeObject ob = new SFSBrowserTreeObject((IFileSystem) getFs(), parentPath.append(childNames[i]));
				if (!getHide() || ob.getInfo().exists()) {
					resultList.add(ob);
				}
			}
			return resultList.toArray();
		} catch (CoreException e) {
			try {
				getFs().getLog().log(e);
			} catch (CoreException e1) {
				// $JL-EXC$ ignore here
			}
			return new Object[0];
		}
	}

	public Object getParent(Object element) {
		SFSBrowserTreeObject actObject = (SFSBrowserTreeObject) element;
		IPath path = actObject.getPath();
		if (path.segmentCount() > 0) {
			try {
				return new SFSBrowserTreeObject((IFileSystem) getFs(), path.removeLastSegments(1));
			} catch (CoreException e) {
				// $JL-EXC$ ignore here
				return null;
			}
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		ISemanticFileSystem fs;
		try {
			fs = getFs();
		} catch (CoreException e2) {
			return false;
		}
		try {
			if (getHide()) {
				IFileInfo[] infos = ((IFileSystem) fs).getStore(((SFSBrowserTreeObject) element).getPath()).childInfos(EFS.NONE, null);
				for (IFileInfo info : infos) {
					if (info.exists()) {
						return true;
					}
				}
				return false;
			}
			return ((IFileSystem) fs).getStore(((SFSBrowserTreeObject) element).getPath()).childInfos(EFS.NONE, null).length > 0;
		} catch (CoreException e) {
			try {
				getFs().getLog().log(e);
			} catch (CoreException e1) {
				// $JL-EXC$ ignore here
			}
			return false;
		}
	}

	public Object[] getElements(Object inputElement) {
		if (getHide()) {
			List<SFSBrowserTreeObject> resultList = new ArrayList<SFSBrowserTreeObject>();
			SFSBrowserTreeObject[] objects = (SFSBrowserTreeObject[]) inputElement;
			for (SFSBrowserTreeObject o : objects) {
				if (o.getInfo().exists()) {
					resultList.add(o);
				}
			}
			return resultList.toArray();
		}
		return (Object[]) inputElement;
	}

	public void dispose() {
		// nothing

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// nothing
	}

	/**
	 * @param value
	 */
	public void setHideNonExisting(boolean value) {
		this.hideNonExisting = value;
	}

	private boolean getHide() {
		return this.hideNonExisting;
	}

	private ISemanticFileSystem getFs() throws CoreException {
		if (this.myFs == null) {
			this.myFs = (ISemanticFileSystem) EFS.getFileSystem(ISemanticFileSystem.SCHEME);
		}
		return this.myFs;
	}

}
