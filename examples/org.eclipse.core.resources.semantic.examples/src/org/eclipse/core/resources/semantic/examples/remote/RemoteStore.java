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
package org.eclipse.core.resources.semantic.examples.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.semantic.examples.SemanticResourcesPluginExamples;
import org.eclipse.core.resources.semantic.examples.remote.RemoteItem.Type;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Represents a remote repository
 * 
 */
public class RemoteStore extends RemoteStoreTransient {

	/** The remote store filename */
	public final static String FILENAME = "simulatedRemoteContent.xml"; //$NON-NLS-1$

	private final static String XML_ATT_NAME = "Name"; //$NON-NLS-1$
	private final static String XML_ELEMENT_VERSION = "Version"; //$NON-NLS-1$
	private final static String XML_ATT_VERSION = "Version"; //$NON-NLS-1$
	private final static String XML_ATT_TYPE = "Type"; //$NON-NLS-1$
	private final static String XML_ATT_TIMESTAMP = "Timestamp"; //$NON-NLS-1$
	private final static String XML_ATT_VAL_EMPTY = ""; //$NON-NLS-1$

	private class XmlContentHandler extends DefaultHandler {

		private RemoteFolder currentFolder;
		private RemoteFile currentFile;
		private String currentVersion = ""; //$NON-NLS-1$

		public XmlContentHandler(RemoteFolder rootFolder) {
			this.currentFolder = rootFolder;
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

			Map<String, String> nameValuePairs = new HashMap<String, String>();
			for (int i = 0; i < attributes.getLength(); i++) {
				String name = attributes.getQName(i);
				String value = attributes.getValue(i);
				nameValuePairs.put(name, value);
			}
			String type = nameValuePairs.get(RemoteStore.XML_ATT_TYPE);
			if (type == null) {
				return;
			}
			if (type.equals(Type.FOLDER.toString())) {
				if (nameValuePairs.get(RemoteStore.XML_ATT_NAME).equals(RemoteStore.XML_ATT_VAL_EMPTY)) {
					return;
				}
				this.currentFolder = this.currentFolder.addFolder(nameValuePairs.get(RemoteStore.XML_ATT_NAME));

			} else if (type.equals(Type.FILE.toString())) {
				String name = nameValuePairs.get(RemoteStore.XML_ATT_NAME);
				long timestamp = Long.parseLong(nameValuePairs.get(RemoteStore.XML_ATT_TIMESTAMP));
				this.currentFile = this.currentFolder.addFile(name, new byte[0], timestamp);
			} else if (type.equals(XML_ELEMENT_VERSION)) {
				String version = nameValuePairs.get(XML_ATT_VERSION);
				this.currentVersion = version;
			}
		}

