package fr.umlv.conc;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Counter2 {
	// private volatile int counter;
	private final AtomicInteger counter = new AtomicInteger(); 

	public int nextInt() {
		return counter.getAndIncrement();
		// return counter++; // Ce n'est pas une opération atomique
	}

	public static void main(String[] args) throws InterruptedException {
		var nbThreads = 4;
		var threads = new Thread[nbThreads];
		var counter = new Counter2();
		IntStream.range(0, nbThreads).forEach(j -> {
			Runnable runnable = () -> {
				for(var i = 0; i < 100_000; i++) {
					counter.nextInt();
				}
			};

			threads[j] = new Thread(runnable);
			threads[j].start();
		});
		for (Thread thread : threads) {
			thread.join();
		}
		System.out.println(counter.counter);
	}
}

/**
 *
 * La methode getAndIncrement (qui est atomic) permet de recupérer et d'incrémenter notre counter en une seul instruction assembleur, comme sur les processeur intel.
 * Aucun Thread peut etre déscheduler durant l'appel de cette methode.
 * 
 * 5 - Il n'y aura pas de blocages de Thread. Les deux sont lock free car elle n'utilisent pas de lock.
 * 
 * 
 **/
