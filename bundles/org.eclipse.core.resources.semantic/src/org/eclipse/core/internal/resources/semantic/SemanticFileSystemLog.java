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

import org.eclipse.core.internal.resources.semantic.util.ISemanticFileSystemLog;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * The log implementation
 * 
 */
class SemanticFileSystemLog implements ISemanticFileSystemLog {

	public void log(CoreException logException) {
		ILog log = Platform.getLog(Platform.getBundle(SemanticResourcesPlugin.PLUGIN_ID));
		if (log != null) {
			IStatus originalStatus = logException.getStatus();
			IStatus logStatus;

			logStatus = new Status(originalStatus.getSeverity(), originalStatus.getPlugin(), originalStatus.getCode(), originalStatus.getMessage(),
					logException);

			log.log(logStatus);
		} else {
			logException.printStackTrace();
		}
	}

}
