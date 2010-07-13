/*******************************************************************************
 * Copyright (c) 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eduard Bartsch (SAP AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.e4twitterclient.model;

public class StatusType {

	private String text = "";
	private String createdAt = "";
	private String source = "";
	private UserType user;

	public String getText() {
		if (this.text != null) {
			return text;
		}
		return "";
	}

	public String getCreatedAt() {
		if (this.createdAt != null) {
			return createdAt;
		}
		return "";
	}

	public String getSource() {
		if (this.source != null) {
			return source;
		}
		return "";
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setUser(UserType user) {
		this.user = user;
	}

	public UserType getUser() {
		return user;
	}

}
