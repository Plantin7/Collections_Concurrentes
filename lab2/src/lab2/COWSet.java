package lab2;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import javax.print.attribute.HashAttributeSet;

public class COWSet<E> {
	private final E[][] hashArray;

	private static final Object[] EMPTY = new Object[0];
	private static final VarHandle HASH_HANDLE;
	static {
		var lookup = MethodHandles.lookup(); 

		//try {
		// HASH_HANDLE = lookup.findVarHandle(COWSet.class, "hashArray", Object[][].class);
		HASH_HANDLE = MethodHandles.arrayElementVarHandle(Object[][].class);
		//		} catch (NoSuchFieldException | IllegalAccessException e) {
		//			throw new AssertionError(e);
		//		}
	}

	@SuppressWarnings("unchecked")
	public COWSet(int capacity) {
		var array = new Object[capacity][];
		Arrays.setAll(array, __ -> EMPTY);
		this.hashArray = (E[][])array;
	}

//		public boolean add(E element) {
//			Objects.requireNonNull(element);
//			var index = element.hashCode() % hashArray.length;
//			for (var e : hashArray[index]) {
//				if (element.equals(e)) {
//					return false;
//				}
//			}
//			var oldArray = hashArray[index];
//			var newArray = Arrays.copyOf(oldArray, oldArray.length + 1);
//			newArray[oldArray.length] = element;
//			hashArray[index] = newArray;
//			return true;
//		}

	public boolean add(E element) {
		Objects.requireNonNull(element);
		var index = element.hashCode() % hashArray.length;

		while (true) {
			var oldArray = (E[]) HASH_HANDLE.getVolatile(hashArray, index);
			for (var e : oldArray) {
				if (element.equals(e)) {
					return false;
				}
			}
			var newArray = Arrays.copyOf(oldArray, oldArray.length + 1);
			newArray[oldArray.length] = element;
			if(HASH_HANDLE.compareAndSet(hashArray, index, oldArray, newArray)) {
				return true;
			}
		}
	}
	public void forEach(Consumer<? super E> consumer) {
		for(var index = 0; index < hashArray.length; index++) {
			// var oldArray = hashArray[index];
			var oldArray = (E[]) HASH_HANDLE.getVolatile(hashArray, index);
			for(var element: oldArray) {
				consumer.accept(element);
			}
		}
	}

	public static void main(String[] args) throws InterruptedException {
		var nbThreads = 2;
		var threads = new Thread[nbThreads];

		var set = new COWSet<Integer>(8);

		IntStream.range(0,nbThreads).forEach(j ->{
			Runnable runnable = () -> {
				for(var i = 0 ; i < 200_000 ; i++) {
					set.add(i);
				}
			};
			threads[j] = new Thread(runnable);
			threads[j].start();
		});

		for (Thread thread : threads) {
			thread.join();
		}

		var tmpSet = new COWSet<Integer>(8); // Used to check if there's duplicate value in set 
		set.forEach(x -> {
		    if (!tmpSet.add(x)) {
		    	System.out.println("[Error] - la valeur suivante est déjà présente : " + x);
		    }
		});
		
		System.out.println("Done");
	}
}

/**
 * 1 - Ce code n'est pas Thread Safe car il y a une data race sur le champ 'hashArray'. Si deux threads tente d'ajouter un element dans 
 *     la hashmap, il se peut qu'il y ait des doublons dans notre hasmap et qu'il n'est pas toute les valeurs, ce qu'on veut eviter
 * 
 * 
 */
