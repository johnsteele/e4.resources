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
package org.eclipse.core.resources.semantic.examples.providers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.resources.semantic.examples.RESTUtil;
import org.eclipse.core.resources.semantic.examples.SemanticResourcesPluginExamples;
import org.eclipse.core.resources.semantic.examples.RESTUtil.IRESTCallback;
import org.eclipse.core.resources.semantic.spi.CachingContentProvider;
import org.eclipse.core.resources.semantic.spi.FileCacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.ICacheServiceFactory;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProviderREST;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.ISemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.SemanticSpiResourceInfo;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore.ResourceType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * This class shows how to work with composite resources (master resource and
 * some satellite resources) that should always be treated as a whole, e.g.
 * deletion of the master resource should delete the satellites,
 * checkout/check-in should affect all of them and so on)
 * <p>
 * A good example is provided by Java ResourceBundle concept with master class
 * and satellite classes for different locales.
 * <p>
 * This content provider follows the Java naming convention for resource bundles
 * to determine files that should be treated as a composite resource, e.g.
 * following resources are treated as part of composite resource:
 * <ul>
 * <li>MyResource.test</li>
 * <li>MyResource_de.test</li>
 * <li>MyResource_en.test</li>
 * <li>MyResource_en_US.test</li>
 * </ul>
 */
public class SampleCompositeResourceContentProvider extends CachingContentProvider implements ISemanticContentProviderREST {

	/**
	 * A visitor
	 */
	public interface IVisitor {
		/**
		 * @param store
		 *            the store
		 * @throws CoreException
		 *             upon failure
		 */
		public void visit(ISemanticFileStore store) throws CoreException;
	}

	private void visitCompositeParts(ISemanticFileStore store, IVisitor visitor, IProgressMonitor monitor) throws CoreException {
		ISemanticFileStore parent = (ISemanticFileStore) store.getParent();
		String filename;
		String fileext;

		if (store.getName().contains(".")) { //$NON-NLS-1$
			filename = store.getName().substring(0, store.getName().lastIndexOf(".")); //$NON-NLS-1$
			fileext = store.getName().substring(store.getName().lastIndexOf("."), store.getName().length()); //$NON-NLS-1$
		} else {
			filename = store.getName();
			fileext = ""; //$NON-NLS-1$
		}

		if (filename.contains("_")) { //$NON-NLS-1$
			filename = filename.substring(0, filename.indexOf("_")); //$NON-NLS-1$
		}

		for (IFileStore child : parent.childStores(EFS.NONE, monitor)) {
			if (child.getName().equals(store.getName()) || child.getName().equals(filename + fileext)
					|| (child.getName().startsWith(filename + "_") && child.getName().endsWith(fileext))) { //$NON-NLS-1$
				visitor.visit((ISemanticFileStore) child);
			}
		}
	}

	public IStatus validateRemove(ISemanticFileStore semanticFileStore, int options, IProgressMonitor monitor) throws CoreException {
		if (semanticFileStore.getType() == ISemanticFileStore.FILE) {
			final int[] counter = new int[1];
			final int[] counterNonReadOnly = new int[1];
			counter[0] = 0;
			counterNonReadOnly[0] = 0;

			this.visitCompositeParts(semanticFileStore, new IVisitor() {
				public void visit(ISemanticFileStore store) throws CoreException {
					counter[0]++;
					if (!store.isLocalOnly() && !SampleCompositeResourceContentProvider.this.isReadOnlyInternal(store)) {
						counterNonReadOnly[0]++;
					}
				}
			}, monitor);

			if ((options & ISemanticFileSystem.VALIDATE_REMOVE_RAISE_ERROR_ON_SCOPE_EXTENSION) != 0) {
				if (counter[0] > 1) {
					return new Status(IStatus.CANCEL, SemanticResourcesPluginExamples.PLUGIN_ID, NLS.bind(
							Messages.SampleCompositeResourceContentProvider_CannotDeletePartOfCompositeResource_XMSG, semanticFileStore
									.getPath().toString()));
				}
			}

			if ((options & ISemanticFileSystem.VALIDATE_REMOVE_IGNORE_RESOURCE_STATE) == 0) {
				if (counterNonReadOnly[0] > 0) {
					return new Status(IStatus.CANCEL, SemanticResourcesPluginExamples.PLUGIN_ID, NLS.bind(
							Messages.SampleCompositeResourceContentProvider_SomeResourcesAreCheckedOut_XMSG, semanticFileStore.getPath()
									.toString()));
				}
			}
			return super.validateRemove(semanticFileStore, options, monitor);
		}
		// TODO 0.1: check state of hierarchy
		return super.validateRemove(semanticFileStore, options, monitor);
	}

