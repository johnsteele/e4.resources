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

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.team.FileModificationValidator;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.history.IFileHistoryProvider;

/**
 * The Semantic Repository Provider
 * 
 */
@SuppressWarnings("deprecation")
public class DelegatingRepositoryProviderStub extends RepositoryProvider {

	private ISemanticFileSystem mySfs;

	public void configureProject() throws CoreException {
		this.getProject();

	}

	public String getID() {
		return ISemanticFileSystem.SFS_REPOSITORY_PROVIDER;
	}

	public void deconfigure() throws CoreException {
		// nothing to do
	}

	public IResourceRuleFactory getRuleFactory() {
		return new DelegatingResourceRuleFactory(getFileSystem());
	}

	public boolean canHandleLinkedResourceURI() {
		return true;
	}

	@Deprecated
	public IFileModificationValidator getFileModificationValidator() {
		return getFileModificationValidator2();
	}

	public FileModificationValidator getFileModificationValidator2() {
		return new SemanticFileModificationValidator();
	}

	public IMoveDeleteHook getMoveDeleteHook() {
		return new MoveDeleteHook(getFileSystem());
	}

	public IStatus validateCreateLink(IResource resource, int updateFlags, URI location) {
		return super.validateCreateLink(resource, updateFlags, location);
	}

	public IFileHistoryProvider getFileHistoryProvider() {
		return new SemanticFileHistoryProvider(getFileSystem());
	}

	private ISemanticFileSystem getFileSystem() {
		if (this.mySfs == null) {
			// TODO 0.1: dependency injection
			try {
				this.mySfs = (ISemanticFileSystem) EFS.getFileSystem(ISemanticFileSystem.SCHEME);
			} catch (CoreException e) {
				// $JL-EXC$
				this.mySfs = null;
			}
		}
		return this.mySfs;
	}
}
