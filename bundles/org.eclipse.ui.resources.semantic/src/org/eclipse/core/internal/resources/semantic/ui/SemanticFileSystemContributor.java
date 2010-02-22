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
package org.eclipse.core.internal.resources.semantic.ui;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ide.fileSystem.FileSystemContributor;

/**
 * Semantic File System contributor (e.g. for creating projects in the SFS)
 */
public class SemanticFileSystemContributor extends FileSystemContributor {

	@Override
	public URI getURI(String aString) {
		try {
			String string;
			if (aString.startsWith(ISemanticFileSystem.SCHEME)) {
				string = aString.substring(ISemanticFileSystem.SCHEME.length()).replace('\\', '/');
				if (string.charAt(0) == ':') {
					string = string.substring(1);
				}
			} else {
				string = aString.replace('\\', '/');
			}
			if (string.equals("")) { //$NON-NLS-1$
				return new URI(ISemanticFileSystem.SCHEME + ':' + '/');
			}
			if (string.charAt(0) == '/') {
				return new URI(ISemanticFileSystem.SCHEME + ':' + string);
			}
			return new URI(ISemanticFileSystem.SCHEME + ':' + '/' + string);
		} catch (URISyntaxException e) {
			// $JL-EXC$ simply ignore and return null
			return null;
		}

	}

	@Override
	public URI browseFileSystem(String initialPath, Shell shell) {

		if (shell == null) {
			// mainly for testing
			Shell newShell = new Shell(Display.getCurrent());
			BrowseSFSDialog dialog = new BrowseSFSDialog(newShell, initialPath);
			dialog.setBlockOnOpen(false);
			dialog.open();
			dialog.close();
			try {
				return new URI(dialog.getSelectedPathString());
			} catch (URISyntaxException e) {
				// $JL-EXC$ ignore here
				return null;
			}

		}

		BrowseSFSDialog dialog = new BrowseSFSDialog(shell, initialPath);
		if (dialog.open() == Window.OK) {
			try {
				return new URI(dialog.getSelectedPathString());
			} catch (URISyntaxException e) {
				// $JL-EXC$ ignore here
				return null;
			}
		}
		return null;

	}
}
