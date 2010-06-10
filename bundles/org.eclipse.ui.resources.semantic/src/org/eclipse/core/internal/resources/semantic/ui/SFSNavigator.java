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
package org.eclipse.core.internal.resources.semantic.ui;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.internal.resources.semantic.ui.util.SFSBrowserTreeObject;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * 
 * This sub-class is for {@link ITabbedPropertySheetPageContributor}
 * implementation
 * 
 */
public class SFSNavigator extends CommonNavigator implements ITabbedPropertySheetPageContributor {
	public static final String VIEW_ID = "org.eclipse.core.resources.semantic.resourceView"; //$NON-NLS-1$

	private static final long AUTOREFREH_MILLI = 5000;
	boolean autoRefreshActive = true;
	IAction autoRefreshAction;
	Job autoRefreshJob;

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySheetPage.class)
			return new TabbedPropertySheetPage(this);
		return super.getAdapter(adapter);
	}

	public String getContributorId() {
		return getSite().getId();
	}

	@Override
	public void createPartControl(Composite aParent) {
		super.createPartControl(aParent);

		getCommonViewer().addOpenListener(new IOpenListener() {

			public void open(OpenEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.getFirstElement() instanceof SFSBrowserTreeObject) {
					final SFSBrowserTreeObject sfs = (SFSBrowserTreeObject) sel.getFirstElement();
					if (!sfs.getInfo().isDirectory()) {

						try {
							ISemanticFileStore sfstore = (ISemanticFileStore) sfs.getStore();
							File tempFile = sfstore.toLocalFile(EFS.CACHE, new NullProgressMonitor());
							IEditorInput input = new FileStoreEditorInput(EFS.getStore(tempFile.toURI())) {

								@Override
								public String getName() {
									return sfs.getStore().getName();
								}

							};
							IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), input,
									EditorsUI.DEFAULT_TEXT_EDITOR_ID);
							// TODO can we somehow listen to close events from
							// the editor and remove the cached file?
							// TODO avoid multiple editors on the same store and
							// link with editor
						} catch (CoreException e) {
							SemanticResourcesUIPlugin.handleError(e.getMessage(), e, true);
						}

					}
				}
			}
		});

		Action refreshAction = new Action(Messages.SFSNavigator_Refresh_XBUT) {

			@Override
			public void run() {
				getCommonViewer().refresh();
			}

		};

		refreshAction.setImageDescriptor(SemanticResourcesUIPlugin.getInstance().getImageRegistry().getDescriptor(
				SemanticResourcesUIPlugin.IMG_REFRESH));

		getViewSite().getActionBars().getToolBarManager().add(refreshAction);

		autoRefreshAction = new Action(Messages.SFSNavigator_AutoRefresh_XMIT, IAction.AS_CHECK_BOX) {

			@Override
			public void run() {
				if (isChecked()) {
					if (autoRefreshJob == null) {
						autoRefreshJob = new Job("Auto-refesh of SFS Browser") { //$NON-NLS-1$

							@Override
							protected IStatus run(IProgressMonitor monitor) {

								Display.getDefault().asyncExec(new Runnable() {

									public void run() {
										getCommonViewer().refresh();
									}
								});
								if (isChecked())
									schedule(AUTOREFREH_MILLI);
								return Status.OK_STATUS;
							}
						};
						autoRefreshJob.setSystem(true);
					}
					autoRefreshJob.schedule();
				} else {
					if (autoRefreshJob != null) {
						autoRefreshJob.cancel();
						autoRefreshJob = null;
					}
				}
			}

		};

		if (autoRefreshActive) {
			autoRefreshAction.setChecked(true);
			autoRefreshAction.run();
		}

		getViewSite().getActionBars().getMenuManager().add(autoRefreshAction);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (autoRefreshJob != null) {
			autoRefreshJob.cancel();
			autoRefreshJob = null;
		}
	}

	@Override
	public void init(IViewSite site, IMemento aMemento) throws PartInitException {
		// keep track of the auto refresh switch
		super.init(site, aMemento);
		if (aMemento == null) {
			return;
		}
		Boolean value = aMemento.getBoolean("AutoRefreshActive"); //$NON-NLS-1$
		if (value != null)
			autoRefreshActive = value.booleanValue();
	}

	@Override
	public void saveState(IMemento aMemento) {
		// keep track of the auto refresh switch
		super.saveState(aMemento);
		aMemento.putBoolean("AutoRefreshActive", autoRefreshAction.isChecked()); //$NON-NLS-1$
	}

}
