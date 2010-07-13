package org.eclipse.core.internal.resources.semantic.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.internal.resources.semantic.ui.util.SFSBrowserTreeObject;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;

public class NavigateToContentViewHandler extends AbstractHandler {

	public static final String NAV_COMMAND_ID = "org.eclipse.ui.resources.semantic.navigateToSemanticContent"; //$NON-NLS-1$
	public static final String NAV_PATH_PARAMETER_ID = "org.eclipse.ui.resources.semantic.path"; //$NON-NLS-1$

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String pathString = event.getParameter(NAV_PATH_PARAMETER_ID);

		try {
			CommonNavigator nav = (CommonNavigator) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
					SFSNavigator.VIEW_ID);
			if (pathString != null) {
				IPath path = new Path(pathString);
				SFSBrowserTreeObject ob = new SFSBrowserTreeObject(EFS.getFileSystem(ISemanticFileSystem.SCHEME), path);
				nav.selectReveal(new StructuredSelection(ob));
			}
		} catch (PartInitException e) {
			throw new ExecutionException(e.getMessage(), e);
		} catch (CoreException e) {
			throw new ExecutionException(e.getMessage(), e);
		}
		return null;
	}

}