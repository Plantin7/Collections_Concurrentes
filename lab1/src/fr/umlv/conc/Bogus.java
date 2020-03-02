package fr.umlv.conc;

public class Bogus {
	private boolean stop;
	private final Object lock = new Object();

	public void runCounter() {
		var localCounter = 0;
		for(;;) {                 // Attente Active ! Utilisation de charge CPU pour rien 
			synchronized (lock) {
				if (stop) {
					break;
				}
				localCounter++;			
			}
		}
		System.out.println(localCounter);
	}

	public void stop() {
		synchronized (lock) {
			stop = true;
		}
	}

	public static void main(String[] args) throws InterruptedException {
		var bogus = new Bogus();
		var thread = new Thread(bogus::runCounter);
		thread.start();
		Thread.sleep(100);
		bogus.stop();
		thread.join();
	}
}

/**
 *
 * 1 - Il y a une data race entre le Thread cr�e et le Thread main sur la varaible membre "stop". 
 * Cela produit une boucle infini car la JVM optimise le code, car il consid�re la variable stop comme une varaible local, car elle n'est pas utilis�.
 * 2 - Done
 * 3 - Done
 * 4 - La programmation lock-free : 
 *     En d�clarant la variable avec le mot cl� "volatile", la variable sera imm�diatement dans la m�moire du Thread main. 
 *     De plus, toutes les lectures de la variable seront lues directement � partir de la m�moire du Thread main.
 *
 **/