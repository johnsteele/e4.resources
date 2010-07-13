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
package org.eclipse.e4.demo.e4twitterclient.handlers;

import org.eclipse.core.internal.net.ProxyData;
import org.eclipse.core.internal.net.ProxySelector;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard page for adding a REST Demo resource
 * 
 */
@SuppressWarnings("restriction")
public class SetProxiesPage extends WizardPage {

	/**
	 * The page name
	 */
	public static final String NAME = SetProxiesPage.class.getName();

	String httpProxy = "";
	String httpsProxy = "";
	int httpProxyPort = 0;
	int httpsProxyPort = 0;

	public SetProxiesPage() {
		super(SetProxiesPage.NAME);
		setTitle("Set Proxies");
	}

	public void createControl(Composite parent) {

		Composite main = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout(4, false);
		main.setLayout(layout);

		final Combo proxySelector = new Combo(main, SWT.DROP_DOWN);
		proxySelector.add("Direct");
		proxySelector.add("Manual");
		proxySelector.add("Native");

		GridDataFactory.fillDefaults().grab(true, false).span(4, 1).applyTo(proxySelector);

		proxySelector.setText(ProxySelector.getDefaultProvider());
		ProxyData[] data = ProxySelector.getProxyData(ProxySelector.getDefaultProvider());

		for (ProxyData proxyData : data) {
			if (proxyData.getType() == IProxyData.HTTP_PROXY_TYPE) {
				SetProxiesPage.this.httpProxy = proxyData.getHost();
				SetProxiesPage.this.httpProxyPort = proxyData.getPort();
			}
			if (proxyData.getType() == IProxyData.HTTPS_PROXY_TYPE) {
				SetProxiesPage.this.httpsProxy = proxyData.getHost();
				SetProxiesPage.this.httpsProxyPort = proxyData.getPort();
			}
		}

		Label httpProxyLabel = new Label(main, SWT.NONE);
		httpProxyLabel.setText("HTTP Proxy");
		final Text httpProxyInput = new Text(main, SWT.BORDER);

		httpProxyInput.setText(httpProxy == null ? "" : httpProxy);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(httpProxyInput);

		httpProxyInput.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String proxy = httpProxyInput.getText();
				SetProxiesPage.this.httpProxy = proxy;
			}
		});

		Label httpProxyPortLabel = new Label(main, SWT.NONE);
		httpProxyPortLabel.setText("HTTP Proxy Port");
		final Text httpProxyPortInput = new Text(main, SWT.BORDER);
		httpProxyPortInput.setText(Integer.toString(httpProxyPort));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(httpProxyPortInput);

		httpProxyPortInput.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String port = httpProxyPortInput.getText();
				if (port.equals("")) { //$NON-NLS-1$
					SetProxiesPage.this.httpProxyPort = 0;
				} else {
					SetProxiesPage.this.httpProxyPort = Integer.parseInt(port);
				}
			}
		});

		Label httpsProxyLabel = new Label(main, SWT.NONE);
		httpsProxyLabel.setText("HTTPS Proxy");
		final Text httpsProxyInput = new Text(main, SWT.BORDER);
		httpsProxyInput.setText(httpsProxy == null ? "" : httpsProxy);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(httpsProxyInput);

		httpsProxyInput.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String proxy = httpsProxyInput.getText();
				SetProxiesPage.this.httpsProxy = proxy;
			}
		});

		Label httpsProxyPortLabel = new Label(main, SWT.NONE);
		httpsProxyPortLabel.setText("HTTPS Proxy Port");
		final Text httpsProxyPortInput = new Text(main, SWT.BORDER);
		httpsProxyPortInput.setText(Integer.toString(httpsProxyPort));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(httpsProxyPortInput);

		httpsProxyPortInput.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String port = httpsProxyPortInput.getText();
				if (port.equals("")) { //$NON-NLS-1$
					SetProxiesPage.this.httpsProxyPort = 0;
				} else {
					SetProxiesPage.this.httpsProxyPort = Integer.parseInt(port);
				}
			}
		});

		Button apply = new Button(main, SWT.PUSH);
		apply.setText("Apply");

		apply.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				String selection = proxySelector.getText();

				if (selection.equals("Manual")) {
					ProxyData[] data1 = ProxySelector.getProxyData(selection);

					for (ProxyData proxyData : data1) {
						if (proxyData.getType() == IProxyData.HTTP_PROXY_TYPE) {
							// substitute with valid values
							proxyData.setHost(SetProxiesPage.this.httpProxy);
							proxyData.setPort(SetProxiesPage.this.httpProxyPort);
						}
						if (proxyData.getType() == IProxyData.HTTPS_PROXY_TYPE) {
							// substitute with valid values
							proxyData.setHost(SetProxiesPage.this.httpsProxy);
							proxyData.setPort(SetProxiesPage.this.httpsProxyPort);
						}
					}

					ProxySelector.setProxyData(selection, data1);
				}

				ProxySelector.setActiveProvider(selection);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing to do
			}
		});

		setControl(main);

	}

}
