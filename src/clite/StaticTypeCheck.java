/*
 * StaticTypeCheck.java implements the Clite type system for detecting 
 * type errors in Clite programs at compile-time. It is defined by
 * the functions V and the auxiliary functions typing and typeOf.
 * These functions use classes in the abstract syntax of Clite.
 */

package clite;

import java.util.ArrayList;

public class StaticTypeCheck {

	// Boolean toTypeCheck = true;
	static boolean hasReturn;

	// make map of declarations
	public static TypeMap typing(Declarations d) {
		checkDupl(d); // check for duplicate declarations

		TypeMap map = new TypeMap();
		for (Declaration di : d)
			if (di instanceof VariableDecl) {
				VariableDecl vd = (VariableDecl) di;
				map.put(vd.v, vd.t);
			}

		return map;
	}

	// make sure there is only one of each declared variable
	public static void checkDupl(Declarations d) {
		// System.err.println("Checked for duplicate variable names.");

		for (int i = 0; i < d.size() - 1; i++)
			for (int j = i + 1; j < d.size(); j++) {
				Declaration di = d.get(i);
				Declaration dj = d.get(j);
				// System.err.println("Compare " + d.get(i).v.toString() + "::"
				// + d.get(j).v.toString());
				check(!di.v.equals(dj.v), "duplicate declaration: " + dj.v);
			}
	}

	// make sure no duplicate locals / parameters
	public static void checkDupl(Declarations params, Declarations locals) {
		// System.err.println("Checked for duplicate parameter/local names.");

		for (int i = 0; i < params.size(); i++) {
			Declaration par = params.get(i);

			// check for duplicate parameter names
			for (int j = i + 1; j < params.size(); j++) {
				Declaration par2 = params.get(j);
				check(!par.v.equals(par2.v),
						"duplicate parameter declaration: " + par2.v);
			}

			// check for duplicate parameter/local names
			for (int j = 0; j < locals.size(); j++) {
				Declaration loc = locals.get(j);
				check(!par.v.equals(loc.v),
						"duplicate parameter/local declaration: " + loc.v);
			}
		}
	}

	// make sure no duplicate globals / functions
	public static void checkDupl(Declarations g, Functions f) {
		// System.err.println("Checked for duplicate global/function names.");

		for (int i = 0; i < g.size(); i++) {
			Declaration decl = g.get(i);

			for (int j = 0; j < f.size(); j++) {
				Function func = f.get(j);
				check(!decl.v.toString().equals(func.id.toString()),
						"duplicate global and function names: "
								+ func.id.toString());
			}
		}
	}

	public static void check(boolean test, String msg) {
		if (test)
			return;
		System.err.println(msg);
		//System.exit(1);
	}

	// check that there is a main function and
	// validate the types of the functions
	public static void V(Program p) {
		// System.err.println("made it to V(Program)");

		// check that main exists
		boolean hasMain = false;
		for (Function f : p.functions) {
			if (f.id.toString().equals("main"))
				hasMain = true;
		}
		// if (hasMain == false)
		// System.err.println("No main exists.");
		// else
		// System.err.println("Main found.");

		// check for duplicate global and function names
		checkDupl(p.globals, p.functions);

		TypeMap globalMap = typing(p.globals);
		System.out.println("Globals = ");
		globalMap.display();

		V(p.functions, typing(p.globals));
	}

	// validate functions
	// put function's parameters and locals into TypeMap
	public static void V(Functions f, TypeMap t) {
		// System.err.println("made it to V(Functions, TypeMap)");

		for (Function func : f) {
			System.out.print("Function " + func.id.toString() + " = \n");
			TypeMap varMap = new TypeMap();

			for (int i = 0; i < f.size(); i++) {
				varMap.put(f.get(i).id, f.get(i).t);
			}

			varMap.putAll(t);
			// make sure there are locals before putting in map
			if (func.locals != null) {
				if (func.locals.size() > 0) {
					varMap.putAll(typing(func.locals));
				}
			}

			// make sure there are parameters before putting in map
			if (func.params != null) {
				if (func.params.size() > 0) {
					varMap.putAll(typing(func.params));
				}
			}

			checkDupl(func.params, func.locals);

			V(func, f, varMap);
			varMap.display(f);
			// System.err.println("members.size() =  " +
			// func.body.members.size());
		}

	}

