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

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;

/**
 * The {@link ISemanticFolder} implementation.
 * 
 */
public class SemanticFolderAdapterImpl extends SemanticResourceAdapterImpl implements ISemanticFolder {
	private final IContainer container;

	SemanticFolderAdapterImpl(IContainer container, ISemanticFileSystem fileSystem) {
		super(container, fileSystem);
		this.container = container;
	}

	public ISemanticResource getResource(String name) throws CoreException {
		ISemanticFileStoreInternal store = getOwnStore();

		ISemanticFileStoreInternal childStore = store.getChildResource(name);

		return wrapChildWithResource(name, childStore);
	}

	public ISemanticResource addResource(String name, int options, IProgressMonitor monitor) throws CoreException {

		checkCurrentRule(RuleType.REFRESH);

		ISemanticFileStoreInternal store = getOwnStore();

		ISemanticFileStoreInternal childStore = store.addResourceFromRemote(name, monitor);

		refreshLocalIfNeeded(RuleType.MODIFY, getRuleForType(RuleType.MODIFY, this.container), options, monitor);

		return wrapChildWithResource(name, childStore);
	}

	public ISemanticFile addFile(String name, int options, IProgressMonitor monitor) throws CoreException {

		validateName(name, IResource.FILE);

		checkCurrentRule(RuleType.REFRESH);

		ISemanticFileStoreInternal store = getOwnStore();

		store.addFileFromRemote(name, monitor);

		refreshLocalIfNeeded(RuleType.MODIFY, getRuleForType(RuleType.MODIFY, this.container), options, monitor);

		return wrapChildWithFile(name);
	}

	public ISemanticFolder addFolder(String name, int options, IProgressMonitor monitor) throws CoreException {

		validateName(name, IResource.FOLDER);

		checkCurrentRule(RuleType.REFRESH);

		ISemanticFileStoreInternal store = getOwnStore();

		store.addFolderFromRemote(name, monitor);

		refreshLocalIfNeeded(RuleType.MODIFY, getRuleForType(RuleType.MODIFY, this.container), options, monitor);

		return wrapChildWithFolder(name);
	}

	public ISemanticFile addFile(String name, URI uri, int options, IProgressMonitor monitor) throws CoreException {

		validateName(name, IResource.FILE);

		checkCurrentRule(RuleType.REFRESH);

		ISemanticFileStoreInternal store = getOwnStore();

		store.addFileFromRemoteByURI(name, uri, monitor);

		refreshLocalIfNeeded(RuleType.MODIFY, getRuleForType(RuleType.MODIFY, this.container), options, monitor);

		return wrapChildWithFile(name);
	}

	public ISemanticFolder addFolder(String name, URI uri, int options, IProgressMonitor monitor) throws CoreException {

		validateName(name, IResource.FOLDER);

		checkCurrentRule(RuleType.REFRESH);

		ISemanticFileStoreInternal store = getOwnStore();

		store.addFolderFromRemoteByURI(name, uri, monitor);

		refreshLocalIfNeeded(RuleType.MODIFY, getRuleForType(RuleType.MODIFY, this.container), options, monitor);

		return wrapChildWithFolder(name);
	}

	public ISemanticFolder addFolder(String name, String contentProviderID, Map<QualifiedName, String> properties, int options,
			IProgressMonitor monitor) throws CoreException {

		validateName(name, IResource.FOLDER);

		checkCurrentRule(RuleType.REFRESH);

		ISemanticFileStoreInternal store = getOwnStore();

		store.addResource(name, true, contentProviderID, properties, monitor);

		refreshLocalIfNeeded(RuleType.MODIFY, getRuleForType(RuleType.MODIFY, this.container), options, monitor);

		return wrapChildWithFolder(name);
	}

	public ISemanticFile addFile(String name, String contentProviderID, Map<QualifiedName, String> properties, int options,
			IProgressMonitor monitor) throws CoreException {

		validateName(name, IResource.FOLDER);

		checkCurrentRule(RuleType.REFRESH);

		ISemanticFileStoreInternal store = getOwnStore();

		store.addResource(name, false, contentProviderID, properties, monitor);

		refreshLocalIfNeeded(RuleType.MODIFY, getRuleForType(RuleType.MODIFY, this.container), options, monitor);

		return wrapChildWithFile(name);
	}

