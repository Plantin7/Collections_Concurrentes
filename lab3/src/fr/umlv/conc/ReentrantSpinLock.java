package fr.umlv.conc;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class ReentrantSpinLock {
	private volatile int lock;
	private Thread ownerThread; // gain de performance
	
	private static final VarHandle LOCK_HANDLE;
	
	static {
		 var lookup = MethodHandles.lookup();
		 try {
			LOCK_HANDLE = lookup.findVarHandle(ReentrantSpinLock.class, "lock", int.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}

	public void lock() {
		var currentThread = Thread.currentThread();
		
		while(true) {
			if(LOCK_HANDLE.compareAndSet(this, 0, 1)) {
				ownerThread = currentThread; // ecriture non volatile mais ca marche !
				return;
			}
			if(ownerThread == currentThread) {
				lock++;
				return;
			}
			Thread.onSpinWait();
		}
		// id�e de l'algo
		// on r�cup�re la thread courante
		// si lock est == � 0, on utilise un CAS pour le mettre � 1 et
		//   on sauvegarde la thread qui poss�de le lock dans ownerThread.
		// sinon on regarde si la thread courante n'est pas ownerThread,
		//   si oui alors on incr�mente lock.
		//
		// et il faut une boucle pour retenter le CAS apr�s avoir appel� onSpinWait()
	}

	public void unlock() {
		if(ownerThread != Thread.currentThread()) {
			throw new IllegalStateException();
		}
		var lock = this.lock; // Lecture volatile
		if(lock == 1) {
			ownerThread = null; // pas besoin de faire l'ecriture 
			this.lock = 0; // Ecriture volatile, verifie que tout les champs sont ecris en RAM, tu dis au CPU tu me r�organise pas les champs
			return;
		}
		
		this.lock = lock - 1;
		// id�e de l'algo
		// si la thread courante est != ownerThread, on p�te une exception
		// si lock == 1, on remet ownerThread � null
		// on d�cr�mente lock
	}

	public static void main(String[] args) throws InterruptedException {
		var runnable = new Runnable() {
			private int counter;
			private final ReentrantSpinLock spinLock = new ReentrantSpinLock();

			@Override
			public void run() {
				for(var i = 0; i < 1_000_000; i++) {
					spinLock.lock();
					try {
						spinLock.lock();
						try {
							counter++;
						} finally {
							spinLock.unlock();
						}
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
