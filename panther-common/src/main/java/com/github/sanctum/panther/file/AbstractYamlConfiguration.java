package com.github.sanctum.panther.file;

public abstract class AbstractYamlConfiguration extends Configurable {

	@Override
	public final Extension getType() {
		return Type.YAML;
	}
}
