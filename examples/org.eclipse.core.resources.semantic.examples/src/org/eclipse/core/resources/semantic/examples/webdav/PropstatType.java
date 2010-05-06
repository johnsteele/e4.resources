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

/**
 * <p>
 * 
 */
public class PropstatType {

	protected PropType prop;
	protected String status;

	/**
	 * 
	 */
	public PropType getProp() {
		return prop;
	}

	/**
	 * 
	 */
	public void setProp(PropType value) {
		this.prop = value;
	}

	/**
	 * 
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * 
	 */
	public void setStatus(String value) {
		this.status = value;
	}

}