		public void characters(char ch[], int start, int length) throws SAXException {
			if (this.currentFile == null && this.currentVersion.equals("")) { //$NON-NLS-1$
				return;
			}
			if ((length % 2) != 0) {
				return;
			}
			if (!this.currentVersion.equals("")) { //$NON-NLS-1$
				try {
					byte[] bytes = stringToBytes(new String(ch).substring(start, start + length));
					this.currentFile.myVersions.put(this.currentVersion, bytes);
				} catch (IllegalArgumentException e) {
					throw new SAXException(e);
				}

			} else if (this.currentFile != null) {
				try {
					byte[] bytes = stringToBytes(new String(ch).substring(start, start + length));
					// we need to make sure the timestamp is not set and the
					// version are left alone
					if (bytes.length > 0) {
						this.currentFile.myContent = bytes;
					}
				} catch (IllegalArgumentException e) {
					throw new SAXException(e);
				}
			}
		}

		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (qName.equals(Type.FOLDER.name())) {
				this.currentFolder = this.currentFolder.getParent();
			} else if (qName.equals(Type.FILE.name())) {
				this.currentFile = null;
			} else if (qName.equals(XML_ELEMENT_VERSION)) {
				this.currentVersion = ""; //$NON-NLS-1$
			}
		}

	}

	/**
	 * @param container
	 *            the project
	 */
	public RemoteStore(IContainer container) {
		super(container);
	}

	/**
	 * Serializes
	 * 
	 * @param monitor
	 * @throws CoreException
	 */
	public void serialize(IProgressMonitor monitor) throws CoreException {

		RemoteFolder folder = getRootFolder();
		DocumentBuilder db;
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			IStatus error = new Status(IStatus.ERROR, SemanticResourcesPluginExamples.PLUGIN_ID, null, e);
			throw new CoreException(error);
		}
		Document doc = db.newDocument();

		Element root = doc.createElement("Root"); //$NON-NLS-1$
		doc.appendChild(root);

		serializeItem(doc, folder, root);

		DOMSource domSource = new DOMSource(doc);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = tf.newTransformer();
		} catch (TransformerConfigurationException e) {
			IStatus error = new Status(IStatus.ERROR, SemanticResourcesPluginExamples.PLUGIN_ID, null, e);
			throw new CoreException(error);
		}
		transformer.setOutputProperty(OutputKeys.STANDALONE, "no"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		try {
			transformer.transform(domSource, result);
		} catch (TransformerException e) {
			IStatus error = new Status(IStatus.ERROR, SemanticResourcesPluginExamples.PLUGIN_ID, null, e);
			throw new CoreException(error);
		}

		InputStream is;
		try {
			is = new ByteArrayInputStream(writer.getBuffer().toString().getBytes("UTF-8")); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			// $JL-EXC$
			is = new ByteArrayInputStream(writer.getBuffer().toString().getBytes());
		}

		IFile file = this.myContainer.getProject().getFile(RemoteStore.FILENAME);
		if (!file.exists()) {
			file.create(is, false, null);
		} else {
			file.setContents(is, EFS.NONE, monitor);
		}

	}

	/**
	 * Reads
	 * 
	 * @throws CoreException
	 */
	public void deserialize() throws CoreException {

		reset();

		IFile file = this.myContainer.getProject().getFile(new Path(RemoteStore.FILENAME));
		if (file.exists()) {
			RemoteFolder folder = getRootFolder();

			XmlContentHandler handler = new XmlContentHandler(folder);

			InputStream is = null;
			try {
				try {
					SAXParserFactory.newInstance().newSAXParser().parse(file.getContents(), handler);
				} catch (SAXException e) {
					IStatus error = new Status(IStatus.ERROR, SemanticResourcesPluginExamples.PLUGIN_ID, null, e);
					throw new CoreException(error);
				} catch (IOException e) {
					IStatus error = new Status(IStatus.ERROR, SemanticResourcesPluginExamples.PLUGIN_ID, null, e);
					throw new CoreException(error);
				} catch (ParserConfigurationException e) {
					IStatus error = new Status(IStatus.ERROR, SemanticResourcesPluginExamples.PLUGIN_ID, null, e);
					throw new CoreException(error);
				}
			} finally {
				Util.safeClose(is);
			}
		}

	}

	private void serializeItem(Document doc, RemoteItem actItem, Element element) {

		Element child = doc.createElement(actItem.getType().name());
		child.setAttribute(RemoteStore.XML_ATT_TYPE, actItem.getType().toString());
		child.setAttribute(RemoteStore.XML_ATT_NAME, actItem.getName());
		element.appendChild(child);

		if (actItem.getType() == Type.FOLDER) {
			RemoteFolder folder = (RemoteFolder) actItem;
			for (RemoteItem item : folder.getChildren()) {
				serializeItem(doc, item, child);
			}
		} else {
			RemoteFile file = (RemoteFile) actItem;
			child.setAttribute(RemoteStore.XML_ATT_TIMESTAMP, Long.toString(file.getTimestamp()));

			CDATASection section = doc.createCDATASection(bytesToString(file.myContent));
			child.appendChild(section);

			Map<String, byte[]> versions = file.myVersions;
			for (Map.Entry<String, byte[]> version : versions.entrySet()) {
				Element versionElement = doc.createElement(XML_ELEMENT_VERSION);
				versionElement.setAttribute(RemoteStore.XML_ATT_TYPE, XML_ELEMENT_VERSION);
				child.appendChild(versionElement);
				versionElement.setAttribute(XML_ATT_VERSION, version.getKey());
				String contentData = bytesToString(version.getValue());
				CDATASection contentSection = doc.createCDATASection(contentData);
				versionElement.appendChild(contentSection);
			}

		}
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.myContainer.getFullPath() == null) ? 0 : this.myContainer.getFullPath().hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemoteStore other = (RemoteStore) obj;
		if (this.myContainer.getFullPath() == null) {
			if (other.myContainer.getFullPath() != null)
				return false;
		} else if (!this.myContainer.getFullPath().equals(other.myContainer.getFullPath()))
			return false;
		return true;
	}

	private static byte[] stringToBytes(String string) {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int token = 0;

		while (token + 1 < string.length()) {

			byte value = (byte) (Integer.parseInt(new String(new char[] { string.charAt(token), string.charAt(++token) }), 16) - 128);
			bos.write(value);
			token++;
		}

		return bos.toByteArray();
	}

	private static String bytesToString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();

		for (byte c : bytes) {
			String hexString = Integer.toHexString(c + 128);
			sb.append(hexString);
		}
		return sb.toString();
	}

}
