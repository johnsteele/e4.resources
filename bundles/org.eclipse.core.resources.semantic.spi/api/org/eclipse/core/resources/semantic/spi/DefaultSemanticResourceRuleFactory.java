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

import org.eclipse.core.resources.IResource;

/**
 * The default implementation for the {@link ISemanticResourceRuleFactory}.
 * <p>
 * This will return the root store of the relevant content provider for any
 * requested rule. Content providers may chose to override
 * {@link ISemanticContentProvider#getRuleFactory()} by returning a subclass of
 * this or an own implementation of {@link ISemanticResourceRuleFactory} to
 * achieve a different behavior.
 * <p>
 * The default behavior should be overridden if it causes too much lock
 * contention.
 * <p>
 * Since the returned {@link ISemanticFileStore} instances may point to
 * non-existing {@link IResource} instances (in particular when obtaining
 * refresh rules), the parent hierarchy of the returned
 * {@link ISemanticFileStore} will be automatically traversed upwards to the
 * first existing {@link IResource} which will be used as scheduling rule.
 * <p>
 * Note that a return value of <code>null</code> means that the workspace root
 * is used as scheduling rule. This behavior is intended as fall-back solution
 * and should be avoided wherever possible.
 * <p>
 * 
 * @since 4.0
 * 
 */
public class DefaultSemanticResourceRuleFactory implements ISemanticResourceRuleFactory {

	protected final ISemanticFileStore rootStore;

	/**
	 * @param rootStore
	 *            the root store of the responsible content provider
	 */
	public DefaultSemanticResourceRuleFactory(ISemanticFileStore rootStore) {
		this.rootStore = rootStore;
	}

	public ISemanticFileStore charsetRule(ISemanticFileStore resource) {
		return this.rootStore;
	}

	public ISemanticFileStore copyRule(ISemanticFileStore source, ISemanticFileStore destination) {
		return this.rootStore;
	}

	public ISemanticFileStore createRule(ISemanticFileStore resource) {
		return this.rootStore;
	}

	public ISemanticFileStore deleteRule(ISemanticFileStore resource) {
		return this.rootStore;
	}

	public ISemanticFileStore markerRule(ISemanticFileStore resource) {
		// TODO 0.1: there seems to be a bug in the
		// org.eclipse.core.internal.resources.Rules class which prevents this
		// from ever being called
		return this.rootStore;
	}

	public ISemanticFileStore modifyRule(ISemanticFileStore resource) {
		return this.rootStore;
	}

	public ISemanticFileStore moveRule(ISemanticFileStore source, ISemanticFileStore destination) {
		return this.rootStore;
	}

	public ISemanticFileStore refreshRule(ISemanticFileStore resource) {
		return this.rootStore;
	}

	public ISemanticFileStore validateEditRule(ISemanticFileStore[] resources) {
		return this.rootStore;
	}

}
