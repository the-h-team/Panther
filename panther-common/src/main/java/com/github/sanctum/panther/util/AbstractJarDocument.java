package com.github.sanctum.panther.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractJarDocument implements Iterator<String> {

	File file = null;
	Scanner scanner = null;

	protected AbstractJarDocument(@NotNull InputStream stream) {
		initialize(stream);
	}

	public AbstractJarDocument(@NotNull JarFile file, @NotNull String entry) {
		InputStream stream;
		try {
			JarEntry e = file.getJarEntry(entry);
			if (e != null) {
				stream = file.getInputStream(e);
			} else throw new IOException();
		} catch (IOException e) {
			throw new IllegalStateException("Jar entry " + entry + " not found.");
		}
		if (stream != null) initialize(stream);
	}

	void initialize(@NotNull InputStream stream) {
		try {
			File file = File.createTempFile(UUID.nameUUIDFromBytes(stream.toString().getBytes(StandardCharsets.UTF_8)).toString(), ".pif");
			file.deleteOnExit();
			OutputStream out = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int read;
			while ((read = stream.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			out.close();
			this.file = file;
			this.scanner = new Scanner(file);
		} catch (IOException e) {
			throw new IllegalStateException("Unable to read file", e);
		}
	}

	@Override
	public void remove() {
		scanner.remove();
	}

	@Override
	public void forEachRemaining(Consumer<? super String> action) {
		scanner.forEachRemaining(action);
	}

	@Override
	public boolean hasNext() {
		return scanner.hasNext();
	}

	@Override
	public String next() {
		return scanner.next();
	}

	public @NotNull File toFile() {
		return file;
	}
}
