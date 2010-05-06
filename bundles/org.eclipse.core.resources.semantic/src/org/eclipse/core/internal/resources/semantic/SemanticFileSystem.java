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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticDB;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBFactory;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeNodeType;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeRoot;
import org.eclipse.core.internal.resources.semantic.util.ISemanticFileSystemLog;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticURILocatorService;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderFederation;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

/**
 * The Semantic File System.
 * <p>
 * In order to access specific methods, this may be cast to
 * {@link ISemanticFileSystem}.
 * 
 */
public class SemanticFileSystem extends FileSystem implements ISemanticFileSystem {

	private static final String METADATA_FILENAME = "metadata.xmi"; //$NON-NLS-1$
	final static IPath EMPTY = new Path(""); //$NON-NLS-1$

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock readLock = this.rwl.readLock();
	private final Lock writeLock = this.rwl.writeLock();
	private final ISemanticFileSystemLog log;

	private SemanticDB db;
	Resource metadataResource;
	private SemanticURILocatorService uriLocator;
	private boolean isDelayedFlush = false;
	private boolean needsFlush = false;

	/**
	 * No-argument constructor
	 */
	public SemanticFileSystem() {

		this.log = new SemanticFileSystemLog();

		init();
	}

	public String[] getRootNames() throws CoreException {

		if (this.db == null) {
			throw new SemanticResourceException(SemanticResourceStatusCode.SFS_DB_NOT_INITIALIZED, SemanticFileSystem.EMPTY,
					Messages.SemanticFileSystem_NotInitialized_XMSG);
		}

		List<String> result = new ArrayList<String>();

		try {
			lockForRead();

			EList<TreeRoot> roots = this.db.getRoots();

			for (TreeRoot treeRoot : roots) {
				result.add(treeRoot.getName());
			}
		} finally {
			unlockForRead();
		}
		return result.toArray(new String[0]);
	}

	/**
	 * Requests a database update.
	 * 
	 * @param force
	 *            <code>true</code> for immediate update
	 * @throws CoreException
	 *             in case of failure
	 */
	public void requestFlush(boolean force) throws CoreException {
		this.needsFlush = true;

		if (!this.isDelayedFlush || force) {
			saveSemanticDB();
		}
	}

	@Override
	public IFileStore getStore(URI uri) {
		if (ISemanticFileSystem.SCHEME.equals(uri.getScheme())) {
			return getFileStoreImplementation(uri);
		}
		return EFS.getNullFileSystem().getStore(uri);
	}

	IFileStore getFileStoreImplementation(String pathString, String queryString) {
		IPath path;

		if (pathString != null) {
			path = new Path(null, pathString);
		} else {
			path = Path.EMPTY;
		}

		if (this.db == null) {
			return EFS.getNullFileSystem().getStore(path);
		}
		// we do never return the virtual root node ("semanticfs:/")
		if (path.segmentCount() > 0) {
			try {
				lockForWrite();

				EList<TreeRoot> roots = this.db.getRoots();

				for (TreeRoot treeRoot : roots) {
					if (path.segment(0).equals(treeRoot.getName())) {
						if (path.segmentCount() == 1) {
							return getStore(treeRoot);
						}
						return getFileStoreRecursive(path, treeRoot, queryString);
					}
				}
			} finally {
				unlockForWrite();
			}

			// no root found, create new one
			TreeRoot treeRoot;

			try {
				lockForWrite();
				if (path.segmentCount() == 1) {
					treeRoot = createRootNode(path.segment(0), queryString);
					return getStore(treeRoot);
				}
				treeRoot = createRootNode(path.segment(0));
			} finally {
				unlockForWrite();
			}

			try {
				lockForWrite();

				return getFileStoreRecursive(path, treeRoot, queryString);
			} finally {
				unlockForWrite();
			}
		}

		return EFS.getNullFileSystem().getStore(path);
	}

	private IFileStore getFileStoreImplementation(URI uri) {
		String pathString = uri.getPath();
		String queryString = uri.getQuery();
		return getFileStoreImplementation(pathString, queryString);
	}

