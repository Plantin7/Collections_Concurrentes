package fr.umlv.conc;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class Linked<E> {
	// Classe non mutal, aucun problème de Thread
	private static class Entry<E> {
		private final E element;
		private final Entry<E> next;

		private Entry(E element, Entry<E> next) {
			this.element = element;
			this.next = next;
		}
	}

	private final AtomicReference<Entry<E>> head = new AtomicReference<>();

	public void addFirst(E element) {
		Objects.requireNonNull(element);
		while(true) {
			var currentHead = head.get();			
			if(head.compareAndSet(currentHead, new Entry<>(element, currentHead))) {
				return;
			}
		}
	}

	public int size() {
		var size = 0;
		for(var link = head.get(); link != null; link = link.next) { // pas obligé de lire en RAM
			size ++;
		}
		return size;
	}
	
	public static void main(String[] args) throws InterruptedException {
		var nbThreads = 4;
		var threads = new Thread[nbThreads];
		var linkedList = new Linked<Integer>();
		
		IntStream.range(0, nbThreads).forEach(j -> {
			Runnable runnable = () -> {
				for(var i = 0 ; i < 100_000 ; i++) {
					linkedList.addFirst(i);
				}
			};
			
			threads[j] = new Thread(runnable);
			threads[j].start();
		});
		for (Thread thread : threads) {
			thread.join();
		}
		
		System.out.println(linkedList.size());
		
	}
}

/**
 * 1 - Deux raisons :
 *     Le code n'est pas Thread safe car la lecture en RAM n'est obligatoire pour la méthode size(), le HEAD n'est pas mis à jour!
 *     La méthode addFirst() est pas thread safe, si plusieurs Thread appelent addfirst(), on aura des pertes de maillons.
 * 
 * 3 - L'Atomic reference n'est pas super efficace  
 * 
 **/