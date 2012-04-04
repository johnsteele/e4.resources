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
import org.eclipse.core.internal.resources.semantic.SfsTraceLocation;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.resources.semantic.SemanticResourceException;
import org.eclipse.core.resources.semantic.SemanticResourceStatusCode;
import org.eclipse.core.resources.semantic.spi.ISemanticContentProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.ISemanticResourceRuleFactory;
import org.eclipse.core.resources.team.ResourceRuleFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

/**
 * Delegates to the responsible content provider
 * 
 */
public class DelegatingResourceRuleFactory extends ResourceRuleFactory {

	protected enum RuleType {
		CREATE, DELETE, MODIFY, VALIDATE_EDIT, REFRESH, CHARSET, COPY, MOVE
	}

	final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

	/**
	 * @param actSfs
	 *            the SFS
	 */
	public DelegatingResourceRuleFactory(ISemanticFileSystem actSfs) {
		// nothing
	}

	@Override
	public ISchedulingRule charsetRule(IResource resource) {

		if (SfsTraceLocation.RULEFACTORY.isActive()) {
			SfsTraceLocation.getTrace().traceEntry(SfsTraceLocation.RULEFACTORY.getLocation(), resource.getFullPath().toString());
		}

		ISchedulingRule result;
		try {
			ISemanticResource sres = (ISemanticResource) resource.getAdapter(ISemanticResource.class);
			if (sres != null) {
				ISemanticFileStore sfs = (ISemanticFileStore) EFS.getStore(resource.getLocationURI());

				result = toRule(new IResource[] {resource}, RuleType.CHARSET, sfs.getEffectiveContentProvider().getRuleFactory()
						.charsetRule(sfs));
			} else {
				if (SfsTraceLocation.RULEFACTORY.isActive()) {
					SfsTraceLocation.getTrace().trace(SfsTraceLocation.RULEFACTORY.getLocation(),
							Messages.DelegatingResourceRuleFactory_ResourceNotAdapter_XMSG);
				}
				result = this.root;
			}
		} catch (CoreException e) {
			if (SfsTraceLocation.RULEFACTORY.isActive()) {
				SfsTraceLocation.getTrace().trace(SfsTraceLocation.RULEFACTORY.getLocation(), e.getMessage(), e);
			}
			// $JL-EXC$ ignore here
			result = this.root;
		}

		if (SfsTraceLocation.RULEFACTORY.isActive()) {
			if (result != null && result instanceof IResource) {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.RULEFACTORY.getLocation(),
						((IResource) result).getFullPath().toString());
			} else {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.RULEFACTORY.getLocation(), result);
			}
		}
		return new MultiRule(new ISchedulingRule[] {result, super.charsetRule(resource)});
	}

	@Override
	public ISchedulingRule copyRule(IResource source, IResource destination) {

		if (SfsTraceLocation.RULEFACTORY.isActive()) {
			SfsTraceLocation.getTrace().traceEntry(SfsTraceLocation.RULEFACTORY.getLocation(), destination.getFullPath().toString());
		}

		ISchedulingRule result;
		try {
			ISemanticResource sourceres = (ISemanticResource) source.getAdapter(ISemanticResource.class);
			ISemanticResource desinationres = (ISemanticResource) destination.getAdapter(ISemanticResource.class);
			if (sourceres != null && desinationres != null) {

				ISemanticFileStore sourceStore = (ISemanticFileStore) EFS.getStore(source.getLocationURI());
				ISemanticFileStore destinationStore = (ISemanticFileStore) EFS.getStore(destination.getLocationURI());

				if (sourceStore.getEffectiveContentProvider().getClass().getName().equals(
				// source and target provider are the same
						destinationStore.getEffectiveContentProvider().getClass().getName())) {
					result = toRule(new IResource[] {source}, RuleType.COPY, sourceStore.getEffectiveContentProvider().getRuleFactory()
							.copyRule(sourceStore, destinationStore));
				}
				// if source and target come from different providers, return
				// MultiRule
				result = toRule(new IResource[] {source, destination}, RuleType.COPY, sourceStore.getEffectiveContentProvider()
						.getRuleFactory().copyRule(sourceStore, destinationStore), destinationStore.getEffectiveContentProvider()
						.getRuleFactory().copyRule(sourceStore, destinationStore));
			} else {
				if (SfsTraceLocation.RULEFACTORY.isActive()) {
					SfsTraceLocation.getTrace().trace(SfsTraceLocation.RULEFACTORY.getLocation(),
							Messages.DelegatingResourceRuleFactory_ResourceNotAdapter_XMSG);
				}
				result = this.root;
			}
		} catch (CoreException e) {
			if (SfsTraceLocation.RULEFACTORY.isActive()) {
				SfsTraceLocation.getTrace().trace(SfsTraceLocation.RULEFACTORY.getLocation(), e.getMessage(), e);
			}
			// $JL-EXC$ ignore here
			result = this.root;
		}

		if (SfsTraceLocation.RULEFACTORY.isActive()) {
			if (result != null && result instanceof IResource) {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.RULEFACTORY.getLocation(),
						((IResource) result).getFullPath().toString());
			} else {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.RULEFACTORY.getLocation(), result);
			}
		}

		return result;
	}

	@Override
	public ISchedulingRule createRule(IResource resource) {

		if (SfsTraceLocation.RULEFACTORY.isActive()) {
			SfsTraceLocation.getTrace().traceEntry(SfsTraceLocation.RULEFACTORY.getLocation(), resource.getFullPath().toString());
		}

		ISchedulingRule result;

		try {
			result = this.calculateRuleForType2(resource, RuleType.CREATE);
		} catch (CoreException e) {
			if (SfsTraceLocation.RULEFACTORY.isActive()) {
				SfsTraceLocation.getTrace().trace(SfsTraceLocation.RULEFACTORY.getLocation(), e.getMessage(), e);
			}
			// $JL-EXC$ ignore here
			result = this.root;
		}

		if (SfsTraceLocation.RULEFACTORY.isActive()) {
			if (result != null && result instanceof IResource) {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.RULEFACTORY.getLocation(),
						((IResource) result).getFullPath().toString());
			} else {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.RULEFACTORY.getLocation(), result);
			}
		}
		return result;
	}

	@Override
	public ISchedulingRule deleteRule(IResource resource) {

		if (SfsTraceLocation.RULEFACTORY.isActive()) {
			SfsTraceLocation.getTrace().traceEntry(SfsTraceLocation.RULEFACTORY.getLocation(), resource.getFullPath().toString());
		}

		ISchedulingRule result;
		try {
			result = this.calculateRuleForType2(resource, RuleType.DELETE);
		} catch (CoreException e) {
			if (SfsTraceLocation.RULEFACTORY.isActive()) {
				SfsTraceLocation.getTrace().trace(SfsTraceLocation.RULEFACTORY.getLocation(), e.getMessage(), e);
			}
			// $JL-EXC$ ignore here
			result = this.root;
		}

		if (SfsTraceLocation.RULEFACTORY.isActive()) {
			if (result != null && result instanceof IResource) {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.RULEFACTORY.getLocation(),
						((IResource) result).getFullPath().toString());
			} else {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.RULEFACTORY.getLocation(), result);
			}
		}

		return result;

	}

	@Override
	public ISchedulingRule modifyRule(IResource resource) {

		if (SfsTraceLocation.RULEFACTORY.isActive()) {
			SfsTraceLocation.getTrace().traceEntry(SfsTraceLocation.RULEFACTORY.getLocation(), resource.getFullPath().toString());
		}

		ISchedulingRule result;
		try {
			ISemanticResource sres = (ISemanticResource) resource.getAdapter(ISemanticResource.class);
			if (sres != null) {
				ISemanticFileStore sfs = (ISemanticFileStore) EFS.getStore(resource.getLocationURI());

				result = toRule(new IResource[] {resource}, RuleType.MODIFY,
						sfs.getEffectiveContentProvider().getRuleFactory().modifyRule(sfs));
			} else {
				if (SfsTraceLocation.RULEFACTORY.isActive()) {
					SfsTraceLocation.getTrace().trace(SfsTraceLocation.RULEFACTORY.getLocation(),
							Messages.DelegatingResourceRuleFactory_ResourceNotAdapter_XMSG);
				}
				result = this.root;
			}
		} catch (CoreException e) {
			if (SfsTraceLocation.RULEFACTORY.isActive()) {
				SfsTraceLocation.getTrace().trace(SfsTraceLocation.RULEFACTORY.getLocation(), e.getMessage(), e);
			}
			// $JL-EXC$ ignore here
			result = this.root;
		}

		if (SfsTraceLocation.RULEFACTORY.isActive()) {
			if (result != null && result instanceof IResource) {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.RULEFACTORY.getLocation(),
						((IResource) result).getFullPath().toString());
			} else {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.RULEFACTORY.getLocation(), result);
			}
		}

		ISchedulingRule superRule = super.modifyRule(resource);
		if (superRule.contains(result)) {
			return superRule;
		}
		return result;
	}

	@Override
	public ISchedulingRule moveRule(IResource source, IResource destination) {

		if (SfsTraceLocation.RULEFACTORY.isActive()) {
			SfsTraceLocation.getTrace().traceEntry(SfsTraceLocation.RULEFACTORY.getLocation(),
					new Object[] {source.getFullPath().toString(), destination.getFullPath().toString()});
		}

		// when a resource is moved across project, the projects are locked as a
		// whole
		if (!source.getFullPath().segment(0).equals(destination.getFullPath().segment(0))) {
			return new MultiRule(new ISchedulingRule[] {modifyRule(source.getProject()), modifyRule(destination.getProject())});
		}

		ISchedulingRule result;
		try {
			ISemanticResource sourceres = (ISemanticResource) source.getAdapter(ISemanticResource.class);
			ISemanticResource desinationres = (ISemanticResource) destination.getAdapter(ISemanticResource.class);
			if (sourceres != null && desinationres != null) {

				ISemanticFileStore sourceStore = (ISemanticFileStore) EFS.getStore(source.getLocationURI());
				ISemanticFileStore destinationStore = (ISemanticFileStore) EFS.getStore(destination.getLocationURI());
				// if source and target come from different providers, return
				// root
				if (sourceStore.getEffectiveContentProvider().getClass().getName()
						.equals(destinationStore.getEffectiveContentProvider().getClass().getName())) {
					result = toRule(new IResource[] {source}, RuleType.MOVE, sourceStore.getEffectiveContentProvider().getRuleFactory()
							.moveRule(sourceStore, destinationStore));
				} else {
					result = toRule(new IResource[] {source, destination}, RuleType.MOVE, sourceStore.getEffectiveContentProvider()
							.getRuleFactory().moveRule(sourceStore, destinationStore), destinationStore.getEffectiveContentProvider()
							.getRuleFactory().moveRule(sourceStore, destinationStore));
				}
			} else {
				if (SfsTraceLocation.RULEFACTORY.isActive()) {
					SfsTraceLocation.getTrace().trace(SfsTraceLocation.RULEFACTORY.getLocation(),
							Messages.DelegatingResourceRuleFactory_ResourceNotAdapter_XMSG);
				}
				result = this.root;
			}
		} catch (CoreException e) {
			if (SfsTraceLocation.RULEFACTORY.isActive()) {
				SfsTraceLocation.getTrace().trace(SfsTraceLocation.RULEFACTORY.getLocation(), e.getMessage(), e);
			}
			// $JL-EXC$ ignore here
			result = this.root;
		}

		if (SfsTraceLocation.RULEFACTORY.isActive()) {
			if (result != null && result instanceof IResource) {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.RULEFACTORY.getLocation(),
						((IResource) result).getFullPath().toString());
			} else {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.RULEFACTORY.getLocation(), result);
			}
		}

		return result;
	}

	@Override
	public ISchedulingRule refreshRule(IResource resource) {

		if (SfsTraceLocation.RULEFACTORY.isActive()) {
			SfsTraceLocation.getTrace().traceEntry(SfsTraceLocation.RULEFACTORY.getLocation(), resource.getFullPath().toString());
		}

		ISchedulingRule result;
		try {
			result = calculateRuleForType2(resource, RuleType.REFRESH);
		} catch (CoreException e) {
			if (SfsTraceLocation.RULEFACTORY.isActive()) {
				SfsTraceLocation.getTrace().trace(SfsTraceLocation.RULEFACTORY.getLocation(), e.getMessage(), e);
			}
			// $JL-EXC$ ignore here
			result = this.root;
		}

		if (SfsTraceLocation.RULEFACTORY.isActive()) {
			if (result != null && result instanceof IResource) {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.RULEFACTORY.getLocation(),
						((IResource) result).getFullPath().toString());
			} else {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.RULEFACTORY.getLocation(), result);
			}
		}

		return result;
	}

	@Override
	public ISchedulingRule validateEditRule(IResource[] resources) {

		if (SfsTraceLocation.RULEFACTORY.isActive()) {
			Object[] input = new Object[resources.length];
			for (int i = 0; i < resources.length; i++) {
				input[i] = resources[i].getFullPath().toString();
			}
			SfsTraceLocation.getTrace().traceEntry(SfsTraceLocation.RULEFACTORY.getLocation(), input);
		}

		ISchedulingRule result;

		ISemanticFileStore[] stores = allStoresFromSameProvider(resources);
		if (stores != null) {
			// all resources belong to the same content provider
			try {
				ISemanticResource sres = (ISemanticResource) resources[0].getAdapter(ISemanticResource.class);
				if (sres != null) {

					result = toRule(resources, RuleType.VALIDATE_EDIT, stores[0].getEffectiveContentProvider().getRuleFactory()
							.validateEditRule(stores));
				} else {
					if (SfsTraceLocation.RULEFACTORY.isActive()) {
						SfsTraceLocation.getTrace().trace(SfsTraceLocation.RULEFACTORY.getLocation(),
								Messages.DelegatingResourceRuleFactory_ResourceNotAdapter_XMSG);
					}
					result = this.root;
				}
			} catch (CoreException e) {
				if (SfsTraceLocation.RULEFACTORY.isActive()) {
					SfsTraceLocation.getTrace().trace(SfsTraceLocation.RULEFACTORY.getLocation(), e.getMessage(), e);
				}
				// $JL-EXC$ ignore here
				result = this.root;
			}
		} else {
			// we could collect the resources per effective content provider and
			// call each provider once for optimization, but then it's not clear
			// whether
			// each provider would get all resources or just the ones they are
			// responsible for,
			// so let's do it resource by resource for the time being
			try {
				Set<ISemanticFileStore> ruleStores = new HashSet<ISemanticFileStore>();
				for (IResource res : resources) {
					ISemanticResource sres = (ISemanticResource) res.getAdapter(ISemanticResource.class);
					if (sres != null) {

						IFileStore fs = EFS.getStore(res.getLocationURI());
						if (fs instanceof ISemanticFileStore) {
							ISemanticFileStore sfs = (ISemanticFileStore) fs;
							ISemanticFileStore ruleStore = sfs.getEffectiveContentProvider().getRuleFactory()
									.validateEditRule(new ISemanticFileStore[] {sfs});
							if (ruleStore != null) {
								ruleStores.add(ruleStore);
							} else {
								ruleStores.clear();
								break;
							}
						} else {
							ruleStores.clear();
							break;
						}
					} else {
						ruleStores.clear();
						break;
					}
				}
				if (ruleStores.isEmpty()) {
					if (SfsTraceLocation.RULEFACTORY.isActive()) {
						SfsTraceLocation.getTrace().trace(SfsTraceLocation.RULEFACTORY.getLocation(),
								Messages.DelegatingResourceRuleFactory_ResourceNotAdapter_XMSG);
					}
					result = this.root;
				} else {
					result = toRule(resources, RuleType.VALIDATE_EDIT, ruleStores.toArray(new ISemanticFileStore[0]));
				}
			} catch (CoreException e) {
				if (SfsTraceLocation.RULEFACTORY.isActive()) {
					SfsTraceLocation.getTrace().trace(SfsTraceLocation.RULEFACTORY.getLocation(), e.getMessage(), e);
				}
				// $JL-EXC$ ignore here
				result = this.root;
			}
		}

		if (SfsTraceLocation.RULEFACTORY.isActive()) {
			if (result != null && result instanceof IResource) {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.RULEFACTORY.getLocation(),
						((IResource) result).getFullPath().toString());
			} else {
				SfsTraceLocation.getTrace().traceExit(SfsTraceLocation.RULEFACTORY.getLocation(), result);
			}
		}

		return result;
	}

	private ISchedulingRule toRule(IResource[] resources, RuleType type, ISemanticFileStore... stores) throws CoreException {

		if (stores.length == 1) {
			// single rule
			if (stores[0] == null) {
				// if the rule is null, return the root
				return this.root;
			}

			IResource rule = getRuleForStore(stores[0], resources[0], type);

			if (rule == null) {
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
				// null translates to workspace root, so we don't need a multi
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
				throw new SemanticResourceException(SemanticResourceStatusCode.RESOURCE_FOR_STORE_NOT_FOUND, store.getPath(),
						MessageFormat.format(Messages.DelegatingResourceRuleFactory_NoExistingParentFound_XMSG, stores[0].getPath()
								.toString()));
			}
			rules[i] = rule;
		}
		return new MultiRule(rules);
	}

	private IResource getRuleForStore(ISemanticFileStore store, IResource resource, RuleType type) {
		IResource rule = null;

		IResource[] rules;

		if (store.getType() == ISemanticFileStore.FILE) {
			rules = this.root.findFilesForLocationURI(store.toURI());
		} else {
			rules = this.root.findContainersForLocationURI(store.toURI());
		}

		IPath resourcePath = resource.getFullPath();
		int max = 0;
		for (int i = 0; i < rules.length; i++) {
			IPath path1 = rules[i].getFullPath();

			int matchingSegments = resourcePath.matchingFirstSegments(path1);
			if (matchingSegments > max) {
				max = matchingSegments;
				rule = rules[i];
			}
		}
		if (rule == null) {
			switch (type) {
				case REFRESH :
				case CREATE :
				case DELETE :
				case MOVE :
					rule = resource.getParent();
					break;
				case MODIFY :
					rule = resource;
					break;
				default :
					break;
			}
		}
		return rule;
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

	ISchedulingRule calculateRuleForType(IResource resource, RuleType type) throws CoreException {
		ISchedulingRule result;
		ISemanticResource sres = (ISemanticResource) resource.getAdapter(ISemanticResource.class);
		if (sres != null) {
			ISemanticFileStore rule;
			ISemanticFileStore sfs = (ISemanticFileStore) EFS.getStore(resource.getLocationURI());
			ISemanticContentProvider effectiveProvider = sfs.getEffectiveContentProvider();
			if (effectiveProvider.getRootStore().getPath().equals(sfs.getPath())) {
				// federation: we ask the parent provider to determine the
				// proper rule for the root store of the provider
				ISemanticFileStore parentStore = (ISemanticFileStore) sfs.getParent();
				if (parentStore != null) {
					rule = determineRule(parentStore, type);
				} else {
					rule = determineRule(sfs, type);
				}
			} else {
				rule = determineRule(sfs, type);
			}
			result = toRule(new IResource[] {resource}, type, rule);
		} else {
			if (SfsTraceLocation.RULEFACTORY.isActive()) {
				SfsTraceLocation.getTrace().trace(SfsTraceLocation.RULEFACTORY.getLocation(),
						Messages.DelegatingResourceRuleFactory_ResourceNotAdapter_XMSG);
			}
			result = this.root;
		}
		return result;
	}

	private ISemanticFileStore determineRule(ISemanticFileStore sfs, RuleType type) throws CoreException {
		ISemanticContentProvider effectiveProvider = sfs.getEffectiveContentProvider();
		IPath rootStorePath = effectiveProvider.getRootStore().getPath();
		ISemanticResourceRuleFactory factory = effectiveProvider.getRuleFactory();
		ISemanticFileStore rule = getRuleForType(factory, type, sfs);
		IPath rulePath;

		while (rule != null) {
			rulePath = rule.getPath();
			if (!rootStorePath.isPrefixOf(rulePath)) {
				// we are outside the current content provider
				effectiveProvider = rule.getEffectiveContentProvider();
				rootStorePath = effectiveProvider.getRootStore().getPath();
				factory = effectiveProvider.getRuleFactory();

				rule = getRuleForType(factory, type, rule);
			} else {
				break;
			}
		}
		return rule;
	}

	private ISchedulingRule calculateRuleForType2(IResource resource, RuleType type) throws CoreException {
		ISchedulingRule result;
		ISemanticResource sres = (ISemanticResource) resource.getAdapter(ISemanticResource.class);
		if (sres != null) {
			ISemanticFileStore sfs = (ISemanticFileStore) EFS.getStore(resource.getLocationURI());

			ISemanticFileStore rule = determineRule2(sfs, type);

			result = toRule(new IResource[] {resource}, type, rule);
		} else {
			if (SfsTraceLocation.RULEFACTORY.isActive()) {
				SfsTraceLocation.getTrace().trace(SfsTraceLocation.RULEFACTORY.getLocation(),
						Messages.DelegatingResourceRuleFactory_ResourceNotAdapter_XMSG);
			}
			result = this.root;
		}
		return result;
	}

	private ISemanticFileStore determineRule2(ISemanticFileStore sfs, RuleType type) throws CoreException {
		ISemanticContentProvider effectiveProvider = sfs.getEffectiveContentProvider();
		ISemanticResourceRuleFactory factory = effectiveProvider.getRuleFactory();
		ISemanticFileStore rule = getRuleForType(factory, type, sfs);
		return rule;
	}

	private ISemanticFileStore getRuleForType(ISemanticResourceRuleFactory factory, RuleType type, ISemanticFileStore rule) {
		switch (type) {
			case REFRESH :
				rule = factory.refreshRule(rule);
				break;
			case CREATE :
				rule = factory.createRule(rule);
				break;
			case DELETE :
				rule = factory.deleteRule(rule);
				break;
			default :
				break;
		}
		return rule;
	}

}
