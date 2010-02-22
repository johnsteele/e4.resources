package org.eclipse.core.resources.semantic.examples;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Select the scenarios
 * 
 */
public class SelectScenarioPage extends WizardPage {
	/**
	 * The Scenario
	 * 
	 */
	public enum Scenario {
		/**
		 * Default
		 */
		DEFAULTCONTENTPROVIDER("DefaultContentProvider", Messages.SelectScenarioPage_DefaultProviderScenario_XFLD), //$NON-NLS-1$
		/**
		 * Web Service
		 */
		WEBSERVICECONTENTPROVIDER("SampleWSDLXSDContentProvider", Messages.SelectScenarioPage_WebServiceScenario_XFLD), //$NON-NLS-1$
		/**
		 * Composite Resource
		 */
		COMPOSITECONTENTPROVIDER("SampleCompositeResourceContentProvider", Messages.SelectScenarioPage_CompositeProvider_XFLD), //$NON-NLS-1$
		/**
		 * Remote Store
		 */
		REMOTESTORECONTENTPROVIDER("RemoteStoreContentProvider", Messages.SelectScenarioPage_RemoteStoreScenario_XFLD); //$NON-NLS-1$

		private final String folderName;
		private final String description;

		private Scenario(String folderName, String description) {
			this.folderName = folderName;
			this.description = description;
		}

		/**
		 * @return the folder name
		 */
		public String getFolderName() {
			return this.folderName;
		}

		/**
		 * 
		 * @return the description
		 */
		public String getDescription() {
			return this.description;
		}
	}

	Set<Scenario> scenarios = new HashSet<Scenario>();

	/**
     * 
     */
	public SelectScenarioPage() {
		super(SelectScenarioPage.class.getName());
		setTitle(Messages.CreateDemoProjectPage_CreateProject_XGRP);
		for (Scenario sc : Scenario.values()) {
			this.scenarios.add(sc);
		}
	}

	public void createControl(Composite parent) {

		setMessage(Messages.SelectScenarioPage_SelectScenarios_XMSG);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);

		Composite main = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(main);
		main.setLayout(new GridLayout(1, false));

		for (Scenario scenario : Scenario.values()) {
			addButton(main, scenario);
		}

		check();
		setControl(main);

	}

	/**
	 * 
	 * @return the result
	 */
	public Set<Scenario> getScenarios() {
		return this.scenarios;
	}

	private void addButton(Composite main, final Scenario scenario) {

		final Button but = new Button(main, SWT.CHECK);
		but.setText(scenario.getDescription());
		but.setSelection(this.scenarios.contains(scenario));
		but.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (but.getSelection()) {
					SelectScenarioPage.this.scenarios.add(scenario);
				} else {
					SelectScenarioPage.this.scenarios.remove(scenario);
				}
				check();
			}

		});

	}

	protected void check() {
		setPageComplete(!this.scenarios.isEmpty());
	}

}
