/**
 *  OpenKM, Open Document Management System (http://www.openkm.com)
 *  Copyright (c) 2006-2015  Paco Avila & Josep Llort
 *
 *  No bytes were intentionally harmed during the development of this application.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.openkm.frontend.client.widget.massive;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.openkm.frontend.client.Main;
import com.openkm.frontend.client.bean.GWTFolder;
import com.openkm.frontend.client.service.OKMFolderService;
import com.openkm.frontend.client.service.OKMFolderServiceAsync;
import com.openkm.frontend.client.service.OKMRepositoryService;
import com.openkm.frontend.client.service.OKMRepositoryServiceAsync;
import com.openkm.frontend.client.util.Util;

/**
 * Folder tree
 * 
 * @author jllort
 *
 */
public class FolderSelectTree extends Composite {
	private Tree tree;
	private TreeItem actualItem;
	private final OKMFolderServiceAsync folderService = (OKMFolderServiceAsync) GWT.create(OKMFolderService.class);
	private final OKMRepositoryServiceAsync repositoryService = (OKMRepositoryServiceAsync) GWT.create(OKMRepositoryService.class);
	TreeItem rootItem = new TreeItem(Util.imageItemHTML("img/menuitem_childs.gif", "root_schema", "top"));
	
	/**
	 * Folder Tree
	 */
	public FolderSelectTree() {
		tree = new Tree();
		rootItem.setStyleName("okm-TreeItem");
		rootItem.setUserObject(new GWTFolder());
		rootItem.setSelected(true);
		rootItem.setState(true);
		tree.setStyleName("okm-Tree");
		tree.addItem(rootItem);
		tree.addSelectionHandler(new SelectionHandler<TreeItem>() {
			@Override
			public void onSelection(SelectionEvent<TreeItem> event) {
				boolean refresh = true;
				TreeItem item = event.getSelectedItem();
				
				// Enables or disables move button ( evalues security to move to folder with permissions )
				if (rootItem.equals(item)) {
					Main.get().categoriesPopup.enable(false);
				} else {
					Main.get().categoriesPopup.enable(true);
				}
				
				// Case that not refreshing tree and file browser ( right click )
				if (actualItem.equals(item)) {
					refresh = false;
				} else {
					// Disables actual item because on changing active node by
					// application this it's not changed automatically
					if (!actualItem.equals(item)) {
						actualItem.setSelected(false);
						actualItem = item;
					} else {
						refresh = false;
					}
				}
				
				if (refresh) {
					refresh(true);
				}
			}
		});
		actualItem = tree.getItem(0);
		initWidget(tree);
	}
	
	/**
	 * Resets all tree values
	 */
	public void reset() {
		actualItem = rootItem;
		actualItem.setSelected(true);
		while (actualItem.getChildCount()>0) {
			actualItem.getChild(0).remove();
		}
		getCategories();
	}
	
	/**
	 * Refresh asyncronous subtree branch
	 */
	final AsyncCallback<List<GWTFolder>> callbackGetChilds = new AsyncCallback<List<GWTFolder>>() {
		public void onSuccess(List<GWTFolder> result) {
			boolean directAdd = true;

			// If has no childs directly add values is permited
			if (actualItem.getChildCount() > 0) {
				directAdd = false;
				// to prevent remote folder remove it disables all tree branch 
				// items and after sequentially activate
				hideAllBranch(actualItem);
			}
			
			// On refreshing not refreshed the actual item values but must 
			// ensure that has childs value is consistent
			if (result.isEmpty()) {
				((GWTFolder) actualItem.getUserObject()).setHasChildren(false);
			} else {
				((GWTFolder) actualItem.getUserObject()).setHasChildren(true);
			}
			
			// Ads folders childs if exists
			for (Iterator<GWTFolder> it = result.iterator(); it.hasNext();) {
				GWTFolder folder = it.next();
				TreeItem folderItem = new TreeItem(folder.getName());
				folderItem.setUserObject(folder);
				folderItem.setStyleName("okm-TreeItem");
				
				// If has no childs directly add values is permited, else 
				// evalues each node to refresh, remove or add
				if (directAdd) {
					evaluesFolderIcon(folderItem);
					actualItem.addItem(folderItem);
				} else {
					// sequentially activate items and refreshes values
					addFolder(actualItem,folderItem);
				}
			}
			
			actualItem.setState(true);
			evaluesFolderIcon(actualItem);
		}

		public void onFailure(Throwable caught) {
			Main.get().showError("GetChilds", caught);
		}
	};
	
