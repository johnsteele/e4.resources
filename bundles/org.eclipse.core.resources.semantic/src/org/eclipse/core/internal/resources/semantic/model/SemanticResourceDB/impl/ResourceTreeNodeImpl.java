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
 * $Id: ResourceTreeNodeImpl.java,v 1.1 2010/02/10 20:56:40 ebartsch Exp $
 */
package org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl;

import java.util.Collection;
import java.util.HashMap;

import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.ResourceTreeNode;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.SemanticResourceDBPackage;
import org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.TreeNodeType;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Resource Tree Node</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>
 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.ResourceTreeNodeImpl#getName
 * <em>Name</em>}</li>
 * <li>
 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.ResourceTreeNodeImpl#getChildren
 * <em>Children</em>}</li>
 * <li>
 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.ResourceTreeNodeImpl#getParent
 * <em>Parent</em>}</li>
 * <li>
 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.ResourceTreeNodeImpl#isExists
 * <em>Exists</em>}</li>
 * <li>
 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.ResourceTreeNodeImpl#getTemplateID
 * <em>Template ID</em>}</li>
 * <li>
 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.ResourceTreeNodeImpl#getPersistentProperties
 * <em>Persistent Properties</em>}</li>
 * <li>
 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.ResourceTreeNodeImpl#isLocalOnly
 * <em>Local Only</em>}</li>
 * <li>
 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.ResourceTreeNodeImpl#getType
 * <em>Type</em>}</li>
 * <li>
 * {@link org.eclipse.core.internal.resources.semantic.model.SemanticResourceDB.impl.ResourceTreeNodeImpl#getSessionProperties
 * <em>Session Properties</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
@SuppressWarnings("unqualified-field-access")
public class ResourceTreeNodeImpl extends EObjectImpl implements ResourceTreeNode {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The cached value of the '{@link #getChildren() <em>Children</em>}'
	 * containment reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getChildren()
	 * @generated
	 * @ordered
	 */
	protected EList<ResourceTreeNode> children;

	/**
	 * The default value of the '{@link #isExists() <em>Exists</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #isExists()
	 * @generated
	 * @ordered
	 */
	protected static final boolean EXISTS_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isExists() <em>Exists</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #isExists()
	 * @generated
	 * @ordered
	 */
	protected boolean exists = EXISTS_EDEFAULT;

	/**
	 * The default value of the '{@link #getTemplateID() <em>Template ID</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getTemplateID()
	 * @generated
	 * @ordered
	 */
	protected static final String TEMPLATE_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTemplateID() <em>Template ID</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getTemplateID()
	 * @generated
	 * @ordered
	 */
	protected String templateID = TEMPLATE_ID_EDEFAULT;

	/**
	 * The cached value of the '{@link #getPersistentProperties()
	 * <em>Persistent Properties</em>}' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @see #getPersistentProperties()
	 * @generated
	 * @ordered
	 */
	protected HashMap<String, String> persistentProperties;

	/**
	 * The default value of the '{@link #isLocalOnly() <em>Local Only</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #isLocalOnly()
	 * @generated
	 * @ordered
	 */
	protected static final boolean LOCAL_ONLY_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isLocalOnly() <em>Local Only</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #isLocalOnly()
	 * @generated
	 * @ordered
	 */
	protected boolean localOnly = LOCAL_ONLY_EDEFAULT;

	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final TreeNodeType TYPE_EDEFAULT = TreeNodeType.FILE;

	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected TreeNodeType type = TYPE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getSessionProperties()
	 * <em>Session Properties</em>}' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @see #getSessionProperties()
	 * @generated
	 * @ordered
	 */
	protected HashMap<QualifiedName, Object> sessionProperties;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ResourceTreeNodeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SemanticResourceDBPackage.Literals.RESOURCE_TREE_NODE;
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
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SemanticResourceDBPackage.RESOURCE_TREE_NODE__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EList<ResourceTreeNode> getChildren() {
		if (children == null) {
			children = new EObjectContainmentWithInverseEList<ResourceTreeNode>(ResourceTreeNode.class, this,
					SemanticResourceDBPackage.RESOURCE_TREE_NODE__CHILDREN, SemanticResourceDBPackage.RESOURCE_TREE_NODE__PARENT);
		}
		return children;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public ResourceTreeNode getParent() {
		if (eContainerFeatureID() != SemanticResourceDBPackage.RESOURCE_TREE_NODE__PARENT)
			return null;
		return (ResourceTreeNode) eContainer();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newParent
	 * @param msgs
	 * @return object
	 * @generated
	 */
	public NotificationChain basicSetParent(ResourceTreeNode newParent, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject) newParent, SemanticResourceDBPackage.RESOURCE_TREE_NODE__PARENT, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setParent(ResourceTreeNode newParent) {
		if (newParent != eInternalContainer()
				|| (eContainerFeatureID() != SemanticResourceDBPackage.RESOURCE_TREE_NODE__PARENT && newParent != null)) {
			if (EcoreUtil.isAncestor(this, newParent))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString()); //$NON-NLS-1$
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newParent != null)
				msgs = ((InternalEObject) newParent).eInverseAdd(this, SemanticResourceDBPackage.RESOURCE_TREE_NODE__CHILDREN,
						ResourceTreeNode.class, msgs);
			msgs = basicSetParent(newParent, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SemanticResourceDBPackage.RESOURCE_TREE_NODE__PARENT, newParent,
					newParent));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public boolean isExists() {
		return exists;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setExists(boolean newExists) {
		boolean oldExists = exists;
		exists = newExists;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SemanticResourceDBPackage.RESOURCE_TREE_NODE__EXISTS, oldExists, exists));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public String getTemplateID() {
		return templateID;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setTemplateID(String newTemplateID) {
		String oldTemplateID = templateID;
		templateID = newTemplateID;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SemanticResourceDBPackage.RESOURCE_TREE_NODE__TEMPLATE_ID, oldTemplateID,
					templateID));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public HashMap<String, String> getPersistentProperties() {
		return persistentProperties;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setPersistentProperties(HashMap<String, String> newPersistentProperties) {
		HashMap<String, String> oldPersistentProperties = persistentProperties;
		persistentProperties = newPersistentProperties;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SemanticResourceDBPackage.RESOURCE_TREE_NODE__PERSISTENT_PROPERTIES,
					oldPersistentProperties, persistentProperties));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public boolean isLocalOnly() {
		return localOnly;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setLocalOnly(boolean newLocalOnly) {
		boolean oldLocalOnly = localOnly;
		localOnly = newLocalOnly;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SemanticResourceDBPackage.RESOURCE_TREE_NODE__LOCAL_ONLY, oldLocalOnly,
					localOnly));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public TreeNodeType getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setType(TreeNodeType newType) {
		TreeNodeType oldType = type;
		type = newType == null ? TYPE_EDEFAULT : newType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SemanticResourceDBPackage.RESOURCE_TREE_NODE__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public HashMap<QualifiedName, Object> getSessionProperties() {
		return sessionProperties;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setSessionProperties(HashMap<QualifiedName, Object> newSessionProperties) {
		HashMap<QualifiedName, Object> oldSessionProperties = sessionProperties;
		sessionProperties = newSessionProperties;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SemanticResourceDBPackage.RESOURCE_TREE_NODE__SESSION_PROPERTIES,
					oldSessionProperties, sessionProperties));
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
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__CHILDREN :
				return ((InternalEList<InternalEObject>) (InternalEList<?>) getChildren()).basicAdd(otherEnd, msgs);
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__PARENT :
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetParent((ResourceTreeNode) otherEnd, msgs);
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
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__CHILDREN :
				return ((InternalEList<?>) getChildren()).basicRemove(otherEnd, msgs);
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__PARENT :
				return basicSetParent(null, msgs);
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
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__PARENT :
				return eInternalContainer().eInverseRemove(this, SemanticResourceDBPackage.RESOURCE_TREE_NODE__CHILDREN,
						ResourceTreeNode.class, msgs);
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
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__NAME :
				return getName();
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__CHILDREN :
				return getChildren();
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__PARENT :
				return getParent();
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__EXISTS :
				return isExists();
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__TEMPLATE_ID :
				return getTemplateID();
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__PERSISTENT_PROPERTIES :
				return getPersistentProperties();
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__LOCAL_ONLY :
				return isLocalOnly();
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__TYPE :
				return getType();
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__SESSION_PROPERTIES :
				return getSessionProperties();
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
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__NAME :
				setName((String) newValue);
				return;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__CHILDREN :
				getChildren().clear();
				getChildren().addAll((Collection<? extends ResourceTreeNode>) newValue);
				return;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__PARENT :
				setParent((ResourceTreeNode) newValue);
				return;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__EXISTS :
				setExists((Boolean) newValue);
				return;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__TEMPLATE_ID :
				setTemplateID((String) newValue);
				return;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__PERSISTENT_PROPERTIES :
				setPersistentProperties((HashMap<String, String>) newValue);
				return;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__LOCAL_ONLY :
				setLocalOnly((Boolean) newValue);
				return;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__TYPE :
				setType((TreeNodeType) newValue);
				return;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__SESSION_PROPERTIES :
				setSessionProperties((HashMap<QualifiedName, Object>) newValue);
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
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__NAME :
				setName(NAME_EDEFAULT);
				return;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__CHILDREN :
				getChildren().clear();
				return;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__PARENT :
				setParent((ResourceTreeNode) null);
				return;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__EXISTS :
				setExists(EXISTS_EDEFAULT);
				return;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__TEMPLATE_ID :
				setTemplateID(TEMPLATE_ID_EDEFAULT);
				return;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__PERSISTENT_PROPERTIES :
				setPersistentProperties((HashMap<String, String>) null);
				return;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__LOCAL_ONLY :
				setLocalOnly(LOCAL_ONLY_EDEFAULT);
				return;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__TYPE :
				setType(TYPE_EDEFAULT);
				return;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__SESSION_PROPERTIES :
				setSessionProperties((HashMap<QualifiedName, Object>) null);
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
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__NAME :
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__CHILDREN :
				return children != null && !children.isEmpty();
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__PARENT :
				return getParent() != null;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__EXISTS :
				return exists != EXISTS_EDEFAULT;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__TEMPLATE_ID :
				return TEMPLATE_ID_EDEFAULT == null ? templateID != null : !TEMPLATE_ID_EDEFAULT.equals(templateID);
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__PERSISTENT_PROPERTIES :
				return persistentProperties != null;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__LOCAL_ONLY :
				return localOnly != LOCAL_ONLY_EDEFAULT;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__TYPE :
				return type != TYPE_EDEFAULT;
			case SemanticResourceDBPackage.RESOURCE_TREE_NODE__SESSION_PROPERTIES :
				return sessionProperties != null;
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
		result.append(" (name: "); //$NON-NLS-1$
		result.append(name);
		result.append(", exists: "); //$NON-NLS-1$
		result.append(exists);
		result.append(", templateID: "); //$NON-NLS-1$
		result.append(templateID);
		result.append(", persistentProperties: "); //$NON-NLS-1$
		result.append(persistentProperties);
		result.append(", localOnly: "); //$NON-NLS-1$
		result.append(localOnly);
		result.append(", type: "); //$NON-NLS-1$
		result.append(type);
		result.append(", sessionProperties: "); //$NON-NLS-1$
		result.append(sessionProperties);
		result.append(')');
		return result.toString();
	}

} // ResourceTreeNodeImpl
