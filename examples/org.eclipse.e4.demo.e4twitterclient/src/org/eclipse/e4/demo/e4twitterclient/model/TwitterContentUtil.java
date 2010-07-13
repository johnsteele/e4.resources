/*******************************************************************************
 * Copyright (c) 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eduard Bartsch (SAP AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.e4twitterclient.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.semantic.ISemanticFile;
import org.eclipse.core.resources.semantic.ISemanticFileSystem;
import org.eclipse.core.resources.semantic.ISemanticFolder;
import org.eclipse.core.resources.semantic.ISemanticResource;
import org.eclipse.core.resources.semantic.SyncDirection;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.demo.e4twitterclient.Activator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TwitterContentUtil {

	/** Twitter client project nature */
	public static final String PROJECT_NATURE = "org.eclipse.e4.demo.e4twitterclient.projectnature"; //$NON-NLS-1$

	public static final String CONTENT_FOLDER = ".e4demotwittercontent"; //$NON-NLS-1$

	public static final String USER_TIMELINE_EXTENSION = "twitterusertimeline"; //$NON-NLS-1$
	public static final String USER_FOLLOWERS_EXTENSION = "twitterfollowers"; //$NON-NLS-1$
	public static final String USER_FRIENDS_EXTENSION = "twitterfriends"; //$NON-NLS-1$
	public static final String USER_INFO_EXTENSION = "twitteruserinfo"; //$NON-NLS-1$

	public static final String USER_FOLLOWERS = "user." + USER_FOLLOWERS_EXTENSION; //$NON-NLS-1$
	public static final String USER_TIMELINE = "user." + USER_TIMELINE_EXTENSION; //$NON-NLS-1$
	public static final String USER_FRIENDS = "user." + USER_FRIENDS_EXTENSION; //$NON-NLS-1$
	public static final String USER_INFO = "user." + USER_INFO_EXTENSION; //$NON-NLS-1$
	public static final String USER_IMAGE = "user.twitterimage"; //$NON-NLS-1$

	public static final String PROJECT_NAME = "TwitterDemo"; //$NON-NLS-1$

	public static class TwitterContentBase {
		public IPath resource;
	}

	public static class TwitterUser extends TwitterContentBase {
		String name;
		String fullName;
		public ArrayList<TwitterUserAspect> aspects;

		TwitterUser(String name, String fullName, IPath resource, ArrayList<TwitterUserAspect> aspects) {
			this.name = name;
			this.fullName = fullName;
			this.resource = resource;
			this.aspects = aspects;
			for (TwitterUserAspect twitterUserAspect : aspects) {
				twitterUserAspect.user = this;
			}
		}

		public String getName() {
			return this.name;
		}

		public String getFullName() {
			if (this.fullName != null) {
				return this.fullName;
			}
			return "";
		}

		public List<TwitterUserAspect> getAspects() {
			return this.aspects;
		}
	}

	public enum TwitterUserAspectType {
		info, timeline, followers, friends;
	}

	public static class TwitterUserAspect extends TwitterContentBase {
		public TwitterUser user;
		public TwitterUserAspectType type;
		public String label;
	}

	public static UserType getUserInfo(IProject project, String userName) throws CoreException {
		IFolder folder = project.getFolder(CONTENT_FOLDER);

		if (!folder.exists()) {
			return null;
		}

		IFolder userFolder = folder.getFolder(userName);

		if (!userFolder.exists()) {
			return null;
		}

		IFile userInfoFile = userFolder.getFile(USER_INFO);

		if (userInfoFile.exists()) {
			return getUserInfo(userInfoFile);
		}

		return null;
	}

	public static IProject getTwitterDemoProject(IWorkspace workspace) {
		return workspace.getRoot().getProject(PROJECT_NAME);
	}

	public static IProject getOrCreateTwitterDemoProject(final IWorkspace workspace, IProgressMonitor monitor) throws CoreException,
			TeamException {
		IProject project = getTwitterDemoProject(workspace);
		if (!project.exists()) {
			IProjectDescription description = workspace.newProjectDescription(PROJECT_NAME);

			try {
				description.setLocationURI(new URI(ISemanticFileSystem.SCHEME, null, "/" + PROJECT_NAME, null)); //$NON-NLS-1$
				description.setNatureIds(new String[] {PROJECT_NATURE});
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			project.create(description, null);
		}

		if (!project.isOpen()) {
			project.open(monitor);
			RepositoryProvider.map(project, ISemanticFileSystem.SFS_REPOSITORY_PROVIDER);
		}
		return project;
	}

	public static Node openDocument(IFile file) throws CoreException {
		InputStream content = file.getContents();
		try {
			return parseDocument(content);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		} finally {
			if (content != null) {
				try {
					content.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	public static Node parseDocument(InputStream content) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

		Document doc = builder.parse(content);

		Node node = doc.getDocumentElement();

		return node;
	}

	public static UserType getUserInfo(IFile userInfoFile) throws CoreException {
		Node root = openDocument(userInfoFile);

		if (root == null)
			return null;

		UserType userType = ContentHandler.loadUser(root);
		return userType;
	}

	public static List<StatusType> getUserTimeline(IProject project, String userName) throws CoreException {
		IFolder folder = project.getFolder(CONTENT_FOLDER);

		if (!folder.exists()) {
			return null;
		}

		IFolder userFolder = folder.getFolder(userName);

		if (!userFolder.exists()) {
			return null;
		}

		IFile userTimelineFile = userFolder.getFile(USER_TIMELINE);

		if (userTimelineFile.exists()) {
			return getUserTimeline(userTimelineFile);
		}
		return null;
	}

	public static List<StatusType> getUserTimeline(IFile userTimelineFile) throws CoreException {
		Node root = openDocument(userTimelineFile);
		if (root == null)
			return null;

		NodeList children = root.getChildNodes();
		List<StatusType> statuses = new ArrayList<StatusType>();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			String name = child.getNodeName();

			if (name.equals("status")) {
				statuses.add(ContentHandler.loadStatus(child));
			}
		}
		return statuses;
	}

	public static List<UserType> getFollowers(IProject project, String userName) throws CoreException {
		IFolder folder = project.getFolder(CONTENT_FOLDER);

		if (!folder.exists()) {
			return null;
		}

		IFolder userFolder = folder.getFolder(userName);

		if (!userFolder.exists()) {
			return null;
		}

		IFile userFollowersFile = userFolder.getFile(USER_FOLLOWERS);

		if (userFollowersFile.exists()) {
			return getFollowers(userFollowersFile);
		}
		return null;
	}

	public static ArrayList<UserType> getFriends(IProject project, String userName) throws CoreException {
		IFolder folder = project.getFolder(CONTENT_FOLDER);

		if (!folder.exists()) {
			return null;
		}

		IFolder userFolder = folder.getFolder(userName);

		if (!userFolder.exists()) {
			return null;
		}

		IFile userFriendsFile = userFolder.getFile(USER_FRIENDS);

		if (userFriendsFile.exists()) {
			return getFriends(userFriendsFile);
		}
		return null;
	}

	public static List<UserType> getFollowers(IFile userFollowersFile) throws CoreException {
		Node root = openDocument(userFollowersFile);
		if (root == null)
			return null;

		ArrayList<UserType> users = new ArrayList<UserType>();

		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			String name = child.getNodeName();

			if (name.equals("user")) {
				users.add(ContentHandler.loadUser(child));
			}
		}
		return users;
	}

	public static ArrayList<UserType> getFriends(IFile userFriendsFile) throws CoreException {
		Node root = openDocument(userFriendsFile);
		if (root == null)
			return null;

		ArrayList<UserType> users = new ArrayList<UserType>();

		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			String name = child.getNodeName();

			if (name.equals("user")) {
				users.add(ContentHandler.loadUser(child));
			}
		}
		return users;
	}

	public static TwitterUser[] getUsers(IProject project) throws CoreException {
		IFolder folder = project.getFolder(CONTENT_FOLDER);

		if (folder.exists()) {
			IResource[] members = folder.members();
			ArrayList<TwitterUser> users = new ArrayList<TwitterUser>();

			for (int i = 0; i < members.length; i++) {
				ArrayList<TwitterUserAspect> aspects = new ArrayList<TwitterUserAspect>();

				IResource[] content = ((IFolder) members[i]).members();

				for (IResource iResource : content) {
					TwitterUserAspect aspect = new TwitterUserAspect();

					if (iResource.getName().equals(USER_INFO)) {
						aspect.type = TwitterUserAspectType.info;
						aspect.label = "Info"; //$NON-NLS-1$
					} else if (iResource.getName().equals(USER_TIMELINE)) {
						aspect.type = TwitterUserAspectType.timeline;
						aspect.label = "Tweets"; //$NON-NLS-1$
					} else if (iResource.getName().equals(USER_FOLLOWERS)) {
						aspect.type = TwitterUserAspectType.followers;
						aspect.label = "Followers"; //$NON-NLS-1$
					} else if (iResource.getName().equals(USER_FRIENDS)) {
						aspect.type = TwitterUserAspectType.friends;
						aspect.label = "Following"; //$NON-NLS-1$
					} else {
						continue;
					}
					aspect.resource = iResource.getFullPath();
					aspects.add(aspect);
				}

				UserType userInfo = TwitterContentUtil.getUserInfo(project, members[i].getName());
				if (userInfo != null) {
					String fullName = userInfo.getName();
					users.add(new TwitterUser(members[i].getName(), fullName, members[i].getFullPath(), aspects));
				}
			}
			return users.toArray(new TwitterUser[0]);
		}
		return new TwitterUser[0];
	}

	public static TwitterUser getUser(IProject project, String userName) throws CoreException {
		IFolder folder = project.getFolder(CONTENT_FOLDER);

		if (!folder.exists()) {
			return null;
		}

		IFolder userFolder = folder.getFolder(userName);

		if (!userFolder.exists()) {
			return null;
		}

		return getUser(userFolder);
	}

	public static TwitterUser getUser(IFolder userFolder) throws CoreException {
		if (userFolder.exists()) {
			ArrayList<TwitterUserAspect> aspects = new ArrayList<TwitterUserAspect>();

			IResource[] content = userFolder.members();

			for (IResource iResource : content) {
				TwitterUserAspect aspect = new TwitterUserAspect();

				if (iResource.getName().equals(USER_INFO)) {
					aspect.type = TwitterUserAspectType.info;
					aspect.label = "Info"; //$NON-NLS-1$
				} else if (iResource.getName().equals(USER_TIMELINE)) {
					aspect.type = TwitterUserAspectType.timeline;
					aspect.label = "Tweets"; //$NON-NLS-1$
				} else if (iResource.getName().equals(USER_FOLLOWERS)) {
					aspect.type = TwitterUserAspectType.followers;
					aspect.label = "Followers"; //$NON-NLS-1$
				} else if (iResource.getName().equals(USER_FRIENDS)) {
					aspect.type = TwitterUserAspectType.friends;
					aspect.label = "Following"; //$NON-NLS-1$
				} else {
					continue;
				}
				aspect.resource = iResource.getFullPath();
				aspects.add(aspect);
			}

			UserType userInfo = TwitterContentUtil.getUserInfo(userFolder.getProject(), userFolder.getName());
			if (userInfo != null) {
				String fullName = userInfo.getName();
				return new TwitterUser(userFolder.getName(), fullName, userFolder.getFullPath(), aspects);
			}
		}
		return null;
	}

	public static void removeUserContent(IProject project, String userName, IProgressMonitor monitor) throws CoreException {
		IFolder folder = project.getFolder(CONTENT_FOLDER);

		if (!folder.exists()) {
			return;
		}

		IFolder userFolder = folder.getFolder(userName);

		if (!userFolder.exists()) {
			return;
		}
		userFolder.delete(true, null);
	}

	public static void createUserContent(IProject project, String userName, IProgressMonitor monitor) throws CoreException {
		IFolder contentFolder = getOrCreateFolder(project, TwitterContentUtil.CONTENT_FOLDER, monitor);
		IFolder userFolder = getOrCreateFolder(contentFolder, userName, monitor);
		ISemanticFolder sFolder = (ISemanticFolder) userFolder.getAdapter(ISemanticFolder.class);

		ISemanticFile infoFile = createFileContent(sFolder, USER_INFO, "http://api.twitter.com/1/users/show/" + userName + ".xml", monitor); //$NON-NLS-1$ //$NON-NLS-2$
		infoFile.getAdaptedFile().refreshLocal(IResource.DEPTH_INFINITE, monitor);

		UserType userType = getUserInfo(infoFile.getAdaptedFile());
		if (userType.getProfileImageUrl() != null) {
			createFileContent(sFolder, USER_IMAGE, userType.getProfileImageUrl(), monitor);
		}

		createFileContent(sFolder, USER_FOLLOWERS, "http://api.twitter.com/1/statuses/followers/" + userName + ".xml", monitor); //$NON-NLS-1$ //$NON-NLS-2$
		createFileContent(sFolder, USER_FRIENDS, "http://api.twitter.com/1/statuses/friends/" + userName + ".xml", monitor); //$NON-NLS-1$ //$NON-NLS-2$
		createFileContent(sFolder, USER_TIMELINE, "http://api.twitter.com/1/statuses/user_timeline/" + userName + ".xml", monitor); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void refreshUserContent(IProject project, String userName, IProgressMonitor monitor) throws CoreException {
		IFolder contentFolder = getOrCreateFolder(project, TwitterContentUtil.CONTENT_FOLDER, monitor);
		IFolder userFolder = getOrCreateFolder(contentFolder, userName, monitor);
		ISemanticFolder sFolder = (ISemanticFolder) userFolder.getAdapter(ISemanticFolder.class);

		ISemanticFile infoFile = refreshFileContent(sFolder, USER_INFO, "http://api.twitter.com/1/users/show/" + userName + ".xml", monitor); //$NON-NLS-1$ //$NON-NLS-2$
		infoFile.getAdaptedFile().refreshLocal(IResource.DEPTH_INFINITE, monitor);

		UserType userType = getUserInfo(infoFile.getAdaptedFile());
		if (userType.getProfileImageUrl() != null) {
			refreshFileContent(sFolder, USER_IMAGE, userType.getProfileImageUrl(), monitor);
		} else {
			// TODO remove image file
		}

		refreshFileContent(sFolder, USER_FOLLOWERS, "http://api.twitter.com/1/statuses/followers/" + userName + ".xml", monitor); //$NON-NLS-1$ //$NON-NLS-2$
		refreshFileContent(sFolder, USER_FRIENDS, "http://api.twitter.com/1/statuses/friends/" + userName + ".xml", monitor); //$NON-NLS-1$ //$NON-NLS-2$
		refreshFileContent(sFolder, USER_TIMELINE, "http://api.twitter.com/1/statuses/user_timeline/" + userName + ".xml", monitor); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static ISemanticFile createFileContent(ISemanticFolder userFolder, String fileName, String uriString, IProgressMonitor monitor)
			throws CoreException {
		URI uri;
		try {
			if (!userFolder.hasResource(fileName)) {
				uri = new URI(uriString);

				return userFolder.addFile(fileName, uri, ISemanticFileSystem.NONE, monitor);
			}
		} catch (URISyntaxException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}
		return (ISemanticFile) userFolder.getResource(fileName);
	}

	private static ISemanticFile refreshFileContent(ISemanticFolder userFolder, String fileName, String uriString, IProgressMonitor monitor)
			throws CoreException {
		URI uri;
		try {
			if (!userFolder.hasResource(fileName)) {
				uri = new URI(uriString);

				return userFolder.addFile(fileName, uri, ISemanticFileSystem.NONE, monitor);
			}
			ISemanticResource resource = userFolder.getResource(fileName);

			resource.synchronizeContentWithRemote(SyncDirection.INCOMING, ISemanticFileSystem.NONE, monitor);
		} catch (URISyntaxException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}
		return (ISemanticFile) userFolder.getResource(fileName);
	}

	private static IFolder getOrCreateFolder(IContainer root, String folderName, IProgressMonitor monitor) throws CoreException {
		IFolder folder = root.getFolder(new Path(folderName));

		if (!folder.exists()) {
			folder.create(true, true, monitor);
		}
		return folder;
	}

	public static Image getUserImage(IProject project, String userName) {
		IFolder folder = project.getFolder(CONTENT_FOLDER);

		if (!folder.exists()) {
			return null;
		}

		IFolder userFolder = folder.getFolder(userName);

		if (!userFolder.exists()) {
			return null;
		}

		IFile userInfoFile = userFolder.getFile(USER_IMAGE);

		if (userInfoFile.exists()) {
			return getUserImage(userInfoFile);
		}

		return null;
	}

	public static Image getUserImage(final IFile file) {
		ImageDescriptor desc = new ImageDescriptor() {
			@Override
			public ImageData getImageData() {
				InputStream stream = null;
				try {
					stream = file.getContents();
					return new ImageData(stream);
				} catch (CoreException e) {
					e.printStackTrace();
				} finally {
					try {
						if (stream != null) {
							stream.close();
						}
					} catch (IOException ex) {
						// ignore
					}
				}
				return null;
			}
		};
		return desc.createImage();
	}

}
