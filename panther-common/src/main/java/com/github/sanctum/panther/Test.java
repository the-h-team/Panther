package com.github.sanctum.panther;

import com.github.sanctum.panther.container.PantherQueue;
import com.github.sanctum.panther.event.Vent;

@Vent.Link.Key("Main class")
public final class Test implements Vent.Host {

	Test() {
	}

	public static void main(String[] args) throws InterruptedException {

		PantherQueue<String> queue = new PantherQueue<>();
		queue.add("Fart");
		queue.add("Nugget");
		queue.add("Cheese");

		String c = queue.poll();
		System.out.println(c);
		String c2 = queue.poll();
		System.out.println(c2);
		String c3 = queue.poll();
		System.out.println(c3);


	}
}
