package fr.umlv.conc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class Linked2<E> {
	// Classe non mutal, aucun problème de Thread
	private static class Entry<E> {
		private final E element;
		private final Entry<E> next;

		private Entry(E element, Entry<E> next) {
			this.element = element;
			this.next = next;
		}
	}

	private volatile Entry head;
	
	private final static VarHandle HANDLE;
	
	static {
		var lookup = MethodHandles.lookup();
		try {
			HANDLE = lookup.findVarHandle(Linked2.class, "head", Entry.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}

	public void addFirst(E element) {
		Objects.requireNonNull(element);
		while(true) {
			var head = this.head;
			var entry = new Entry(element, head);
			if(HANDLE.compareAndSet(this, head, entry)) {
				return;
			}
		}
	}

	public int size() {
		var size = 0;
		for(var link = head; link != null; link = link.next) { // pas obligé de lire en RAM
			size ++;
		}
		return size;
	}
	
	public static void main(String[] args) throws InterruptedException {
		var nbThreads = 4;
		var threads = new Thread[nbThreads];
		var linkedList = new Linked2<Integer>();
		
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
 * 
 **/