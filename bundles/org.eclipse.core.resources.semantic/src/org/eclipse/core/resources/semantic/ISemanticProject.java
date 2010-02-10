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

import org.eclipse.core.resources.IProject;

/**
 * Provides additional Semantic FS methods for workspace resources of type
 * project.
 * <p>
 * Semantic Projects are the entry point into the Semantic File System.
 * <p>
 * A typical code sequence for creating a Semantic Project would look like this:
 * <p>
 * <code>
 * <pre>
 * 
 * 	...
 * 	String projectName = ...
 * 	final IWorkspace workspace = ResourcesPlugin.getWorkspace();
 * 	final IProject project = workspace.getRoot().getProject(projectName);
 * 
 * 	IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {
 * 		public void run(IProgressMonitor monitor) throws CoreException {
 * 		
 * 			IProjectDescription description = workspace.newProjectDescription(projectName);
 * 			// nature handling could be done here
 * 			try {
 * 				// we have to map the project to a location in the Semantic File System
 * 				description.setLocationURI(new URI(ISemanticFileSystem.SCHEME + ":/" + projectName)); //$NON-NLS-1$
 * 			} catch (URISyntaxException e) {
 * 				// really not likely, though
 * 				throw new RuntimeException(e);
 * 			}
 * 			project.create(description, monitor);
 * 			project.open(monitor);
 *  
 * 			// map this to the SFS team provider
 * 			RepositoryProvider.map(project, ISemanticFileSystem.SFS_REPOSITORY_PROVIDER);
 * 			// now we can start with semantic work, e.g. add some persistent properties
 * 			ISemanticProject semanticProject = (ISemanticProject) project.getAdapter(ISemanticProject.class);
 * 			spr.setPersistentProperty(...);
 * 			...
 * 
 * 		}
 * 	};
 * 
 * 	workspace.run(myRunnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);
 * 		
 * </pre>
 * </code>
 * <p>
 * An instance of {@link ISemanticProject} can be obtained from an existing
 * project instance using the following code sequence:
 * <p>
 * <code><pre>
 *  IProject project = ...;
 *  ISemanticProject semanticProject = (ISemanticProject) project.getAdapter(ISemanticProject.class);
 *  // this will return null if the project is not a Semantic Project
 *  if ( semanticProject != null ) {
 *    ...				
 *  }
 * </code></pre>
 * 
 * @since 4.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISemanticProject extends ISemanticFolder {
	/**
	 * @return the adapted project
	 */
	IProject getAdaptedProject();
}
