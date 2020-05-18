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
package com.marcomoesman.verdant.fernflower;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;

public class FernflowerBridge extends IFernflowerLogger implements IBytecodeProvider, IResultSaver {

	private final Map<String, FFDecompiledClass> decompiledClasses;
	
	public FernflowerBridge() {
		this.decompiledClasses = new HashMap<String, FFDecompiledClass>();
	}
	
	@Override
	public void closeArchive(String arg0, String arg1) {}

	@Override
	public void copyEntry(String arg0, String arg1, String arg2, String arg3) {}

	@Override
	public void copyFile(String arg0, String arg1, String arg2) {}

	@Override
	public void createArchive(String arg0, String arg1, Manifest arg2) {}

	@Override
	public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
		System.out.println("Decompile: " + entryName);
		this.decompiledClasses.put(entryName, new FFDecompiledClass(entryName, content));
	}

	@Override
	public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {}

	@Override
	public void saveDirEntry(String arg0, String arg1, String arg2) {
	}

	@Override
	public void saveFolder(String arg0) {
	}

	@Override
	public byte[] getBytecode(String externalPath, String internalPath) throws IOException {
		File file = new File(externalPath);
		if (internalPath == null) {
			return getBytes(file);
		} else {
			try (ZipFile archive = new ZipFile(file)) {
				ZipEntry entry = archive.getEntry(internalPath);
				if (entry == null)
					throw new IOException("Entry not found: " + internalPath);
				return getBytes(archive, entry);
			}
		}
	}

	public byte[] getBytes(ZipFile archive, ZipEntry entry) throws IOException {
		try (InputStream stream = archive.getInputStream(entry)) {
			return readBytes(stream, (int) entry.getSize());
		}
	}

	public byte[] getBytes(File file) throws IOException {
		try (FileInputStream stream = new FileInputStream(file)) {
			return readBytes(stream, (int) file.length());
		}
	}

	public byte[] readBytes(InputStream stream, int length) throws IOException {
		byte[] bytes = new byte[length];

		int n = 0, off = 0;
		while (n < length) {
			int count = stream.read(bytes, off + n, length - n);
			if (count < 0) {
				throw new IOException("premature end of stream");
			}
			n += count;
		}

		return bytes;
	}

	@Override
	public void writeMessage(String string, Severity severity) {
		System.out.println(severity.prefix + string);
	}

	@Override
	public void writeMessage(String string, Severity severity, Throwable throwable) {
		System.out.println(severity.prefix + string);
		throwable.printStackTrace();
	}
	
	public FFDecompiledClass getDecompiledClass(String entryName) {
		return this.decompiledClasses.get(entryName);
	}

	public void cleanup() {
		this.decompiledClasses.clear();
	}

}
