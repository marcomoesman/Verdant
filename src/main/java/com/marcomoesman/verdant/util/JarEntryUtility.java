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
package com.marcomoesman.verdant.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarEntryUtility {

	private JarFile jarFile;

	public JarEntryUtility(JarFile jarFile) {
		this.jarFile = jarFile;
	}

	public List<String> getAllEntriesFromJar() {
		List<String> mass = new ArrayList<>();
		Enumeration<JarEntry> entries = this.jarFile.entries();
		while (entries.hasMoreElements()) {
			JarEntry e = entries.nextElement();
			if (!e.isDirectory()) {
				mass.add(e.getName());
			}
		}
		return mass;
	}

	public List<String> getEntriesWithoutInnerClasses() {
		List<String> mass = new ArrayList<>();
		Enumeration<JarEntry> entries = this.jarFile.entries();
		Set<String> possibleInnerClasses = new HashSet<String>();
		Set<String> baseClasses = new HashSet<String>();

		while (entries.hasMoreElements()) {
			JarEntry e = entries.nextElement();
			if (!e.isDirectory()) {
				String entryName = e.getName();

				if (entryName != null && entryName.trim().length() > 0) {
					entryName = entryName.trim();

					if (!entryName.endsWith(".class")) {
						mass.add(entryName);

					} else if (entryName.matches(".*[^(/|\\\\)]+\\$[^(/|\\\\)]+$")) {
						possibleInnerClasses.add(entryName);

					} else {
						baseClasses.add(entryName);
						mass.add(entryName);
					}
				}
			}
		}

		for (String inner : possibleInnerClasses) {
			String innerWithoutTail = inner.replaceAll("\\$[^(/|\\\\)]+\\.class$", "");
			if (!baseClasses.contains(innerWithoutTail + ".class")) {
				mass.add(inner);
			}
		}
		return mass;
	}

	public JarFile getJarFile() {
		return this.jarFile;
	}
}