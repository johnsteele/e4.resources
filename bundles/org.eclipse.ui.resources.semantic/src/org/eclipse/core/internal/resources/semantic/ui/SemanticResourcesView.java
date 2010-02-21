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
package org.eclipse.core.internal.resources.semantic.ui;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.internal.resources.semantic.ui.util.SFSBrowserPropertiesContentProvider;
import org.eclipse.core.internal.resources.semantic.ui.util.SFSBrowserPropertiesLabelProvider;
import org.eclipse.core.internal.resources.semantic.ui.util.SFSBrowserTreeContentProvider;
import org.eclipse.core.internal.resources.semantic.ui.util.SFSBrowserTreeLabelProvider;
import org.eclipse.core.internal.resources.semantic.ui.util.SFSBrowserTreeObject;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.fileSystem.FileSystemContributor;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

/**
 * View on the content of the Semantic File System.
 * 
 * Also used in the {@link FileSystemContributor} implementation of
 * {@link SemanticFileSystemContributor} which uses the controls here in {@link BrowseSFSDialog}.
 * <p>
 * Depending on whether this is actually used as view or as dialog, the buttons will either be
 * implemented as {@link ToolBar} or as {@link Action} contributed to the {@link ToolBarManager}; in
 * addition, refresh will behave differently (blocking the UI in case of the dialog while working
 * asynchronously in view mode.
 * 
 */
public class SemanticResourcesView extends ViewPart {

	// some required state
	String selectedPath;
	boolean autoRefresh = false;
	Job scheduledJob;

	// controls needed for refresh

	// the hierarchy tree
	TreeViewer sfsTree;
	// existence check box
	Button existsBox;
	// last modified time stamp
	Text timestampText;
	// the properties (session and persistent)
	TreeViewer propsTable;

	// date format with millisecond resolution
	static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss::SSS"); //$NON-NLS-1$
	// refresh rate for auto-refresh
	static final int REFRESH_RATE = 5000;

