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
 * $Id: TreeRootImpl.java,v 1.1 2010/02/10 20:56:39 ebartsch Exp $
 */
package org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl;

import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticDB;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeRoot;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Tree Root</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>
 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.TreeRootImpl#getParentDB
 * <em>Parent DB</em>}</li>
 * <li>
 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.TreeRootImpl#getRootURI
 * <em>Root URI</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
@SuppressWarnings("unqualified-field-access")
public class TreeRootImpl extends ResourceTreeNodeImpl implements TreeRoot {
	/**
	 * The default value of the '{@link #getRootURI() <em>Root URI</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getRootURI()
	 * @generated
	 * @ordered
	 */
	protected static final String ROOT_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRootURI() <em>Root URI</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getRootURI()
	 * @generated
	 * @ordered
	 */
	protected String rootURI = ROOT_URI_EDEFAULT;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected TreeRootImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SemanticResourceDBPackage.Literals.TREE_ROOT;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public SemanticDB getParentDB() {
		if (eContainerFeatureID() != SemanticResourceDBPackage.TREE_ROOT__PARENT_DB)
			return null;
		return (SemanticDB) eContainer();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newParentDB
	 * @param msgs
	 * @return object
	 * @generated
	 */
	public NotificationChain basicSetParentDB(SemanticDB newParentDB, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject) newParentDB, SemanticResourceDBPackage.TREE_ROOT__PARENT_DB, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setParentDB(SemanticDB newParentDB) {
		if (newParentDB != eInternalContainer()
				|| (eContainerFeatureID() != SemanticResourceDBPackage.TREE_ROOT__PARENT_DB && newParentDB != null)) {
			if (EcoreUtil.isAncestor(this, newParentDB))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString()); //$NON-NLS-1$
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newParentDB != null)
				msgs = ((InternalEObject) newParentDB).eInverseAdd(this, SemanticResourceDBPackage.SEMANTIC_DB__ROOTS, SemanticDB.class,
						msgs);
			msgs = basicSetParentDB(newParentDB, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SemanticResourceDBPackage.TREE_ROOT__PARENT_DB, newParentDB, newParentDB));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public String getRootURI() {
		return rootURI;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setRootURI(String newRootURI) {
		String oldRootURI = rootURI;
		rootURI = newRootURI;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SemanticResourceDBPackage.TREE_ROOT__ROOT_URI, oldRootURI, rootURI));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SemanticResourceDBPackage.TREE_ROOT__PARENT_DB :
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetParentDB((SemanticDB) otherEnd, msgs);
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
			case SemanticResourceDBPackage.TREE_ROOT__PARENT_DB :
				return basicSetParentDB(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		switch (eContainerFeatureID()) {
			case SemanticResourceDBPackage.TREE_ROOT__PARENT_DB :
				return eInternalContainer().eInverseRemove(this, SemanticResourceDBPackage.SEMANTIC_DB__ROOTS, SemanticDB.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case SemanticResourceDBPackage.TREE_ROOT__PARENT_DB :
				return getParentDB();
			case SemanticResourceDBPackage.TREE_ROOT__ROOT_URI :
				return getRootURI();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case SemanticResourceDBPackage.TREE_ROOT__PARENT_DB :
				setParentDB((SemanticDB) newValue);
				return;
			case SemanticResourceDBPackage.TREE_ROOT__ROOT_URI :
				setRootURI((String) newValue);
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
			case SemanticResourceDBPackage.TREE_ROOT__PARENT_DB :
				setParentDB((SemanticDB) null);
				return;
			case SemanticResourceDBPackage.TREE_ROOT__ROOT_URI :
				setRootURI(ROOT_URI_EDEFAULT);
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
			case SemanticResourceDBPackage.TREE_ROOT__PARENT_DB :
				return getParentDB() != null;
			case SemanticResourceDBPackage.TREE_ROOT__ROOT_URI :
				return ROOT_URI_EDEFAULT == null ? rootURI != null : !ROOT_URI_EDEFAULT.equals(rootURI);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy())
			return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (rootURI: "); //$NON-NLS-1$
		result.append(rootURI);
		result.append(')');
		return result.toString();
	}

} // TreeRootImpl
