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

import java.util.ArrayList;
import java.util.List;

public class UserType {

	private String profileImageUrl = "";
	private String screenName = "";
	private String name = "";
	private String description = "";
	private String location = "";
	private ArrayList<StatusType> statuses = new ArrayList<StatusType>();

	public String getScreenName() {
		if (this.screenName != null) {
			return screenName;
		}
		return "";
	}

	public String getName() {
		if (this.name != null) {
			return name;
		}
		return "";
	}

	public String getDescription() {
		if (this.description != null) {
			return description;
		}
		return "";
	}

	public String getLocation() {
		if (this.location != null) {
			return location;
		}
		return "";
	}

	public List<StatusType> getStatusTypes() {
		return statuses;
	}

	public String getProfileImageUrl() {
		if (this.profileImageUrl != null) {
			return profileImageUrl;
		}
		return "";
	}

	public void setProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}