	public boolean hasResource(String name) throws CoreException {
		ISemanticFileStoreInternal store = getOwnStore();

		return store.hasResource(name);
	}

	public IContainer getAdaptedContainer() {
		return this.container;
	}

	public ISemanticFile createFileRemotely(String name, InputStream source, Object context, int options, IProgressMonitor monitor)
			throws CoreException {

		validateName(name, IResource.FILE);

		checkCurrentRule(RuleType.REFRESH);

		ISemanticFileStoreInternal store = getOwnStore();

		store.createFileRemotely(name, source, context, monitor);

		refreshLocalIfNeeded(RuleType.MODIFY, getRuleForType(RuleType.MODIFY, this.container), options, monitor);

		return wrapChildWithFile(name);
	}

	public ISemanticResource createResourceRemotely(String name, Object context, int options, IProgressMonitor monitor)
			throws CoreException {

		checkCurrentRule(RuleType.REFRESH);

		ISemanticFileStoreInternal store = getOwnStore();

		ISemanticFileStoreInternal childStore = store.createResourceRemotely(name, context, monitor);

		refreshLocalIfNeeded(RuleType.MODIFY, getRuleForType(RuleType.MODIFY, this.container), options, monitor);

		return wrapChildWithResource(name, childStore);
	}

	public IStatus validateRemoteCreate(String name, Object shell) {

		try {

			ISemanticFileStoreInternal store = getOwnStore();

			return store.validateRemoteCreate(name, shell);
		} catch (CoreException e) {
			if (SfsTraceLocation.CORE.isActive()) {
				SfsTraceLocation.getTrace().trace(SfsTraceLocation.CORE.getLocation(), e.getMessage(), e);
			}
			return e.getStatus();
		}

	}

	/**
	 * @param name
	 * @param childStore
	 * @return resource
	 */
	private ISemanticResource wrapChildWithResource(String name, ISemanticFileStoreInternal childStore) {
		IResource resource = null;

		if (childStore != null) {
			if (childStore.getType() == ISemanticFileStore.FILE) {
				resource = this.container.getFile(new Path(name));
			} else {
				resource = this.container.getFolder(new Path(name));
			}
		}

		if (resource != null) {
			ISemanticResource child = (ISemanticResource) resource.getAdapter(ISemanticResource.class);
			return child;
		}
		return null;
	}

	private ISemanticFile wrapChildWithFile(String name) {
		IResource resource;

		resource = this.container.getFile(new Path(name));

		if (resource != null) {
			ISemanticFile child = (ISemanticFile) resource.getAdapter(ISemanticFile.class);
			return child;
		}
		return null;
	}

	private ISemanticFolder wrapChildWithFolder(String name) {
		IResource resource;

		resource = this.container.getFolder(new Path(name));

		if (resource != null) {
			ISemanticFolder child = (ISemanticFolder) resource.getAdapter(ISemanticFolder.class);
			return child;
		}
		return null;
	}

	private void validateName(String name, int type) throws CoreException {
		IStatus test = this.container.getWorkspace().validateName(name, type);
		if (!test.isOK()) {
			throw new SemanticResourceException(SemanticResourceStatusCode.INVALID_RESOURCE_NAME, this.container.getFullPath(), test
					.getMessage());
		}

	}

	public IResource[] findURI(URI uri, IProgressMonitor monitor) throws CoreException {
		ISemanticFileStoreInternal store = getOwnStore();

		ArrayList<IResource> resources = new ArrayList<IResource>();
		IPath[] paths = store.findURI(uri, monitor);
		IPath root = ((ISemanticFileStore) store).getPath();

		for (IPath iPath : paths) {
			if (root.isPrefixOf(iPath)) {
				IResource res = this.container.findMember(makeRelativeTo(root, iPath));
				if (res != null) {
					resources.add(res);
				}
			}
		}

		return resources.toArray(new IResource[resources.size()]);
	}

	// compatibility with 3.4
	private IPath makeRelativeTo(IPath base, IPath other) {
		String baseString = base.toString();
		String otherString = other.toString();

		if (baseString.length() == otherString.length()) {
			return Path.EMPTY;
		}

		if (base.hasTrailingSeparator()) {
			return new Path(otherString.substring(baseString.length()));
		}

		// skip slash to make relative path
		return new Path(otherString.substring(baseString.length() + 1));
	}

}
