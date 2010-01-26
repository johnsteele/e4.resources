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

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.resources.semantic.util.ISemanticFileSystemTrace;
import org.eclipse.core.internal.resources.semantic.util.TraceLocation;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

/**
 * Delegates to the responsible content provider
 * 
 */
public class DelegatingResourceRuleFactory implements IResourceRuleFactory {

	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	private ISemanticFileSystemTrace trace;

	/**
	 * @param actSfs
	 *            the SFS
	 */
	public DelegatingResourceRuleFactory(ISemanticFileSystem actSfs) {
		this.trace = actSfs.getTrace();
	}

	public ISchedulingRule buildRule() {
		// since this has no resource parameter, we can't obtain the provider
		return this.root;
	}

	public ISchedulingRule charsetRule(IResource resource) {
		try {
			ISemanticResource sres = (ISemanticResource) resource.getAdapter(ISemanticResource.class);
			if (sres != null) {
				ISemanticFileStore sfs = (ISemanticFileStore) EFS.getStore(resource.getLocationURI());

				return toRule(sfs.getEffectiveContentProvider().getRuleFactory().charsetRule(sfs));
			}
		} catch (CoreException e) {
			this.trace.trace(TraceLocation.RULEFACTORY, e);
			// $JL-EXC$ ignore here
			return this.root;
		}
		return this.root;
	}

	public ISchedulingRule copyRule(IResource source, IResource destination) {

		try {
			ISemanticResource sourceres = (ISemanticResource) source.getAdapter(ISemanticResource.class);
			ISemanticResource desinationres = (ISemanticResource) destination.getAdapter(ISemanticResource.class);
			if (sourceres != null && desinationres != null) {

				ISemanticFileStore sourceStore = (ISemanticFileStore) EFS.getStore(source.getLocationURI());
				ISemanticFileStore destinationStore = (ISemanticFileStore) EFS.getStore(destination.getLocationURI());

				if (sourceStore.getEffectiveContentProvider().getClass().getName().equals(
				// source and target provider are the same
						destinationStore.getEffectiveContentProvider().getClass().getName())) {
					return toRule(sourceStore.getEffectiveContentProvider().getRuleFactory().copyRule(sourceStore, destinationStore));
				}
				// if source and target come from different providers, return
				// MultiRule
				return toRule(sourceStore.getEffectiveContentProvider().getRuleFactory().copyRule(sourceStore, destinationStore),
						destinationStore.getEffectiveContentProvider().getRuleFactory().copyRule(sourceStore, destinationStore));
			}
		} catch (CoreException e) {
			this.trace.trace(TraceLocation.RULEFACTORY, e);
			// $JL-EXC$ ignore here
			return this.root;
		}
		return this.root;
	}

	public ISchedulingRule createRule(IResource resource) {
		try {
			ISemanticResource sres = (ISemanticResource) resource.getAdapter(ISemanticResource.class);
			if (sres != null) {

				ISemanticFileStore sfs = (ISemanticFileStore) EFS.getStore(resource.getLocationURI());
				ISemanticContentProvider effectiveProvider = sfs.getEffectiveContentProvider();
				// federation: we ask the parent provider to determine the
				// proper rule
				if (effectiveProvider.getRootStore().getPath().equals(sfs.getPath())) {
					ISemanticFileStore parentStore = (ISemanticFileStore) sfs.getParent();
					if (parentStore != null) {
						return toRule(parentStore.getEffectiveContentProvider().getRuleFactory().createRule(sfs));
					}
				}
				return toRule(effectiveProvider.getRuleFactory().createRule(sfs));
			}
		} catch (CoreException e) {
			this.trace.trace(TraceLocation.RULEFACTORY, e);
			// $JL-EXC$ ignore here
			return this.root;
		}
		return this.root;
	}

	public ISchedulingRule deleteRule(IResource resource) {
		try {
			ISemanticResource sres = (ISemanticResource) resource.getAdapter(ISemanticResource.class);
			if (sres != null) {

				ISemanticFileStore sfs = (ISemanticFileStore) EFS.getStore(resource.getLocationURI());
				ISemanticContentProvider effectiveProvider = sfs.getEffectiveContentProvider();
				// federation: we ask the parent provider to determine the
				// proper rule
				if (effectiveProvider.getRootStore().getPath().equals(sfs.getPath())) {
					ISemanticFileStore parentStore = (ISemanticFileStore) sfs.getParent();
					if (parentStore != null) {
						return toRule(parentStore.getEffectiveContentProvider().getRuleFactory().deleteRule(sfs));
					}
				}
				return toRule(effectiveProvider.getRuleFactory().deleteRule(sfs));

			}
		} catch (CoreException e) {
			this.trace.trace(TraceLocation.RULEFACTORY, e);
			// $JL-EXC$ ignore here
			return this.root;
		}
		return this.root;
	}

