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
package org.eclipse.e4.demo.e4twitterclient.parts;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.demo.e4twitterclient.Activator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SFSViewer {

	// some required state
	String selectedPath;
	boolean autoRefresh = true;
	Job scheduledJob;

	// controls needed for refresh

	// the hierarchy tree
	TreeViewer sfsTree;
	// last modified time stamp
	Text timestampText;
	// the properties (session and persistent)
	TreeViewer propsTable;

	// refresh rate for auto-refresh
	static final int REFRESH_RATE = 30000;
	// date format with millisecond resolution
	final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss::SSS"); //$NON-NLS-1$

	@Inject
	SFSViewer(Composite parent) {

		Composite main = new Composite(parent, SWT.NONE);

		main.setLayout(new GridLayout(2, false));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(main);

		SFSBrowserTreeObject[] input;
		try {
			input = getRootObjects();
		} catch (CoreException e) {
			// for example, the SFS can not be accessed; in this case, we show a
			// label with the
			// status text and return
			Label errorLabel = new Label(main, SWT.NONE);
			errorLabel.setText(e.getStatus().getMessage());
			return;
		}

		// pathLabel and path are hidden if nothing is selected
		final Label pathLabel = new Label(main, SWT.NONE);
		GridDataFactory.fillDefaults().exclude(true).align(SWT.LEFT, SWT.CENTER).applyTo(pathLabel);
		pathLabel.setText("Path");

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

		Label timestampLabel = new Label(propsMain, SWT.NONE);
		timestampLabel.setText("Timestamp");

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

		scheduleRefresh(0);

		this.sfsTree.setInput(input);

		if (this.selectedPath != null && !this.selectedPath.equals("")) { //$NON-NLS-1$
			try {
				SFSBrowserTreeObject object = getTreeObject(this.selectedPath);
				this.sfsTree.setSelection(new StructuredSelection(object));
			} catch (CoreException e) {
				Platform.getLog(Platform.getBundle(Activator.PLUGIN_ID)).log(e.getStatus());
			}
		}

		this.sfsTree.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.isEmpty()) {
					path.setText(""); //$NON-NLS-1$
					SFSViewer.this.selectedPath = null;
					propsMain.setText(""); //$NON-NLS-1$
					SFSViewer.this.propsTable.setInput(null);
					SFSViewer.this.timestampText.setText(""); //$NON-NLS-1$					
					sf.setMaximizedControl(SFSViewer.this.sfsTree.getTree());
				} else {
					SFSBrowserTreeObject selected = (SFSBrowserTreeObject) sel.getFirstElement();
					SFSViewer.this.selectedPath = selected.getPath().toString();
					path.setText(SFSViewer.this.selectedPath);
					propsMain.setText(MessageFormat.format("Properties of {0}", selected.getInfo().getName()));
					SFSViewer.this.timestampText.setText(df.format(new Date(selected.getInfo().getLastModified())));
					SFSViewer.this.propsTable.setInput(selected);
					SFSViewer.this.propsTable.expandAll();
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

	@PreDestroy
	public void dispose() {
		// make sure to cancel the job
		if (this.scheduledJob != null) {
			this.scheduledJob.cancel();
			this.scheduledJob = null;
		}
	}

	void refreshInternal() throws CoreException {

		final SFSBrowserTreeObject[] newInput = getRootObjects();

		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				Object[] expandedObjects = SFSViewer.this.sfsTree.getExpandedElements();
				SFSViewer.this.sfsTree.setInput(newInput);
				SFSViewer.this.sfsTree.setExpandedElements(expandedObjects);

				if (SFSViewer.this.selectedPath != null) {
					SFSBrowserTreeObject selected = (SFSBrowserTreeObject) SFSViewer.this.propsTable.getInput();
					SFSViewer.this.propsTable.refresh();
					SFSViewer.this.propsTable.expandAll();
					if (selected != null) {
						SFSViewer.this.timestampText.setText(df.format(new Date(selected.getInfo().getLastModified())));
					}
				} else {
					SFSViewer.this.propsTable.setInput(null);
					SFSViewer.this.timestampText.setText(""); //$NON-NLS-1$
				}

			}
		});

		if (this.autoRefresh) {
			scheduleRefresh(REFRESH_RATE);
		}

	}

	void scheduleRefresh(long delay) {

		// view mode: run asynchronously
		Job refreshJob = new Job("SFSRefreshJob") {

			@Override
			public IStatus run(IProgressMonitor monitor) {
				try {
					refreshInternal();
				} catch (CoreException e) {
					return e.getStatus();
				}
				return new Status(IStatus.OK, Activator.PLUGIN_ID, ""); //$NON-NLS-1$
			}
		};

		// don't show this in the UI
		refreshJob.setSystem(true);

		refreshJob.schedule(delay);
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