	public void createPartControl(Composite parent) {

		Composite main = new Composite(parent, SWT.NONE);

		ToolBar tb;

		if (getViewSite() == null) {
			// pop-up mode: we use right-to-left so that this looks more like the view tool bar
			// manager
			tb = new ToolBar(main, SWT.HORIZONTAL | SWT.RIGHT_TO_LEFT);
		} else {
			// view mode: we don't add a tool bar, as we use the view tool bar manager
			tb = null;
		}

		main.setLayout(new GridLayout(2, false));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(main);

		SFSBrowserTreeObject[] input;
		try {
			input = getRootObjects();
		} catch (CoreException e) {
			// for example, the SFS can not be accessed; in this case, we show a label with the
			// status text and return
			Label errorLabel = new Label(main, SWT.NONE);
			errorLabel.setText(e.getStatus().getMessage());
			return;
		}

		// pathLabel and path are hidden if nothing is selected
		final Label pathLabel = new Label(main, SWT.NONE);
		GridDataFactory.fillDefaults().exclude(true).align(SWT.LEFT, SWT.CENTER).applyTo(pathLabel);
		pathLabel.setText(Messages.SemanticResourcesView_Path_XFLD);

		final Text path = new Text(main, SWT.BORDER | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).exclude(true).align(SWT.FILL, SWT.CENTER).applyTo(path);

		// left: tree, right: properties (if something is selected in the tree)
		final SashForm sf = new SashForm(main, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, true).applyTo(sf);

		this.sfsTree = new TreeViewer(sf);

		SFSBrowserTreeLabelProvider treeLabelProvider = new SFSBrowserTreeLabelProvider();
		treeLabelProvider.configureTreeColumns(this.sfsTree);
		this.sfsTree.setContentProvider(new SFSBrowserTreeContentProvider());
		this.sfsTree.setLabelProvider(treeLabelProvider);

		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(this.sfsTree.getTree());

		final Group propsMain = new Group(sf, SWT.SHADOW_ETCHED_IN);
		sf.setMaximizedControl(this.sfsTree.getTree());
		propsMain.setLayout(new GridLayout(2, false));

		// exists check box
		this.existsBox = new Button(propsMain, SWT.CHECK);
		this.existsBox.setText(Messages.SemanticResourcesView_Exists_XFLD);
		this.existsBox.setEnabled(false);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(this.existsBox);

		Label timestampLabel = new Label(propsMain, SWT.NONE);
		timestampLabel.setText(Messages.SemanticResourcesView_Timestamp_XFLD);

		// last modified
		this.timestampText = new Text(propsMain, SWT.BORDER);
		this.timestampText.setEnabled(false);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(this.timestampText);

		// properties (session and persistent)
		this.propsTable = new TreeViewer(propsMain);
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(this.propsTable.getTree());

		SFSBrowserPropertiesLabelProvider labelProvider = new SFSBrowserPropertiesLabelProvider();
		labelProvider.configureTreeColumns(this.propsTable);

		this.propsTable.setContentProvider(new SFSBrowserPropertiesContentProvider());
		this.propsTable.setLabelProvider(labelProvider);

		if (tb == null) {
			// view mode with tool bar manager
			IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();

			Action hideNonExisting = new Action(Messages.BrowseSFSDialog_Hide_XCKL, IAction.AS_CHECK_BOX) {

				public void run() {
					SFSBrowserTreeContentProvider prov = (SFSBrowserTreeContentProvider) SemanticResourcesView.this.sfsTree
							.getContentProvider();
					prov.setHideNonExisting(isChecked());
					SemanticResourcesView.this.sfsTree.refresh();

				}
			};

			mgr.add(hideNonExisting);

			Action refresh = new Action(Messages.BrowseSFSDialog_Refresh_XBUT, IAction.AS_PUSH_BUTTON) {

				public void run() {
					scheduleRefresh(0);
				}
			};

			mgr.add(refresh);

			Action autoRefreshAction = new Action(Messages.SemanticResourcesView_AutoRefresh_XCKL, IAction.AS_CHECK_BOX) {

				public void run() {

					SemanticResourcesView.this.autoRefresh = isChecked();
					if (isChecked()) {
						scheduleRefresh(0);
					}

				}

			};

			ActionContributionItem it = new ActionContributionItem(autoRefreshAction);
			mgr.add(it);

			mgr.update(false);
			// some trick to add the tool tip text
			ToolItem autoRefreshItem = (ToolItem) it.getWidget();
			autoRefreshItem
					.setToolTipText(MessageFormat.format(Messages.SemanticResourcesView_RefreshRate_XTOL, new Integer(REFRESH_RATE)));

		} else {

			// pop-up mode with own tool bar
			GridDataFactory.fillDefaults().span(2, 1).applyTo(tb);

			// inverse order as we add right-to-left

			ToolItem refresh = new ToolItem(tb, SWT.PUSH);
			refresh.setText(Messages.BrowseSFSDialog_Refresh_XBUT);
			refresh.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent evt) {
					scheduleRefresh(0);
				}

			});

			final ToolItem hideNonExisting = new ToolItem(tb, SWT.CHECK);
			hideNonExisting.setText(Messages.BrowseSFSDialog_Hide_XCKL);

			hideNonExisting.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {

					SFSBrowserTreeContentProvider prov = (SFSBrowserTreeContentProvider) SemanticResourcesView.this.sfsTree
							.getContentProvider();
					prov.setHideNonExisting(hideNonExisting.getSelection());

					SemanticResourcesView.this.sfsTree.refresh();
				}

			});

			// no auto-refresh in pop-up mode

		}

		this.sfsTree.setInput(input);

		if (this.selectedPath != null && !this.selectedPath.equals("")) { //$NON-NLS-1$
			try {
				SFSBrowserTreeObject object = getTreeObject(this.selectedPath);
				this.sfsTree.setSelection(new StructuredSelection(object));
			} catch (CoreException e) {
				Platform.getLog(Platform.getBundle(SemanticResourcesUIPlugin.PLUGIN_ID)).log(e.getStatus());
			}
		}

		this.sfsTree.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.isEmpty()) {
					path.setText(""); //$NON-NLS-1$
					SemanticResourcesView.this.selectedPath = null;
					propsMain.setText(""); //$NON-NLS-1$
					SemanticResourcesView.this.propsTable.setInput(null);
					SemanticResourcesView.this.existsBox.setSelection(false);
					SemanticResourcesView.this.timestampText.setText(""); //$NON-NLS-1$					
					sf.setMaximizedControl(SemanticResourcesView.this.sfsTree.getTree());
				} else {
					SFSBrowserTreeObject selected = (SFSBrowserTreeObject) sel.getFirstElement();
					SemanticResourcesView.this.selectedPath = selected.getPath().toString();
					path.setText(SemanticResourcesView.this.selectedPath);
					propsMain.setText(MessageFormat.format(Messages.SemanticResourcesView_PropertiesOf_XGRP, selected.getInfo().getName()));
					SemanticResourcesView.this.existsBox.setSelection(selected.getInfo().exists());
					SemanticResourcesView.this.timestampText.setText(SemanticResourcesView.df.format(new Date(selected.getInfo()
							.getLastModified())));
					SemanticResourcesView.this.propsTable.setInput(selected);
					SemanticResourcesView.this.propsTable.expandAll();
					sf.setMaximizedControl(null);
				}

				path.setVisible(!sel.isEmpty());
				pathLabel.setVisible(!sel.isEmpty());

				GridData gd = (GridData) path.getLayoutData();
				gd.exclude = sel.isEmpty();

				gd = (GridData) pathLabel.getLayoutData();
				gd.exclude = sel.isEmpty();

				path.getParent().layout(true);

			}
		});

	}

	public void setFocus() {
		// nothing to do
	}

	/**
	 * @return the selected path
	 */
	public String getSelectedPath() {
		return this.selectedPath;
	}

	/**
	 * @param path
	 *            the path to select
	 */
	public void setSelectedPath(String path) {
		this.selectedPath = path;
	}

	public void dispose() {
		// make sure to cancel the job
		if (this.scheduledJob != null) {
			this.scheduledJob.cancel();
			this.scheduledJob = null;
		}
		super.dispose();
	}

	void refreshInternal() throws CoreException {

		final SFSBrowserTreeObject[] newInput = getRootObjects();

		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				Object[] expandedObjects = SemanticResourcesView.this.sfsTree.getExpandedElements();
				SemanticResourcesView.this.sfsTree.setInput(newInput);
				SemanticResourcesView.this.sfsTree.setExpandedElements(expandedObjects);

				if (SemanticResourcesView.this.selectedPath != null) {
					SFSBrowserTreeObject selected = (SFSBrowserTreeObject) SemanticResourcesView.this.propsTable.getInput();
					SemanticResourcesView.this.propsTable.refresh();
					SemanticResourcesView.this.propsTable.expandAll();
					if (selected != null) {
						SemanticResourcesView.this.existsBox.setSelection(selected.getInfo().exists());
						SemanticResourcesView.this.timestampText.setText(SemanticResourcesView.df.format(new Date(selected.getInfo()
								.getLastModified())));
					}
				} else {
					SemanticResourcesView.this.propsTable.setInput(null);
					SemanticResourcesView.this.existsBox.setSelection(false);
					SemanticResourcesView.this.timestampText.setText(""); //$NON-NLS-1$
				}

			}
		});

		if (this.autoRefresh) {
			scheduleRefresh(REFRESH_RATE);
		}

	}

	void scheduleRefresh(long delay) {

		if (getSite() == null) {
			// pop-up mode: block the UI (we can't display progress in a modal dialog)
			try {
				// cancellation is not supported in the underlying method of SFS
				PlatformUI.getWorkbench().getProgressService().run(true, false, new IRunnableWithProgress() {

					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							refreshInternal();
						} catch (CoreException e) {
							// $JL-EXC$
							throw new InvocationTargetException(e);
						}

					}
				});
			} catch (InvocationTargetException e1) {
				// $JL-EXC$ specific handling
				Throwable cause = e1.getCause();
				if (cause instanceof CoreException) {
					Platform.getLog(Platform.getBundle(SemanticResourcesUIPlugin.PLUGIN_ID)).log(((CoreException) cause).getStatus());
				}
			} catch (InterruptedException e1) {
				// $JL-EXC$ ignore here
				return;
			}

		} else {
			// view mode: run asynchronously
			Job refreshJob = new Job(Messages.SemanticResourcesView_RefreshJob_XGRP) {

				public IStatus run(IProgressMonitor monitor) {
					try {
						refreshInternal();
					} catch (CoreException e) {
						return e.getStatus();
					}
					return new Status(IStatus.OK, SemanticResourcesUIPlugin.PLUGIN_ID, ""); //$NON-NLS-1$
				}
			};

			// don't show this in the UI
			refreshJob.setSystem(true);

			IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) getSite().getService(
					IWorkbenchSiteProgressService.class);
			// keep track of this job
			this.scheduledJob = refreshJob;
			// by scheduling with the service, we'll get the italic font in the tab
			// while the job is running (if it is running long enough for the display to update)
			service.schedule(refreshJob, delay);
		}
	}

	SFSBrowserTreeObject[] getRootObjects() throws CoreException {
		ISemanticFileSystem fs = (ISemanticFileSystem) EFS.getFileSystem(ISemanticFileSystem.SCHEME);
		String[] roots = fs.getRootNames();
		SFSBrowserTreeObject[] paths = new SFSBrowserTreeObject[roots.length];
		for (int i = 0; i < roots.length; i++) {
			IPath path = new Path('/' + roots[i]);
			paths[i] = new SFSBrowserTreeObject((IFileSystem) fs, path);
		}
		return paths;
	}

	SFSBrowserTreeObject getTreeObject(String path) throws CoreException {
		IFileSystem fs = EFS.getFileSystem(ISemanticFileSystem.SCHEME);
		return new SFSBrowserTreeObject(fs, new Path(path));
	}
}