	public ISchedulingRule markerRule(IResource resource) {

		// TODO 0.2: the current org.eclipse.core.internal.resources.Rules
		// implementation does not delegate to this method

		return null;

		// try {
		// ISemanticResource sres = (ISemanticResource)
		// resource.getAdapter(ISemanticResource.class);
		// if (sres != null) {
		// ISemanticFileStore sfs = (ISemanticFileStore)
		// EFS.getStore(resource.getLocationURI());
		//
		// return
		// toRule(sfs.getEffectiveContentProvider().getRuleFactory().markerRule(sfs));
		// }
		// } catch (CoreException e) {
		// SemanticFileSystemTrace.trace(TraceLocation.RULEFACTORY, e);
		// // $JL-EXC$ ignore here
		// return this.root;
		// }
		// return this.root;
	}

	public ISchedulingRule modifyRule(IResource resource) {
		try {
			ISemanticResource sres = (ISemanticResource) resource.getAdapter(ISemanticResource.class);
			if (sres != null) {
				ISemanticFileStore sfs = (ISemanticFileStore) EFS.getStore(resource.getLocationURI());

				return toRule(sfs.getEffectiveContentProvider().getRuleFactory().modifyRule(sfs));
			}
		} catch (CoreException e) {
			this.trace.trace(TraceLocation.RULEFACTORY, e);
			// $JL-EXC$ ignore here
			return this.root;
		}
		return this.root;
	}

	public ISchedulingRule moveRule(IResource source, IResource destination) {

		try {
			ISemanticResource sourceres = (ISemanticResource) source.getAdapter(ISemanticResource.class);
			ISemanticResource desinationres = (ISemanticResource) destination.getAdapter(ISemanticResource.class);
			if (sourceres != null && desinationres != null) {

				ISemanticFileStore sourceStore = (ISemanticFileStore) EFS.getStore(source.getLocationURI());
				ISemanticFileStore destinationStore = (ISemanticFileStore) EFS.getStore(destination.getLocationURI());
				// if source and target come from different providers, return
				// root
				if (sourceStore.getEffectiveContentProvider().getClass().getName().equals(
						destinationStore.getEffectiveContentProvider().getClass().getName())) {
					return toRule(sourceStore.getEffectiveContentProvider().getRuleFactory().moveRule(sourceStore, destinationStore));
				}
				return toRule(sourceStore.getEffectiveContentProvider().getRuleFactory().moveRule(sourceStore, destinationStore),
						destinationStore.getEffectiveContentProvider().getRuleFactory().moveRule(sourceStore, destinationStore));
			}
		} catch (CoreException e) {
			this.trace.trace(TraceLocation.RULEFACTORY, e);
			// $JL-EXC$ ignore here
			return this.root;
		}
		return this.root;
	}

	public ISchedulingRule refreshRule(IResource resource) {
		try {
			ISemanticResource sres = (ISemanticResource) resource.getAdapter(ISemanticResource.class);
			if (sres != null) {

				ISemanticFileStore sfs = (ISemanticFileStore) EFS.getStore(resource.getLocationURI());
				ISemanticContentProvider effectiveProvider = sfs.getEffectiveContentProvider();
				// // federation: we ask the parent provider to determine the
				// proper rule
				// if
				// (effectiveProvider.getRootStore().getPath().equals(sfs.getPath()))
				// {
				// ISemanticFileStore parentStore = (ISemanticFileStore)
				// sfs.getParent();
				// if (parentStore != null) {
				// return
				// toRule(parentStore.getEffectiveContentProvider().getRuleFactory().refreshRule(sfs));
				// }
				// }
				return toRule(effectiveProvider.getRuleFactory().refreshRule(sfs));

			}
		} catch (CoreException e) {
			this.trace.trace(TraceLocation.RULEFACTORY, e);
			// $JL-EXC$ ignore here
			return this.root;
		}
		return this.root;
	}

