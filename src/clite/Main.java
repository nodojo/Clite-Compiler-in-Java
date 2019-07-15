/*
 * The driver code for the project
 */

package clite;

// Jamie Leviner
// I didn't get finished with Semantics so newton.cpp
// throws an error.

/*
 * EMPLOYER DISCLAIMER:
 *     This project is still incomplete.  Out of a class with
 *     7 students, only one student completed more than me.  
 *     This is one of the most difficult assignments given to 
 *     the students in our curriculum.  The instructor means 
 *     for it to be a gauntlet, a challenge for his students.
 *     It is rare that any students complete the assignment. 
 */

public class Main {

	public static void main(String[] args) {
		// String filename = "programs/fib.cpp"; // TT
		String filename = "programs/functions.cpp";
		// String filename = "programs/gcd.cpp"; // TT
		// String filename = "programs/hanoi.cpp"; // TT
		// String filename = "programs/newton.cpp";
		// String filename = "programs/recFib.cpp";// TT

		Lexer lexer = new Lexer(filename);
		Token tok = lexer.next();
		while (tok != Token.eofTok) {
			System.out.println(tok.toString());
			tok = lexer.next();
		}

		System.out.println("Begin parsing... " + filename);
		Parser parser = new Parser(new Lexer(filename));
		Program prog = parser.program();
		prog.display();

		System.out.println("\nBegin type checking..." + filename + "\n");
		//System.out.print("\nGlobals = ");
		TypeMap map = StaticTypeCheck.typing(prog.globals);
		//map.display();
		StaticTypeCheck.V(prog);
		System.out.println("No type errors\n");

		System.out.println("Transformed Abstract Syntax Tree");
		Program out = TypeTransformer.T(prog, map);
		out.display();

		System.out.println("\nBegin interpreting..." + filename);
		Semantics semantics = new Semantics();
		State state = semantics.M(out);
		 System.out.println("\nProject incomplete");
		 System.out.println("\nFinal State");
		// state.display( );
	}
}
