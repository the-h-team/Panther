package com.github.sanctum.panther;

import com.github.sanctum.panther.event.Vent;
import com.github.sanctum.panther.util.TaskChain;

@Vent.Link.Key("Main class")
public final class Test implements Vent.Host {

	Test() {
	}

	public static void main(String[] args) {

		TaskChain chain = TaskChain.getAsynchronous();

		chain.run(() -> System.out.println("Hello"));


	}
}
