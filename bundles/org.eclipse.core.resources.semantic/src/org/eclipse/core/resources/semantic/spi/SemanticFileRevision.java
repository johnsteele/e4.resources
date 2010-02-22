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
package org.eclipse.core.resources.semantic.spi;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.ITag;

/**
 * An abstract helper implementation for {@link IFileRevision}
 */
public abstract class SemanticFileRevision implements IFileRevision {

	private final String myAuthor;
	private final String myComment;
	private final long myTimestamp;
	private URI myUri;
	private final String myName;

	/**
	 * Constructs an instance of this class
	 * 
	 * @param semanticFileStore
	 *            the file store
	 * @param timestamp
	 *            the timestamp
	 * @param author
	 *            may be <code>null</code>
	 * @param comment
	 *            may be <code>null</code>
	 */
	public SemanticFileRevision(ISemanticFileStore semanticFileStore, long timestamp, String author, String comment) {
		this.myAuthor = author;
		this.myComment = comment;
		this.myTimestamp = timestamp;
		this.myName = semanticFileStore.getName();
		try {
			this.myUri = new URI(semanticFileStore.getPath().toString());
		} catch (URISyntaxException e) {
			// $JL-EXC$ really not likely
			this.myUri = null;
		}

	}

	public boolean exists() {
		return true;
	}

	public String getAuthor() {
		return this.myAuthor;
	}

	public String getComment() {
		return this.myComment;
	}

	public String getContentIdentifier() {
		return Long.toString(this.myTimestamp);
	}

	public String getName() {
		return this.myName;
	}

	public ITag[] getTags() {
		return new ITag[0];
	}

	public long getTimestamp() {
		return this.myTimestamp;
	}

	public URI getURI() {
		return this.myUri;
	}

	public boolean isPropertyMissing() {
		return false;
	}

	/**
	 * @throws CoreException
	 */
	public IFileRevision withAllProperties(IProgressMonitor monitor) throws CoreException {
		return this;
	}

}
