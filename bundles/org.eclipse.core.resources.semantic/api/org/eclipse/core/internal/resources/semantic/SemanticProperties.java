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
package org.eclipse.core.internal.resources.semantic;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode;
import org.eclipse.core.resources.semantic.ISemanticProperties;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

/**
 * The {@link ISemanticProperties} implementation.
 * 
 */
public abstract class SemanticProperties extends FileStore implements ISemanticProperties {

	protected final ResourceTreeNode node;
	protected final SemanticFileSystem fs;

	SemanticProperties(SemanticFileSystem fs, ResourceTreeNode node) {
		this.fs = fs;
		this.node = node;
	}

	public Map<QualifiedName, String> getPersistentProperties() throws CoreException {

		try {
			this.fs.lockForRead();
			Map<QualifiedName, String> result = new HashMap<QualifiedName, String>();
			HashMap<String, String> atts = this.node.getPersistentProperties();
			if (atts == null) {
				return result;
			}
			for (Map.Entry<String, String> entry : atts.entrySet()) {
				String qualifier;
				String localName;
				int index = entry.getKey().indexOf('^');
				if (index > 0) {
					qualifier = entry.getKey().substring(0, index);
					localName = entry.getKey().substring(index + 1);
				} else {
					qualifier = null;
					localName = entry.getKey();
				}
				result.put(new QualifiedName(qualifier, localName), entry.getValue());
			}
			return result;
		} finally {
			this.fs.unlockForRead();
		}

	}

	public String getPersistentProperty(QualifiedName key) throws CoreException {

		try {
			this.fs.lockForRead();
			HashMap<String, String> nodeProps = this.node.getPersistentProperties();
			if (nodeProps == null) {
				return null;
			}
			String keyString = Util.qualifiedNameToString(key);
			return nodeProps.get(keyString);
		} finally {
			this.fs.unlockForRead();
		}

	}

	public void setPersistentProperty(QualifiedName key, String value) throws CoreException {

		Util.assertQualifiedNameValid(key);

		String keyString = Util.qualifiedNameToString(key);
		try {
			this.fs.lockForWrite();

			String oldValue = null;

			HashMap<String, String> map = this.node.getPersistentProperties();
			if (map == null) {
				map = new HashMap<String, String>();
			} else {
				oldValue = map.get(keyString);
			}
			if (value != null) {
				map.put(keyString, value);
			} else {
				map.remove(keyString);
			}
			this.node.setPersistentProperties(map);

			this.notifyPersistentPropertySet(keyString, oldValue, value);

			this.fs.requestFlush(false);
		} finally {
			this.fs.unlockForWrite();
		}

	}

	protected abstract void notifyPersistentPropertySet(String keyString, String oldValue, String newValue) throws CoreException;

	public void setSessionProperty(QualifiedName key, Object value) throws CoreException {

		Util.assertQualifiedNameValid(key);

		try {
			this.fs.lockForWrite();

			HashMap<QualifiedName, Object> map = this.node.getSessionProperties();
			if (map == null) {
				map = new HashMap<QualifiedName, Object>();
				this.node.setSessionProperties(map);
			}
			if (value != null) {
				map.put(key, value);
			} else {
				map.remove(key);
			}

		} finally {
			this.fs.unlockForWrite();
		}

	}

	public Map<QualifiedName, Object> getSessionProperties() throws CoreException {

		try {
			this.fs.lockForRead();
			Map<QualifiedName, Object> result = new HashMap<QualifiedName, Object>();

			HashMap<QualifiedName, Object> atts = this.node.getSessionProperties();
			if (atts == null) {
				return result;
			}
			// we copy the map here (QualifiedName is immutable)
			for (Map.Entry<QualifiedName, Object> entry : atts.entrySet()) {
				result.put(entry.getKey(), entry.getValue());
			}
			return result;
		} finally {
			this.fs.unlockForRead();
		}
	}

	public Object getSessionProperty(QualifiedName key) throws CoreException {

		try {
			this.fs.lockForRead();
			HashMap<QualifiedName, Object> nodeProps = this.node.getSessionProperties();
			if (nodeProps == null) {
				return null;
			}
			return nodeProps.get(key);
		} finally {
			this.fs.unlockForRead();
		}
	}

}
