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
package org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc --> The <b>Package</b> for the model. It contains
 * accessors for the meta objects to represent
 * <ul>
 * <li>each class,</li>
 * <li>each feature of each class,</li>
 * <li>each enum,</li>
 * <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * 
 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBFactory
 * @model kind="package"
 * @generated
 */
public interface SemanticResourceDBPackage extends EPackage {
	/**
	 * The package name. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	String eNAME = "SemanticResourceDB"; //$NON-NLS-1$

	/**
	 * The package namespace URI. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	String eNS_URI = "http://www.eclipse.org/core/2009/resources/semantic"; //$NON-NLS-1$

	/**
	 * The package namespace name. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	String eNS_PREFIX = "SemanticResourceDB"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	SemanticResourceDBPackage eINSTANCE = org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.SemanticResourceDBPackageImpl
			.init();

	/**
	 * The meta object id for the '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.ResourceTreeNodeImpl
	 * <em>Resource Tree Node</em>}' class. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.ResourceTreeNodeImpl
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.SemanticResourceDBPackageImpl#getResourceTreeNode()
	 * @generated
	 */
	int RESOURCE_TREE_NODE = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int RESOURCE_TREE_NODE__NAME = 0;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference
	 * list. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int RESOURCE_TREE_NODE__CHILDREN = 1;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int RESOURCE_TREE_NODE__PARENT = 2;

	/**
	 * The feature id for the '<em><b>Exists</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int RESOURCE_TREE_NODE__EXISTS = 3;

	/**
	 * The feature id for the '<em><b>Template ID</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int RESOURCE_TREE_NODE__TEMPLATE_ID = 4;

	/**
	 * The feature id for the '<em><b>Persistent Properties</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int RESOURCE_TREE_NODE__PERSISTENT_PROPERTIES = 5;

	/**
	 * The feature id for the '<em><b>Local Only</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int RESOURCE_TREE_NODE__LOCAL_ONLY = 6;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int RESOURCE_TREE_NODE__TYPE = 7;

	/**
	 * The feature id for the '<em><b>Session Properties</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int RESOURCE_TREE_NODE__SESSION_PROPERTIES = 8;

	/**
	 * The feature id for the '<em><b>Path</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int RESOURCE_TREE_NODE__PATH = 9;

	/**
	 * The feature id for the '<em><b>Query Part</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int RESOURCE_TREE_NODE__QUERY_PART = 10;

	/**
	 * The feature id for the '<em><b>Remote URI</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int RESOURCE_TREE_NODE__REMOTE_URI = 11;

	/**
	 * The number of structural features of the '<em>Resource Tree Node</em>'
	 * class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int RESOURCE_TREE_NODE_FEATURE_COUNT = 12;

	/**
	 * The meta object id for the '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.SemanticDBImpl
	 * <em>Semantic DB</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.SemanticDBImpl
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.SemanticResourceDBPackageImpl#getSemanticDB()
	 * @generated
	 */
	int SEMANTIC_DB = 1;

	/**
	 * The feature id for the '<em><b>Roots</b></em>' containment reference
	 * list. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int SEMANTIC_DB__ROOTS = 0;

	/**
	 * The number of structural features of the '<em>Semantic DB</em>' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int SEMANTIC_DB_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.TreeRootImpl
	 * <em>Tree Root</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.TreeRootImpl
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.SemanticResourceDBPackageImpl#getTreeRoot()
	 * @generated
	 */
	int TREE_ROOT = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TREE_ROOT__NAME = RESOURCE_TREE_NODE__NAME;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference
	 * list. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TREE_ROOT__CHILDREN = RESOURCE_TREE_NODE__CHILDREN;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TREE_ROOT__PARENT = RESOURCE_TREE_NODE__PARENT;