	public static void V(Function func, Functions functions, TypeMap varMap) {
		hasReturn = false;
		// System.err.println("\nChecking function: " + func.id);
		// System.err.println("members.size() =  " + func.body.members.size());

		for (int i = 0; i < func.body.members.size(); i++) {
			Statement s = (Statement) func.body.members.get(i);
			// System.err.println("members[" + i + "] = "
			// + func.body.members.get(i).toString().substring(6));

			if (s instanceof Return) {
				// System.err.println("Got into s instanceof Return.");
				check(!hasReturn, func.id + " has multiple return statements.");
				V((Return) s, functions, varMap);
				hasReturn = true;
			} else if (s instanceof Call) {
				// System.err.println("Got into s instanceof Call.");
				check(!hasReturn, "Return must be last expression in "
						+ func.id);
				V((Statement) ((Call) s), functions, varMap);
			} else {
				check(!hasReturn, "Return must be last expression in "
						+ func.id);
				V(s, functions, varMap);
			}
		} // end for loop

		if (!func.t.id.equals("void") && !func.id.toString().equals("main")) {
			check(hasReturn, "Non-void function " + func.id
					+ " doesn't have return statement.");
		} else if (func.t.equals(Type.VOID)) {
			check(!hasReturn, "Void function " + func.id
					+ " has return statement.");
		}
	}

	public static Type typeOf(Expression e, Functions f, TypeMap tm) {
		if (e instanceof Value)
			return ((Value) e).type;
		if (e instanceof Variable) {
			Variable v = (Variable) e;
			check(tm.containsKey(v), "undefined variable: " + v);
			return (Type) tm.get(v);
		}
		if (e instanceof Binary) {
			Binary b = (Binary) e;
			if (b.op.ArithmeticOp())
				if (typeOf(b.term1, f, tm) == Type.FLOAT)
					return (Type.FLOAT);
				else
					return (Type.INT);
			if (b.op.RelationalOp() || b.op.BooleanOp())
				return (Type.BOOL);
		}
		if (e instanceof Unary) {
			Unary u = (Unary) e;
			if (u.op.NotOp())
				return (Type.BOOL);
			else if (u.op.NegateOp())
				return typeOf(u.term, f, tm);
			else if (u.op.intOp())
				return (Type.INT);
			else if (u.op.floatOp())
				return (Type.FLOAT);
			else if (u.op.charOp())
				return (Type.CHAR);
		}
		if (e instanceof Call) {
			// System.err.println("typeOf:e instanceof Call.");
			Call c = (Call) e;
			Function func = null;

			for (int i = 0; i < f.size(); i++) {
				if (f.get(i).id.equals(c.v)) {
					func = f.get(i);
					// System.err.println(f.get(i).id);
				}
				// System.err.println(f.get(i).id);
			}

			// for (int i = 0; i < f.size(); i++) {
			// if (!f.get(i).id.equals(c.v)) {
			// System.err.println("Function: " + f.get(i).id +" : "+ "Call: " +
			// c.v.id);
			// }
			// }

			tm.put(func.id, func.t);
			return func.t;
		}

		throw new IllegalArgumentException("should never reach here");
	}

	public static void V(Expression e, Functions f, TypeMap tm) {
		if (e instanceof Value)
			return;
		if (e instanceof Variable) {
			Variable v = (Variable) e;
			check(tm.containsKey(v), "undeclared variable: " + v);
			return;
		}
		if (e instanceof Binary) {
			Binary b = (Binary) e;
			Type typ1 = typeOf(b.term1, f, tm);
			Type typ2 = typeOf(b.term2, f, tm);
			V(b.term1, f, tm);
			V(b.term2, f, tm);
			if (b.op.ArithmeticOp())
				check(typ1 == typ2 && (typ1 == Type.INT || typ1 == Type.FLOAT),
						"type error for " + b.op);
			else if (b.op.RelationalOp())
				check(typ1 == typ2, "type error for " + b.op);
			else if (b.op.BooleanOp())
				check(typ1 == Type.BOOL && typ2 == Type.BOOL, b.op
						+ ": non-bool operand");
			else
				throw new IllegalArgumentException("should never reach here");
			return;
		}
		if (e instanceof Unary) {
			Unary u = (Unary) e;
			Type typ1 = typeOf(u.term, f, tm);
			V(u.term, f, tm);
			if (u.op.equals(Operator.NOT))
				check(typ1 == Type.BOOL, "! has non-bool operand");
			else if (u.op.equals(Operator.NEG))
				check(typ1 == Type.INT || typ1 == Type.FLOAT,
						"Unary - has non-int/float operand");
			else if (u.op.equals(Operator.FLOAT))
				check(typ1 == Type.INT, "float() has non-int operand");
			else if (u.op.equals(Operator.CHAR))
				check(typ1 == Type.INT, "char() has non-int operand");
			else if (u.op.equals(Operator.INT))
				check(typ1 == Type.FLOAT || typ1 == Type.CHAR,
						"int() has non-float/char operand");
			else
				throw new IllegalArgumentException("should never reach here");
			return;
		}
		if (e instanceof Call) {
			V((Call) e, f, tm);
			Call c = (Call) e;

			//System.err.println("Call assigned to variable: " + c.v.id);

			tm.put(c.v, typeOf(c, f, tm));
			return;
		}

		throw new IllegalArgumentException("should never reach here");
	}

