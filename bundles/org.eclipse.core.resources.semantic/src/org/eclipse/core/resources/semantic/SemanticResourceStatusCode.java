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
package org.eclipse.core.resources.semantic;

/**
 * Defines type-safe status code constants relevant for the Semantic File
 * System.
 * <p>
 * {@link SemanticResourceException}s can only be constructed using this enum.
 * <p>
 * Status objects created by the Resources plug-in bear one of these status
 * codes and the plug-in ID defined in {@link #PLUGIN_ID}.
 * <p>
 * Status code definitions
 * <ul>
 * <li>General constants [0-98]</li>
 * <li>Information Only [0-32]</li>
 * <li>Warnings [33-65]</li>
 * <li>Errors [66-98]</li>
 * </ul>
 * 
 * @since 4.0
 * @see org.eclipse.core.runtime.IStatus
 */
public enum SemanticResourceStatusCode {

	/**
	 * Synchronization: information
	 */
	SYNC_INFO(3),

	/**
	 * Synchronization: warning
	 */
	SYNC_WARNING(36),

	/**
	 * A locking conflict was detected
	 */
	LOCK_CONFLICT(66),

	/**
	 * A workspace operation was started without obtaining a scheduling rule
	 */
	CALLED_OUTSIDE_OF_SCHEDULING_RULE(67),

	/**
	 * 
	 */
	STORE_NOT_FOUND(68),

	/**
	 * 
	 */
	RESOURCE_ALREADY_EXISTS(69),

	/**
	 * 
	 */
	CACHED_CONTENT_NOT_FOUND(70),

	/**
	 * 
	 */
	METHOD_NOT_SUPPORTED(71),

	/**
	 * When trying to create a folder if a file with that path already exists
	 * and vice versa.
	 */
	RESOURCE_WITH_OTHER_TYPE_EXISTS(72),

	/**
	 * 
	 */
	RESOURCE_PARENT_DOESNT_EXIST(73),

	/**
	 * If the SFS can not be initialized properly
	 */
	SFS_INITIALIZATION_ERROR(74),

	/**
	 * Write failure on metadata file
	 */
	SFS_ERROR_WRITING_METADATA(75),

	/**
	 * No extension found for a content provider ID
	 */
	UNKNOWN_CONTENT_PROVIDER_ID(76),

	/**
	 * Remote resource not found
	 */
	REMOTE_RESOURCE_NOT_FOUND(77),
	/**
	 * Attribute setters require a QualifiedName with non-null qualifier
	 */
	QUALIFIED_NAME_MUST_HAVE_QUALIFIER(78),
	/**
	 * REST Content provider: remote URI property is null
	 */
	REMOTE_URI_NOT_FOUND(79),
	/**
	 * REST Content provider: remote connection problems
	 */
	REMOTE_CONNECT_EXCEPTION(80),
	/**
	 * Utility: transfer bytes method
	 */
	UTIL_BYTE_TRANSER(81),
	/**
	 * A resource can not be found in the workspace
	 */
	RESOURCE_FOR_STORE_NOT_FOUND(82),
	/**
	 * An invalid resource type was encountered
	 */
	INVALID_RESOURCE_TYPE(83),
	/**
	 * An invalid resource name was encountered
	 */
	INVALID_RESOURCE_NAME(84),
	/**
	 * A Semantic Project is not accessible
	 */
	PROJECT_NOT_ACCESSIBLE(85),
	/**
	 * A Semantic Project is not mapped to the SFS Team provider
	 */
	PROJECT_NOT_MAPPED(86),
	/**
	 * Synchronization: error
	 */
	SYNC_ERROR(87),
	/**
	 * A URI syntax problem was encountered
	 */
	INVALID_URI_SYNTAX(88),
	/**
	 * Problems opening a URL connection
	 */
	URL_CONNECT_EXCEPTION(89),
	/**
	 * DB not initialized
	 */
	SFS_DB_NOT_INITIALIZED(90),
	/**
	 * Problems with the automatic local refresh
	 */
	AUTO_REFRESH(91),

	/**
	 * If the File Cache management can not be initialized properly
	 */
	FILECACHE_INITIALIZATION_ERROR(92),

	/**
	 * If the File Cache management can not be initialized properly
	 */
	FILECACHE_ERROR_WRITING_METADATA(93),

	/**
	 * Attempt to set a timestamp of cache file failed
	 */
	FILECACHE_ERROR_SETTING_TIMESTAMP(94),

	/**
	 * Attempt to delete a cache file failed
	 */
	FILECACHE_CACHEFILE_DELETION_FAILED(95),

	/**
	 * Error renaming temporary file
	 */
	FILECACHE_CACHEFILE_RENAME_FAILED(96),

	/**
	 * Error writing cache content to file system
	 */
	FILECACHE_ERROR_WRITING_CONTENT(97),

	/**
	 * Error creating cache file
	 */
	FILECACHE_CACHEFILE_CREATION_FAILED(98),

	/**
	 * Semantic File Store not accessible
	 * 
	 * @since 0.2
	 */
	NOT_ACCESSIBLE(99);

	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "org.eclipse.core.resources.semantic.shared"; //$NON-NLS-1$

	private static final int MAXSIZE = 100;
	private final static SemanticResourceStatusCode[] myIndexArray = new SemanticResourceStatusCode[MAXSIZE];
	private static boolean initialized = false;

	private final int code;

	private SemanticResourceStatusCode(int value) {
		if (value >= MAXSIZE) {
			throw new IllegalArgumentException("Integer value must be < " + MAXSIZE); //$NON-NLS-1$ this can not happend unless the code is changed -> no localization
		}
		this.code = value;
	}

	/**
	 * Converts a status code to it's int value.
	 * 
	 * @return the int value
	 */
	public int toInt() {
		return this.code;
	}

	/**
	 * Converts an int to the status code.
	 * 
	 * @param value
	 *            the int value
	 * @return the status code, or <code>null</code> if no code exists for that
	 *         value
	 */
	public SemanticResourceStatusCode fromInt(int value) {
		synchronized (myIndexArray) {

			if (!initialized) {
				for (SemanticResourceStatusCode enumValue : SemanticResourceStatusCode.values()) {
					if (myIndexArray[enumValue.toInt()] != null) {
						throw new IllegalArgumentException("Integer value already in use: " + enumValue.toInt()); //$NON-NLS-1$ this can not happend unless the code is changed -> no localization
					}
					myIndexArray[enumValue.toInt()] = enumValue;
				}
			}
		}
		return myIndexArray[value];
	}

}