	/**
	 * The feature id for the '<em><b>Exists</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TREE_ROOT__EXISTS = RESOURCE_TREE_NODE__EXISTS;

	/**
	 * The feature id for the '<em><b>Template ID</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TREE_ROOT__TEMPLATE_ID = RESOURCE_TREE_NODE__TEMPLATE_ID;

	/**
	 * The feature id for the '<em><b>Persistent Properties</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TREE_ROOT__PERSISTENT_PROPERTIES = RESOURCE_TREE_NODE__PERSISTENT_PROPERTIES;

	/**
	 * The feature id for the '<em><b>Local Only</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TREE_ROOT__LOCAL_ONLY = RESOURCE_TREE_NODE__LOCAL_ONLY;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TREE_ROOT__TYPE = RESOURCE_TREE_NODE__TYPE;

	/**
	 * The feature id for the '<em><b>Session Properties</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TREE_ROOT__SESSION_PROPERTIES = RESOURCE_TREE_NODE__SESSION_PROPERTIES;

	/**
	 * The feature id for the '<em><b>Path</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TREE_ROOT__PATH = RESOURCE_TREE_NODE__PATH;

	/**
	 * The feature id for the '<em><b>Query Part</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TREE_ROOT__QUERY_PART = RESOURCE_TREE_NODE__QUERY_PART;

	/**
	 * The feature id for the '<em><b>Remote URI</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TREE_ROOT__REMOTE_URI = RESOURCE_TREE_NODE__REMOTE_URI;

	/**
	 * The feature id for the '<em><b>Parent DB</b></em>' container reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TREE_ROOT__PARENT_DB = RESOURCE_TREE_NODE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Root URI</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TREE_ROOT__ROOT_URI = RESOURCE_TREE_NODE_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Tree Root</em>' class. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TREE_ROOT_FEATURE_COUNT = RESOURCE_TREE_NODE_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeNodeType
	 * <em>Tree Node Type</em>}' enum. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeNodeType
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.SemanticResourceDBPackageImpl#getTreeNodeType()
	 * @generated
	 */
	int TREE_NODE_TYPE = 3;

	/**
	 * The meta object id for the '<em>Persistent Properties</em>' data type.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see java.util.HashMap
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.SemanticResourceDBPackageImpl#getPersistentProperties()
	 * @generated
	 */
	int PERSISTENT_PROPERTIES = 4;

	/**
	 * The meta object id for the '<em>Session Properties</em>' data type. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see java.util.HashMap
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.SemanticResourceDBPackageImpl#getSessionProperties()
	 * @generated
	 */
	int SESSION_PROPERTIES = 5;

	/**
	 * Returns the meta object for class '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode
	 * <em>Resource Tree Node</em>}'. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @return the meta object for class '<em>Resource Tree Node</em>'.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode
	 * @generated
	 */
	EClass getResourceTreeNode();

	/**
	 * Returns the meta object for the attribute '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getName
	 * <em>Name</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getName()
	 * @see #getResourceTreeNode()
	 * @generated
	 */
	EAttribute getResourceTreeNode_Name();

	/**
	 * Returns the meta object for the containment reference list '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getChildren
	 * <em>Children</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the containment reference list '
	 *         <em>Children</em>'.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getChildren()
	 * @see #getResourceTreeNode()
	 * @generated
	 */
	EReference getResourceTreeNode_Children();

	/**
	 * Returns the meta object for the container reference '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getParent
	 * <em>Parent</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the container reference '<em>Parent</em>'.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getParent()
	 * @see #getResourceTreeNode()
	 * @generated
	 */
	EReference getResourceTreeNode_Parent();

	/**
	 * Returns the meta object for the attribute '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#isExists
	 * <em>Exists</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Exists</em>'.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#isExists()
	 * @see #getResourceTreeNode()
	 * @generated
	 */
	EAttribute getResourceTreeNode_Exists();

	/**
	 * Returns the meta object for the attribute '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getTemplateID
	 * <em>Template ID</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Template ID</em>'.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getTemplateID()
	 * @see #getResourceTreeNode()
	 * @generated
	 */
	EAttribute getResourceTreeNode_TemplateID();

	/**
	 * Returns the meta object for the attribute '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getPersistentProperties
	 * <em>Persistent Properties</em>}'. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Persistent Properties</em>
	 *         '.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getPersistentProperties()
	 * @see #getResourceTreeNode()
	 * @generated
	 */
	EAttribute getResourceTreeNode_PersistentProperties();

	/**
	 * Returns the meta object for the attribute '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#isLocalOnly
	 * <em>Local Only</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Local Only</em>'.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#isLocalOnly()
	 * @see #getResourceTreeNode()
	 * @generated
	 */
	EAttribute getResourceTreeNode_LocalOnly();

	/**
	 * Returns the meta object for the attribute '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getType
	 * <em>Type</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getType()
	 * @see #getResourceTreeNode()
	 * @generated
	 */
	EAttribute getResourceTreeNode_Type();

	/**
	 * Returns the meta object for the attribute '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getSessionProperties
	 * <em>Session Properties</em>}'. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @return the meta object for the attribute '<em>Session Properties</em>'.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getSessionProperties()
	 * @see #getResourceTreeNode()
	 * @generated
	 */
	EAttribute getResourceTreeNode_SessionProperties();