	/**
	 * Gets asyncronous root node
	 */
	final AsyncCallback<GWTFolder> callbackGetCategoriesFolder = new AsyncCallback<GWTFolder>() {
		public void onSuccess(GWTFolder result) {
			// Only executes on initalization and the actualItem is root 
			// element on initialization
			//We put the id on root
			actualItem.setUserObject(result);
			evaluesFolderIcon(actualItem);			
			actualItem.setState(true);
			actualItem.setSelected(true);
			
			getChilds(result.getPath());
		}

		public void onFailure(Throwable caught) {
			Main.get().showError("GetCategoriesFolder", caught);
		}
	};
	
	/**
	 * Refresh the folders on a item node
	 * 
	 * @param path The folder path selected to list items
	 */
	public void getChilds(String path) {
		folderService.getChilds(path, false, null, callbackGetChilds);
	}	
	
	/**
	 * Gets the root
	 */
	public void getCategories() {	
		repositoryService.getCategoriesFolder(callbackGetCategoriesFolder);
	}
	
	/**
	 * Refresh the tree node
	 */
	public void refresh(boolean reset) {
		String path = ((GWTFolder) actualItem.getUserObject()).getPath();
		getChilds(path);
	}
	
	/**
	 * Hides all items on a brach
	 * 
	 * @param actualItem The actual item active
	 */
	public void hideAllBranch(TreeItem actualItem) {
		int i = 0;
		int count = actualItem.getChildCount();
		
		for (i=0; i<count; i++) {
			actualItem.getChild(i).setVisible(false);
		}
	}
	
	/**
	 * Adds folders to actual item if not exists or refreshes it values
	 * 
	 * @param actualItem The actual item active
	 * @param newItem New item to be added, or refreshed
	 */
	public void addFolder(TreeItem actualItem , TreeItem newItem) {
		int i = 0;
		boolean found = false;
		int count = actualItem.getChildCount();
		GWTFolder folder;
		GWTFolder newFolder = (GWTFolder) newItem.getUserObject();
		String folderPath = newFolder.getPath(); 
		
		for (i=0; i<count; i++) {
			folder = (GWTFolder) actualItem.getChild(i).getUserObject();
			// If item is found actualizate values
			if ((folder).getPath().equals(folderPath)) {
				found = true;
				actualItem.getChild(i).setVisible(true);
				actualItem.getChild(i).setUserObject(newFolder);
				evaluesFolderIcon(actualItem.getChild(i));
			}
		}
		
		if (!found) {
			evaluesFolderIcon(newItem);
			actualItem.addItem(newItem);
		}
	}
	
	/**
	 * Gets the actual Uuid of the selected directory tree
	 * 
	 * @return The actual path of selected directory
	 */
	public GWTFolder getCategory() {
		return ((GWTFolder) actualItem.getUserObject());
	}
	
	/**
	 * Evalues actual folder icon to prevent other user interaction with the same folder
	 * this ensures icon and object hasChildsValue are consistent
	 */
	public void evaluesFolderIcon(TreeItem item) {
		GWTFolder folderItem = (GWTFolder) item.getUserObject();
		preventFolderInconsitences(item);
		
		// Looks if must change icon on parent if now has no childs and properties with user security atention
		if (folderItem.isHasChildren()) {
			item.setHTML(Util.imageItemHTML("img/menuitem_childs.gif", folderItem.getName(), "top"));
		} else {
			item.setHTML(Util.imageItemHTML("img/menuitem_empty.gif", folderItem.getName(), "top"));
		}
	}
	
	/**
	 * Prevents folder incosistences between server ( multi user deletes folder ) and tree
	 * nodes drawed
	 * 
	 * @param item The tree node
	 */
	public void preventFolderInconsitences(TreeItem item) {
		GWTFolder folderItem = (GWTFolder) item.getUserObject();
		
		// Case that must remove all items node
		if (item.getChildCount() > 0 && !folderItem.isHasChildren()) {
			while (item.getChildCount() > 0) {
				item.getChild(0).remove();
			}
		}
		if (item.getChildCount()< 1 && !folderItem.isHasChildren()) {
			folderItem.setHasChildren(false);
		}
	}	
}