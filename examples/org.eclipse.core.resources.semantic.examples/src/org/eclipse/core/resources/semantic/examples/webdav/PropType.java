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
public class PropType {

	private String getlastmodified;
	private String getcontenttype;
	private String displayname;
	private boolean isFolder;
	private boolean supportsLocking;
	private String lockToken;
	private String eTag;

	public boolean getIsFolder() {
		return isFolder;
	}

	public void setIsFolder(boolean bIsFolder) {
		this.isFolder = bIsFolder;
	}

	/**
	 * Gets the value of the getlastmodified property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLastmodified() {
		return getlastmodified;
	}

	/**
	 * Sets the value of the getlastmodified property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setLastmodified(String value) {
		this.getlastmodified = value;
	}

	/**
	 * Gets the value of the getcontenttype property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getContentType() {
		return getcontenttype;
	}

	/**
	 * Sets the value of the getcontenttype property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setContentType(String value) {
		this.getcontenttype = value;
	}

	/**
	 * Gets the value of the displayname property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDisplayname() {
		return displayname;
	}

	/**
	 * Sets the value of the displayname property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDisplayname(String value) {
		this.displayname = value;
	}

	/**
	 * 
	 * @param eTag
	 */
	public void setETag(String eTag) {
		this.eTag = eTag;
	}

	/**
	 * 
	 * @return ETag
	 */
	public String getETag() {
		return eTag;
	}

	public void setSupportsLocking(boolean supportsLocking) {
		this.supportsLocking = supportsLocking;
	}

	public boolean getSupportsLocking() {
		return supportsLocking;
	}

	public void setLockToken(String lockToken) {
		this.lockToken = lockToken;
	}

	public String getLockToken() {
		return lockToken;
	}

}