	/**
	 * Returns the meta object for the attribute '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getPath
	 * <em>Path</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Path</em>'.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getPath()
	 * @see #getResourceTreeNode()
	 * @generated
	 */
	EAttribute getResourceTreeNode_Path();

	/**
	 * Returns the meta object for the attribute '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getQueryPart
	 * <em>Query Part</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Query Part</em>'.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getQueryPart()
	 * @see #getResourceTreeNode()
	 * @generated
	 */
	EAttribute getResourceTreeNode_QueryPart();

	/**
	 * Returns the meta object for the attribute '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getRemoteURI
	 * <em>Remote URI</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Remote URI</em>'.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getRemoteURI()
	 * @see #getResourceTreeNode()
	 * @generated
	 */
	EAttribute getResourceTreeNode_RemoteURI();

	/**
	 * Returns the meta object for class '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticDB
	 * <em>Semantic DB</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for class '<em>Semantic DB</em>'.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticDB
	 * @generated
	 */
	EClass getSemanticDB();

	/**
	 * Returns the meta object for the containment reference list '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticDB#getRoots
	 * <em>Roots</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the containment reference list '
	 *         <em>Roots</em>'.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticDB#getRoots()
	 * @see #getSemanticDB()
	 * @generated
	 */
	EReference getSemanticDB_Roots();

	/**
	 * Returns the meta object for class '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeRoot
	 * <em>Tree Root</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for class '<em>Tree Root</em>'.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeRoot
	 * @generated
	 */
	EClass getTreeRoot();

	/**
	 * Returns the meta object for the container reference '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeRoot#getParentDB
	 * <em>Parent DB</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the container reference '<em>Parent DB</em>'.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeRoot#getParentDB()
	 * @see #getTreeRoot()
	 * @generated
	 */
	EReference getTreeRoot_ParentDB();

	/**
	 * Returns the meta object for the attribute '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeRoot#getRootURI
	 * <em>Root URI</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Root URI</em>'.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeRoot#getRootURI()
	 * @see #getTreeRoot()
	 * @generated
	 */
	EAttribute getTreeRoot_RootURI();

	/**
	 * Returns the meta object for enum '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeNodeType
	 * <em>Tree Node Type</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for enum '<em>Tree Node Type</em>'.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeNodeType
	 * @generated
	 */
	EEnum getTreeNodeType();

	/**
	 * Returns the meta object for data type '{@link java.util.HashMap
	 * <em>Persistent Properties</em>}'. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @return the meta object for data type '<em>Persistent Properties</em>'.
	 * @see java.util.HashMap
	 * @model 
	 *        instanceClass="java.util.HashMap<java.lang.String, java.lang.String>"
	 * @generated
	 */
	EDataType getPersistentProperties();

	/**
	 * Returns the meta object for data type '{@link java.util.HashMap
	 * <em>Session Properties</em>}'. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @return the meta object for data type '<em>Session Properties</em>'.
	 * @see java.util.HashMap
	 * @model instanceClass=
	 *        "java.util.HashMap<org.eclipse.core.runtime.QualifiedName, java.lang.Object>"
	 * @generated
	 */
	EDataType getSessionProperties();

	/**
	 * Returns the factory that creates the instances of the model. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	SemanticResourceDBFactory getSemanticResourceDBFactory();

	/**
	 * <!-- begin-user-doc --> Defines literals for the meta objects that
	 * represent
	 * <ul>
	 * <li>each class,</li>
	 * <li>each feature of each class,</li>
	 * <li>each enum,</li>
	 * <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@SuppressWarnings("hiding")
	interface Literals {
		/**
		 * The meta object literal for the '
		 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.ResourceTreeNodeImpl
		 * <em>Resource Tree Node</em>}' class. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.ResourceTreeNodeImpl
		 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.SemanticResourceDBPackageImpl#getResourceTreeNode()
		 * @generated
		 */
		EClass RESOURCE_TREE_NODE = eINSTANCE.getResourceTreeNode();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute
		 * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute RESOURCE_TREE_NODE__NAME = eINSTANCE.getResourceTreeNode_Name();

		/**
		 * The meta object literal for the '<em><b>Children</b></em>'
		 * containment reference list feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @generated
		 */
		EReference RESOURCE_TREE_NODE__CHILDREN = eINSTANCE.getResourceTreeNode_Children();

