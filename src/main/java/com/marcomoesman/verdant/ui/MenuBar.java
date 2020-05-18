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

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.alee.laf.menu.WebMenu;
import com.alee.laf.menu.WebMenuBar;
import com.alee.laf.menu.WebMenuItem;
import com.marcomoesman.verdant.Verdant;
import com.marcomoesman.verdant.ui.util.LinkMouseAdapter;

public class MenuBar extends WebMenuBar {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -2040807932169540455L;
	
	private final UserInterface userInterface;

	public MenuBar(UserInterface userInterface) {
		this.userInterface = userInterface;
		
		final WebMenu fileMenu = new WebMenu("File");
		this.createFileMenu(fileMenu);
		this.add(fileMenu);
		
		final WebMenu helpMenu = new WebMenu("Help");
		this.createHelpMenu(helpMenu);
		this.add(helpMenu);
	}

	private void createHelpMenu(WebMenu helpMenu) {
		JMenuItem menuItem = new WebMenuItem("Report issue");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().browse(new URL("https://github.com/marcomoesman/Verdant/issues").toURI());
				} catch (IOException | URISyntaxException ex) {
					ex.printStackTrace();
				}
			}
		});
		helpMenu.add(menuItem);
		helpMenu.addSeparator();
		
		menuItem = new WebMenuItem("License");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().browse(new URL("https://github.com/marcomoesman/Verdant/blob/master/LICENSE").toURI());
				} catch (IOException | URISyntaxException ex) {
					ex.printStackTrace();
				}
			}
		});
		helpMenu.add(menuItem);
		
		menuItem = new WebMenuItem("About");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPanel aboutPanel = new JPanel();
				aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.PAGE_AXIS));
				JLabel title = new JLabel("Verdant " + Verdant.getVersion());
				title.setFont(new Font("Tahoma", Font.PLAIN, 18));
				aboutPanel.add(title);
				aboutPanel.add(new JLabel("Created by Marco Moesman"));
				String project = "https://github.com/marcomoesman/Verdant";
				JLabel link = new JLabel("<html><font color=\"#000099\"><u>" + project + "</u></font></html>");
				link.setCursor(new Cursor(Cursor.HAND_CURSOR));
				link.addMouseListener(new LinkMouseAdapter(project));
				aboutPanel.add(link);
				aboutPanel.add(new JLabel(" "));
				aboutPanel.add(new JLabel("<html><b>Powered by:</b></html>"));
				String fernflowerUrl = "https://github.com/fesh0r/fernflower";
				link = new JLabel("<html><font color=\"#000099\"><u>" + fernflowerUrl + "</u></font></html>");
				link.setCursor(new Cursor(Cursor.HAND_CURSOR));
				link.addMouseListener(new LinkMouseAdapter(fernflowerUrl));
				aboutPanel.add(link);
				aboutPanel.add(new JLabel("(c) 2020 JetBrains"));
				aboutPanel.add(new JLabel(" "));
				String rsyntaxUrl = "https://github.com/bobbylight/RSyntaxTextArea";
				link = new JLabel("<html><font color=\"#000099\"><u>" + rsyntaxUrl + "</u></font></html>");
				link.setCursor(new Cursor(Cursor.HAND_CURSOR));
				link.addMouseListener(new LinkMouseAdapter(rsyntaxUrl));
				aboutPanel.add(link);
				aboutPanel.add(new JLabel("(c) 2020 Robert Futrell"));
				aboutPanel.add(new JLabel(" "));
				String weblafUrl = "https://github.com/mgarin/weblaf";
				link = new JLabel("<html><font color=\"#000099\"><u>" + weblafUrl + "</u></font></html>");
				link.setCursor(new Cursor(Cursor.HAND_CURSOR));
				link.addMouseListener(new LinkMouseAdapter(rsyntaxUrl));
				aboutPanel.add(link);
				aboutPanel.add(new JLabel("(c) 2020 mgarin"));
				aboutPanel.add(new JLabel(" "));
				JOptionPane.showMessageDialog(null, aboutPanel);
			}
		});
		helpMenu.add(menuItem);
	}

	private void createFileMenu(WebMenu fileMenu) {
		JMenuItem menuItem = new WebMenuItem("Open File");
		menuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileChooserInterface fileChooser = FileChooserInterface.create("*.jar", "*.zip", "*.class");
				File file = fileChooser.show(userInterface);
				if (file == null) {
					return;
				}
				
				System.out.println("Opening file: " + file.getName());
				userInterface.openFile(file);
			}
		});
		fileMenu.add(menuItem);
		
		menuItem = new WebMenuItem("Close File");
		menuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				userInterface.closeFile();
			}
		});
		fileMenu.add(menuItem);
		fileMenu.addSeparator();
	}
	
}
