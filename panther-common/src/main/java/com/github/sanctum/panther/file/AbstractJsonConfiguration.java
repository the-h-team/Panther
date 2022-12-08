package com.github.sanctum.panther.file;

public abstract class AbstractJsonConfiguration extends Configurable {

	@Override
	public final Extension getType() {
		return Type.JSON;
	}
}
