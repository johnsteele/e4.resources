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
 * $Id: SemanticDBImpl.java,v 1.1 2010/02/10 20:56:39 ebartsch Exp $
 */
package org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl;

import java.util.Collection;

import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticDB;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeRoot;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Semantic DB</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>
 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.SemanticDBImpl#getRoots
 * <em>Roots</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
@SuppressWarnings("unqualified-field-access")
public class SemanticDBImpl extends EObjectImpl implements SemanticDB {
	/**
	 * The cached value of the '{@link #getRoots() <em>Roots</em>}' containment
	 * reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getRoots()
	 * @generated
	 * @ordered
	 */
	protected EList<TreeRoot> roots;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected SemanticDBImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SemanticResourceDBPackage.Literals.SEMANTIC_DB;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EList<TreeRoot> getRoots() {
		if (roots == null) {
			roots = new EObjectContainmentWithInverseEList<TreeRoot>(TreeRoot.class, this, SemanticResourceDBPackage.SEMANTIC_DB__ROOTS,
					SemanticResourceDBPackage.TREE_ROOT__PARENT_DB);
		}
		return roots;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	@SuppressWarnings("unchecked")
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SemanticResourceDBPackage.SEMANTIC_DB__ROOTS :
				return ((InternalEList<InternalEObject>) (InternalEList<?>) getRoots()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SemanticResourceDBPackage.SEMANTIC_DB__ROOTS :
				return ((InternalEList<?>) getRoots()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case SemanticResourceDBPackage.SEMANTIC_DB__ROOTS :
				return getRoots();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case SemanticResourceDBPackage.SEMANTIC_DB__ROOTS :
				getRoots().clear();
				getRoots().addAll((Collection<? extends TreeRoot>) newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case SemanticResourceDBPackage.SEMANTIC_DB__ROOTS :
				getRoots().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case SemanticResourceDBPackage.SEMANTIC_DB__ROOTS :
				return roots != null && !roots.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} // SemanticDBImpl
