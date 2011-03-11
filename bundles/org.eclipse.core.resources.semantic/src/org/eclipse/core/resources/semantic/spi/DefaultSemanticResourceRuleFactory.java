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

/**
 * The default implementation for the {@link ISemanticResourceRuleFactory}.
 * <p>
 * This will return the root store of the relevant content provider for any
 * requested rule. Content providers may chose to override
 * {@link ISemanticContentProvider#getRuleFactory()} by returning a subclass of
 * this class to achieve a different behavior.
 * <p>
 * The default behavior should be overridden if it causes too much lock
 * contention.
 * <p>
 * Note that a return value of <code>null</code> means that the workspace root
 * is used as scheduling rule. This behavior is intended as fall-back solution
 * and should be avoided wherever possible.
 * <p>
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
		return (ISemanticFileStore) this.rootStore.getParent();
	}

	public ISemanticFileStore createRule(ISemanticFileStore resource) {
		return (ISemanticFileStore) this.rootStore.getParent();
	}

	public ISemanticFileStore deleteRule(ISemanticFileStore resource) {
		return (ISemanticFileStore) this.rootStore.getParent();
	}

	public ISemanticFileStore modifyRule(ISemanticFileStore resource) {
		return this.rootStore;
	}

	public ISemanticFileStore moveRule(ISemanticFileStore source, ISemanticFileStore destination) {
		return (ISemanticFileStore) this.rootStore.getParent();
	}

	public ISemanticFileStore refreshRule(ISemanticFileStore resource) {
		return (ISemanticFileStore) this.rootStore.getParent();
	}

	public ISemanticFileStore validateEditRule(ISemanticFileStore[] resources) {
		return (ISemanticFileStore) this.rootStore.getParent();
	}

}
