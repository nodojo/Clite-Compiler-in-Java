/*
 * TypeMap.java implements the type map for storing
 * the variables and array references declared in the program.
 */

package clite;

import java.util.*;

public class TypeMap extends HashMap<VariableRef, Type> {

	public void display () {
		System.out.print("  { ");
		String sep = "";
		for (VariableRef key : keySet() ) {
			System.out.print(sep + "<" + key + ", " + get(key).getId() + ">");
			sep = ", ";
		}
		System.out.println(" }\n");
	}
	
	public void display (Functions f) {
		String sep = "  ";
		String sep2 = "";

		for (VariableRef key : keySet() ) {
			if (f != null) {
				Function func = null;

				for (int a = 0; a < f.size(); a++) {
					if (f.get(a).id.equals(key)) {
						func = f.get(a);
					}
				}

				if (func != null) {
					System.out.print(sep + "<" + key +"(");

					if (func.params != null) {
						for (int a = 0; a < func.params.size(); a++) {
							System.out.print(sep2 + "<" + func.params.get(a).v + ", " + func.params.get(a).t.id + ">");
							sep2 = ", ";
						}
					}

					System.out.print("), " + get(key).getId() + ">");
				} else {
					System.out.print(sep + "<" + key + ", " + get(key).getId() + ">");
				}
			} else {
				System.out.print(sep + "<" + key + ", " + get(key).getId() + ">");
			}
			
			sep = ",\n  ";
			sep2 = "";
		}

		System.out.println("\n}\n");
	}
}