	private IFileStore getFileStoreRecursive(IPath path, ResourceTreeNode rootNode, String queryString) {
		String[] segments = path.segments();
		ResourceTreeNode currentNode = rootNode;
		boolean ready = true;

		for (int i = 1; i < segments.length; i++) {
			String name = segments[i];
			EList<ResourceTreeNode> children = currentNode.getChildren();
			boolean found = false;

			for (ResourceTreeNode resourceTreeNode : children) {
				if (resourceTreeNode.getName().equals(name)) {
					currentNode = resourceTreeNode;
					found = true;
					break;
				}
			}
			if (!found) {
				ready = false;
				break;
			}
		}
		if (ready) {
			return getStore(currentNode);
		}

		ResourceTreeNode newNode = createNonExistingNode(path);

		if (applyQueryParameters(newNode, queryString)) {
			if (newNode.getType().equals(TreeNodeType.FOLDER) || newNode.getType().equals(TreeNodeType.PROJECT)) {
				ISemanticFileStore store = getStore(newNode);
				try {
					store.mkdir(EFS.NONE, null);
				} catch (CoreException e) {
					// ignore since no exceptions are allowed here
				}
				return store;
			}
		}

		return getStore(newNode);
	}

	ResourceTreeNode getParentNode(ResourceTreeNode childNode) {
		try {
			this.lockForRead();

			if (childNode.isExists()) {
				return childNode.getParent();
			}

			if (childNode.getPath() != null) {
				int idx = childNode.getPath().lastIndexOf("/"); //$NON-NLS-1$
				if (idx >= 0) {
					String parentPath = childNode.getPath().substring(0, idx);
					return this.getNodeByPath(parentPath);
				}
			}
		} finally {
			this.unlockForRead();
		}
		return null;
	}

	public List<ResourceTreeNode> getNodesByPath(String pathString) {
		ArrayList<ResourceTreeNode> nodes = new ArrayList<ResourceTreeNode>();
		IPath path = new Path(null, pathString);

		if (path.segmentCount() > 0) {
			try {
				lockForRead();

				if (this.db != null) {
					EList<TreeRoot> roots = this.db.getRoots();

					for (TreeRoot treeRoot : roots) {
						if (path.segment(0).equals(treeRoot.getName())) {
							nodes.add(treeRoot);

							if (path.segmentCount() == 1) {
								return nodes;
							}

							ResourceTreeNode currentNode = treeRoot;
							boolean withinExistingTree = true;

							for (int i = 1; i < path.segmentCount(); i++) {
								String name = path.segment(i);

								if (withinExistingTree) {
									EList<ResourceTreeNode> children = currentNode.getChildren();
									boolean found = false;
									for (ResourceTreeNode resourceTreeNode : children) {
										if (resourceTreeNode.getName().equals(name)) {
											currentNode = resourceTreeNode;
											found = true;
											break;
										}
									}

									if (!found) {
										currentNode = createNonExistingNode(path.removeLastSegments(path.segmentCount() - i - 1));
										withinExistingTree = false;
									}
								} else {
									currentNode = createNonExistingNode(path.removeLastSegments(path.segmentCount() - i - 1));
								}

								nodes.add(currentNode);
							}
							return nodes;
						}
					}
				}

				// no root found, create new node hierarchy outside the tree
				nodes.add(createRootNode(path.segment(0)));

				for (int i = 1; i < path.segmentCount(); i++) {
					nodes.add(createNonExistingNode(path.removeLastSegments(path.segmentCount() - i - 1)));
				}
			} finally {
				unlockForRead();
			}
		}

		return nodes;
	}

	ResourceTreeNode getNodeByPath(String pathString) {
		IPath path = new Path(null, pathString);

		// we do never return the virtual root node ("semanticfs:/")
		if (path.segmentCount() > 0) {
			try {
				lockForRead();

				if (this.db != null) {
					EList<TreeRoot> roots = this.db.getRoots();

					for (TreeRoot treeRoot : roots) {
						if (path.segment(0).equals(treeRoot.getName())) {
							if (path.segmentCount() == 1) {
								return treeRoot;
							}

							String[] segments = path.segments();
							ResourceTreeNode currentNode = treeRoot;
							boolean ready = true;

							for (int i = 1; i < segments.length; i++) {
								String name = segments[i];
								EList<ResourceTreeNode> children = currentNode.getChildren();
								boolean found = false;

								for (ResourceTreeNode resourceTreeNode : children) {
									if (resourceTreeNode.getName().equals(name)) {
										currentNode = resourceTreeNode;
										found = true;
										break;
									}
								}
								if (!found) {
									ready = false;
									break;
								}
							}
							if (ready) {
								return currentNode;
							}
							return createNonExistingNode(path);
						}
					}
				}
			} finally {
				unlockForRead();
			}

			// no root found, create new node outside the tree
			if (path.segmentCount() == 1) {
				return createRootNode(path.segment(0));
			}

			return createNonExistingNode(path);
		}

		return null;
	}

