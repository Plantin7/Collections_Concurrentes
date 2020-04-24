package fr.umlv.structconc;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorOperators.Associative;
import jdk.incubator.vector.VectorSpecies;

public class Vectorized {
	public static int sumLoop(int[] array) {
		var sum = 0;
		for(var value: array) {
			sum += value;
		}
		return sum;
	}

	private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED; // va demander la taille d'un vector, nombre de ints du processeur

	public static int sumReduceLanes(int[] array) {
		var sum = 0;

		var i = 0;
		var limit = array.length - (array.length % SPECIES.length());  // main loop
		for (; i < limit; i += SPECIES.length()) { // 8 int par 8 int (dépend du processeur c'est un exemple)
			var vec = IntVector.fromArray(SPECIES, array, i); // créer un vecteur depuis notre array
			var res = vec.reduceLanes(VectorOperators.ADD); // somme de tout les élément partielr, operation ATOMIQUE, une seul operation processeur
			sum += res;

		}
		for (; i < array.length; i++) {                             // post loop
			sum += array[i];
		}

		return sum;
	}

	public static int sumLanewise (int[] array) {
		var res = IntVector.zero(SPECIES);

		var i = 0;
		var limit = array.length - (array.length % SPECIES.length());  // main loop
		for (; i < limit; i += SPECIES.length()) { 
			var vec = IntVector.fromArray(SPECIES, array, i); 
			res = res.add(vec);

		}
		var sum = res.reduceLanes(VectorOperators.ADD);
		
		for (; i < array.length; i++) {                             // post loop
			sum += array[i];
		}

		return sum;

	}
	
	public static int differenceLanewise (int[] array) {
		if (array.length == 0) {
			return 0;
		}

		var res = IntVector.zero(SPECIES);

		var i = 0;
		var limit = array.length - (array.length % SPECIES.length());  // main loop
		
		for (; i < limit; i += SPECIES.length()) { 
			var vec = IntVector.fromArray(SPECIES, array, i); 
			res = res.sub(vec);
		}
		var sum = res.reduceLanes(VectorOperators.ADD);
		
		for (; i < array.length; i++) {                             // post loop
			sum -= array[i];
		}
		return sum;
	}
	
	public static int[] minmax (int[] array) {
		var vecMax = IntVector.broadcast(SPECIES, Integer.MIN_VALUE);
		var vecMin = IntVector.broadcast(SPECIES, Integer.MAX_VALUE);

		var i = 0;
		var limit = array.length - (array.length % SPECIES.length());  // main loop
		
		for (; i < limit; i += SPECIES.length()) { 
			var vec = IntVector.fromArray(SPECIES, array, i); 
			vecMax = vecMax.max(vec);
			vecMin = vecMin.min(vec);
		}
		
		var max = vecMax.reduceLanes(VectorOperators.MAX);
		var min = vecMin.reduceLanes(VectorOperators.MIN);
		
		for (; i < array.length; i++) {                             // post loop
			if(array[i] > max) {
				max = array[i];
			}
			if(array[i] < min) {
				min = array[i];
			}
		}
		
		int[] minmax = {min, max};
		return minmax;
	}
	public static void main(String[] args) {
		int[] array = {1, 2, 58, 4, 5, 6, 7, 8, 9, 10, 20, 30, -3, 18, 10, 2, 72, 28};
		System.out.println(minmax(array));
	}
}
