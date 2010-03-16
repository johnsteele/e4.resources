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
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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

	private IPath getPathForTrace() {

		try {
			this.fs.lockForRead();

			StringBuilder sb = new StringBuilder(50);
			sb.append('/');
			sb.append(this.node.getName());
			ResourceTreeNode parent = this.node.getParent();
			while (parent != null) {
				sb.insert(0, parent.getName());
				sb.insert(0, '/');
				parent = parent.getParent();
			}
			return new Path(sb.toString());
		} catch (RuntimeException rte) {
			// $JL-EXC$
			return new Path(this.node.getName());

		} finally {
			this.fs.unlockForRead();
		}

	}

	/**
	 * @throws CoreException
	 */
	public Map<QualifiedName, String> getPersistentProperties() throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceEntry(SfsTraceLocation.CORE_VERBOSE.getLocation(), getPathForTrace().toString());
		}

		checkAccessible();

		Map<QualifiedName, String> result = new HashMap<QualifiedName, String>();
		try {
			this.fs.lockForRead();
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
			if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.CORE_VERBOSE.getLocation(), result);
			}
		}

	}

	/**
	 * @throws CoreException
	 */
	public String getPersistentProperty(QualifiedName key) throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceEntry(SfsTraceLocation.CORE_VERBOSE.getLocation(),
					new Object[] {getPathForTrace().toString(), key});
		}

		String result = null;
		try {
			this.fs.lockForRead();
			HashMap<String, String> nodeProps = this.node.getPersistentProperties();
			if (nodeProps == null) {
				return result;
			}
			String keyString = Util.qualifiedNameToString(key);
			result = nodeProps.get(keyString);

			return result;
		} finally {
			this.fs.unlockForRead();
			if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.CORE_VERBOSE.getLocation(), result);
			}
		}

	}

	public void setPersistentProperty(QualifiedName key, String value) throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceEntry(SfsTraceLocation.CORE_VERBOSE.getLocation(), getPathForTrace().toString());
		}

		checkAccessible();

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

	/**
	 * @throws CoreException
	 */
	public void setSessionProperty(QualifiedName key, Object value) throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceEntry(SfsTraceLocation.CORE_VERBOSE.getLocation(), getPathForTrace().toString());
		}

		checkAccessible();

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

	/**
	 * @throws CoreException
	 */
	public Map<QualifiedName, Object> getSessionProperties() throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceEntry(SfsTraceLocation.CORE_VERBOSE.getLocation(), getPathForTrace().toString());
		}

		checkAccessible();

		Map<QualifiedName, Object> result = new HashMap<QualifiedName, Object>();

		try {
			this.fs.lockForRead();
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
			if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.CORE_VERBOSE.getLocation(), result);
			}
		}
	}

	/**
	 * @throws CoreException
	 */
	public Object getSessionProperty(QualifiedName key) throws CoreException {

		if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
			SfsTraceLocation.getTrace().traceEntry(SfsTraceLocation.CORE_VERBOSE.getLocation(),
					new Object[] {getPathForTrace().toString(), key});
		}

		Object result = null;
		try {
			this.fs.lockForRead();
			HashMap<QualifiedName, Object> nodeProps = this.node.getSessionProperties();
			if (nodeProps == null) {
				return result;
			}
			result = nodeProps.get(key);
			return result;
		} finally {
			this.fs.unlockForRead();
			if (SfsTraceLocation.CORE_VERBOSE.isActive()) {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.CORE_VERBOSE.getLocation(), result);
			}
		}
	}

	/**
	 * Similar to
	 * <code>org.eclipse.core.internal.resources.Resource#checkAccessible(int)</code>
	 * <p>
	 * The corresponding Resource method is called on:
	 * <ul>
	 * <li>Container.memebers()</li>
	 * <li>Container.setDefaultCharset()</li>
	 * <li>File.appendContents()</li>
	 * <li>File.create() (on the parent)</li>
	 * <li>File.getContentDescription()</li>
	 * <li>File.getContents()</li>
	 * <li>File.getEncoding()</li>
	 * <li>File.setCharset()</li>
	 * <li>File.setContents()</li>
	 * <li>Folder.create() (on the parent)</li>
	 * <li>Project.checkAccessible()</li>
	 * <li>Project.getDescription()</li>
	 * <li>Project.getNature()</li>
	 * <li>Project.getReferencedProjects()</li>
	 * <li>Project.hasNature</li>
	 * <li>Project.isNatureEnabled()</li>
	 * <li>Project.setDescription()</li>
	 * <li>Resource.accept()</li>
	 * <li>Resource.createLink()</li>
	 * <li>Resource.copy()</li>
	 * <li>Resource.move()</li>
	 * <li>Resource.createMarker()</li>
	 * <li>Resource.deleteMarkers()</li>
	 * <li>Resource.findMarkers()</li>
	 * <li>Resource.findMaxProblemSeverity()</li>
	 * <li>Resource.setDerived()</li>
	 * <li>Resource.setHidden()</li>
	 * <li>Resource.setTeamPrivateMember()</li>
	 * </ul>
	 * 
	 * @throws CoreException
	 */
	protected void checkAccessible() throws CoreException {

		if (!node.isExists()) {
			throw new SemanticResourceException(SemanticResourceStatusCode.PROJECT_NOT_ACCESSIBLE, getPathForTrace(),
					Messages.SemanticProperties_StoreNotAccessible_XMSG);
		}

	}
}