	ISemanticContentProvider mkdir(ResourceTreeNode actNode, ResourceTreeNode parent) throws CoreException {
		ISemanticContentProvider parentProvider = null;

		if (parent != null && !parent.isExists()) {
			ResourceTreeNode parentParent = this.getParentNode(parent);
			parentProvider = mkdir(parent, parentParent);
		} else if (parent != null) { // and exists
			parentProvider = this.getStore(parent).getEffectiveContentProvider();
		} else { // parent == null
			parentProvider = this.getStore(actNode).getEffectiveContentProvider();
		}

		return makeFolder(actNode, parent, parentProvider);
	}

	ISemanticContentProvider makeFolder(ResourceTreeNode actNode, ResourceTreeNode parent, ISemanticContentProvider parentProvider)
			throws CoreException {
		ISemanticContentProvider effectiveProvider = parentProvider;
		String federatedContentProviderId = null;

		if (actNode.getType() == TreeNodeType.FILE) {
			// wrong type encountered
			throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_WITH_OTHER_TYPE_EXISTS, this.getStore(actNode)
					.getPath(), Messages.SemanticFileStore_MkDirOnFile_XMSG);
		}

		if (!actNode.isExists() && actNode.getTemplateID() == null) {
			if (parentProvider instanceof ISemanticContentProviderFederation) {
				federatedContentProviderId = ((ISemanticContentProviderFederation) parentProvider).getFederatedProviderIDForPath(new Path(
						actNode.getPath()));
			}

			actNode.setTemplateID(federatedContentProviderId);

		}

		// TODO 0.2: this should be intercepted by the content provider to
		// avoid unwanted folder creation
		this.switchToExists(actNode, parent);

		if (actNode.getType() != TreeNodeType.PROJECT) {
			// set type folder if it is not already a project
			actNode.setType(TreeNodeType.FOLDER);
		}

