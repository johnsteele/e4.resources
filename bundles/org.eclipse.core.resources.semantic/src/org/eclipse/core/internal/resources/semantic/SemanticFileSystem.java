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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
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

	protected static final QualifiedName ATTRIBUTE_URI = new QualifiedName(SemanticResourcesPlugin.PLUGIN_ID, "URIString"); //$NON-NLS-1$ 

	private static final String METADATA_FILENAME = "metadata.xmi"; //$NON-NLS-1$
	private final static IPath EMPTY = new Path(""); //$NON-NLS-1$

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

	public IFileStore getStore(URI uri) {
		if (ISemanticFileSystem.SCHEME.equals(uri.getScheme())) {
			return getFileStoreImplementation(uri);
		}
		return EFS.getNullFileSystem().getStore(uri);
	}

	private IFileStore getFileStoreImplementation(URI uri) {

		String pathString = uri.getPath();
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
				lockForRead();

				EList<TreeRoot> roots = this.db.getRoots();

				for (TreeRoot treeRoot : roots) {
					if (path.segment(0).equals(treeRoot.getName())) {
						if (path.segmentCount() == 1) {
							return getStore(treeRoot);
						}
						return getFileStoreRecursive(path, treeRoot);
					}
				}
			} finally {
				unlockForRead();
			}

			// no root found, create new one
			TreeRoot treeRoot;

			try {
				lockForWrite();
				treeRoot = createRootNode(path.segment(0));
			} finally {
				unlockForWrite();
			}

			try {
				lockForRead();

				if (path.segmentCount() == 1) {
					return getStore(treeRoot);
				}
				return getFileStoreRecursive(path, treeRoot);
			} finally {
				unlockForRead();
			}
		}

		return EFS.getNullFileSystem().getStore(path);
	}

	private IFileStore getFileStoreRecursive(IPath path, ResourceTreeNode rootNode) {
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
		return EFS.getNullFileSystem().getStore(path);
	}

	protected SemanticFileStore getStore(ResourceTreeNode node) {
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
		root.setType(TreeNodeType.PROJECT);
		root.setParentDB(this.db);

		return root;
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

	class SemanticURILocatorService implements ISemanticURILocatorService {
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

		public void rebuildMapping(IProgressMonitor monitor) {
			this.uri2pathMapping.clear();

			String keyString = Util.qualifiedNameToString(ATTRIBUTE_URI);

			TreeIterator<EObject> contents = this.fs.metadataResource.getAllContents();

			while (contents.hasNext()) {
				EObject eObject = contents.next();
				if (eObject instanceof ResourceTreeNode) {
					ResourceTreeNode node = (ResourceTreeNode) eObject;

					if (node.getPersistentProperties() != null) {
						String uriString = node.getPersistentProperties().get(keyString);

						if (uriString != null) {
							addURI(getPathForNode(node), uriString);
						}
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
