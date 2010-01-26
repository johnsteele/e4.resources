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
package org.eclipse.core.internal.resources.semantic.team;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.team.ui.history.HistoryPageSource;
import org.eclipse.ui.part.Page;

/**
 * The "Team History Page" provider for Semantic Resources
 * 
 */
public class SemanticHistoryPageSource extends HistoryPageSource {

	public boolean canShowHistoryFor(Object object) {
		if (object instanceof IResource) {
			ISemanticResource res = (ISemanticResource) ((IResource) object).getAdapter(ISemanticResource.class);

			return res != null;
		}
		return false;
	}

	public Page createPage(Object object) {
		return new SemanticHistoryPage((IResource) object);
	}

}
