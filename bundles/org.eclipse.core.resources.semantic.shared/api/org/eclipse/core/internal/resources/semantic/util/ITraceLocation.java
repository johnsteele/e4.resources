package org.eclipse.core.internal.resources.semantic.util;

/**
 * Helper interface for trace location management
 * <p>
 * Additional methods could return a description or such...
 * 
 */
public interface ITraceLocation {

	/**
	 * @return the location, e.g. "/debug/mainArea/subArea"
	 */
	public String getLocation();

	/**
	 * @return <code>true</code> if the location is active
	 */
	public boolean isActive();

}
