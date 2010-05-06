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
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.IProgressMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PropfindResponseReader {

	ObjectFactory factory = new ObjectFactory();

	public PropfindResponseReader() {
		//
	}

	public MultistatusType loadMultitatusTypeFromResponse(InputStream content, IProgressMonitor monitor) throws IOException {
		MultistatusType ms = null;

		try {
			if (content != null) {
				try {
					DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

					Document doc = builder.parse(content);

					Node node = doc.getDocumentElement();

					if (node != null) {
						String name = node.getNodeName();

						if (name.endsWith(":multistatus") //$NON-NLS-1$
								|| name.equals("multistatus")) { //$NON-NLS-1$
							ms = factory.createMultistatusType();

							loadMultitatus(node, ms);
						}
					}
				} catch (ParserConfigurationException e) {
					throw new IOException(e.getMessage());
				} catch (SAXException e) {
					throw new IOException(e.getMessage());
				}
			}
		} finally {
			Util.safeClose(content);
		}
		return ms;
	}

	private void loadMultitatus(Node root, MultistatusType ms) {
		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			String name = node.getNodeName();

			if (name.endsWith(":response") || name.equals("response")) { //$NON-NLS-1$ //$NON-NLS-2$
				ResponseType response = factory.createResponseType();

				ms.getResponse().add(response);
				loadResponse(node, response);
			}
		}
	}

	private void loadResponse(Node root, ResponseType response) {
		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			String name = node.getNodeName();

			if (name.endsWith(":href") || name.equals("href")) { //$NON-NLS-1$ //$NON-NLS-2$
				response.setHref(getTextFromNode(node));
			}

			if (name.endsWith(":propstat") || name.equals("propstat")) { //$NON-NLS-1$ //$NON-NLS-2$
				PropstatType propstat = factory.createPropstatType();

				response.getPropstat().add(propstat);
				loadPropstat(node, propstat);
			}
		}
	}

	private void loadPropstat(Node root, PropstatType propstat) {
		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			String name = node.getNodeName();

			if (name.endsWith(":status") || name.equals("status")) { //$NON-NLS-1$ //$NON-NLS-2$
				propstat.setStatus(getTextFromNode(node));
			}

			if (name.endsWith(":prop") || name.equals("prop")) { //$NON-NLS-1$ //$NON-NLS-2$
				PropType prop = factory.createPropType();

				propstat.setProp(prop);
				loadProp(node, prop);
			}
		}
	}

	private void loadProp(Node root, PropType prop) {
		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			String name = node.getNodeName();

			if (name.endsWith(":getlastmodified") //$NON-NLS-1$
					|| name.equals("getlastmodified")) { //$NON-NLS-1$
				prop.setLastmodified(getTextFromNode(node));
			}
			if (name.endsWith(":getcontenttype") //$NON-NLS-1$
					|| name.equals("getcontenttype")) { //$NON-NLS-1$
				prop.setContentType(getTextFromNode(node));
			}
			if (name.endsWith(":getetag") //$NON-NLS-1$
					|| name.equals("getetag")) { //$NON-NLS-1$
				prop.setETag(getTextFromNode(node));
			}
			if (name.endsWith(":displayname") || name.equals("displayname")) { //$NON-NLS-1$ //$NON-NLS-2$
				prop.setDisplayname(getTextFromNode(node));
			}
			if (name.endsWith(":resourcetype") || name.equals("resourcetype")) { //$NON-NLS-1$ //$NON-NLS-2$
				Node child = node.getFirstChild();

				if (child != null) {
					String childName = child.getNodeName();

					if (childName.endsWith(":collection") || childName.equals("collection")) { //$NON-NLS-1$ //$NON-NLS-2$
						prop.setIsFolder(true);
					} else {
						prop.setIsFolder(false);
					}
				}
			}
			if (name.endsWith(":supportedlock") || name.equals("supportedlock")) { //$NON-NLS-1$ //$NON-NLS-2$
				NodeList lockEntryNodes = node.getChildNodes();
				for (int j = 0; j < lockEntryNodes.getLength(); j++) {
					Node lockEntryNode = lockEntryNodes.item(j);

					NodeList lockNodes = lockEntryNode.getChildNodes();
					for (int k = 0; k < lockNodes.getLength(); k++) {
						Node lockNode = lockNodes.item(k);
						String lockNodeName = lockNode.getNodeName();
						if (lockNodeName.endsWith(":lockscope") || lockNodeName.equals("lockscope")) { //$NON-NLS-1$ //$NON-NLS-2$
							Node lockScopeNode = lockNode.getFirstChild();

							String lockScopeNodeName = lockScopeNode.getNodeName();
							if (lockScopeNodeName.endsWith(":exclusive") || lockScopeNodeName.equals("exclusive")) { //$NON-NLS-1$ //$NON-NLS-2$
								prop.setSupportsLocking(true);
							}
						}
					}
				}
			}
		}
	}

	private String getTextFromNode(Node node) {
		Node text = node.getFirstChild();
		if (text != null) {
			return text.getNodeValue();
		}
		String value = node.getNodeValue();
		if (value != null) {
			return value;
		}
		return null;
	}
}
