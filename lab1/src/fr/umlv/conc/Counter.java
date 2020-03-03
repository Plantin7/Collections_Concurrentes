package fr.umlv.conc;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Counter {
	// private volatile int counter;
	private final AtomicInteger counter = new AtomicInteger(); 

	public int nextInt() {
		while(true) {
			var localCounter = counter.get();
			if(counter.compareAndSet(localCounter, localCounter + 1)) {
				return localCounter;				
			}
		}
		// return counter++; // Ce n'est pas une opération atomique
	}

	public static void main(String[] args) throws InterruptedException {
		var nbThreads = 4;
		var threads = new Thread[nbThreads];
		var counter = new Counter();
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
* 1 - Ce code n'est pas Thread safe.
* 2 - En mettant le mot clef "volatile", cela ne corrige pas les problèmes de concurrences. 
*     En effet il y a plusieurs instructions assembleur pour le champ counter ce qui veut dire que le schéduleur peut déschéduler un Thread avant l'affectation de la nouvelle valeur.
*     Volatile ici est inutile.
* 
*
**/