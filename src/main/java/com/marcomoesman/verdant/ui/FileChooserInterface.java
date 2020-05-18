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
import java.util.Arrays;

import javax.swing.filechooser.FileFilter;

import com.alee.laf.filechooser.WebFileChooser;

public class FileChooserInterface {

	private final WebFileChooser fileChooser;
	
	public FileChooserInterface(String... acceptedFileTypes) {
		this.fileChooser = new WebFileChooser();
		{
			Arrays.asList(acceptedFileTypes).forEach(fileType -> {
				this.fileChooser.addChoosableFileFilter(new FileFilter() {
					@Override
					public String getDescription() {
						return fileType;
					}
					
					@Override
					public boolean accept(File file) {
						if (file.isDirectory())
							return true;
						return file.getName().toLowerCase().endsWith(fileType.substring(1));
					}
				});
			});
			this.fileChooser.setFileSelectionMode(WebFileChooser.FILES_ONLY);
			this.fileChooser.setMultiSelectionEnabled(false);
		}
	}
	
	public File show(UserInterface userInterface) {
		int value = this.fileChooser.showOpenDialog(userInterface);
		if (value != WebFileChooser.APPROVE_OPTION)
			return null;
		return this.fileChooser.getSelectedFile();
	}
	
	public static FileChooserInterface create(String... acceptedFileTypes) {
		return new FileChooserInterface(acceptedFileTypes);
	}
	
}