	public static void V(Statement s, Functions f, TypeMap tm) {
		if (s == null)
			throw new IllegalArgumentException("AST error: null statement");
		if (s instanceof Skip)
			return;
		if (s instanceof Assignment) {
			Assignment a = (Assignment) s;
			check(tm.containsKey(a.target), " undefined target in assignment: "
					+ a.target);
			V(a.source, f, tm);
			Type ttype = (Type) tm.get(a.target);
			Type srctype = typeOf(a.source, f, tm);
			if (ttype != srctype) {
				if (ttype == Type.FLOAT)
					check(srctype == Type.INT, "mixed mode assignment to "
							+ a.target);
				else if (ttype == Type.INT)
					check(srctype == Type.CHAR, "mixed mode assignment to "
							+ a.target);
				else
					check(false, "mixed mode assignment to " + a.target);
			}
			return;
		}
		if (s instanceof Conditional) {
			Conditional c = (Conditional) s;
			V(c.test, f, tm);
			check(typeOf(c.test, f, tm) == Type.BOOL,
					"non-bool test in conditional");
			V(c.thenbranch, f, tm);
			V(c.elsebranch, f, tm);
			return;
		}
		if (s instanceof Loop) {
			Loop l = (Loop) s;
			V(l.test, f, tm);
			check(typeOf(l.test, f, tm) == Type.BOOL, "loop has non-bool test");
			V(l.body, f, tm);
			return;
		}
		if (s instanceof Block) {
			Block b = (Block) s;
			for (int j = 0; j < b.members.size(); j++)
				V((Statement) (b.members.get(j)), f, tm);
			return;
		}
		// Don't do anything here, just bounce it back up
		if (s instanceof Call) {
			//System.err.println("V(Statement)if (e instanceof Call)");
			return;
		}
		if (s instanceof Return) {
			// System.err
			// .println("V(Statement)if (e instanceof Return)");
			Return r = (Return) s;
			Function func = null;
			for (int a = 0; a < f.size(); a++) {
				if (f.get(a).id.equals(r.target)) {
					func = f.get(a);
				}
			}

			Type t = typeOf(r.result, f, tm);
			tm.put(func.id, func.t);
			check(t == func.t, "Function " + func.id + "returning wrong type.");
			hasReturn = true;
			return;
		}

		throw new IllegalArgumentException("should never reach here");
	}

	public static void V(Return r, Functions f, TypeMap functionMap) {
		V(r.result, f, functionMap);
	}

	// make sure function call has same number of parameters as function
	public static void V(Call s, Functions f, TypeMap functionMap) {
		Function func = null;

		for (int i = 0; i < f.size(); i++) {
			if (s.v.id.equals(f.get(i).id.toString())) {
				func = f.get(i);
			}
		}

		// puts function call into TypeMap of function
		functionMap.put(func.id, func.t);

		ArrayList<Declaration> funParams = new ArrayList<Declaration>();
		funParams.addAll(func.params);
		ArrayList<Expression> callArgs = new ArrayList<Expression>();
		callArgs.addAll(s.arguments);

		int flag = 0;
		for (int i = 0; i < funParams.size(); i++) {
			Declaration d = funParams.get(i);

			check(i < callArgs.size(),
					"Less parameters in call than in function: " + func.id);

			Expression e = callArgs.get(i);
			Type type = typeOf(e, f, functionMap);
			check(d.t.equals(type), "Wrong type in function call: " + func.id);

			flag++;
		}

		check(!(flag < callArgs.size()),
				"Less parameters in function than in call: " + func.id);
	}

} // class StaticTypeCheck
