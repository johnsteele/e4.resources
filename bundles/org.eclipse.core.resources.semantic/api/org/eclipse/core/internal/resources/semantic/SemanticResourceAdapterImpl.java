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

import java.net.URI;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.resources.semantic.util.ISemanticFileSystemTrace;
import org.eclipse.core.internal.resources.semantic.util.TraceLocation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.resources.semantic.ISemanticResourceInfo;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.RepositoryProvider;

/**
 * The {@link ISemanticResource} base implementation.
 * 
 */
public abstract class SemanticResourceAdapterImpl implements ISemanticResource {

	protected final ISemanticFileSystemTrace myTrace;

	/**
	 * Used for rule checking
	 * 
	 */
	protected enum RuleType {
		CREATE, DELETE, MODIFY, VALIDATE_EDIT
	}

	private final IResource resource;

	SemanticResourceAdapterImpl(IResource resource, ISemanticFileSystem fileSystem) {
		this.resource = resource;
		this.myTrace = fileSystem.getTrace();
	}

	public IResource getAdaptedResource() {
		return this.resource;
	}

	/**
	 * 
	 * @return store instance
	 * @throws CoreException
	 *             if store can not be determined
	 */
	protected ISemanticFileStoreInternal getOwnStore() throws CoreException {
		URI uri = this.resource.getLocationURI();

		try {
			IFileStore store = EFS.getStore(uri);

			if (store instanceof ISemanticFileStoreInternal) {
				return (ISemanticFileStoreInternal) store;
			} else if (store.getFileSystem() == EFS.getNullFileSystem()) {
				throw new SemanticResourceException(SemanticResourceStatusCode.STORE_NOT_FOUND, this.resource.getFullPath(),
						Messages.SemanticResourceAdapterImpl_NullFile_XMSG);
			} else {
				// quite unlikely (the URI was changed on the resource, e.g. on
				// a project or symbolic link)
				throw new SemanticResourceException(SemanticResourceStatusCode.STORE_NOT_FOUND, this.resource.getFullPath(), NLS.bind(
						Messages.SemanticResourceAdapterImpl_NoSemanticStore_XMSG, uri.toString()));
			}
		} catch (CoreException e) {
			throw new SemanticResourceException(SemanticResourceStatusCode.STORE_NOT_FOUND, this.resource.getFullPath(), null, e);
		}
	}

	protected ISchedulingRule checkCurrentRule(RuleType ruleType) throws CoreException {
		Job job = Job.getJobManager().currentJob();

		if (job != null) {

			ISchedulingRule checkRule = getRuleForType(ruleType, this.resource);

			ISchedulingRule rule = job.getRule();
			if (rule != null) {

				if (!rule.contains(checkRule)) {
					throw new SemanticResourceException(SemanticResourceStatusCode.LOCK_CONFLICT, this.resource.getFullPath(),
							Messages.SemanticResourceAdapterImpl_OperationNotCoveredByRule_XMSG);
				}
			} else {
				// TODO 0.1: we need to investigate again what to do when rule
				// is null
				this.myTrace.trace(TraceLocation.CORE, new SemanticResourceException(
						SemanticResourceStatusCode.CALLED_OUTSIDE_OF_SCHEDULING_RULE, this.resource.getFullPath(),
						Messages.SemanticResourceAdapterImpl_JobNoRule_XMSG));

			}
			return checkRule;

		}

		throw new SemanticResourceException(SemanticResourceStatusCode.CALLED_OUTSIDE_OF_SCHEDULING_RULE, this.resource.getFullPath(),
				Messages.SemanticResourceAdapterImpl_CalledOutsideRule_XMSG);

	}

	private ISchedulingRule getRuleForType(RuleType ruleType, IResource actResource) throws SemanticResourceException {

		if (actResource instanceof IWorkspaceRoot) {
			return actResource;
		}
		// TODO 0.1: check life cycle issues with project close/delete
		IProject project = actResource.getProject();
		if (!project.isAccessible()) {
			// project closed or deleted
			throw new SemanticResourceException(SemanticResourceStatusCode.PROJECT_NOT_ACCESSIBLE, actResource.getFullPath(), NLS.bind(
					Messages.SemanticResourceAdapterImpl_ProjectNotAccessible_XMSG, project.getName()));
		}

		// we obtain the "expected" rule from the content provider
		RepositoryProvider provider = RepositoryProvider.getProvider(project, ISemanticFileSystem.SFS_REPOSITORY_PROVIDER);
		if (provider == null) {
			throw new SemanticResourceException(SemanticResourceStatusCode.PROJECT_NOT_MAPPED, actResource.getFullPath(), NLS.bind(
					Messages.SemanticResourceAdapterImpl_ProjectNotMapped_XMSG, project.getName()));
		}
		IResourceRuleFactory rf = provider.getRuleFactory();

		ISchedulingRule checkRule;

		switch (ruleType) {
		case DELETE:
			checkRule = rf.deleteRule(actResource);
			break;
		case CREATE:
			checkRule = rf.createRule(actResource);
			break;
		case MODIFY:
			checkRule = rf.modifyRule(actResource);
			break;
		case VALIDATE_EDIT:
			checkRule = rf.validateEditRule(new IResource[] { actResource });
			break;
		default:
			throw new RuntimeException(Messages.SemanticResourceAdapterImpl_UnknownRuleType_XMSG);
		}
		return checkRule;
	}

