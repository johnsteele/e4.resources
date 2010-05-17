package org.eclipse.core.resources.semantic.examples.wizard;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.semantic.examples.Messages;
import org.eclipse.core.resources.semantic.examples.SemanticResourcesPluginExamples;
import org.eclipse.core.resources.semantic.examples.webdav.WebDAVUtil;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class AddWebDAVResourceCheckURLPage extends WizardPage {

	public AddWebDAVResourceCheckURLPage() {
		super(AddWebDAVResourceNameAndURLPage.class.getName());
		setTitle(Messages.AddWebDAVResourceCheckURLPage_PageTitle);
	}

	public boolean isFolder() {
		return asFolder.getSelection();
	}

	public boolean shouldRetrieveContent() {
		return retrieveContent.getSelection();
	}

	Text urlText;
	Button asFolder;
	Button asFile;

	Button ignoreCheckResults;
	Button retrieveContent;

	String url;

	public void setUrl(String newUrl) {
		setPageComplete(false);
		this.url = newUrl;
		if (urlText != null)
			if (this.url == null)
				urlText.setText(""); //$NON-NLS-1$
			else
				urlText.setText(this.url);
	}

	public void createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(2, false));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(main);

		new Label(main, SWT.NONE).setText(Messages.AddWebDAVResourceCheckURLPage_URLLabel);
		urlText = new Text(main, SWT.BORDER);
		urlText.setEnabled(false);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(urlText);
		if (url != null)
			urlText.setText(url);
		asFolder = new Button(main, SWT.RADIO);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(asFolder);
		asFolder.setText(Messages.AddWebDAVResourceCheckURLPage_FolderLabel);
		asFolder.setEnabled(false);
		asFolder.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				checkPage();
			}
		});

		asFile = new Button(main, SWT.RADIO);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(asFile);
		asFile.setText(Messages.AddWebDAVResourceCheckURLPage_FileLabel);
		asFile.setEnabled(false);
		asFile.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				checkPage();
			}
		});

		ignoreCheckResults = new Button(main, SWT.CHECK);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(ignoreCheckResults);
		ignoreCheckResults.setText(Messages.AddWebDAVResourceCheckURLPage_IgnoreButton);
		ignoreCheckResults.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				asFolder.setEnabled(ignoreCheckResults.getSelection());
				asFile.setEnabled(ignoreCheckResults.getSelection());
				checkPage();
			}

		});

		retrieveContent = new Button(main, SWT.CHECK);
		retrieveContent.setSelection(true);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(retrieveContent);
		retrieveContent.setText(Messages.AddWebDAVResourceCheckURLPage_RetrieveContentAfterFinish);

		setPageComplete(false);

		setControl(main);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			checkPage();
		}
	}

	void checkPage() {

		setErrorMessage(null);
		setMessage(null, IMessageProvider.WARNING);

		try {
			if (url == null) {
				setErrorMessage(Messages.AddWebDAVResourceCheckURLPage_NoURLMessage);
				return;
			}
			final URI uri;
			try {
				uri = new URI(url);
			} catch (URISyntaxException e) {
				setErrorMessage(NLS.bind(Messages.AddWebDAVResourceCheckURLPage_InvalidUrlMessage, url));
				return;
			}

			if (!ignoreCheckResults.getSelection()) {
				asFolder.setSelection(false);
				asFile.setSelection(false);

				final boolean[] isFolder = new boolean[] {true};
				final Exception[] exception = new Exception[] {null};

				try {
					getWizard().getContainer().run(false, false, new IRunnableWithProgress() {

						public void run(IProgressMonitor monitor) {
							try {
								isFolder[0] = WebDAVUtil.checkWebDAVURL(uri, monitor);
							} catch (Exception e) {
								exception[0] = e;
							}
						}
					});
				} catch (InvocationTargetException e) {
					SemanticResourcesPluginExamples.handleError(e.getMessage(), e, true);
				} catch (InterruptedException e) {
					SemanticResourcesPluginExamples.handleError(e.getMessage(), e, true);
				}

				if (exception[0] != null) {
					setErrorMessage(NLS.bind(Messages.AddWebDAVResourceCheckURLPage_InvalidURLWithCause, url, exception[0].getMessage()));
					return;
				}
				if (isFolder[0]) {
					asFolder.setSelection(true);
				} else {
					asFile.setSelection(true);
				}
			}

			if (!asFile.getSelection() && !asFolder.getSelection())
				setErrorMessage(Messages.AddWebDAVResourceCheckURLPage_MustBeFileOrFolderMessage);

		} finally {
			boolean errorFound = getErrorMessage() != null;
			if (!errorFound && ignoreCheckResults.getSelection()) {
				setMessage(Messages.AddWebDAVResourceCheckURLPage_ChecksIgnoreMessage, IMessageProvider.WARNING);
			}
			setPageComplete(getErrorMessage() == null);
		}

	}
}
