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
package org.eclipse.core.resources.semantic.examples;

import java.util.List;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.internal.resources.semantic.provider.DefaultContentProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.semantic.examples.providers.RemoteStoreContentProvider;
import org.eclipse.core.resources.semantic.examples.providers.SampleCompositeResourceContentProvider;
import org.eclipse.core.resources.semantic.examples.providers.SampleWSDLXSDContentProvider;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Contribution of the example application
 * 
 */
public class ExamplesContribution extends AbstractContributionFactory {

	static ImageDescriptor ADDIMAGE;
	static final ImageDescriptor CREATEIMAGE;

	static {
		ADDIMAGE = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ADD);
		if (ADDIMAGE == null) {
			// IMG_OBJ_ADD does not seem to work in Eclipse 3.4
			ADDIMAGE = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY);
		}
		CREATEIMAGE = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD);
	}

	/**
	 * Default constructor
	 */
	public ExamplesContribution() {
		super("popup:org.eclipse.ui.popup.any?after=additions", null); //$NON-NLS-1$
	}

	private static final class MyExpression extends Expression {

		private final int[] types;
		private String[] providerClasses;

		MyExpression(String providerClass, int... types) {
			this.types = types;
			this.providerClasses = new String[] {providerClass};
		}

		void addProviderClass(String classString) {
			String[] oldArray = this.providerClasses;

			this.providerClasses = new String[oldArray.length + 1];
			System.arraycopy(oldArray, 0, this.providerClasses, 0, oldArray.length);
			this.providerClasses[this.providerClasses.length - 1] = classString;

		}

		@Override
		@SuppressWarnings({"rawtypes"})
		public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {

			Object var = context.getDefaultVariable();

			IResource selected = (IResource) getSelectedObject(context, IResource.class, false);
			if (selected != null) {
				IResource res = (IResource) ((List) var).get(0);
				if (res.getProject().hasNature(SemanticResourcesPluginExamples.EXAPMLE_NATURE)) {
					ISemanticFileStore store = (ISemanticFileStore) EFS.getStore(res.getLocationURI());
					boolean typeFound = false;
					for (int type : this.types) {
						if (type == store.getType()) {
							typeFound = true;
						}
					}
					if (!typeFound) {
						return EvaluationResult.FALSE;
					}
					boolean classFound = false;
					String providerClass = store.getEffectiveContentProvider().getClass().getName();
					for (String classString : this.providerClasses) {
						if (classString.equals(providerClass)) {
							classFound = true;
						}
					}
					if (classFound) {
						return EvaluationResult.TRUE;
					}
				}
			}
			return EvaluationResult.FALSE;

		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		private <T> T getSelectedObject(IEvaluationContext ctx, Class T, boolean adapt) {

			Object selection = ctx.getVariable("selection"); //$NON-NLS-1$
			if (!(selection instanceof IStructuredSelection)) {
				return null;
			}
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() != 1) {
				return null;
			}

			if (!(ssel.getFirstElement() instanceof IResource)) {
				return null;
			}
			IResource r = (IResource) ssel.getFirstElement();
			if (adapt) {
				Object adapted = r.getAdapter(T);
				try {
					return (T) adapted;
				} catch (ClassCastException e1) {
					// $JL-EXC$ ignore
					return null;
				}
			}
			try {
				return (T) r;
			} catch (ClassCastException e1) {
				// $JL-EXC$ ignore
				return null;
			}

		}

	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {

		contributeRemoteStoreActions(serviceLocator, additions);

		contributeAddRestResource(serviceLocator, additions);

		contributeWebServiceAddFile(serviceLocator, additions);

		contributeAddFileActions(serviceLocator, additions);

	}

	private void contributeWebServiceAddFile(IServiceLocator serviceLocator, IContributionRoot additions) {
		CommandContributionItemParameter param;
		CommandContributionItem citem;
		// Add from file system
		param = new CommandContributionItemParameter(
				serviceLocator,
				"org.eclipse.core.resources.semantic.examples_addWSDLResource", "org.eclipse.core.resources.semantic.examples_addWSDLResource", //$NON-NLS-1$ //$NON-NLS-2$
				CommandContributionItem.STYLE_PUSH);
		param.icon = ADDIMAGE;
		citem = new CommandContributionItem(param);

		MyExpression myExp = new MyExpression(SampleWSDLXSDContentProvider.class.getName(), ISemanticFileStore.FOLDER);

		additions.addContributionItem(citem, myExp);
	}

	private void contributeRemoteStoreActions(IServiceLocator serviceLocator, IContributionRoot additions) {

		// Add Resource From Remote: dialog with next layer of add-able
		// resources
		CommandContributionItemParameter param = new CommandContributionItemParameter(serviceLocator,
				"org.eclipse.core.resources.semantic.examples_addFromRemote", "org.eclipse.core.resources.semantic.examples_addFromRemote", //$NON-NLS-1$ //$NON-NLS-2$
				CommandContributionItem.STYLE_PUSH);
		param.icon = ADDIMAGE;
		CommandContributionItem citem = new CommandContributionItem(param);

		// remote store content provider
		MyExpression myExp = new MyExpression(RemoteStoreContentProvider.class.getName(), ISemanticFileStore.FOLDER);

		additions.addContributionItem(citem, myExp);

		// Create remote resource
		param = new CommandContributionItemParameter(serviceLocator,
				"org.eclipse.core.resources.semantic.examples_createRemote", "org.eclipse.core.resources.semantic.examples_createRemote", //$NON-NLS-1$ //$NON-NLS-2$
				CommandContributionItem.STYLE_PUSH);
		param.icon = CREATEIMAGE;
		citem = new CommandContributionItem(param);

		// remote store content provider
		myExp = new MyExpression(RemoteStoreContentProvider.class.getName(), ISemanticFileStore.FOLDER);

		additions.addContributionItem(citem, myExp);

	}

	private void contributeAddRestResource(IServiceLocator serviceLocator, IContributionRoot additions) {

		CommandContributionItemParameter param;
		CommandContributionItem citem;
		// Add REST resource
		param = new CommandContributionItemParameter(
				serviceLocator,
				"org.eclipse.core.resources.semantic.examples_addRESTResource", "org.eclipse.core.resources.semantic.examples_addRESTResource", //$NON-NLS-1$ //$NON-NLS-2$
				CommandContributionItem.STYLE_PUSH);
		param.icon = ADDIMAGE;
		citem = new CommandContributionItem(param);

		// default content provider
		MyExpression myExp = new MyExpression(DefaultContentProvider.class.getName(), ISemanticFileStore.FOLDER);

		additions.addContributionItem(citem, myExp);

	}

	private void contributeAddFileActions(IServiceLocator serviceLocator, IContributionRoot additions) {

		CommandContributionItemParameter param;
		CommandContributionItem citem;
		// Add REST resource
		param = new CommandContributionItemParameter(
				serviceLocator,
				"org.eclipse.core.resources.semantic.examples_addFileFromRemote", "org.eclipse.core.resources.semantic.examples_addFileFromRemote", //$NON-NLS-1$ //$NON-NLS-2$
				CommandContributionItem.STYLE_PUSH);
		param.icon = ADDIMAGE;
		citem = new CommandContributionItem(param);

		// default content provider
		MyExpression myExp = new MyExpression(DefaultContentProvider.class.getName(), ISemanticFileStore.FOLDER);

		myExp.addProviderClass(SampleCompositeResourceContentProvider.class.getName());
		myExp.addProviderClass(SampleWSDLXSDContentProvider.class.getName());

		additions.addContributionItem(citem, myExp);

	}

}
