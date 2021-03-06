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
package org.eclipse.core.internal.resources.semantic.util;

import org.eclipse.core.runtime.CoreException;

/**
 * The Semantic File System Log.
 * 
 */
public interface ISemanticFileSystemLog {
	/**
	 * Logs a {@link CoreException}
	 * 
	 * @param exception
	 *            the exception
	 */
	public void log(CoreException exception);

}
