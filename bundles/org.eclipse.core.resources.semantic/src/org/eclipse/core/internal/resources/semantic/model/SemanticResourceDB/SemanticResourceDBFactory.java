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
 * $Id: //til/til.sfs/dev/src/_org.eclipse.core.resources.semantic/ecp/api/org/eclipse/core/internal/resources/semantic/model/SemanticResourceDB/SemanticResourceDBFactory.java#2 $
 */
package org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc --> The <b>Factory</b> for the model. It provides a
 * create method for each non-abstract class of the model. <!-- end-user-doc -->
 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage
 * @generated
 */
public interface SemanticResourceDBFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 */
	SemanticResourceDBFactory eINSTANCE = org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.SemanticResourceDBFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Resource Tree Node</em>'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return a new object of class '<em>Resource Tree Node</em>'.
	 * @generated
	 */
	ResourceTreeNode createResourceTreeNode();

	/**
	 * Returns a new object of class '<em>Semantic DB</em>'.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @return a new object of class '<em>Semantic DB</em>'.
	 * @generated
	 */
	SemanticDB createSemanticDB();

	/**
	 * Returns a new object of class '<em>Tree Root</em>'.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @return a new object of class '<em>Tree Root</em>'.
	 * @generated
	 */
	TreeRoot createTreeRoot();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	SemanticResourceDBPackage getSemanticResourceDBPackage();

} // SemanticResourceDBFactory
