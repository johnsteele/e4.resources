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
package org.eclipse.core.resources.semantic;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;

/**
 * Represents status related to semantic resources in the Semantic File System
 * plug-in.
 * <p>
 * Status objects created by the Semantic File System bear the
 * {@link SemanticResourceStatusCode#PLUGIN_ID} and one of the Status codes
 * defined in {@link SemanticResourceStatusCode}.
 * 
 * @since 4.0
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * 
 */
public interface ISemanticResourceStatus extends IStatus {
	/**
	 * 
	 * @return the path to the resource
	 */
	public IPath getPath();

}
