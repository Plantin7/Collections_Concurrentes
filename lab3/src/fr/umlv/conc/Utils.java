package fr.umlv.conc;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.file.Path;


public class Utils {
	private static Path HOME;
	
	private Utils() {
		HOME = Path.of(System.getenv("HOMEPATH"));
	}
	
	public Path getHome() {
		return HOME;
	}

	private static class LazyHolder  {
		private static final Utils INSTANCE = new Utils();
	}

	public static Utils getInstance() {
		return LazyHolder.INSTANCE;
	}

	public static void main(String[] args) throws InterruptedException {
		var runnable = new Runnable() {
			@Override
			public void run() {
				for(var i = 0; i < 1_000_000; i++) {
					System.out.println(Utils.getInstance().getHome());
				}
			}
		};
		var t1 = new Thread(runnable);
		var t2 = new Thread(runnable);
		t1.start();
		t2.start();
		t1.join();
		t2.join();
	}
}
















//
//public class Utils {
//	private static Path HOME;
//	private static final Object lock = new Object();
//
//	private static final VarHandle PATH_HANDLE;
//
//	static {
//		var lookup = MethodHandles.lookup();
//
//		try {
//			PATH_HANDLE = lookup.findStaticVarHandle(Utils.class, "HOME", Path.class);
//		} catch (NoSuchFieldException | IllegalAccessException e) {
//			throw new AssertionError(e);
//		}
//	}
//
//	public static Path getHome() {
//		var home = (Path) PATH_HANDLE.getAcquire();
//		if (home == null) {
//			synchronized(lock) {
//				home = (Path) PATH_HANDLE.getAcquire();
//				if (home == null) {
//					PATH_HANDLE.setRelease(Path.of(System.getenv("HOMEPATH")));  // TODO ??
//				}
//			}
//		}
//		return (Path) PATH_HANDLE.getAcquire();
//	}
//	
//	public static void main(String[] args) throws InterruptedException {
//		var runnable = new Runnable() {
//			@Override
//			public void run() {
//				for(var i = 0; i < 1_000_000; i++) {
//					System.out.println(Utils.getHome());
//				}
//			}
//		};
//		var t1 = new Thread(runnable);
//		var t2 = new Thread(runnable);
//		t1.start();
//		t2.start();
//		t1.join();
//		t2.join();
//	}
//}

//public class Utils {
//	private static volatile Path HOME;
//	private static final Object lock = new Object();
//
//	public static Path getHome() {
//		if (HOME == null) {
//			synchronized(lock) { // deadlock possible
//				if (HOME == null) {
//					HOME = Path.of(System.getenv("HOME")); // pas fini dêtre initialisé, objet path pas fini dêtre initialisé car réorganisé par le JIT
//				}
//			}
//		}
//		return HOME; // les champs ne cet objet ne sont pas forcement initialisé
//	}
//}
// 1er thread -> pas eu le temp d'initialisé le champ path

//public class Utils {
//	private static Path HOME;
//	private static final Object lock = new Object();
//
//	public static Path getHome() {
//		synchronized (lock) {
//			if (HOME == null) {
//				// peut être sechéculer ici, on peut faire 2 appels a getEnv (avant d'avoir mis le synchronized)
//				return HOME = Path.of(System.getenv("HOME"));
//			}
//			return HOME;	
//		}
//	}
//}
