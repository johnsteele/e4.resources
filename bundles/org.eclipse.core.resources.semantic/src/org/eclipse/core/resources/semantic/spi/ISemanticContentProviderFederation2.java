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

import org.eclipse.core.runtime.IPath;

/**
 * This interface must be implemented by content providers that would like to
 * provide a dynamic assignment of child stores to federated content providers.
 * <p>
 * 
 * @since 0.3
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISemanticContentProviderFederation2 {
	/**
	 * @see ISemanticContentProviderFederation2#getFederatedProviderInfoForPath(IPath)
	 * 
	 */
	public class FederatedProviderInfo {

		/**
		 * 
		 * @param contentProviderID
		 *            ID of federated Content Provider
		 * @param rootNodePosition
		 *            index for the root store of the federated provider
		 *            relative to root store of this content provider; position
		 *            of 0 corresponds to the root store of this content
		 *            provider and must never be returned
		 * @see ISemanticContentProviderFederation2#getFederatedProviderInfoForPath(IPath)
		 */
		public FederatedProviderInfo(String contentProviderID, int rootNodePosition) {
			this.contentProviderID = contentProviderID;
			this.rootNodePosition = rootNodePosition;
		}

		/**
		 * ID of federated Content Provider
		 */
		final public String contentProviderID;
		/**
		 * position for the root store of the federated provider relative to
		 * root store of this content provider; position of 0 corresponds to the
		 * root store of this content provider and must never be returned
		 */
		final public int rootNodePosition;
	}

	/**
	 * Returns a structure that contains the content provider ID and the root
	 * node position in case when the provided path belongs to a federated
	 * content provider. Returns <code>null</code> in all other cases, e.g. for
	 * a path that is handled by this content provider.
	 * <p>
	 * Since it is possible to create resource handle for arbitrary paths, the
	 * implementation should simply return <code>null</code> for any path that
	 * it can not validate.
	 * <p>
	 * When called multiple times during an Eclipse session, the method must
	 * return the same result for the same input. Failure to return the same
	 * data may result in unpredictable failures.
	 * <p>
	 * Example: Given the root path of this provider at /a/b and the federated
	 * providers "hugo" and "felix" that should be rooted at /a/b/c/d and
	 * /a/b/c1 respectively , the method should return following values for
	 * following inputs:
	 * 
	 * <pre>
	 *  /a/b		null
	 *  /a/b/c 		null
	 *  /a/b/c/d	{ "hugo", 2 }
	 *  /a/b/c/d/e	{ "hugo", 2 }
	 *  /a/b/c1 	{ "felix", 1 }
	 *  /a/b/c1/d 	{ "felix", 1 }
	 *  /a/b/c1/d/e	{ "felix", 1 }
	 *  /a/b/c2 	null
	 *  /a/b/c3 	null 
	 *  ...		...
	 * </pre>
	 * 
	 * Here is an example implementation:
	 * 
	 * <pre>
	 * public FederatedProviderInfo getFederatedProviderInfoForPath(IPath path) {
	 * 	IPath checkPath = path.removeFirstSegments(getRootStore().getPath().segmentCount());
	 * 
	 * 	if (checkPath.segmentCount() == 0) {
	 * 		return null;
	 * 	}
	 * 
	 * 	if (checkPath.segment(0).equals(&quot;c1&quot;)) {
	 * 		return new FederatedProviderInfo(&quot;felix&quot;, 1);
	 * 	}
	 * 
	 * 	if (checkPath.segmentCount() &gt;= 2) {
	 * 		if (checkPath.segment(0).equals(&quot;c&quot;)) {
	 * 			if (checkPath.segment(1).equals(&quot;d&quot;)) {
	 * 				return new FederatedProviderInfo(&quot;hugo&quot;, 2);
	 * 			}
	 * 		}
	 * 	}
	 * 
	 * 	return null;
	 * }
	 * </pre>
	 * 
	 * @param path
	 *            the full path of the resource
	 * @return a provider info or <code>null</code>
	 */
	public FederatedProviderInfo getFederatedProviderInfoForPath(IPath path);

}
