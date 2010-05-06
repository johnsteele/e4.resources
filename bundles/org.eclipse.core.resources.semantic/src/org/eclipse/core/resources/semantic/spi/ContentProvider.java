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
package org.eclipse.core.resources.semantic.spi;

import org.eclipse.core.internal.resources.semantic.SemanticResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;

/**
 * This class implements the base class for SPI contract of
 * {@link ISemanticContentProvider}.
 * 
 * <p>
 * All content provider implementations must subclass from this class or one of
 * the subclasses in order to ensure that new SPI methods can be introduced
 * without breaking compatibility.
 * <p>
 * This class is intended to be subclassed.
 * 
 * @since 4.0
 * @see ISemanticContentProvider
 */
public abstract class ContentProvider implements ISemanticContentProvider {

	private static final QualifiedName ATTRIBUTE_URI = new QualifiedName(SemanticResourcesPlugin.PLUGIN_ID, "URIString"); //$NON-NLS-1$ 
	private static final QualifiedName CONTENT_TYPE_KEY = new QualifiedName(SemanticResourcesPlugin.PLUGIN_ID, "ContentType"); //$NON-NLS-1$;
	private static final QualifiedName ATTRIBUTE_READONLY = new QualifiedName(SemanticResourcesPlugin.PLUGIN_ID, "readonly"); //$NON-NLS-1$ 
	private static final String TRUE = "true"; //$NON-NLS-1$

	private ISemanticFileStore rootStore;

	/**
	 * The no-argument constructor
	 */
	public ContentProvider() {
		// we need a no-arguments constructor
	}

	/**
	 * Initializes this content provider instance with the root store.
	 * <p>
	 * This method is called by the SFS immediately after the content provider
	 * instance is created. This method must not be called by clients.
	 * 
	 * @param rootStore
	 *            The root store of the content provider.
	 */
	public final void setRootStore(ISemanticFileStore rootStore) {
		this.rootStore = rootStore;
	}

	public ISemanticFileStore getRootStore() {
		return this.rootStore;
	}

	/**
	 * The internal URI String getter method
	 * 
	 * @param childStore
	 *            the store
	 * @return the URI as String
	 * @throws CoreException
	 *             upon failure
	 * @deprecated {@link ISemanticFileStore#getRemoteURIString()} should be
	 *             used instead
	 */
	@Deprecated
	public String getURIStringInternal(ISemanticFileStore childStore) throws CoreException {
		String uriString = childStore.getPersistentProperty(ATTRIBUTE_URI);
		if (uriString == null) {
			uriString = childStore.getRemoteURIString();
		}
		return uriString;
	}

	/**
	 * The internal URI String setter method
	 * 
	 * @param childStore
	 *            the store
	 * @param uriString
	 *            the URI as String
	 * @throws CoreException
	 *             upon failure
	 * @deprecated {@link ISemanticFileStore#setRemoteURIString(String)} should
	 *             be used instead
	 */
	@Deprecated
	public void setURIStringInternal(ISemanticFileStore childStore, String uriString) throws CoreException {
		childStore.setPersistentProperty(ATTRIBUTE_URI, null);
		childStore.setRemoteURIString(uriString);
	}

	/**
	 * Extending Eclipse Content Type Management to handle also MIME/HTTP
	 * Content Types
	 * 
	 * @param childStore
	 * @return content type or <code>null</code>
	 * @throws CoreException
	 */
	public String getContentTypeInternal(ISemanticFileStore childStore) throws CoreException {
		return childStore.getPersistentProperty(CONTENT_TYPE_KEY);
	}

	/**
	 * Extending Eclipse Content Type Management to handle also MIME/HTTP
	 * Content Types
	 * 
	 * @param childStore
	 * @param contentType
	 *            new content type or <code>null</code>
	 * 
	 * @throws CoreException
	 */
	public void setContentTypeInternal(ISemanticFileStore childStore, String contentType) throws CoreException {
		childStore.setPersistentProperty(CONTENT_TYPE_KEY, contentType);
	}

	/**
	 * 
	 * @param semanticFileStore
	 *            the store
	 * @return the read-only flag
	 * @throws CoreException
	 *             upon failure
	 */
	public boolean isReadOnlyInternal(ISemanticFileStore semanticFileStore) throws CoreException {

		return semanticFileStore.getPersistentProperty(ATTRIBUTE_READONLY) != null;
	}

	/**
	 * Sets the read-only flag
	 * 
	 * @param childStore
	 *            the store
	 * @param readonly
	 *            the flag
	 * @throws CoreException
	 *             upon failure
	 */
	public void setReadOnlyInternal(ISemanticFileStore childStore, boolean readonly) throws CoreException {
		if (readonly) {
			childStore.setPersistentProperty(ATTRIBUTE_READONLY, TRUE);
		} else {
			childStore.setPersistentProperty(ATTRIBUTE_READONLY, null);
		}
	}

	//
	// ISemanticContentProvider
	//

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	public void onImplicitStoreCreate(ISemanticFileStore newStore) {
		// by default, we do nothing
	}

	public ISemanticResourceRuleFactory getRuleFactory() {
		// the default rule factory always returns the root store
		return new DefaultSemanticResourceRuleFactory(this.rootStore);
	}

	/**
	 * @throws CoreException
	 */
	public IStatus validateRemove(ISemanticFileStore semanticFileStore, int options, IProgressMonitor monitor) throws CoreException {
		return new Status(IStatus.OK, SemanticResourcesPlugin.PLUGIN_ID, ""); //$NON-NLS-1$
	}

}