	protected void refreshLocalIfNeeded(RuleType ruleType, ISchedulingRule rule, int options, IProgressMonitor monitor)
			throws CoreException {
		if ((options & ISemanticFileSystem.SUPPRESS_REFRESH) == 0) {

			if (rule instanceof IResource) {
				((IResource) rule).refreshLocal(IResource.DEPTH_INFINITE, monitor);
				return;
			}

			throw new SemanticResourceException(SemanticResourceStatusCode.AUTO_REFRESH, new Path(""), NLS.bind( //$NON-NLS-1$
					Messages.SemanticResourceAdapterImpl_RuleNoResource_XMSG, ruleType.name()));
		}
	}

	public void deleteRemotely(int options, IProgressMonitor monitor) throws CoreException {

		ISchedulingRule rule = checkCurrentRule(RuleType.DELETE);

		ISemanticFileStoreInternal store = getOwnStore();

		store.deleteRemotely(monitor);

		refreshLocalIfNeeded(RuleType.DELETE, rule, options, monitor);

	}

	public void remove(int options, IProgressMonitor monitor) throws CoreException {

		ISchedulingRule rule = checkCurrentRule(RuleType.DELETE);

		ISemanticFileStoreInternal store = getOwnStore();

		store.removeFromWorkspace(monitor);

		refreshLocalIfNeeded(RuleType.DELETE, rule, options, monitor);

	}

	public IStatus validateRemoteDelete(Object shell) {
		try {
			ISemanticFileStoreInternal store = getOwnStore();

			return store.validateRemoteDelete(shell);
		} catch (CoreException e) {
			this.myTrace.trace(TraceLocation.CORE, e);
			return e.getStatus();
		}
	}

	public IStatus validateRemove(int options, IProgressMonitor monitor) {
		try {
			ISemanticFileStoreInternal store = getOwnStore();

			return store.validateRemove(options, monitor);
		} catch (CoreException e) {
			this.myTrace.trace(TraceLocation.CORE, e);
			return e.getStatus();
		}
	}

	public void synchronizeContentWithRemote(SyncDirection direction, int options, IProgressMonitor monitor) throws CoreException {
		ISchedulingRule rule = checkCurrentRule(RuleType.MODIFY);

		ISemanticFileStoreInternal store = getOwnStore();

		store.synchronizeContentWithRemote(direction, monitor);

		refreshLocalIfNeeded(RuleType.MODIFY, rule, options, monitor);
	}

	public ISemanticResourceInfo fetchResourceInfo(int options, IProgressMonitor monitor) throws CoreException {
		ISemanticFileStoreInternal store = getOwnStore();

		return store.fetchResourceInfo(options, monitor);
	}

	public String getContentProviderID() throws CoreException {
		ISemanticFileStoreInternal store = getOwnStore();

		return store.getContentProviderID();
	}

	public IStatus lockResource(int options, IProgressMonitor monitor) throws CoreException {
		// we check the MODIFY rule
		ISchedulingRule rule = checkCurrentRule(RuleType.MODIFY);
		ISemanticFileStoreInternal store = getOwnStore();

		IStatus result = store.lockResource(monitor);

		if (result.isOK()) {
			refreshLocalIfNeeded(RuleType.MODIFY, rule, options, monitor);
		}

		return result;
	}

	public IStatus unlockResource(int options, IProgressMonitor monitor) throws CoreException {

		// we check the MODIFY rule
		ISchedulingRule rule = checkCurrentRule(RuleType.MODIFY);
		ISemanticFileStoreInternal store = getOwnStore();

		IStatus result = store.unlockResource(monitor);

		if (result.isOK()) {
			refreshLocalIfNeeded(RuleType.MODIFY, rule, options, monitor);
		}

		return result;
	}

	public Map<QualifiedName, String> getPersistentProperties() throws CoreException {
		ISemanticFileStoreInternal store = getOwnStore();

		return store.getPersistentProperties();
	}

	public String getPersistentProperty(QualifiedName key) throws CoreException {
		ISemanticFileStoreInternal store = getOwnStore();

		return store.getPersistentProperty(key);
	}

	public Map<QualifiedName, Object> getSessionProperties() throws CoreException {
		ISemanticFileStoreInternal store = getOwnStore();

		return store.getSessionProperties();
	}

	public Object getSessionProperty(QualifiedName key) throws CoreException {
		ISemanticFileStoreInternal store = getOwnStore();

		return store.getSessionProperty(key);
	}

	public void setPersistentProperty(QualifiedName key, String value) throws CoreException {
		// we check the MODIFY rule
		checkCurrentRule(RuleType.MODIFY);
		ISemanticFileStoreInternal store = getOwnStore();

		store.setPersistentProperty(key, value);

	}

	public void setSessionProperty(QualifiedName key, Object value) throws CoreException {
		// we check the MODIFY rule
		checkCurrentRule(RuleType.MODIFY);
		ISemanticFileStoreInternal store = getOwnStore();

		store.setSessionProperty(key, value);

	}

	public void setRemoteURI(URI uri, int options, IProgressMonitor monitor) throws CoreException {
		ISchedulingRule rule = checkCurrentRule(RuleType.MODIFY);

		ISemanticFileStoreInternal store = getOwnStore();

		store.setRemoteURI(uri, monitor);

		refreshLocalIfNeeded(RuleType.MODIFY, rule, options, monitor);
	}

}