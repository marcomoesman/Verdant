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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.alee.api.data.BoxOrientation;
import com.alee.api.data.CompassDirection;
import com.alee.extended.behavior.ComponentResizeBehavior;
import com.alee.extended.canvas.WebCanvas;
import com.alee.extended.memorybar.WebMemoryBar;
import com.alee.extended.overlay.AlignedOverlay;
import com.alee.extended.overlay.WebOverlay;
import com.alee.extended.statusbar.WebStatusBar;
import com.alee.laf.label.WebLabel;
import com.alee.managers.notification.NotificationManager;
import com.alee.managers.style.StyleId;

public class UserInterface extends JFrame {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -5877177035290618927L;
	
	private final MenuBar menuBar;
	private final WebLabel loadedProjectLabel;
	private final ProjectWindow projectWindow;
	
	public UserInterface() {
		super("Verdant Decompiler");
		
		this.menuBar = new MenuBar(this);
		this.setJMenuBar(this.menuBar);
		
		this.setIconImage(new ImageIcon(
				Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/icon.png"))).getImage());
		this.setPreferredSize(new Dimension(1200, 680));
		
		this.loadedProjectLabel = new WebLabel("No project loaded");
		this.initializeStatus();
		
		this.projectWindow = new ProjectWindow(this);
		this.getContentPane().add(this.projectWindow);
		
		this.setDefaultCloseOperation(2);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				UserInterface.this.setVisible(false);
				UserInterface.this.dispose();
				UserInterface.this.shutdown();
			}
		});
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	protected void shutdown() {		
		try {
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					Runtime.getRuntime().halt(0);
				}
			}, 10000L);
			// Close current file
			UserInterface.this.closeFile();
			// Say goodbyte
			System.out.println("Goodbye.");
			// Shutdown program
			System.exit(0);
		} catch (Throwable ignored) {
			Runtime.getRuntime().halt(0);
		}
	}

	private void initializeStatus() {
		final WebStatusBar statusBar = new WebStatusBar();
		
		statusBar.addSpacing(5);
		statusBar.add(this.loadedProjectLabel);
		statusBar.addSpacingToEnd(10);

		final WebOverlay memoryBarOverlay = new WebOverlay();

		memoryBarOverlay.setContent(new WebMemoryBar().setPreferredWidth(150));

		final WebCanvas resizeCorner = new WebCanvas(StyleId.canvasGripperSE);
		new ComponentResizeBehavior(resizeCorner, CompassDirection.southEast).install();

		memoryBarOverlay.addOverlay(new AlignedOverlay(resizeCorner, BoxOrientation.right, BoxOrientation.bottom,
				new Insets(0, 0, -1, -1)));

		statusBar.addToEnd(memoryBarOverlay);

		add(statusBar, BorderLayout.SOUTH);

		NotificationManager.setMargin(0, 0, statusBar.getPreferredSize().height, 0);
	}
	
	public void setProjectTitle(String title) {
		if (title == null) {
			this.setTitle("Verdant Decompiler");
			this.loadedProjectLabel.setText("No project loaded");
			return;
		}
		
		this.setTitle("Verdant Decompiler | " + title);
		this.loadedProjectLabel.setText(title);
	}

	public void openFile(File file) {
		// Send file to ProjectWindow
		this.projectWindow.openFile(file);
		// Update project title
		this.setProjectTitle(file.getName());
	}

	public void closeFile() {
		// Send close to ProjectWindow
		this.projectWindow.closeFile();
		// Reset project title
		this.setProjectTitle(null);
	}
	
}
