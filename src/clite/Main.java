/*
 * The driver code for the project
 */

package clite;

// Semantics is still unfinished so newton.cpp throws an error.

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
