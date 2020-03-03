package fr.umlv.conc;

public class Bogus {
	private boolean stop;
	private final Object lock = new Object();

	public void runCounter() {
		var localCounter = 0;
		for(;;) {                 // Attente Active ! Utilisation de charge CPU pour rien 
			synchronized (lock) { // force la lecture en RAM
				if (stop) {
					break;
				}
				localCounter++;			
			}
		}
		System.out.println(localCounter);
	}

	public void stop() {
		synchronized (lock) { // force l'écriture en RAM (plus long)
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
 * 3 - Done : 49489900
 * 4 - La programmation lock-free : 
 *     En déclarant la variable avec le mot clé "volatile", la variable sera immédiatement dans la mémoire du Thread main. 
 *     De plus, toutes les lectures de la variable seront lues directement à partir de la mémoire du Thread main.
 *     
 *     AtomiqueIntecture lecture en RAM en une seul instruction. Instruction atomique du processeur.
 *     Volatile ne me met cette varaible dans un registre (Lecture et ecriture en ram donc plus long) Garanti moins forte que synchronized // varibale locale dans les registres
 *     varHandle : AtomicInteger a besoin d'un objet, ce qui coute chère, on utilise donc varHandle
 *     compareAndSet : 
 **/