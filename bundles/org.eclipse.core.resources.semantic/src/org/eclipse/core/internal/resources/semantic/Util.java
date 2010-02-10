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
package org.eclipse.core.internal.resources.semantic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.QualifiedName;

/**
 * Contains utility methods
 */
public class Util {

	private Util() {
		// static methods only
	}

	/**
	 * The following will be checked:
	 * <ul>
	 * <li>Neither qualifier nor local name must contain "^"</li>
	 * </ul>
	 * 
	 * @param name
	 *            the name to check
	 */
	public static void assertQualifiedNameValid(QualifiedName name) {

		Assert.isLegal(name.getQualifier().indexOf('^') == -1, Messages.Util_QualifierNoCirconflex_XMSG);
		Assert.isLegal(name.getLocalName().indexOf('^') == -1, Messages.Util_LocalNameNoCirconflex_XMSG);
	}

	/**
	 * Converts a {@link QualifiedName} to it's string representation (using "^"
	 * as separator)
	 * 
	 * @param name
	 *            the qualified name
	 * @return the String as concatenated from qualifier, "^", and the local
	 *         name)
	 */
	public static String qualifiedNameToString(QualifiedName name) {
		StringBuilder sb = new StringBuilder(50);
		sb.append(name.getQualifier());
		sb.append('^');
		sb.append(name.getLocalName());
		return sb.toString();
	}

	/**
	 * Closes a stream and ignores any resulting exception.
	 * 
	 * @param in
	 *            the stream
	 * 
	 */
	public static void safeClose(InputStream in) {
		try {
			if (in != null)
				in.close();
		} catch (IOException e) {
			// $JL-EXC$ ignore
		}
	}

	/**
	 * Closes a stream and ignores any resulting exception.
	 * 
	 * @param out
	 *            the stream
	 * 
	 */
	public static void safeClose(OutputStream out) {
		try {
			if (out != null)
				out.close();
		} catch (IOException e) {
			// $JL-EXC$ ignore
		}
	}

}
