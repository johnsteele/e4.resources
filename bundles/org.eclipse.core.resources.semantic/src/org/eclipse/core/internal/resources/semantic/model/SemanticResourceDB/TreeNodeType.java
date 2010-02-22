/**
 * <copyright>
 * Copyright (c) 2009 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eduard Bartsch (SAP AG) - initial API and implementation
 *    Mathias Kinzler (SAP AG) - initial API and implementation
 * </copyright>
 *
 * $Id: TreeNodeType.java,v 1.1 2010/02/10 20:56:41 ebartsch Exp $
 */
package org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc --> A representation of the literals of the enumeration '
 * <em><b>Tree Node Type</b></em>', and utility methods for working with them.
 * <!-- end-user-doc -->
 * 
 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage#getTreeNodeType()
 * @model
 * @generated
 */
@SuppressWarnings("unqualified-field-access")
public enum TreeNodeType implements Enumerator {
	/**
	 * The '<em><b>FOLDER</b></em>' literal object. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @see #FOLDER_VALUE
	 * @generated
	 * @ordered
	 */
	FOLDER(2, "FOLDER", "FOLDER"), //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * The '<em><b>FILE</b></em>' literal object. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @see #FILE_VALUE
	 * @generated
	 * @ordered
	 */
	FILE(1, "FILE", "FILE"), //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * The '<em><b>PROJECT</b></em>' literal object. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @see #PROJECT_VALUE
	 * @generated
	 * @ordered
	 */
	PROJECT(3, "PROJECT", "PROJECT"), //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * The '<em><b>UNKNOWN</b></em>' literal object. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @see #UNKNOWN_VALUE
	 * @generated
	 * @ordered
	 */
	UNKNOWN(0, "UNKNOWN", "UNKNOWN"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * The '<em><b>FOLDER</b></em>' literal value. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>FOLDER</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @see #FOLDER
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int FOLDER_VALUE = 2;

	/**
	 * The '<em><b>FILE</b></em>' literal value. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>FILE</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @see #FILE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int FILE_VALUE = 1;

	/**
	 * The '<em><b>PROJECT</b></em>' literal value. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>PROJECT</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @see #PROJECT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int PROJECT_VALUE = 3;

	/**
	 * The '<em><b>UNKNOWN</b></em>' literal value. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>UNKNOWN</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @see #UNKNOWN
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int UNKNOWN_VALUE = 0;

	/**
	 * An array of all the '<em><b>Tree Node Type</b></em>' enumerators. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private static final TreeNodeType[] VALUES_ARRAY = new TreeNodeType[] { FOLDER, FILE, PROJECT, UNKNOWN, };

	/**
	 * A public read-only list of all the '<em><b>Tree Node Type</b></em>'
	 * enumerators. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public static final List<TreeNodeType> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Tree Node Type</b></em>' literal with the specified
	 * literal value. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param literal
	 * @return object
	 * @generated
	 */
	public static TreeNodeType get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			TreeNodeType result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Tree Node Type</b></em>' literal with the specified
	 * name. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param name
	 * @return object
	 * 
	 * @generated
	 */
	public static TreeNodeType getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			TreeNodeType result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Tree Node Type</b></em>' literal with the specified
	 * integer value. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 * @return object
	 * 
	 * @generated
	 */
	public static TreeNodeType get(int value) {
		switch (value) {
		case FOLDER_VALUE:
			return FOLDER;
		case FILE_VALUE:
			return FILE;
		case PROJECT_VALUE:
			return PROJECT;
		case UNKNOWN_VALUE:
			return UNKNOWN;
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private final int value;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private final String name;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private final String literal;

	/**
	 * Only this class can construct instances. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	private TreeNodeType(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public int getValue() {
		return value;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public String getLiteral() {
		return literal;
	}

	/**
	 * Returns the literal value of the enumerator, which is its string
	 * representation. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString() {
		return literal;
	}

} // TreeNodeType