		if (actNode.getTemplateID() != null) {
			effectiveProvider = this.getStore(actNode).getEffectiveContentProvider();
		}
		return effectiveProvider;
	}

	void switchToExists(ResourceTreeNode node, ResourceTreeNode parent) {
		if (!node.isExists()) {
			if (node instanceof TreeRoot) {
				((TreeRoot) node).setParentDB(this.db);
			} else {

				if (parent != null) {
					node.setParent(parent);
				}
			}

			node.setPath(null);
			node.setExists(true);
		}
	}

	private ResourceTreeNode createNonExistingNode(IPath childPath) {
		// all callers have a lock, so we don't lock here
		ResourceTreeNode child = SemanticResourceDBFactory.eINSTANCE.createResourceTreeNode();

		child.setName(childPath.lastSegment());
		child.setType(TreeNodeType.UNKNOWN);
		child.setExists(false);
		child.setPath(childPath.toString());
		child.setLocalOnly(true);

		return child;
	}

	public IPath getPathForNode(ResourceTreeNode node) {
		try {
			lockForRead();
			if (node.isExists()) {
				StringBuilder sb = new StringBuilder(50);
				sb.append('/');
				sb.append(node.getName());
				ResourceTreeNode parent = node.getParent();
				while (parent != null) {
					sb.insert(0, parent.getName());
					sb.insert(0, '/');
					parent = parent.getParent();
				}
				return new Path(sb.toString());
			}
			if (node.getPath() != null) {
				return new Path(node.getPath());
			}

			return EMPTY;
		} finally {
			unlockForRead();
		}
	}

	protected ISemanticFileStore getStore(ResourceTreeNode node) {
		return new SemanticFileStore(this, node);
	}

	private void init() {

		File metadataFolder = SemanticResourcesPlugin.getCacheLocation().toFile();
		metadataFolder.mkdirs();

		File metadataFile = new File(metadataFolder, SemanticFileSystem.METADATA_FILENAME);

		String metadataLocation = metadataFile.getAbsolutePath();

		if (!metadataFile.exists()) {
			initSemanticDB(metadataLocation);

		} else {
			loadSemanticDB(metadataLocation);
		}
	}

	private void loadSemanticDB(String metadataLocation) {
		try {
			SemanticResourceDBPackage pkg = SemanticResourceDBPackage.eINSTANCE;
			pkg.eClass();

			try {
				this.lockForWrite();

				ResourceSet resourceset = new ResourceSetImpl();

				org.eclipse.emf.common.util.URI uri = org.eclipse.emf.common.util.URI.createFileURI(metadataLocation);

				// Register the appropriate resource factory to handle all file
				// extensions that would cover XMI as well.
				resourceset.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION,
						new XMIResourceFactoryImpl());

				this.metadataResource = resourceset.createResource(uri);

				this.metadataResource.load(null);

				EList<EObject> contents = this.metadataResource.getContents();

				for (EObject eObject : contents) {
					if (eObject instanceof SemanticDB) {
						this.db = (SemanticDB) eObject;
						this.migrateSemanticDB();
						break;
					}
				}
			} finally {
				this.unlockForWrite();
			}
			this.needsFlush = false;
		} catch (IOException e) {
			for (Diagnostic diagnostic : this.metadataResource.getErrors()) {
				this.log.log(new SemanticResourceException(SemanticResourceStatusCode.SFS_INITIALIZATION_ERROR, SemanticFileSystem.EMPTY,
						diagnostic.getMessage()));
			}
			this.log.log(new SemanticResourceException(SemanticResourceStatusCode.SFS_INITIALIZATION_ERROR, SemanticFileSystem.EMPTY,
					Messages.SemanticFileSystem_SFSInitError_XMSG, e));

		}
	}

	private void migrateSemanticDB() {
		TreeIterator<EObject> objects = this.db.eAllContents();
		ArrayList<ResourceTreeNode> toBeRemoved = new ArrayList<ResourceTreeNode>();

		try {
			while (objects.hasNext()) {
				EObject eObject = objects.next();
				if (eObject instanceof TreeRoot) {
					TreeRoot root = (TreeRoot) eObject;

					if (!root.isExists()) {
						toBeRemoved.add(root);
					}
				} else if (eObject instanceof ResourceTreeNode) {
					ResourceTreeNode node = (ResourceTreeNode) eObject;

					if (!node.isExists()) {
						toBeRemoved.add(node);
					}
				}
			}

			for (ResourceTreeNode resourceTreeNode : toBeRemoved) {
				if (resourceTreeNode instanceof TreeRoot) {
					((TreeRoot) resourceTreeNode).setParentDB(null);
				} else {
					resourceTreeNode.setParent(null);
				}
			}
		} catch (Throwable e) {
			this.db = null;
			this.log.log(new SemanticResourceException(SemanticResourceStatusCode.SFS_INITIALIZATION_ERROR, SemanticFileSystem.EMPTY,
					Messages.SemanticFileSystem_SFSInitError_XMSG, e));
		}

	}

	private void initSemanticDB(String metadataLocation) {
		try {
			this.lockForWrite();
			ResourceSet resourceset = new ResourceSetImpl();

			org.eclipse.emf.common.util.URI uri = org.eclipse.emf.common.util.URI.createFileURI(metadataLocation);

			this.metadataResource = resourceset.createResource(uri);

			this.db = SemanticResourceDBFactory.eINSTANCE.createSemanticDB();

			this.metadataResource.getContents().add(this.db);

			this.needsFlush = true;
		} finally {
			this.unlockForWrite();
		}
	}

	private TreeRoot createRootNode(String name) {
		TreeRoot root = SemanticResourceDBFactory.eINSTANCE.createTreeRoot();

		root.setName(name);
		root.setExists(false);
		root.setPath("/" + name); //$NON-NLS-1$
		root.setType(TreeNodeType.PROJECT);
		// root.setParentDB(this.db);

		return root;
	}

	private TreeRoot createRootNode(String name, String queryString) {
		TreeRoot root = SemanticResourceDBFactory.eINSTANCE.createTreeRoot();

		root.setName(name);
		root.setQueryPart(queryString);
		root.setExists(false);
		root.setPath("/" + name); //$NON-NLS-1$
		root.setType(TreeNodeType.PROJECT);

		if (applyQueryParameters(root, queryString)) {
			root.setExists(true);
			root.setPath(null);
			root.setParentDB(this.db);
		}
		return root;
	}

	private boolean applyQueryParameters(ResourceTreeNode node, String queryString) {
		boolean createRequested = false;

		if (queryString != null) {
			SemanticQueryParser parser = new SemanticQueryParser(queryString);

			node.setTemplateID(parser.getProviderID());
			node.setType(parser.getType());
			node.setRemoteURI(parser.getURI());
			createRequested = parser.getShouldCreate();
		}
		return createRequested;
	}

	private void saveSemanticDB() throws CoreException {
		try {
			if (this.needsFlush) {
				if (SfsTraceLocation.CORE_DB.isActive()) {
					SfsTraceLocation.getTrace().traceEntry(SfsTraceLocation.CORE_DB.getLocation());
				}
				try {
					this.lockForWrite();
					this.metadataResource.save(null);
					this.needsFlush = false;
				} finally {
					this.unlockForWrite();
				}
			}
		} catch (IOException e) {
			SemanticResourceException ex = new SemanticResourceException(SemanticResourceStatusCode.SFS_ERROR_WRITING_METADATA,
					SemanticFileSystem.EMPTY, Messages.SemanticFileSystem_SFSUpdateError_XMSG, e);
			throw ex;

		}
	}

	protected void lockForRead() {
		this.readLock.lock();
	}

	protected void unlockForRead() {
		this.readLock.unlock();
	}

	protected void lockForWrite() {
		this.writeLock.lock();
	}

	protected void unlockForWrite() {
		this.writeLock.unlock();
	}

	public String getPathToDb() {

		File metadataFolder = SemanticResourcesPlugin.getCacheLocation().toFile();
		metadataFolder.mkdirs();

		File metadataFile = new File(metadataFolder, SemanticFileSystem.METADATA_FILENAME);

		String metadataLocation = metadataFile.getAbsolutePath();

		return metadataLocation;
	}

	public ISemanticFileSystemLog getLog() {
		return this.log;
	}

	static final class SemanticURILocatorService implements ISemanticURILocatorService {
		private HashMap<String, ArrayList<IPath>> uri2pathMapping = new HashMap<String, ArrayList<IPath>>();
		final SemanticFileSystem fs;
		boolean needsRebuild;

		/**
		 * @param fs
		 * 
		 */
		public SemanticURILocatorService(SemanticFileSystem fs) {
			this.fs = fs;
		}

		/**
		 * @throws CoreException
		 */
		public IPath[] locateURI(URI uri) throws CoreException {
			try {
				this.fs.lockForWrite();

				if (this.needsRebuild) {
					this.rebuildMapping(null);
				}

				ArrayList<IPath> paths = this.uri2pathMapping.get(uri.toString());
				if (paths != null) {
					return paths.toArray(new IPath[paths.size()]);
				}
				return new IPath[0];
			} finally {
				this.fs.unlockForWrite();
			}
		}

		/**
		 * @throws CoreException
		 */
		public IPath[] locateURI(URI uri, IPath rootpath) throws CoreException {
			try {
				this.fs.lockForWrite();

				if (this.needsRebuild) {
					this.rebuildMapping(null);
				}

				ArrayList<IPath> paths = this.uri2pathMapping.get(uri.toString());
				if (paths != null) {
					ArrayList<IPath> filteredpaths = new ArrayList<IPath>();

					for (IPath iPath : paths) {
						if (rootpath.isPrefixOf(iPath)) {
							filteredpaths.add(iPath);
						}
					}

					return filteredpaths.toArray(new IPath[filteredpaths.size()]);
				}
				return new IPath[0];
			} finally {
				this.fs.unlockForWrite();
			}
		}

		public IPath getPathForNode(ResourceTreeNode node) {
			return this.fs.getPathForNode(node);
		}

		public void rebuildMapping(IProgressMonitor monitor) {
			this.uri2pathMapping.clear();

			TreeIterator<EObject> contents = this.fs.metadataResource.getAllContents();

			while (contents.hasNext()) {
				EObject eObject = contents.next();
				if (eObject instanceof ResourceTreeNode) {
					ResourceTreeNode node = (ResourceTreeNode) eObject;

					String uriString = node.getRemoteURI();

					if (uriString != null) {
						addURI(getPathForNode(node), uriString);
					}
				}
			}
			this.needsRebuild = false;
		}

		public void addURI(IPath path, String uriString) {
			ArrayList<IPath> paths = this.uri2pathMapping.get(uriString);

			if (paths != null) {
				paths.add(path);
			} else {
				paths = new ArrayList<IPath>();

				paths.add(path);
				this.uri2pathMapping.put(uriString, paths);
			}
		}

		public void removeURI(IPath path, String uriString) {
			ArrayList<IPath> paths = this.uri2pathMapping.get(uriString);

			paths.remove(path);

			if (paths.isEmpty()) {
				this.uri2pathMapping.remove(uriString);
			}
		}

		public void requestRebuild() {
			this.needsRebuild = true;
		}

	}

	/**
	 * @throws CoreException
	 */
	public ISemanticURILocatorService getURILocatorService(IProgressMonitor monitor) throws CoreException {
		try {
			this.lockForWrite();
			if (this.uriLocator == null) {
				this.uriLocator = new SemanticURILocatorService(this);

				this.uriLocator.rebuildMapping(monitor);
			}
			return this.uriLocator;
		} finally {
			this.unlockForWrite();
		}
	}

	/**
	 * 
	 */
	public void requestURILocatorRebuild() {
		if (this.uriLocator != null) {
			this.uriLocator.requestRebuild();
		}
	}

	public SemanticURILocatorService getURILocator() {
		return this.uriLocator;
	}
}
