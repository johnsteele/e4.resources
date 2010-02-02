package org.eclipse.core.internal.resources.semantic.ui.team;

import org.eclipse.core.internal.resources.semantic.ui.team.SemanticHistoryPage.SemanticFileRevisionLabelProvider;
import org.eclipse.core.resources.semantic.spi.SemanticFileRevision;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

/**
 * Used to select a revision
 * 
 */
public class SemanticFileRevisionSelectionDialog extends Dialog {

	private final SemanticFileRevision[] myInput;
	private final String myTitle;
	private SemanticFileRevision mySelection;
	TableViewer tv;

	protected SemanticFileRevisionSelectionDialog(Shell parentShell, SemanticFileRevision[] input, String title) {
		super(parentShell);
		this.myTitle = title;
		this.myInput = input;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(this.myTitle);
	}

	/**
	 * 
	 * @return the selected revision
	 */
	public SemanticFileRevision getSelection() {
		return this.mySelection;
	}

	public void create() {
		super.create();
		getButton(Window.OK).setEnabled(false);
	}

	protected Control createDialogArea(Composite parent) {

		Composite myParent = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(myParent);
		myParent.setLayout(new GridLayout(1, false));

		this.tv = new TableViewer(myParent, SWT.SINGLE | SWT.FULL_SELECTION);
		Table table = this.tv.getTable();

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(table);

		this.tv.setContentProvider(new ITreeContentProvider() {

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// nothing
			}

			public void dispose() {
				// nothing
			}

			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}

			public boolean hasChildren(Object element) {
				return false;
			}

			public Object getParent(Object element) {
				return null;
			}

			public Object[] getChildren(Object parentElement) {
				return null;
			}
		});

		SemanticFileRevisionLabelProvider.initColumns(this.tv);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		this.tv.setInput(this.myInput);

		this.tv.addSelectionChangedListener(new ISelectionChangedListener() {

			@SuppressWarnings("synthetic-access")
			public void selectionChanged(SelectionChangedEvent event) {
				getButton(Window.OK).setEnabled(!SemanticFileRevisionSelectionDialog.this.tv.getSelection().isEmpty());
			}
		});

		return myParent;
	}

	protected void okPressed() {
		this.mySelection = (SemanticFileRevision) ((IStructuredSelection) this.tv.getSelection()).getFirstElement();
		super.okPressed();
	}

}
