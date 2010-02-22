package org.eclipse.core.resources.semantic.examples.remote;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class SemanticResourcesPluginExamplesCore extends Plugin {

	public static String PLUGIN_ID = "org.eclipse.core.resources.semantic.examples"; //$NON-NLS-1$

	private static SemanticResourcesPluginExamplesCore INSTANCE;

	public static SemanticResourcesPluginExamplesCore getDefault() {
		if (INSTANCE == null) {
			INSTANCE = new SemanticResourcesPluginExamplesCore();
		}
		return INSTANCE;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		INSTANCE = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		INSTANCE = null;
	}

}
