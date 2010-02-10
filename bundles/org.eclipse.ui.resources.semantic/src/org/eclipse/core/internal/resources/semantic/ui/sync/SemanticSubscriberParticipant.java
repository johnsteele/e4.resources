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
package org.eclipse.core.internal.resources.semantic.ui.sync;

import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;

/**
 * The Semantic FIle Systems Subscriber participant
 * 
 */
public class SemanticSubscriberParticipant extends SubscriberParticipant {

	private final ISemanticFileSystem myFs;

	/**
	 * @param fs
	 *            the Semantic File System
	 * @param threeWay
	 *            if three-way compare is supported
	 */
	public SemanticSubscriberParticipant(ISemanticFileSystem fs, boolean threeWay) {
		super();
		this.myFs = fs;
		try {
			setInitializationData(TeamUI.getSynchronizeManager().getParticipantDescriptor(
					"org.eclipse.core.resources.semantic.participant1")); //$NON-NLS-1$
		} catch (CoreException e) {
			this.myFs.getLog().log(e);
		}
		setSecondaryId(Long.toString(System.currentTimeMillis()));
		setSubscriber(new SemanticSubscriber(threeWay));
	}

}
