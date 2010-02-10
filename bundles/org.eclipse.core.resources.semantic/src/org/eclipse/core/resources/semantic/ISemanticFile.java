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
package org.eclipse.core.resources.semantic;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.team.FileModificationValidator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * Provides additional Semantic FS methods for workspace resources of type file.
 * <p>
 * An instance of {@link ISemanticFile} can be obtained from a workspace file
 * resource instance using following code sequence:
 * <p>
 * <code><pre>
 *  IFile file = ...;
 *  ISemanticFile semanticFile = (ISemanticFile) file.getAdapter(ISemanticFile.class);
 *  if ( semanticFile != null ) {
 *    ...				
 *  }
 * </code></pre>
 * </p>
 * 
 * @since 4.0
 * 
 * @see ISemanticResource
 * @see IFile
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISemanticFile extends ISemanticResource {

	/**
	 * @return the adapted file
	 */
	public IFile getAdaptedFile();

	/**
	 * Validates that the file can be modified. The optional shell may be
	 * supplied if UI-based validation is required. If the shell is
	 * <code>null</code>, the responsible content provider must attempt to
	 * perform the validation in a headless manner.
	 * <p>
	 * Validation will be done just before any attempt to change the file, but
	 * only on read-only resources, and it is expected that the read-only flag
	 * is <code>false</code> after successful validation (see
	 * {@link ISemanticResourceInfo#isReadOnly()}).
	 * <p>
	 * The returned status is <code>IStatus.OK</code> if the content provider
	 * believes the file can be modified. Other return statuses indicate the
	 * reason why the file cannot be modified.
	 * <p>
	 * In order to avoid lock extension, a proper validate edit scheduling rule
	 * must be obtained before executing this method.
	 * <p>
	 * 
	 * @param shell
	 *            a <code>org.eclipse.swt.widgets.Shell</code> to aid in
	 *            UI-based validation or <code>null</code> if the validation
	 *            must be headless
	 * @return a status object that is either {@link IStatus#OK} or describes
	 *         reasons why modifying the given file is not reasonable
	 * 
	 * @see IWorkspace#getRuleFactory()
	 * @see IResourceRuleFactory#validateEditRule(org.eclipse.core.resources.IResource[])
	 * @see IWorkspace#validateEdit(IFile[], Object)
	 * @see FileModificationValidator#validateEdit(IFile[],
	 *      org.eclipse.core.resources.team.FileModificationValidationContext)
	 */
	public IStatus validateEdit(Object shell);

	/**
	 * Validates that the given file can be saved.
	 * <p>
	 * This method is called from <code>IFile#setContents</code> and
	 * <code>IFile#appendContents</code> before any attempt to write data to
	 * disk.
	 * <p>
	 * The returned status is <code>IStatus.OK</code> if the responsible content
	 * provider believes the given file can be successfully saved. In all other
	 * cases the return value is a non-OK status. Note that a return value of
	 * <code>IStatus.OK</code> does not guarantee that the save will succeed.
	 * 
	 * @return a status object that is either {@link IStatus#OK} or describes
	 *         reasons why saving the given file is not reasonable
	 * 
	 * @see FileModificationValidator#validateSave(IFile)
	 * @see IFile#setContents(InputStream, int, IProgressMonitor)
	 * @see IFile#appendContents(InputStream, int, IProgressMonitor)
	 */
	public IStatus validateSave();

	/**
	 * Reverts the changes that may exist in the workspace for this resource.
	 * <p>
	 * After this operation, the workspace resource should represent the state
	 * and the content of this resource in the repository.
	 * <p>
	 * Unless in case of local-only resources, after this operation, the
	 * resource must be read-only.
	 * <p>
	 * Some other resources may be also added as result of this operation. In
	 * order to avoid lock extension, a proper modify scheduling rule must be
	 * obtained before executing this operation.
	 * <p>
	 * Unless {@link ISemanticFileSystem#SUPPRESS_REFRESH} is specified in the
	 * options, this resource will be refreshed locally.
	 * 
	 * @param options
	 *            the options as specified in {@link ISemanticResource}
	 * 
	 * @param monitor
	 *            a progress monitor, or <code>null</code> if progress reporting
	 *            is not desired
	 * @throws CoreException
	 *             in case of operation failure
	 * 
	 * @see IWorkspace#getRuleFactory()
	 * @see IResourceRuleFactory#modifyRule(IResource)
	 * 
	 */
	public void revertChanges(int options, IProgressMonitor monitor) throws CoreException;

}
