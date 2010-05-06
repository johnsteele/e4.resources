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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class MultistatusType {

	protected List<ResponseType> response;

	/**
	 * 
	 */
	public List<ResponseType> getResponse() {
		if (response == null) {
			response = new ArrayList<ResponseType>();
		}
		return this.response;
	}

}
