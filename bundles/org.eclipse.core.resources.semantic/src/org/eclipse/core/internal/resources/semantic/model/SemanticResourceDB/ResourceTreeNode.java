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

import java.util.HashMap;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> A representation of the model object '
 * <em><b>Resource Tree Node</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getParent <em>Parent</em>}</li>
 *   <li>{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#isExists <em>Exists</em>}</li>
 *   <li>{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getTemplateID <em>Template ID</em>}</li>
 *   <li>{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getPersistentProperties <em>Persistent Properties</em>}</li>
 *   <li>{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#isLocalOnly <em>Local Only</em>}</li>
 *   <li>{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getSessionProperties <em>Session Properties</em>}</li>
 *   <li>{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getPath <em>Path</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage#getResourceTreeNode()
 * @model
 * @generated
 */
public interface ResourceTreeNode extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear, there really
	 * should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage#getResourceTreeNode_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Children</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Children</em>' containment reference list
	 * isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Children</em>' containment reference list.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage#getResourceTreeNode_Children()
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getParent
	 * @model opposite="parent" containment="true"
	 * @generated
	 */
	EList<ResourceTreeNode> getChildren();

	/**
	 * Returns the value of the '<em><b>Parent</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getChildren <em>Children</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parent</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent</em>' container reference.
	 * @see #setParent(ResourceTreeNode)
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage#getResourceTreeNode_Parent()
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getChildren
	 * @model opposite="children" transient="false"
	 * @generated
	 */
	ResourceTreeNode getParent();

	/**
	 * Sets the value of the '{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getParent <em>Parent</em>}' container reference.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @param value the new value of the '<em>Parent</em>' container reference.
	 * @see #getParent()
	 * @generated
	 */
	void setParent(ResourceTreeNode value);

	/**
	 * Returns the value of the '<em><b>Exists</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Exists</em>' attribute isn't clear, there
	 * really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Exists</em>' attribute.
	 * @see #setExists(boolean)
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage#getResourceTreeNode_Exists()
	 * @model default="true"
	 * @generated
	 */
	boolean isExists();

	/**
	 * Sets the value of the '
	 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#isExists
	 * <em>Exists</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @param value
	 *            the new value of the '<em>Exists</em>' attribute.
	 * @see #isExists()
	 * @generated
	 */
	void setExists(boolean value);

	/**
	 * Returns the value of the '<em><b>Template ID</b></em>' attribute. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Template ID</em>' attribute isn't clear, there
	 * really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Template ID</em>' attribute.
	 * @see #setTemplateID(String)
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage#getResourceTreeNode_TemplateID()
	 * @model
	 * @generated
	 */
	String getTemplateID();

	/**
	 * Sets the value of the '{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getTemplateID <em>Template ID</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @param value the new value of the '<em>Template ID</em>' attribute.
	 * @see #getTemplateID()
	 * @generated
	 */
	void setTemplateID(String value);

	/**
	 * Returns the value of the '<em><b>Persistent Properties</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Persistent Properties</em>' attribute isn't
	 * clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Persistent Properties</em>' attribute.
	 * @see #setPersistentProperties(HashMap)
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage#getResourceTreeNode_PersistentProperties()
	 * @model dataType="org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.PersistentProperties"
	 * @generated
	 */
	HashMap<String, String> getPersistentProperties();

	/**
	 * Sets the value of the '{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getPersistentProperties <em>Persistent Properties</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @param value the new value of the '<em>Persistent Properties</em>' attribute.
	 * @see #getPersistentProperties()
	 * @generated
	 */
	void setPersistentProperties(HashMap<String, String> value);

	/**
	 * Returns the value of the '<em><b>Local Only</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Local Only</em>' attribute isn't clear, there
	 * really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Local Only</em>' attribute.
	 * @see #setLocalOnly(boolean)
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage#getResourceTreeNode_LocalOnly()
	 * @model default="false"
	 * @generated
	 */
	boolean isLocalOnly();

	/**
	 * Sets the value of the '{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#isLocalOnly <em>Local Only</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @param value the new value of the '<em>Local Only</em>' attribute.
	 * @see #isLocalOnly()
	 * @generated
	 */
	void setLocalOnly(boolean value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * The default value is <code>"FILE"</code>.
	 * The literals are from the enumeration {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeNodeType}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' attribute isn't clear, there really
	 * should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeNodeType
	 * @see #setType(TreeNodeType)
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage#getResourceTreeNode_Type()
	 * @model default="FILE"
	 * @generated
	 */
	TreeNodeType getType();

	/**
	 * Sets the value of the '{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeNodeType
	 * @see #getType()
	 * @generated
	 */
	void setType(TreeNodeType value);

	/**
	 * Returns the value of the '<em><b>Session Properties</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Session Properties</em>' attribute isn't
	 * clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Session Properties</em>' attribute.
	 * @see #setSessionProperties(HashMap)
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage#getResourceTreeNode_SessionProperties()
	 * @model dataType="org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SessionProperties" transient="true"
	 * @generated
	 */
	HashMap<QualifiedName, Object> getSessionProperties();

	/**
	 * Sets the value of the '{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getSessionProperties <em>Session Properties</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @param value the new value of the '<em>Session Properties</em>' attribute.
	 * @see #getSessionProperties()
	 * @generated
	 */
	void setSessionProperties(HashMap<QualifiedName, Object> value);

	/**
	 * Returns the value of the '<em><b>Path</b></em>' attribute. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Path</em>' attribute isn't clear, there really
	 * should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Path</em>' attribute.
	 * @see #setPath(String)
	 * @see org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage#getResourceTreeNode_Path()
	 * @model transient="true"
	 * @generated
	 */
	String getPath();

	/**
	 * Sets the value of the '{@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode#getPath <em>Path</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Path</em>' attribute.
	 * @see #getPath()
	 * @generated
	 */
	void setPath(String value);

} // ResourceTreeNode
