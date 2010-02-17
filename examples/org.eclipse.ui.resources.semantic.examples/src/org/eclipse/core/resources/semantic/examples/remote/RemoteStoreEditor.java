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
package org.eclipse.core.resources.semantic.examples.remote;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.semantic.examples.remote.RemoteItem.Type;
import org.eclipse.core.resources.semantic.spi.Util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

/**
 * Editor for the Remote Store XML
 * 
 */
public class RemoteStoreEditor extends EditorPart {

	/** Icon for folder */
	public static Image FOLDERIMAGE = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
	/** Icon for file */
	public static Image FILEIMAGE = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);

	IFile myFile;
	TreeViewer tv;
	boolean dirty = false;

	public void doSave(IProgressMonitor monitor) {
		if (this.dirty) {
			try {
				((RemoteStore) ((RemoteFolder) this.tv.getInput()).getStore()).serialize(monitor);
				markDirty(false);
			} catch (CoreException e) {
				// $JL-EXC$
				monitor.setCanceled(true);
			}
		}

	}

	public void doSaveAs() {
		// not allowed
	}

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if (input instanceof IFileEditorInput) {
			this.myFile = ((IFileEditorInput) input).getFile();
			if (!this.myFile.getName().equals(RemoteStore.FILENAME)) {
				throw new PartInitException(NLS.bind(Messages.RemoteStoreEditor_WrongFile_XMSG, RemoteStore.FILENAME));
			}
			setSite(site);
			setInput(input);
		} else {
			throw new PartInitException(Messages.RemoteStoreEditor_WrongInput_XMSG);
		}

	}

	public boolean isDirty() {
		return this.dirty;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	public void createPartControl(Composite parent) {

		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, false));

		ToolBar bar = new ToolBar(main, SWT.HORIZONTAL);
		ToolItem newFolder = new ToolItem(bar, SWT.PUSH);
		newFolder.setText(Messages.RemoteStoreEditor_NewRootFolder_XBUT);
		newFolder.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				createChild(true, (RemoteFolder) RemoteStoreEditor.this.tv.getInput());

			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing				
			}
		});

		ToolItem newFile = new ToolItem(bar, SWT.PUSH);
		newFile.setText(Messages.RemoteStoreEditor_NewRootFile_XBUT);
		newFile.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				createChild(false, (RemoteFolder) RemoteStoreEditor.this.tv.getInput());

			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing				
			}
		});

		final SashForm sash = new SashForm(main, SWT.VERTICAL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(sash);

		this.tv = new TreeViewer(sash, SWT.SINGLE | SWT.FULL_SELECTION);

		this.tv.setContentProvider(new ITreeContentProvider() {

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// nothing
			}

			public void dispose() {
				// nothing
			}

			public Object[] getElements(Object inputElement) {
				return ((RemoteFolder) inputElement).getChildren().toArray();
			}

			public boolean hasChildren(Object element) {
				RemoteItem item = (RemoteItem) element;
				if (item.getType() == Type.FOLDER) {
					return !((RemoteFolder) item).getChildren().isEmpty();
				}
				return false;
			}

			public Object getParent(Object element) {
				RemoteFolder folder = ((RemoteItem) element).getParent();
				return folder;

			}

			public Object[] getChildren(Object parentElement) {
				RemoteItem item = (RemoteItem) parentElement;
				if (item.getType() == Type.FOLDER) {
					RemoteFolder folder = (RemoteFolder) item;
					return folder.getChildren().toArray(new RemoteItem[0]);
				}
				return new Object[0];
			}
		});

		this.tv.setLabelProvider(new ITableLabelProvider() {

			public void removeListener(ILabelProviderListener listener) {
				// nothing
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void dispose() {
				// nothing
			}

			public void addListener(ILabelProviderListener listener) {
				// nothing
			}

			public String getColumnText(Object element, int columnIndex) {
				RemoteItem item = (RemoteItem) element;
				switch (columnIndex) {
				case 0:
					return item.getName();

				default:
					return null;
				}
			}

			public Image getColumnImage(Object element, int columnIndex) {
				switch (columnIndex) {
				case 0:
					if (((RemoteItem) element).getType() == Type.FOLDER) {
						return RemoteStoreEditor.FOLDERIMAGE;
					}
					return RemoteStoreEditor.FILEIMAGE;
				default:
					return null;
				}
			}
		});

		final Tree tree = this.tv.getTree();
		TreeColumn column = new TreeColumn(tree, SWT.NONE);
		column.setText(Messages.RemoteStoreEditor_Name_XCOL);
		column.setWidth(300);

		this.tv.getTree().addMenuDetectListener(new MenuDetectListener() {

			public void menuDetected(MenuDetectEvent e) {

				fillContextMenu(tree, e);

			}
		});

		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);

		final IResourceDeltaVisitor dv = new IResourceDeltaVisitor() {

			public boolean visit(IResourceDelta delta) throws CoreException {
				if (delta.getKind() == IResourceDelta.REMOVED) {
					if (delta.getFullPath().equals(RemoteStoreEditor.this.myFile.getFullPath())) {
						// we should somehow close the editor here
						Display.getDefault().asyncExec(new Runnable() {

							public void run() {
								getSite().getPage().closeEditor(RemoteStoreEditor.this, false);

							}
						});
						return false;
					}
				}
				if (delta.getKind() == IResourceDelta.CHANGED && (delta.getFlags() & IResourceDelta.CONTENT) > 0) {
					if (delta.getFullPath().equals(RemoteStoreEditor.this.myFile.getFullPath())) {
						refresh();
						return false;
					}
				}

				return true;
			}
		};

		this.myFile.getProject().getWorkspace().addResourceChangeListener(new IResourceChangeListener() {

			public void resourceChanged(IResourceChangeEvent event) {
				try {
					if (event.getDelta() != null) {
						event.getDelta().accept(dv);
					}
				} catch (CoreException e) {
					// $JL-EXC$ ignore here
				}

			}
		});

		Composite fileParent = new Composite(sash, SWT.NONE);
		fileParent.setLayout(new GridLayout(3, false));

		Label timestampLabel = new Label(fileParent, SWT.NONE);
		timestampLabel.setText(Messages.RemoteStoreEditor_Timestamp_XFLD);
		final Text timestamp = new Text(fileParent, SWT.SINGLE);
		timestamp.setEnabled(false);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(timestamp);

		Button newStamp = new Button(fileParent, SWT.PUSH);
		newStamp.setText(Messages.RemoteStoreEditor_UpdateTime_XBUT);
		newStamp.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				Object selected = ((IStructuredSelection) RemoteStoreEditor.this.tv.getSelection()).getFirstElement();
				if (selected instanceof RemoteFile) {
					((RemoteFile) selected).setTimestamp(System.currentTimeMillis());
					markDirty(true);
				}

			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing

			}
		});

		final Label label = new Label(fileParent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.LEAD, SWT.TOP).applyTo(label);
		final Text content = new Text(fileParent, SWT.MULTI);
		content.setEditable(false);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, true).applyTo(content);

		sash.setMaximizedControl(this.tv.getControl());

		this.tv.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = event.getSelection();
				Object first = ((IStructuredSelection) sel).getFirstElement();
				if (first instanceof RemoteFile) {
					sash.setMaximizedControl(null);
					RemoteFile file = (RemoteFile) first;

					timestamp.setText(file.format.format(new Date(file.getTimestamp())));
					try {

						try {
							String encoding = file.getStore().getDefaultCharset();
							label.setText(NLS.bind(Messages.RemoteStoreEditor_Content_XFLD, encoding));
							content.setText(new String(file.getContent(), encoding));
							label.getParent().layout(true);
						} catch (CoreException e) {
							// $JL-EXC$ ignore here
							//$JL-I18N$							
							content.setText(new String(file.getContent()));
						}

					} catch (UnsupportedEncodingException e) {
						// $JL-EXC$ ignore here
						//$JL-I18N$
						content.setText(new String(file.getContent()));
					}
				} else {
					sash.setMaximizedControl(RemoteStoreEditor.this.tv.getControl());
				}

			}
		});

		refresh();

	}

	void markDirty(boolean actDirty) {
		if (this.dirty != actDirty) {
			this.dirty = actDirty;
			firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
		}
		refresh();
	}

	void refresh() {

		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				if (RemoteStoreEditor.this.tv.getTree().isDisposed()) {
					return;
				}
				if (!RemoteStoreEditor.this.dirty) {
					RemoteStore store = (RemoteStore) RemoteStoreEditor.this.myFile.getProject().getAdapter(RemoteStore.class);

					RemoteStoreEditor.this.tv.setInput(store.getRootFolder());
				} else {
					RemoteStoreEditor.this.tv.setInput(RemoteStoreEditor.this.tv.getInput());
				}

				RemoteStoreEditor.this.tv.setSelection(RemoteStoreEditor.this.tv.getSelection());

			}
		});

	}

	void createChild(boolean asFolder, final RemoteFolder parentFolder) {

		final IInputValidator validator = new IInputValidator() {

			public String isValid(String newText) {
				if (parentFolder.hasChild(newText)) {
					return NLS.bind(Messages.RemoteStoreEditor_NameInUse_XMSG, newText);
				}
				return null;
			}
		};

		InputDialog dialog = new InputDialog(getSite().getShell(), Messages.RemoteStoreEditor_CreateChild_XGRP,
				Messages.RemoteStoreEditor_ChildName_XFLD, "", validator); //$NON-NLS-1$

		if (dialog.open() == Window.OK) {
			String name = dialog.getValue();
			if (asFolder) {
				parentFolder.addFolder(name);
			} else {
				parentFolder.addFile(name, new byte[0], System.currentTimeMillis());
			}

			markDirty(true);
		}
	}

	public void setFocus() {
		// nothing
	}

	void fillContextMenu(final Tree tree, MenuDetectEvent mouseEvent) {

		Menu treeMenu = new Menu(tree);
		tree.setMenu(treeMenu);

		if (mouseEvent.getSource() instanceof Tree) {

			TreeItem[] selected = tree.getSelection();
			if (selected.length != 1) {
				return;
			}
			Object data = selected[0].getData();
			if (!(data instanceof RemoteItem)) {
				return;
			}
			final RemoteItem item = (RemoteItem) data;

			MenuItem delete = new MenuItem(treeMenu, SWT.PUSH);
			delete.setText(Messages.RemoteStoreEditor_Delete_XMEN);
			delete.addSelectionListener(new SelectionListener() {

				public void widgetSelected(SelectionEvent e) {
					RemoteFolder parent = item.getParent();
					parent.deleteChild(item.getName());

					markDirty(true);
					RemoteStoreEditor.this.tv.setSelection(new StructuredSelection(parent));
					RemoteStoreEditor.this.tv.expandToLevel(parent, 1);

				}

				public void widgetDefaultSelected(SelectionEvent e) {
					// nothing

				}
			});

			if (item instanceof RemoteFolder) {
				final RemoteFolder folder = (RemoteFolder) item;

				MenuItem createFolder = new MenuItem(treeMenu, SWT.PUSH);
				createFolder.setText(Messages.RemoteStoreEditor_CreateFolder_XMEN);

				createFolder.addSelectionListener(new SelectionListener() {

					public void widgetSelected(SelectionEvent e) {
						createChild(true, folder);
					}

					public void widgetDefaultSelected(SelectionEvent e) {
						// nothing

					}
				});

				MenuItem createFile = new MenuItem(treeMenu, SWT.PUSH);
				createFile.setText(Messages.RemoteStoreEditor_CreateFile_XMEN);

				createFile.addSelectionListener(new SelectionListener() {

					public void widgetSelected(SelectionEvent e) {
						createChild(false, folder);
					}

					public void widgetDefaultSelected(SelectionEvent e) {
						// nothing

					}
				});
			}

			if (item instanceof RemoteFile) {
				final RemoteFile file = (RemoteFile) item;

				MenuItem uploadContent = new MenuItem(treeMenu, SWT.PUSH);
				uploadContent.setText(Messages.RemoteStoreEditor_Upload_XMEN);

				uploadContent.addSelectionListener(new SelectionListener() {

					public void widgetSelected(SelectionEvent e) {

						FileDialog dialog = new FileDialog(getSite().getShell(), SWT.OPEN);
						String result = dialog.open();
						if (result == null) {
							return;
						}

						try {
							InputStream is = new FileInputStream(new File(result));
							Util.transferStreams(is, file.getOutputStream(false), null);
							markDirty(true);
						} catch (FileNotFoundException e1) {
							// $JL-EXC$
							ErrorDialog.openError(getSite().getShell(), Messages.RemoteStoreEditor_Error_XGRP, e1.getMessage(), null);
						} catch (CoreException e1) {
							ErrorDialog.openError(getSite().getShell(), Messages.RemoteStoreEditor_Error_XGRP, null, e1.getStatus());
						}
						RemoteStoreEditor.this.tv.setSelection(new StructuredSelection(file), true);

					}

					public void widgetDefaultSelected(SelectionEvent e) {
						// nothing
					}
				});

				MenuItem changeContent = new MenuItem(treeMenu, SWT.PUSH);
				changeContent.setText(Messages.RemoteStoreEditor_ChangeContent_XMEN);

				changeContent.addSelectionListener(new SelectionListener() {

					public void widgetSelected(SelectionEvent e) {

						InputDialog dialog = new InputDialog(getSite().getShell(), Messages.RemoteStoreEditor_EnterContent_XGRP,
								Messages.RemoteStoreEditor_NewContent_XFLD, "", null); //$NON-NLS-1$
						if (dialog.open() == Window.OK) {
							try {
								InputStream is = new ByteArrayInputStream(dialog.getValue().getBytes("UTF-8")); //$NON-NLS-1$
								Util.transferStreams(is, file.getOutputStream(false), null);
								markDirty(true);
							} catch (UnsupportedEncodingException e1) {
								// $JL-EXC$
								MessageDialog.openError(getSite().getShell(), Messages.RemoteStoreEditor_Error_XGRP, e1.getMessage());
							} catch (CoreException e1) {
								// $JL-EXC$
								MessageDialog.openError(getSite().getShell(), Messages.RemoteStoreEditor_Error_XGRP, e1.getMessage());
							}
							RemoteStoreEditor.this.tv.setSelection(new StructuredSelection(file), true);
						}

					}

					public void widgetDefaultSelected(SelectionEvent e) {
						// nothing

					}
				});

			}

		}
	}

}
