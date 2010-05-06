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
package org.eclipse.core.internal.resources.semantic;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeNodeType;

public class SemanticQueryParser {

	private String providerID;
	private TreeNodeType type = TreeNodeType.UNKNOWN;
	private QueryParam[] params;
	private String uri;
	private boolean shouldCreate;

	public static class QueryParam {
		String name;
		String value;
	}

	public SemanticQueryParser(String query) {
		params = getQueryParameters(query);
		applyQueryParameters(query);
	}

	public static QueryParam[] getQueryParameters(String query) {
		ArrayList<QueryParam> params = new ArrayList<QueryParam>();

		Pattern p1 = Pattern.compile("([^;&]+)"); //$NON-NLS-1$
		Pattern p2 = Pattern.compile("([^=]*)=([^=]*)"); //$NON-NLS-1$
		Matcher m1 = p1.matcher(query);

		while (m1.find()) {
			Matcher m2 = p2.matcher(m1.group());
			if (m2.find()) {
				QueryParam param = new QueryParam();
				param.name = m2.group(1);
				param.value = m2.group(2);

				params.add(param);
			}
		}

		return params.toArray(new QueryParam[0]);
	}

	public String getProviderID() {
		return providerID;
	}

	public TreeNodeType getType() {
		return type;
	}

	public QueryParam[] getAllParameters() {
		return params;
	}

	public boolean getShouldCreate() {
		return shouldCreate;
	}

	public String getURI() {
		return uri;
	}

	private void applyQueryParameters(String queryString) {
		if (queryString != null) {
			params = getQueryParameters(queryString);

			for (QueryParam param : params) {
				if (param.name.equals("provider")) { //$NON-NLS-1$
					this.providerID = param.value;
				} else if (param.name.equals("type")) { //$NON-NLS-1$
					if (param.value.equals("folder")) { //$NON-NLS-1$
						this.type = TreeNodeType.FOLDER;
					} else if (param.value.equals("project")) { //$NON-NLS-1$
						this.type = TreeNodeType.PROJECT;
					} else if (param.value.equals("file")) { //$NON-NLS-1$
						this.type = TreeNodeType.FILE;
					}
				} else if (param.name.equals("uri")) { //$NON-NLS-1$
					uri = param.value;
				} else if (param.name.equals("create")) { //$NON-NLS-1$
					if (param.value.equals("true")) { //$NON-NLS-1$
						shouldCreate = true;
					}
				}
			}
		}
	}

}
