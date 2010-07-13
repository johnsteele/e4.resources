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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ContentHandler {

	public static UserType loadUser(Node root) {
		UserType userType = new UserType();

		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			String name = child.getNodeName();

			if (name.equals("name")) {
				userType.setName(getChildValue(child));
			} else if (name.equals("screen_name")) {
				userType.setScreenName(getChildValue(child));
			} else if (name.equals("description")) {
				userType.setDescription(getChildValue(child));
			} else if (name.equals("location")) {
				userType.setLocation(getChildValue(child));
			} else if (name.equals("profile_image_url")) {
				userType.setProfileImageUrl(getChildValue(child));
			} else if (name.equals("status")) {
				userType.getStatusTypes().add(loadStatus(child));
			}
		}
		return userType;
	}

	public static String getChildValue(Node node) {
		if (node.getFirstChild() != null) {
			return node.getFirstChild().getNodeValue();
		}
		return null;
	}

	public static StatusType loadStatus(Node node) {
		StatusType status = new StatusType();

		NodeList children = node.getChildNodes();

		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);
			String name = child.getNodeName();

			if (name.equals("text")) {
				status.setText(getChildValue(child));
			} else if (name.equals("created_at")) {
				status.setCreatedAt(getChildValue(child));
			} else if (name.equals("source")) {
				status.setSource(getChildValue(child));
			} else if (name.equals("user")) {
				status.setUser(loadUser(child));
			}
		}
		return status;
	}

}
