/*
 * Copyright (c) 2020 Marco Moesman
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
*/
package com.marcomoesman.verdant.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jetbrains.java.decompiler.main.Fernflower;

import com.alee.laf.panel.WebPanel;
import com.alee.laf.splitpane.WebSplitPane;
import com.alee.laf.tabbedpane.WebTabbedPane;
import com.alee.laf.tree.WebTree;
import com.alee.managers.notification.NotificationIcon;
import com.alee.managers.notification.NotificationManager;
import com.marcomoesman.verdant.fernflower.FernflowerBridge;
import com.marcomoesman.verdant.ui.util.ProjectTreeCellRenderer;
import com.marcomoesman.verdant.ui.util.TreeNodeUserObject;
import com.marcomoesman.verdant.ui.util.TreeObjectListener;
import com.marcomoesman.verdant.util.JarEntryUtility;

public class ProjectWindow extends WebSplitPane {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 6031514830871949070L;

	private final UserInterface userInterface;
	private final WebTree<MutableTreeNode> fileTree; 
	private final WebTabbedPane codeTabbedPane;
	
	private File loadedFile = null;
	private FernflowerBridge fernflowerBridge = null;
	
	public ProjectWindow(UserInterface userInterface) {
		this.userInterface = userInterface;
		
		this.fileTree = new WebTree<MutableTreeNode>();
		this.fileTree.setModel(new DefaultTreeModel(null));
		this.fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.fileTree.setCellRenderer(new ProjectTreeCellRenderer());
		this.fileTree.addMouseListener(new TreeObjectListener(this));
		
		WebPanel projectPanel = new WebPanel();
		projectPanel.setLayout(new BoxLayout(projectPanel, 1));
		projectPanel.setBorder(BorderFactory.createTitledBorder("Project"));
		projectPanel.add(new JScrollPane(this.fileTree));
		
		this.codeTabbedPane = new WebTabbedPane();
		this.codeTabbedPane.setTabLayoutPolicy(WebTabbedPane.SCROLL_TAB_LAYOUT);
		
		WebPanel codePanel = new WebPanel();
		codePanel.setLayout(new BoxLayout(codePanel, 1));
		codePanel.setBorder(BorderFactory.createTitledBorder("Code"));
		codePanel.add(this.codeTabbedPane);
		
		this.setOrientation(WebSplitPane.HORIZONTAL_SPLIT);
		this.setDividerLocation(320); // TODO better way of doing this
		this.setLeftComponent(projectPanel);
		this.setRightComponent(codePanel);
	}

	public void openFile(File file) {
		if (this.loadedFile != null)
			this.closeFile();
		
		this.loadedFile = file;
		this.fernflowerBridge = new FernflowerBridge();
		Fernflower fernflower = new Fernflower(fernflowerBridge, fernflowerBridge, new HashMap<String, Object>(), fernflowerBridge);
		fernflower.addSource(this.loadedFile);
		fernflower.decompileContext();
		this.updateFileTree(file);
	}

	public void closeFile() {
		this.fernflowerBridge.cleanup();
		this.fernflowerBridge = null;
		this.fileTree.setModel(new DefaultTreeModel(null));
		this.loadedFile = null;
		System.gc();
	}

