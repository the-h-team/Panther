package com.github.sanctum.panther.file.handler;

/**
 * An exception thrown when no parent file is found via {@link java.io.InputStream} within a {@link com.github.sanctum.panther.file.Configurable.Editor} write operation.
 */
public class NonExistentParentException extends RuntimeException {
	public NonExistentParentException(String s) {
		super(s);
	}
}
