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
package org.eclipse.core.internal.resources.semantic.ui.team;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.internal.resources.semantic.ui.sync.SemanticSubscriber;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.resources.semantic.spi.ISemanticFileStore;
import org.eclipse.core.resources.semantic.spi.SemanticFileRevision;
import org.eclipse.core.resources.semantic.spi.SemanticResourceVariantComparator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.ui.history.HistoryPage;
import org.eclipse.team.ui.synchronize.SyncInfoCompareInput;

/**
 * The "Team History" page for Semantic Files
 * 
 */
public class SemanticHistoryPage extends HistoryPage {

	private static class TableColumnSorter extends ViewerComparator {

		int direction = 0;
		private final TableColumn myColumn;
		final TableViewer myViewer;
		private final int myIndex;

		TableColumnSorter(TableViewer viewer, TableColumn column, int colIndex) {

			this.myViewer = viewer;
			this.myColumn = column;
			this.myIndex = colIndex;
			this.myColumn.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					if (TableColumnSorter.this.myViewer.getComparator() != null) {
						if (TableColumnSorter.this.myViewer.getComparator() == TableColumnSorter.this) {
							int tdirection = TableColumnSorter.this.direction;

							if (tdirection == SWT.UP) {
								setSorter(TableColumnSorter.this, SWT.DOWN);
							} else if (tdirection == SWT.DOWN) {
								setSorter(TableColumnSorter.this, SWT.NONE);
							}
						} else {
							setSorter(TableColumnSorter.this, SWT.UP);
						}
					} else {
						setSorter(TableColumnSorter.this, SWT.UP);
					}
				}
			});
			setSorter(this, SWT.UP);
		}

		public void setSorter(TableColumnSorter sorter, int direction) {
			if (direction == SWT.NONE) {
				this.myColumn.getParent().setSortColumn(null);
				this.myColumn.getParent().setSortDirection(SWT.NONE);
				this.myViewer.setComparator(null);
			} else {
				this.myColumn.getParent().setSortColumn(this.myColumn);
				sorter.direction = direction;

				if (direction == SWT.UP) {
					this.myColumn.getParent().setSortDirection(SWT.DOWN);
				} else {
					this.myColumn.getParent().setSortDirection(SWT.UP);
				}

				if (this.myViewer.getComparator() == sorter) {
					this.myViewer.refresh();
				} else {
					this.myViewer.setComparator(sorter);
				}

			}
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			switch (this.direction) {
				case SWT.UP :
					return doCompare(e1, e2);
				case SWT.DOWN :
					return -1 * doCompare(e1, e2);
				default :
					return 0;
			}

		}

		private int doCompare(Object e1, Object e2) {

			ITableLabelProvider lp = ((ITableLabelProvider) this.myViewer.getLabelProvider());
			String t1 = lp.getColumnText(e1, this.myIndex);
			String t2 = lp.getColumnText(e2, this.myIndex);
			return t1.compareTo(t2);
		}
	}

	private final static String EMPTY = ""; //$NON-NLS-1$
	private Composite main;
	TableViewer tv;

	/**
	 * @param object
	 *            the resource
	 */
	public SemanticHistoryPage(IResource object) {
		super();
	}

	private final IStructuredContentProvider contentProvider = new IStructuredContentProvider() {

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// nothing special

		}

		public void dispose() {
			// nothing special

		}

		public Object[] getElements(Object inputElement) {
			List<IFileRevision> sortable = new ArrayList<IFileRevision>();
			if (inputElement != null) {
				// sort by timestamp descending
				IFileRevision[] history = ((IFileHistory) inputElement).getFileRevisions();
				for (IFileRevision rev : history) {
					sortable.add(rev);
				}
				Collections.sort(sortable, new Comparator<IFileRevision>() {

					public int compare(IFileRevision o1, IFileRevision o2) {
						return (int) (o2.getTimestamp() - o1.getTimestamp());
					}

				});
				return sortable.toArray();
			}
			return new Object[0];
		}
	};

	/**
	 * Label provider for SemanticFileRevisions
	 * 
	 */
	public final static class SemanticFileRevisionLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			IFileRevision revision = (IFileRevision) element;
			switch (columnIndex) {
				// revision
				case 0 :
					if (revision.getContentIdentifier() == null) {
						return SemanticHistoryPage.EMPTY;
					}
					return revision.getContentIdentifier();
					// timestamp
				case 1 :
					long timestamp = revision.getTimestamp();
					if (timestamp < 0) {
						return SemanticHistoryPage.EMPTY;
					}
					return new SimpleDateFormat().format(new Date(revision.getTimestamp()));
					// user
				case 2 :
					return revision.getAuthor();
				case 3 :
					String comment = revision.getComment();
					if (comment != null) {
						return comment;
					}
					return SemanticHistoryPage.EMPTY;
				default :
					return SemanticHistoryPage.EMPTY;
			}
		}

		/**
		 * Initializes
		 * 
		 * @param viewer
		 */
		public static void initColumns(TableViewer viewer) {
			Table table = viewer.getTable();
			int colIndex = 0;
			TableColumn revName = new TableColumn(table, SWT.NONE);
			revName.setText(Messages.SemanticHistoryPage_Revision_XCOL);
			revName.setWidth(100);
			setSorter(viewer, revName, colIndex++);

			TableColumn revTime = new TableColumn(table, SWT.NONE);
			revTime.setText(Messages.SemanticHistoryPage_Timestamp_XCOL);
			revTime.setWidth(150);
			setSorter(viewer, revTime, colIndex++);

			TableColumn user = new TableColumn(table, SWT.NONE);
			user.setText(Messages.SemanticHistoryPage_User_XCOL);
			user.setWidth(80);
			setSorter(viewer, user, colIndex++);

			TableColumn comment = new TableColumn(table, SWT.NONE);
			comment.setText(Messages.SemanticHistoryPage_Comment_XCOL);
			comment.setWidth(250);
			setSorter(viewer, comment, colIndex++);

			viewer.setLabelProvider(new SemanticFileRevisionLabelProvider());

		}

		private static void setSorter(TableViewer viewer, TableColumn actColumn, int colIndex) {

			new TableColumnSorter(viewer, actColumn, colIndex);

		}

	}

	@Override
	public boolean inputSet() {

		if (!(getInput() instanceof IFile)) {
			this.tv.setInput(null);

		} else {
			IFile input = (IFile) getInput();

			RepositoryProvider repositoryProvider = RepositoryProvider.getProvider(input.getProject(),
					ISemanticFileSystem.SFS_REPOSITORY_PROVIDER);
			IFileHistoryProvider historyProvider = repositoryProvider.getFileHistoryProvider();

			IFileHistory history = historyProvider.getFileHistoryFor(input, EFS.NONE, null);
			this.tv.setInput(history);
		}

		return true;
	}

	@Override
	public void createControl(Composite parent) {

		this.main = new Composite(parent, SWT.NONE);

		this.main.setLayout(new GridLayout(2, false));

		this.tv = new TableViewer(this.main, SWT.MULTI | SWT.FULL_SELECTION);
		final Table table = this.tv.getTable();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);

		SemanticFileRevisionLabelProvider.initColumns(this.tv);

		this.tv.setContentProvider(this.contentProvider);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		table.addMenuDetectListener(new MenuDetectListener() {

			public void menuDetected(MenuDetectEvent e) {
				fillContextMenu(table);
			}

		});

	}

	void fillContextMenu(final Table table) {

		Menu menu = new Menu(table);

		final int count = ((IStructuredSelection) SemanticHistoryPage.this.tv.getSelection()).size();
		try {
			MenuItem item;
			if (count == 1) {
				item = new MenuItem(menu, SWT.PUSH);
				item.setText(Messages.SemanticHistoryPage_Compare_XMIT);
			} else if (count == 2) {
				item = new MenuItem(menu, SWT.PUSH);
				item.setText(Messages.SemanticHistoryPage_ThreeWayCompare_XMIT);
			} else {
				return;
			}

			item.addSelectionListener(new SelectionListener() {

				public void widgetSelected(SelectionEvent evt) {

					try {

						IFile file = (IFile) getInput();
						IFileRevision[] revs = new IFileRevision[2];

						if (count == 2) {
							Object[] selection = ((IStructuredSelection) SemanticHistoryPage.this.tv.getSelection()).toArray();
							SemanticFileRevision[] revInput = new SemanticFileRevision[selection.length];
							System.arraycopy(selection, 0, revInput, 0, selection.length);

							SemanticFileRevisionSelectionDialog dialog = new SemanticFileRevisionSelectionDialog(getSite().getShell(),
									revInput, Messages.SemanticHistoryPage_SelectCommonAncestor_XGRP);
							if (dialog.open() != Window.OK) {
								return;
							}
							SemanticFileRevision ancestor = dialog.getSelection();
							revs[0] = ancestor;
							if (revInput[0] == ancestor) {
								revs[1] = revInput[1];
							} else {
								revs[1] = revInput[0];
							}
						} else {
							revs[1] = (IFileRevision) ((IStructuredSelection) SemanticHistoryPage.this.tv.getSelection()).getFirstElement();
						}

						ISemanticFileStore store = (ISemanticFileStore) EFS.getStore(file.getLocationURI());
						IResourceVariant[] var = SemanticSubscriber.toVariants(revs, store);

						SyncInfo syncinfo = new SyncInfo(file, var[0], var[1], new SemanticResourceVariantComparator(var[0] != null));

						syncinfo.init();

						SyncInfoCompareInput input = new SyncInfoCompareInput(SemanticHistoryPage.EMPTY, syncinfo);
						input.getCompareConfiguration().setLeftEditable(false);
						CompareUI.openCompareEditor(input);

					} catch (CoreException ex) {
						throw new RuntimeException(ex);
					}

				}

				public void widgetDefaultSelected(SelectionEvent evt) {
					// nothing
				}
			});

		} finally {
			table.setMenu(menu);
		}
	}

	@Override
	public Control getControl() {
		return this.main;
	}

	@Override
	public void setFocus() {
		// nothing

	}

	public String getDescription() {
		return Messages.SemanticHistoryPage_SemHistPage_XGRP;
	}

	public String getName() {
		return ((IResource) getInput()).getName();
	}

	public boolean isValidInput(Object object) {
		ISemanticResource res = (ISemanticResource) ((IResource) object).getAdapter(ISemanticResource.class);
		return res != null;
	}

	public void refresh() {
		inputSet();

	}

	@SuppressWarnings({"rawtypes"})
	public Object getAdapter(Class adapter) {
		return null;
	}

}
