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
 * $Id: //til/til.sfs/dev/src/_org.eclipse.core.resources.semantic/ecp/api/org/eclipse/core/internal/resources/semantic/model/SemanticResourceDB/impl/SemanticResourceDBPackageImpl.java#3 $
 */
package org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl;

import java.util.HashMap;

import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticDB;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBFactory;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeNodeType;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeRoot;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EPackageImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Package</b>. <!--
 * end-user-doc -->
 * 
 * @generated
 */
@SuppressWarnings("unqualified-field-access")
public class SemanticResourceDBPackageImpl extends EPackageImpl implements SemanticResourceDBPackage {
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private EClass resourceTreeNodeEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private EClass semanticDBEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private EClass treeRootEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private EEnum treeNodeTypeEEnum = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private EDataType persistentPropertiesEDataType = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private EDataType sessionPropertiesEDataType = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the
	 * package package URI value.
	 * <p>
	 * Note: the correct way to create the package is via the static factory
	 * method {@link #init init()}, which also performs initialization of the
	 * package, or returns the registered package, if one already exists. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private SemanticResourceDBPackageImpl() {
		super(eNS_URI, SemanticResourceDBFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model,
	 * and for any others upon which it depends.
	 * 
	 * <p>
	 * This method is used to initialize
	 * {@link SemanticResourceDBPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access
	 * that field to obtain the package. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @return object
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static SemanticResourceDBPackage init() {
		if (isInited)
			return (SemanticResourceDBPackage) EPackage.Registry.INSTANCE.getEPackage(SemanticResourceDBPackage.eNS_URI);

		// Obtain or create and register package
		SemanticResourceDBPackageImpl theSemanticResourceDBPackage = (SemanticResourceDBPackageImpl) (EPackage.Registry.INSTANCE.get(eNS_URI) instanceof SemanticResourceDBPackageImpl ? EPackage.Registry.INSTANCE
				.get(eNS_URI)
				: new SemanticResourceDBPackageImpl());

		isInited = true;

		// Create package meta-data objects
		theSemanticResourceDBPackage.createPackageContents();

		// Initialize created meta-data
		theSemanticResourceDBPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theSemanticResourceDBPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(SemanticResourceDBPackage.eNS_URI, theSemanticResourceDBPackage);
		return theSemanticResourceDBPackage;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EClass getResourceTreeNode() {
		return resourceTreeNodeEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EAttribute getResourceTreeNode_Name() {
		return (EAttribute) resourceTreeNodeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EReference getResourceTreeNode_Children() {
		return (EReference) resourceTreeNodeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EReference getResourceTreeNode_Parent() {
		return (EReference) resourceTreeNodeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EAttribute getResourceTreeNode_Exists() {
		return (EAttribute) resourceTreeNodeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EAttribute getResourceTreeNode_TemplateID() {
		return (EAttribute) resourceTreeNodeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EAttribute getResourceTreeNode_PersistentProperties() {
		return (EAttribute) resourceTreeNodeEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EAttribute getResourceTreeNode_LocalOnly() {
		return (EAttribute) resourceTreeNodeEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EAttribute getResourceTreeNode_Type() {
		return (EAttribute) resourceTreeNodeEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EAttribute getResourceTreeNode_SessionProperties() {
		return (EAttribute) resourceTreeNodeEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EClass getSemanticDB() {
		return semanticDBEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EReference getSemanticDB_Roots() {
		return (EReference) semanticDBEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EClass getTreeRoot() {
		return treeRootEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EReference getTreeRoot_ParentDB() {
		return (EReference) treeRootEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EAttribute getTreeRoot_RootURI() {
		return (EAttribute) treeRootEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EEnum getTreeNodeType() {
		return treeNodeTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EDataType getPersistentProperties() {
		return persistentPropertiesEDataType;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EDataType getSessionProperties() {
		return sessionPropertiesEDataType;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public SemanticResourceDBFactory getSemanticResourceDBFactory() {
		return (SemanticResourceDBFactory) getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package. This method is guarded to
	 * have no affect on any invocation but its first. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated)
			return;
		isCreated = true;

		// Create classes and their features
		resourceTreeNodeEClass = createEClass(RESOURCE_TREE_NODE);
		createEAttribute(resourceTreeNodeEClass, RESOURCE_TREE_NODE__NAME);
		createEReference(resourceTreeNodeEClass, RESOURCE_TREE_NODE__CHILDREN);
		createEReference(resourceTreeNodeEClass, RESOURCE_TREE_NODE__PARENT);
		createEAttribute(resourceTreeNodeEClass, RESOURCE_TREE_NODE__EXISTS);
		createEAttribute(resourceTreeNodeEClass, RESOURCE_TREE_NODE__TEMPLATE_ID);
		createEAttribute(resourceTreeNodeEClass, RESOURCE_TREE_NODE__PERSISTENT_PROPERTIES);
		createEAttribute(resourceTreeNodeEClass, RESOURCE_TREE_NODE__LOCAL_ONLY);
		createEAttribute(resourceTreeNodeEClass, RESOURCE_TREE_NODE__TYPE);
		createEAttribute(resourceTreeNodeEClass, RESOURCE_TREE_NODE__SESSION_PROPERTIES);

		semanticDBEClass = createEClass(SEMANTIC_DB);
		createEReference(semanticDBEClass, SEMANTIC_DB__ROOTS);

		treeRootEClass = createEClass(TREE_ROOT);
		createEReference(treeRootEClass, TREE_ROOT__PARENT_DB);
		createEAttribute(treeRootEClass, TREE_ROOT__ROOT_URI);

		// Create enums
		treeNodeTypeEEnum = createEEnum(TREE_NODE_TYPE);

		// Create data types
		persistentPropertiesEDataType = createEDataType(PERSISTENT_PROPERTIES);
		sessionPropertiesEDataType = createEDataType(SESSION_PROPERTIES);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model. This
	 * method is guarded to have no affect on any invocation but its first. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized)
			return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		treeRootEClass.getESuperTypes().add(this.getResourceTreeNode());

		// Initialize classes and features; add operations and parameters
		initEClass(resourceTreeNodeEClass, ResourceTreeNode.class, "ResourceTreeNode", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(
				getResourceTreeNode_Name(),
				ecorePackage.getEString(),
				"name", null, 0, 1, ResourceTreeNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(
				getResourceTreeNode_Children(),
				this.getResourceTreeNode(),
				this.getResourceTreeNode_Parent(),
				"children", null, 0, -1, ResourceTreeNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(
				getResourceTreeNode_Parent(),
				this.getResourceTreeNode(),
				this.getResourceTreeNode_Children(),
				"parent", null, 0, 1, ResourceTreeNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(
				getResourceTreeNode_Exists(),
				ecorePackage.getEBoolean(),
				"exists", "true", 0, 1, ResourceTreeNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(
				getResourceTreeNode_TemplateID(),
				ecorePackage.getEString(),
				"templateID", null, 0, 1, ResourceTreeNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(
				getResourceTreeNode_PersistentProperties(),
				this.getPersistentProperties(),
				"persistentProperties", null, 0, 1, ResourceTreeNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(
				getResourceTreeNode_LocalOnly(),
				ecorePackage.getEBoolean(),
				"localOnly", "false", 0, 1, ResourceTreeNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(
				getResourceTreeNode_Type(),
				this.getTreeNodeType(),
				"type", "FILE", 0, 1, ResourceTreeNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(
				getResourceTreeNode_SessionProperties(),
				this.getSessionProperties(),
				"sessionProperties", null, 0, 1, ResourceTreeNode.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(semanticDBEClass, SemanticDB.class, "SemanticDB", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(
				getSemanticDB_Roots(),
				this.getTreeRoot(),
				this.getTreeRoot_ParentDB(),
				"roots", null, 0, -1, SemanticDB.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(treeRootEClass, TreeRoot.class, "TreeRoot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(
				getTreeRoot_ParentDB(),
				this.getSemanticDB(),
				this.getSemanticDB_Roots(),
				"parentDB", null, 0, 1, TreeRoot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getTreeRoot_RootURI(), ecorePackage.getEString(),
				"rootURI", null, 0, 1, TreeRoot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		// Initialize enums and add enum literals
		initEEnum(treeNodeTypeEEnum, TreeNodeType.class, "TreeNodeType"); //$NON-NLS-1$
		addEEnumLiteral(treeNodeTypeEEnum, TreeNodeType.FOLDER);
		addEEnumLiteral(treeNodeTypeEEnum, TreeNodeType.FILE);
		addEEnumLiteral(treeNodeTypeEEnum, TreeNodeType.PROJECT);
		addEEnumLiteral(treeNodeTypeEEnum, TreeNodeType.UNKNOWN);

		// Initialize data types
		initEDataType(persistentPropertiesEDataType, HashMap.class,
				"PersistentProperties", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS, "java.util.HashMap<java.lang.String, java.lang.String>"); //$NON-NLS-1$ //$NON-NLS-2$
		initEDataType(
				sessionPropertiesEDataType,
				HashMap.class,
				"SessionProperties", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS, "java.util.HashMap<org.eclipse.core.runtime.QualifiedName, java.lang.Object>"); //$NON-NLS-1$ //$NON-NLS-2$

		// Create resource
		createResource(eNS_URI);
	}

} // SemanticResourceDBPackageImpl
