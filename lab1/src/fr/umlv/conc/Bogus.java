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
 * 1 - Il y a une data race entre le Thread crée et le Thread main sur la varaible membre "stop". 
 * Cela produit une boucle infini car la JVM optimise le code, car il considère la variable stop comme une varaible local, car elle n'est pas utilisé.
 * 2 - Done
 * 3 - Done
 * 4 - La programmation lock-free : 
 *     En déclarant la variable avec le mot clé "volatile", la variable sera immédiatement dans la mémoire du Thread main. 
 *     De plus, toutes les lectures de la variable seront lues directement à partir de la mémoire du Thread main.
 *
 **/