		/**
		 * The meta object literal for the '<em><b>Parent</b></em>' container
		 * reference feature. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference RESOURCE_TREE_NODE__PARENT = eINSTANCE.getResourceTreeNode_Parent();

		/**
		 * The meta object literal for the '<em><b>Exists</b></em>' attribute
		 * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute RESOURCE_TREE_NODE__EXISTS = eINSTANCE.getResourceTreeNode_Exists();

		/**
		 * The meta object literal for the '<em><b>Template ID</b></em>'
		 * attribute feature. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute RESOURCE_TREE_NODE__TEMPLATE_ID = eINSTANCE.getResourceTreeNode_TemplateID();

		/**
		 * The meta object literal for the '
		 * <em><b>Persistent Properties</b></em>' attribute feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute RESOURCE_TREE_NODE__PERSISTENT_PROPERTIES = eINSTANCE.getResourceTreeNode_PersistentProperties();

		/**
		 * The meta object literal for the '<em><b>Local Only</b></em>'
		 * attribute feature. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute RESOURCE_TREE_NODE__LOCAL_ONLY = eINSTANCE.getResourceTreeNode_LocalOnly();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute
		 * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute RESOURCE_TREE_NODE__TYPE = eINSTANCE.getResourceTreeNode_Type();

		/**
		 * The meta object literal for the '<em><b>Session Properties</b></em>'
		 * attribute feature. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute RESOURCE_TREE_NODE__SESSION_PROPERTIES = eINSTANCE.getResourceTreeNode_SessionProperties();

		/**
		 * The meta object literal for the '<em><b>Path</b></em>' attribute
		 * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute RESOURCE_TREE_NODE__PATH = eINSTANCE.getResourceTreeNode_Path();

		/**
		 * The meta object literal for the '<em><b>Query Part</b></em>'
		 * attribute feature. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute RESOURCE_TREE_NODE__QUERY_PART = eINSTANCE.getResourceTreeNode_QueryPart();

		/**
		 * The meta object literal for the '<em><b>Remote URI</b></em>'
		 * attribute feature. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute RESOURCE_TREE_NODE__REMOTE_URI = eINSTANCE.getResourceTreeNode_RemoteURI();

		/**
		 * The meta object literal for the '
		 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.SemanticDBImpl
		 * <em>Semantic DB</em>}' class. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.SemanticDBImpl
		 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.SemanticResourceDBPackageImpl#getSemanticDB()
		 * @generated
		 */
		EClass SEMANTIC_DB = eINSTANCE.getSemanticDB();

		/**
		 * The meta object literal for the '<em><b>Roots</b></em>' containment
		 * reference list feature. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference SEMANTIC_DB__ROOTS = eINSTANCE.getSemanticDB_Roots();

		/**
		 * The meta object literal for the '
		 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.TreeRootImpl
		 * <em>Tree Root</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc
		 * -->
		 * 
		 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.TreeRootImpl
		 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.SemanticResourceDBPackageImpl#getTreeRoot()
		 * @generated
		 */
		EClass TREE_ROOT = eINSTANCE.getTreeRoot();

		/**
		 * The meta object literal for the '<em><b>Parent DB</b></em>' container
		 * reference feature. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference TREE_ROOT__PARENT_DB = eINSTANCE.getTreeRoot_ParentDB();

		/**
		 * The meta object literal for the '<em><b>Root URI</b></em>' attribute
		 * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute TREE_ROOT__ROOT_URI = eINSTANCE.getTreeRoot_RootURI();

		/**
		 * The meta object literal for the '
		 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeNodeType
		 * <em>Tree Node Type</em>}' enum. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeNodeType
		 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.SemanticResourceDBPackageImpl#getTreeNodeType()
		 * @generated
		 */
		EEnum TREE_NODE_TYPE = eINSTANCE.getTreeNodeType();

		/**
		 * The meta object literal for the '<em>Persistent Properties</em>' data
		 * type. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @see java.util.HashMap
		 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.SemanticResourceDBPackageImpl#getPersistentProperties()
		 * @generated
		 */
		EDataType PERSISTENT_PROPERTIES = eINSTANCE.getPersistentProperties();

		/**
		 * The meta object literal for the '<em>Session Properties</em>' data
		 * type. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @see java.util.HashMap
		 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.SemanticResourceDBPackageImpl#getSessionProperties()
		 * @generated
		 */
		EDataType SESSION_PROPERTIES = eINSTANCE.getSessionProperties();

	}

} // SemanticResourceDBPackage
