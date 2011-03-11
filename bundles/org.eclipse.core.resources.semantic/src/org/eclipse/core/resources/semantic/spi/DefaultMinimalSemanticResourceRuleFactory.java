/*******************************************************************************
 * Copyright (c) 2010 SAP AG.
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
 * The default implementation for the {@link ISemanticResourceRuleFactory} that
 * provides fine-grained locks.
 * <p>
 * Note that a return value of <code>null</code> means that the workspace root
 * is used as scheduling rule. This behavior is intended as fall-back solution
 * and should be avoided wherever possible.
 * <p>
 * 
 * @since 0.3
 * 
 */
public class DefaultMinimalSemanticResourceRuleFactory implements ISemanticResourceRuleFactory {

	protected final ISemanticFileStore rootStore;

	/**
	 * @param rootStore
	 *            the root store of the responsible content provider
	 */
	public DefaultMinimalSemanticResourceRuleFactory(ISemanticFileStore rootStore) {
		this.rootStore = rootStore;
	}

	public ISemanticFileStore charsetRule(ISemanticFileStore store) {
		return store;
	}

	public ISemanticFileStore modifyRule(ISemanticFileStore store) {
		return store;
	}

	public ISemanticFileStore copyRule(ISemanticFileStore source, ISemanticFileStore destination) {
		return null;
	}

	public ISemanticFileStore moveRule(ISemanticFileStore source, ISemanticFileStore destination) {
		return null;
	}

	public ISemanticFileStore refreshRule(ISemanticFileStore store) {
		return getParent(store);
	}

	public ISemanticFileStore deleteRule(ISemanticFileStore store) {
		return getParent(store);
	}

	public ISemanticFileStore createRule(ISemanticFileStore store) {
		return getParent(store);
	}

	public ISemanticFileStore validateEditRule(ISemanticFileStore[] stores) {
		if (stores.length == 1) {
			return getParent(stores[0]);
		}
		return getParent(this.rootStore);
	}

	private ISemanticFileStore getParent(ISemanticFileStore store) {
		if (store.getType() == ISemanticFileStore.PROJECT) {
			return store;
		}
		return (ISemanticFileStore) store.getParent();
	}

}