	private void updateFileTree(File file) {
		try {
			if (file.getName().toLowerCase().endsWith(".zip") || file.getName().toLowerCase().endsWith(".jar")) {
				JarFile jar = new JarFile(file);
				JarEntryUtility utility = new JarEntryUtility(jar);
				List<String> jarEntries = utility.getEntriesWithoutInnerClasses();
				
				System.out.println("Found " + jarEntries.size() + " entries");
				this.buildDirectoryTree(jarEntries);
			} else {
				TreeNodeUserObject topNodeUserObject = new TreeNodeUserObject(getName(file.getName()));
				final DefaultMutableTreeNode top = new DefaultMutableTreeNode(topNodeUserObject);
				this.fileTree.setModel(new DefaultTreeModel(top));

				// open it automatically
				new Thread() {
					public void run() {
						TreePath path = new TreePath(top.getPath());
						openEntry(path);
					};
				}.start();
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			NotificationManager.showInnerNotification("Decompiling complete!");
		}
	}
	
	private void buildDirectoryTree(List<String> jarEntries) {
		TreeNodeUserObject topNodeUserObject = new TreeNodeUserObject(getName(this.loadedFile.getName()));
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(topNodeUserObject);
		List<String> sort = new ArrayList<String>();
		Collections.sort(jarEntries, String.CASE_INSENSITIVE_ORDER);
		for (String m : jarEntries)
			if (m.contains("META-INF") && !sort.contains(m))
				sort.add(m);
		Set<String> set = new HashSet<String>();
		for (String m : jarEntries) {
			if (m.contains("/")) {
				set.add(m.substring(0, m.lastIndexOf("/") + 1));
			}
		}
		List<String> packs = Arrays.asList(set.toArray(new String[] {}));
		Collections.sort(packs, String.CASE_INSENSITIVE_ORDER);
		Collections.sort(packs, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o2.split("/").length - o1.split("/").length;
			}
		});
		for (String pack : packs)
			for (String m : jarEntries)
				if (!m.contains("META-INF") && m.contains(pack) && !m.replace(pack, "").contains("/"))
					sort.add(m);
		for (String m : jarEntries)
			if (!m.contains("META-INF") && !m.contains("/") && !sort.contains(m))
				sort.add(m);
		for (String pack : sort) {
			LinkedList<String> list = new LinkedList<String>(Arrays.asList(pack.split("/")));
			this.loadNodesByNames(top, list);
		}
		this.fileTree.setModel(new DefaultTreeModel(top));
	}
	
	public DefaultMutableTreeNode loadNodesByNames(DefaultMutableTreeNode node, List<String> originalNames) {
		List<TreeNodeUserObject> args = new ArrayList<>();
		for (String originalName : originalNames) {
			args.add(new TreeNodeUserObject(originalName));
		}
		return this.loadNodesByUserObject(node, args);
	}
	
	public DefaultMutableTreeNode loadNodesByUserObject(DefaultMutableTreeNode node, List<TreeNodeUserObject> args) {
		if (args.size() > 0) {
			TreeNodeUserObject name = args.remove(0);
			DefaultMutableTreeNode nod = getChild(node, name);
			if (nod == null)
				nod = new DefaultMutableTreeNode(name);
			node.add(loadNodesByUserObject(nod, args));
		}
		return node;
	}
	
	public DefaultMutableTreeNode getChild(DefaultMutableTreeNode node, TreeNodeUserObject name) {
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> entry = node.children();
		while (entry.hasMoreElements()) {
			DefaultMutableTreeNode nods = entry.nextElement();
			if (((TreeNodeUserObject) nods.getUserObject()).getRealName().equals(name.getRealName())) {
				return nods;
			}
		}
		return null;
	}
	
	public void openEntry(TreePath treePath) {
		String name = "";
		String path = "";
		try {
			if (treePath.getPathCount() > 1) {
				for (int i = 1; i < treePath.getPathCount(); i++) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getPathComponent(i);
					TreeNodeUserObject userObject = (TreeNodeUserObject) node.getUserObject();
					if (i == treePath.getPathCount() - 1) {
						name = userObject.getRealName();
					} else {
						path = path + userObject.getRealName() + "/";
					}
				}
				path = path + name;

				if (this.loadedFile.getName().toLowerCase().endsWith(".jar") || this.loadedFile.getName().toLowerCase().endsWith(".zip")) {
					JarFile jarFile = new JarFile(this.loadedFile);
					JarEntry entry = jarFile.getJarEntry(path);
					if (entry == null) {
						System.out.println("Could not find: " + path);
						throw new FileNotFoundException();
					}
					String entryName = entry.getName();
					if (entryName.endsWith(".class")) {
						entryName = entryName.substring(0, entryName.length() - 5) + "java";
						NotificationManager.showInnerNotification("Opening " + name);
						System.out.println("Find: " + entryName);
						String code = this.fernflowerBridge.getDecompiledClass(entryName).getContent();
						RSyntaxTextArea textArea = new RSyntaxTextArea(25, 70);
						textArea.setCaretPosition(0);
						textArea.requestFocusInWindow();
						textArea.setMarkOccurrences(true);
						textArea.setClearWhitespaceLinesEnabled(false);
						textArea.setEditable(false);
						textArea.setAntiAliasingEnabled(true);
						textArea.setCodeFoldingEnabled(true);
						textArea.setSyntaxEditingStyle(RSyntaxTextArea.SYNTAX_STYLE_JAVA);
						textArea.setText(code);
						this.codeTabbedPane.addTab(name.substring(0, name.length() - 5) + "java", new RTextScrollPane(textArea));
						this.codeTabbedPane.setSelectedIndex(this.codeTabbedPane.getTabCount() - 1);
						// TODO move this mess
					} else {
						NotificationManager.showInnerNotification("Opening " + name);
						try (InputStream in = jarFile.getInputStream(entry);) {
							// TODO
						}
					}
				}
			} else {
				name = this.loadedFile.getName();
				path = this.loadedFile.getPath().replaceAll("\\\\", "/");
				if (name.endsWith(".class")) {
					NotificationManager.showInnerNotification("Decompiling " + name);
					// TODO
				} else {
					NotificationManager.showInnerNotification("Opening " + name);
					try (InputStream in = new FileInputStream(this.loadedFile);) {
						// TODO
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			NotificationManager.showInnerNotification("An error occurred", NotificationIcon.error.getIcon());
		}
	}
	
	private String getName(String path) {
		if (path == null)
			return "";
		int i = path.lastIndexOf("/");
		if (i == -1)
			i = path.lastIndexOf("\\");
		if (i != -1)
			return path.substring(i + 1);
		return path;
	}

	public WebTree<MutableTreeNode> getFileTree() {
		return this.fileTree;
	}
}