	public ISchedulingRule validateEditRule(IResource[] resources) {

		ISemanticFileStore[] stores = allStoresFromSameProvider(resources);
		if (stores != null) {
			// all resources belong to the same content provider
			try {
				ISemanticResource sres = (ISemanticResource) resources[0].getAdapter(ISemanticResource.class);
				if (sres != null) {

					return toRule(stores[0].getEffectiveContentProvider().getRuleFactory().validateEditRule(stores));
				}
			} catch (CoreException e) {
				this.trace.trace(TraceLocation.RULEFACTORY, e);
				// $JL-EXC$ ignore here
				return this.root;
			}
		} else {
			Set<ISemanticFileStore> ruleStores = new HashSet<ISemanticFileStore>();
			// we could collect the resources per effective content provider and
			// call each provider once for optimization, but then it's not clear
			// whether
			// each provider would get all resources or just the ones they are
			// responsible for,
			// so let's do it resource by resource for the time being
			try {
				for (IResource res : resources) {
					ISemanticResource sres = (ISemanticResource) res.getAdapter(ISemanticResource.class);
					if (sres != null) {

						IFileStore fs = EFS.getStore(res.getLocationURI());
						if (fs instanceof ISemanticFileStore) {
							ISemanticFileStore sfs = (ISemanticFileStore) fs;
							ISemanticFileStore ruleStore = sfs.getEffectiveContentProvider().getRuleFactory().validateEditRule(
									new ISemanticFileStore[] { sfs });
							if (ruleStore != null) {
								ruleStores.add(ruleStore);
							} else {
								return this.root;
							}
						}

					}
				}
				return toRule(ruleStores.toArray(new ISemanticFileStore[0]));
			} catch (CoreException e) {
				this.trace.trace(TraceLocation.RULEFACTORY, e);
				// $JL-EXC$ ignore here
			}
		}
		return this.root;
	}

	/*
	 * Added for compatibility with Eclipse 3.6
	 */
	public ISchedulingRule derivedRule(@SuppressWarnings("unused") IResource resource) {
		// TODO 0.1 add implementation
		return null;
	}

	private ISchedulingRule toRule(ISemanticFileStore... stores) throws CoreException {

		if (stores.length == 1) {
			// single rule
			if (stores[0] == null) {
				// if the rule is null, return the root
				return this.root;
			}
			IPath path = stores[0].getPath();
			IResource rule = this.root.findMember(path);
			// we find the first existing resource
			while ((rule == null || !rule.exists()) && path.segmentCount() > 0) {
				path = path.removeLastSegments(1);
				rule = this.root.findMember(path);
			}
			if (rule == null || !rule.exists()) {
				throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_FOR_STORE_NOT_FOUND, stores[0].getPath(),
						MessageFormat.format(Messages.DelegatingResourceRuleFactory_NoExistingParentFound_XMSG, stores[0].getPath()
								.toString()));
			}

			return rule;
		}

		ISchedulingRule[] rules = new ISchedulingRule[stores.length];

		for (int i = 0; i < stores.length; i++) {
			// MultiRule
			ISemanticFileStore store = stores[i];
			if (store == null) {
				// null translates to workspace root, so we don't nee a multi
				// rule here
				return this.root;
			}
			IPath path = stores[i].getPath();
			IResource rule = this.root.findMember(path);
			while ((rule == null || !rule.exists()) && path.segmentCount() > 0) {
				path = path.removeLastSegments(1);
				rule = this.root.findMember(path);
			}
			if (rule == null || !rule.exists()) {
				throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_FOR_STORE_NOT_FOUND, store.getPath(), MessageFormat
						.format(Messages.DelegatingResourceRuleFactory_NoExistingParentFound_XMSG, stores[0].getPath().toString()));
			}
			rules[i] = rule;
		}
		return new MultiRule(rules);
	}

	private ISemanticFileStore[] allStoresFromSameProvider(IResource... resources) {

		ISemanticFileStore[] result = new ISemanticFileStore[resources.length];

		String lastName = null;
		for (int i = 0; i < resources.length; i++) {
			IResource res = resources[i];
			try {
				IFileStore store = EFS.getStore(res.getLocationURI());
				if (!(store instanceof ISemanticFileStore)) {
					return null;
				}
				result[i] = (ISemanticFileStore) store;
				String className = result[i].getEffectiveContentProvider().getClass().getName();
				if (lastName != null && !lastName.equals(className)) {
					return null;
				}
				lastName = className;
			} catch (CoreException e) {
				// $JL-EXC$ ignore
				return null;
			}
		}
		return result;
	}

}
