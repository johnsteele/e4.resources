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
 * 
 */
public class ObjectFactory {

	/**
	 * 
	 */
	public ObjectFactory() {
		//
	}

	/**
	 * Create an instance of {@link PropType }
	 * 
	 */
	public PropType createPropType() {
		return new PropType();
	}

	/**
	 * Create an instance of {@link PropstatType }
	 * 
	 */
	public PropstatType createPropstatType() {
		return new PropstatType();
	}

	/**
	 * Create an instance of {@link ResponseType }
	 * 
	 */
	public ResponseType createResponseType() {
		return new ResponseType();
	}

	/**
	 * Create an instance of {@link MultistatusType }
	 * 
	 */
	public MultistatusType createMultistatusType() {
		return new MultistatusType();
	}

}
