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
 */
package org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl;

import java.util.HashMap;

import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticDB;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBFactory;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeNodeType;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeRoot;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Factory</b>. <!--
 * end-user-doc -->
 * 
 * @generated
 */
public class SemanticResourceDBFactoryImpl extends EFactoryImpl implements SemanticResourceDBFactory {
	/**
	 * Creates the default factory implementation. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	public static SemanticResourceDBFactory init() {
		try {
			SemanticResourceDBFactory theSemanticResourceDBFactory = (SemanticResourceDBFactory) EPackage.Registry.INSTANCE
					.getEFactory("http://www.eclipse.org/core/2009/resources/semantic"); //$NON-NLS-1$ 
			if (theSemanticResourceDBFactory != null) {
				return theSemanticResourceDBFactory;
			}
		} catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new SemanticResourceDBFactoryImpl();
	}

	/**
	 * Creates an instance of the factory. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	public SemanticResourceDBFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE :
				return createResourceTreeNode();
			case SemanticResourceDBPackage.SEMANTIC_DB :
				return createSemanticDB();
			case SemanticResourceDBPackage.TREE_ROOT :
				return createTreeRoot();
			default :
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case SemanticResourceDBPackage.TREE_NODE_TYPE :
				return createTreeNodeTypeFromString(eDataType, initialValue);
			case SemanticResourceDBPackage.PERSISTENT_PROPERTIES :
				return createPersistentPropertiesFromString(eDataType, initialValue);
			case SemanticResourceDBPackage.SESSION_PROPERTIES :
				return createSessionPropertiesFromString(eDataType, initialValue);
			default :
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case SemanticResourceDBPackage.TREE_NODE_TYPE :
				return convertTreeNodeTypeToString(eDataType, instanceValue);
			case SemanticResourceDBPackage.PERSISTENT_PROPERTIES :
				return convertPersistentPropertiesToString(eDataType, instanceValue);
			case SemanticResourceDBPackage.SESSION_PROPERTIES :
				return convertSessionPropertiesToString(eDataType, instanceValue);
			default :
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public ResourceTreeNode createResourceTreeNode() {
		ResourceTreeNodeImpl resourceTreeNode = new ResourceTreeNodeImpl();
		return resourceTreeNode;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public SemanticDB createSemanticDB() {
		SemanticDBImpl semanticDB = new SemanticDBImpl();
		return semanticDB;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public TreeRoot createTreeRoot() {
		TreeRootImpl treeRoot = new TreeRootImpl();
		return treeRoot;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public TreeNodeType createTreeNodeTypeFromString(EDataType eDataType, String initialValue) {
		TreeNodeType result = TreeNodeType.get(initialValue);
		if (result == null)
			throw new IllegalArgumentException(
					"The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return result;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public String convertTreeNodeTypeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@SuppressWarnings("rawtypes")
	public HashMap createPersistentPropertiesFromString(EDataType eDataType, String initialValue) {
		return (HashMap) super.createFromString(initialValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public String convertPersistentPropertiesToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(instanceValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@SuppressWarnings("rawtypes")
	public HashMap createSessionPropertiesFromString(EDataType eDataType, String initialValue) {
		return (HashMap) super.createFromString(initialValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public String convertSessionPropertiesToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(instanceValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public SemanticResourceDBPackage getSemanticResourceDBPackage() {
		return (SemanticResourceDBPackage) getEPackage();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static SemanticResourceDBPackage getPackage() {
		return SemanticResourceDBPackage.eINSTANCE;
	}

} // SemanticResourceDBFactoryImpl