	public ICacheServiceFactory getCacheServiceFactory() throws CoreException {
		return new FileCacheServiceFactory();
	}

	public InputStream openInputStreamInternal(final ISemanticFileStore store, IProgressMonitor monitor,
			final ICacheTimestampSetter timeStampSetter) throws CoreException {
		String remoteURI = this.getURIStringInternal(store);

		if (remoteURI == null) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_URI_NOT_FOUND, store.getPath(), NLS.bind(
					Messages.SampleCompositeResourceContentProvider_RemoteURINotSet_XMSG, store.getPath().toString()));
		}

		try {
			return RESTUtil.openInputStream(remoteURI, new IRESTCallback() {
				public void setTimestamp(long timestamp) {
					timeStampSetter.setTimestamp(timestamp);
				}

				public void setContentType(String contentType) {
					try {
						setContentTypeInternal(store, contentType);
					} catch (CoreException e) {
						// $JL-EXC$ TODO 0.1: error handling
						e.printStackTrace();
					}
				}

				public long getTimestamp() {
					return timeStampSetter.getTimestamp();
				}

				public String getContentType() {
					return null;
				}
			});
		} catch (IOException e) {
			throw new SemanticResourceException(SemanticResourceStatusCode.REMOTE_CONNECT_EXCEPTION, store.getPath(), e.getMessage(), e);
		}
	}

	public void addResource(ISemanticFileStore parentStore, String name, ResourceType resourceType, IProgressMonitor monitor)
			throws CoreException {
		switch (resourceType) {
		case FOLDER_TYPE:
			parentStore.addChildFolder(name);
			break;

		default:
			throw new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, parentStore.getPath(),
					Messages.SampleCompositeResourceContentProvider_NotSupported_XMSG);
		}
	}

	public void addFileFromRemoteByURI(ISemanticFileStore parentStore, String name, URI uri, IProgressMonitor monitor) throws CoreException {
		parentStore.addChildFile(name);
		ISemanticFileStore newChild = (ISemanticFileStore) parentStore.getChild(name);

		this.setURIStringInternal(newChild, uri.toString());
		setReadOnly(newChild, true, monitor);
	}

	public void addFolderFromRemoteByURI(ISemanticFileStore parentStore, String name, URI uri, IProgressMonitor monitor)
			throws CoreException {
		parentStore.addChildFolder(name);
		ISemanticFileStore newChild = (ISemanticFileStore) parentStore.getChild(name);

		this.setURIStringInternal(newChild, uri.toString());
	}

	public ISemanticSpiResourceInfo fetchResourceInfo(ISemanticFileStore semanticFileStore, int options, IProgressMonitor monitor)
			throws CoreException {
		return new SemanticSpiResourceInfo(options, false, false, this.isReadOnlyInternal(semanticFileStore), !semanticFileStore
				.isLocalOnly(), this.getURIStringInternal(semanticFileStore), this.getContentTypeInternal(semanticFileStore));
	}

	public void removeResource(ISemanticFileStore semanticFileStore, final IProgressMonitor monitor) throws CoreException {
		if (semanticFileStore.getType() == ISemanticFileStore.FILE) {
			this.visitCompositeParts(semanticFileStore, new IVisitor() {
				public void visit(ISemanticFileStore store) throws CoreException {
					dropCache(store, monitor, SampleCompositeResourceContentProvider.this.deleteAllVisitor, new MultiStatus(
							SemanticResourcesPluginExamples.PLUGIN_ID, IStatus.OK, null, null));
					store.remove(monitor);
				}
			}, monitor);
		} else {
			dropCache(semanticFileStore, monitor, SampleCompositeResourceContentProvider.this.deleteAllVisitor, new MultiStatus(
					SemanticResourcesPluginExamples.PLUGIN_ID, IStatus.OK, null, null));
			semanticFileStore.remove(monitor);
		}
	}

	public void revertChanges(ISemanticFileStore semanticFileStore, final IProgressMonitor monitor) throws CoreException {
		if (semanticFileStore.getType() == ISemanticFileStore.FILE) {
			this.visitCompositeParts(semanticFileStore, new IVisitor() {
				public void visit(ISemanticFileStore store) throws CoreException {
					if (!store.isLocalOnly()) {
						dropCache(store, monitor, SampleCompositeResourceContentProvider.this.deleteAllVisitor, new MultiStatus(
								SemanticResourcesPluginExamples.PLUGIN_ID, IStatus.OK, null, null));
						setReadOnlyInternal(store, true);
					}
				}
			}, monitor);
		} else {
			throw new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, semanticFileStore.getPath(),
					Messages.SampleCompositeResourceContentProvider_NotSupported_XMSG);
		}
	}

	public void setReadOnly(ISemanticFileStore semanticFileStore, final boolean readonly, IProgressMonitor monitor) throws CoreException {
		this.visitCompositeParts(semanticFileStore, new IVisitor() {
			public void visit(ISemanticFileStore store) throws CoreException {
				if (!store.isLocalOnly()) {
					setReadOnlyInternal(store, readonly);
				}
			}
		}, monitor);
	}

	public void synchronizeContentWithRemote(ISemanticFileStore semanticFileStore, SyncDirection direction, IProgressMonitor monitor,
			MultiStatus status) {
		if (semanticFileStore.getType() == ISemanticFileStore.FILE) {
			if (semanticFileStore.isLocalOnly()) {
				return;
			}

			if (direction == SyncDirection.INCOMING || direction == SyncDirection.BOTH) {
				this.dropCache(semanticFileStore, monitor, this.deleteAllVisitor, status);
				this.fillCache(semanticFileStore, monitor, status);
			}
		} else {
			IFileStore[] childStores;
			try {
				childStores = semanticFileStore.childStores(EFS.NONE, monitor);
			} catch (CoreException e) {
				status.add(e.getStatus());
				return;
			}
			for (IFileStore store : childStores) {
				if (store instanceof ISemanticFileStore) {
					synchronizeContentWithRemote((ISemanticFileStore) store, direction, monitor, status);
				}
			}
		}
	}

	public IStatus validateEdit(ISemanticFileStore[] semanticFileStore, Object shell) {
		MultiStatus status = new MultiStatus(SemanticResourcesPluginExamples.PLUGIN_ID, IStatus.OK, null, null);
		for (ISemanticFileStore iSemanticFileStore : semanticFileStore) {
			// silently mark as writable
			try {
				setReadOnly(iSemanticFileStore, false, null);
			} catch (CoreException e) {
				status.add(e.getStatus());
			}
		}
		return status;
	}

	public IStatus validateSave(ISemanticFileStore semanticFileStore) {
		return validateEdit(new ISemanticFileStore[] { semanticFileStore }, null);
	}

	public String getURIString(ISemanticFileStore semanticFileStore) throws CoreException {
		return getURIStringInternal(semanticFileStore);
	}

	public void setURIString(ISemanticFileStore store, URI uri, IProgressMonitor monitor) throws CoreException {
		if (store.getType() == ISemanticFileStore.FILE) {
			if (!store.isLocalOnly()) {
				setURIStringInternal(store, uri.toString());
				dropCache(store, monitor, SampleCompositeResourceContentProvider.this.deleteAllVisitor, new MultiStatus(
						SemanticResourcesPluginExamples.PLUGIN_ID, IStatus.OK, null, null));
				setReadOnlyInternal(store, true);
			}
		} else {
			throw new SemanticResourceException(SemanticResourceStatusCode.METHOD_NOT_SUPPORTED, store.getPath(),
					Messages.SampleCompositeResourceContentProvider_NotSupported_XMSG);
		}
	}

}
