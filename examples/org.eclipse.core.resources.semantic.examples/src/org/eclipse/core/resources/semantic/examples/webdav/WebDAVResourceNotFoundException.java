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
package org.eclipse.core.resources.semantic.examples.webdav;

import java.io.IOException;

public class WebDAVResourceNotFoundException extends IOException {

	private static final long serialVersionUID = -4839020959910584643L;

	public WebDAVResourceNotFoundException(String message) {
		super(message);
	}

	public WebDAVResourceNotFoundException(String message, Throwable cause) {
		super(message);

		initCause(cause);
	}
}
