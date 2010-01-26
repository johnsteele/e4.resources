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
 * $Id: //til/til.sfs/dev/src/_org.eclipse.core.resources.semantic/ecp/api/org/eclipse/core/internal/resources/semantic/model/SemanticResourceDB/SemanticDB.java#2 $
 */
package org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> A representation of the model object '
 * <em><b>Semantic DB</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticDB#getRoots <em>Roots</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage#getSemanticDB()
 * @model
 * @generated
 */
public interface SemanticDB extends EObject {
	/**
	 * Returns the value of the '<em><b>Roots</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeRoot}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeRoot#getParentDB <em>Parent DB</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Roots</em>' containment reference list isn't
	 * clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Roots</em>' containment reference list.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage#getSemanticDB_Roots()
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeRoot#getParentDB
	 * @model opposite="parentDB" containment="true"
	 * @generated
	 */
	EList<TreeRoot> getRoots();

} // SemanticDB
