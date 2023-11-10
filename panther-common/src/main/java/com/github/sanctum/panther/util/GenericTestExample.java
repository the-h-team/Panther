package com.github.sanctum.panther.util;

import com.github.sanctum.panther.Test;
import com.github.sanctum.panther.annotation.Ordinal;
import com.github.sanctum.panther.file.Generic;
import com.github.sanctum.panther.file.Node;
import java.util.Map;
import org.jetbrains.annotations.NotNull;


public class GenericTestExample implements Generic {

	// set a node variable
	Node node;

	public boolean isTest() {
		Object o = node.get();
		if (o instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) o;
			return map.containsKey("com.github.sanctum.Test");
		}
		return false;
	}

	public Test getTest() {
		return node.get(Test.class);
	}

	@Ordinal(20)
	// @Note("It's imperative that you have this in your class!")
	protected void setNode(@NotNull Node node) {
		this.node = node;
	}

}
