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
 * $Id: //til/til.sfs/dev/src/_org.eclipse.core.resources.semantic/ecp/api/org/eclipse/core/internal/resources/semantic/model/SemanticResourceDB/TreeRoot.java#2 $
 */
package org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB;

/**
 * <!-- begin-user-doc --> A representation of the model object '
 * <em><b>Tree Root</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeRoot#getParentDB <em>Parent DB</em>}</li>
 *   <li>{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeRoot#getRootURI <em>Root URI</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage#getTreeRoot()
 * @model
 * @generated
 */
public interface TreeRoot extends ResourceTreeNode {
	/**
	 * Returns the value of the '<em><b>Parent DB</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticDB#getRoots <em>Roots</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parent DB</em>' container reference isn't
	 * clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent DB</em>' container reference.
	 * @see #setParentDB(SemanticDB)
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage#getTreeRoot_ParentDB()
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticDB#getRoots
	 * @model opposite="roots" transient="false"
	 * @generated
	 */
	SemanticDB getParentDB();

	/**
	 * Sets the value of the '{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeRoot#getParentDB <em>Parent DB</em>}' container reference.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @param value the new value of the '<em>Parent DB</em>' container reference.
	 * @see #getParentDB()
	 * @generated
	 */
	void setParentDB(SemanticDB value);

	/**
	 * Returns the value of the '<em><b>Root URI</b></em>' attribute. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Root URI</em>' attribute isn't clear, there
	 * really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Root URI</em>' attribute.
	 * @see #setRootURI(String)
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage#getTreeRoot_RootURI()
	 * @model
	 * @generated
	 */
	String getRootURI();

	/**
	 * Sets the value of the '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeRoot#getRootURI
	 * <em>Root URI</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @param value
	 *            the new value of the '<em>Root URI</em>' attribute.
	 * @see #getRootURI()
	 * @generated
	 */
	void setRootURI(String value);

} // TreeRoot
