package fr.umlv.conc;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class SpinLock {
	private volatile boolean lock;
	private static final VarHandle LOCK_HANDLE;
	
	static {
		var lookup = MethodHandles.lookup();
		try {
			LOCK_HANDLE = lookup.findVarHandle(SpinLock.class, "lock", boolean.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}

	public void lock() {
		while(!LOCK_HANDLE.compareAndSet(this, false, true)) { // Attente active
			Thread.onSpinWait(); // Instruction assembleur qui appelle pause 
		}
		lock = true;
	}

	public void unlock() {
		lock = false;
	}
	
	public boolean tryLock() {
		return LOCK_HANDLE.compareAndSet(this, false, true);
	}

	public static void main(String[] args) throws InterruptedException {
		var runnable = new Runnable() {
			private int counter;
			private final SpinLock spinLock = new SpinLock();

			@Override
			public void run() {
				for(int i = 0; i < 1_000_000; i++) {
					spinLock.lock();
					try {
						counter++;
					} finally {
						spinLock.unlock();
					}
				}
			}
		};
		var t1 = new Thread(runnable);
		var t2 = new Thread(runnable);
		t1.start();
		t2.start();
		t1.join();
		t2.join();
		System.out.println("counter " + runnable.counter);
	}
}

// Reantrant lock, plusieurs Thread peut rappeler la meme fonction, qui peut être bloqué plusieurs fois par le meme Thread
//
// 2 sort de verrou 
// Pas Thread Safe, atomicInteger trop lent par rapport au varHandle
// pipeLine des CPU, au moment ou j'execute l'instruction de decode la prochaine instruction


// Question 4 : 
// onspinwait -> cette thread la n'est pas schéduler par l'os