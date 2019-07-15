/*
 * Semantics.java implements the semantic interpreter for Clite. It is defined
 * by the functions M that use the classes in the Abstract Syntax of Clite.
 */

package clite;

import java.util.ArrayList;

public class Semantics {

	State M(Program p) {
		// Functions f = p.functions;
		int mainIndex = 0;
		int counter = 0; // flag
		int functionCount = p.functions.size();
		// System.err.print("Number of functions in program: "
		// + Integer.toString(functionCount) + "\n");

		// find main by index in the functions ArrayList
		for (int i = 0; i < p.functions.size(); i++) {
			if (p.functions.get(i).id.id.equals("main")) {
				mainIndex = counter;
				// System.err.print("Main is number: " +
				// Integer.toString(counter + 1));
				// System.err.print("\nMain's index in the ArrayList: " +
				// Integer.toString(counter) + "\n");
			} else
				counter++;
		}

		// use mainIndex as index to retrieve main from p.functions
		return M(p.functions.get(mainIndex).body, p.functions,
				initialState(p.globals));
	}

	State initialState(Declarations d) {
		State state = new State();

		// make sure globals made it in correctly
		// for (int i = 0; i < d.size(); i++)
		// System.err.print("Globals(initialState): <" + d.get(i).v.id + ", "
		// + d.get(i).t.id + ">\n");

		// get started by putting globals in
		for (int i = 0; i < d.size(); i++) {
			state.put(d.get(i).v, Value.mkValue(d.get(i).t));
		}
		if (d.size() > 0)
			state.display(d);
		else
			System.out.print("\nThis program has no existing globals.\n");
			
		return state;
	}

	State M(Statement s, Functions f, State state) {
		if (s instanceof Skip)
			return state;
		// return M((Skip) s, state);
		if (s instanceof Assignment)
			return M((Assignment) s, f, state);
		if (s instanceof Conditional)
			return M((Conditional) s, f, state);
		if (s instanceof Loop)
			return M((Loop) s, f, state);
		if (s instanceof Block)
			return M((Block) s, f, state);
		if (s instanceof Call)
			return callStmt((Call) s, f, state);
		throw new IllegalArgumentException("should never reach here");
	}

	public State callStmt(Call c, Functions f, State state) {
		//System.err.print("Inside callStmt\n");
		// Functions d = null;

		// get the function name
		System.err.print("Call made from main.\n");
		System.err.print("Function call: " + c.v.id);

		return null;
	}

	State M(Assignment a, Functions f, State state) {
		if (a.target instanceof Variable)
			return null;
		else
			return state;
	}

	State M(Block b, Functions f, State state) {
		for (Statement s : b.members)
			state = M(s, f, state);
		return state;
	}

	State M(Conditional c, Functions f, State state) {
		if (M(c.test, f, state).boolValue())
			return M(c.thenbranch, f, state);
		else
			return M(c.elsebranch, f, state);
	}

	State M(Loop l, Functions f, State state) {
		if (M(l.test, f, state).boolValue())
			return M(l, f, M(l.body, f, state));
		else
			return state;
	}

	Value applyBinary(Operator op, Value v1, Value v2) {
		StaticTypeCheck.check(!v1.isUndef() && !v2.isUndef(),
				"reference to undef value");
		if (op.val.equals(Operator.INT_PLUS))
			return new IntValue(v1.intValue() + v2.intValue());
		if (op.val.equals(Operator.INT_MINUS))
			return new IntValue(v1.intValue() - v2.intValue());
		if (op.val.equals(Operator.INT_TIMES))
			return new IntValue(v1.intValue() * v2.intValue());
		if (op.val.equals(Operator.INT_DIV))
			return new IntValue(v1.intValue() / v2.intValue());
		if (op.val.equals(Operator.INT_LT))
			return new BoolValue(v1.intValue() < v2.intValue());
		if (op.val.equals(Operator.INT_LE))
			return new BoolValue(v1.intValue() <= v2.intValue());
		if (op.val.equals(Operator.INT_EQ))
			return new BoolValue(v1.intValue() == v2.intValue());
		if (op.val.equals(Operator.INT_NE))
			return new BoolValue(v1.intValue() != v2.intValue());
		if (op.val.equals(Operator.INT_GE))
			return new BoolValue(v1.intValue() >= v2.intValue());
		if (op.val.equals(Operator.INT_GT))
			return new BoolValue(v1.intValue() > v2.intValue());
		if (op.val.equals(Operator.FLOAT_PLUS))
			return new FloatValue(v1.floatValue() + v2.floatValue());
		if (op.val.equals(Operator.FLOAT_MINUS))
			return new FloatValue(v1.floatValue() - v2.floatValue());
		if (op.val.equals(Operator.FLOAT_TIMES))
			return new FloatValue(v1.floatValue() * v2.floatValue());
		if (op.val.equals(Operator.FLOAT_DIV))
			return new FloatValue(v1.floatValue() / v2.floatValue());
		if (op.val.equals(Operator.FLOAT_LT))
			return new BoolValue(v1.floatValue() < v2.floatValue());
		if (op.val.equals(Operator.FLOAT_LE))
			return new BoolValue(v1.floatValue() <= v2.floatValue());
		if (op.val.equals(Operator.FLOAT_EQ))
			return new BoolValue(v1.floatValue() == v2.floatValue());
		if (op.val.equals(Operator.FLOAT_NE))
			return new BoolValue(v1.floatValue() != v2.floatValue());
		if (op.val.equals(Operator.FLOAT_GE))
			return new BoolValue(v1.floatValue() >= v2.floatValue());
		if (op.val.equals(Operator.FLOAT_GT))
			return new BoolValue(v1.floatValue() > v2.floatValue());
		if (op.val.equals(Operator.CHAR_LT))
			return new BoolValue(v1.charValue() < v2.charValue());
		if (op.val.equals(Operator.CHAR_LE))
			return new BoolValue(v1.charValue() <= v2.charValue());
		if (op.val.equals(Operator.CHAR_EQ))
			return new BoolValue(v1.charValue() == v2.charValue());
		if (op.val.equals(Operator.CHAR_NE))
			return new BoolValue(v1.charValue() != v2.charValue());
		if (op.val.equals(Operator.CHAR_GE))
			return new BoolValue(v1.charValue() >= v2.charValue());
		if (op.val.equals(Operator.CHAR_GT))
			return new BoolValue(v1.charValue() > v2.charValue());
		if (op.val.equals(Operator.BOOL_LT))
			return new BoolValue(v1.intValue() < v2.intValue());
		if (op.val.equals(Operator.BOOL_LE))
			return new BoolValue(v1.intValue() <= v2.intValue());
		if (op.val.equals(Operator.BOOL_EQ))
			return new BoolValue(v1.boolValue() == v2.boolValue());
		if (op.val.equals(Operator.BOOL_NE))
			return new BoolValue(v1.boolValue() != v2.boolValue());
		if (op.val.equals(Operator.BOOL_GE))
			return new BoolValue(v1.intValue() >= v2.intValue());
		if (op.val.equals(Operator.BOOL_GT))
			return new BoolValue(v1.intValue() > v2.intValue());
		if (op.val.equals(Operator.AND))
			return new BoolValue(v1.boolValue() && v2.boolValue());
		if (op.val.equals(Operator.OR))
			return new BoolValue(v1.boolValue() || v2.boolValue());
		throw new IllegalArgumentException("should never reach here");
	}

	Value applyUnary(Operator op, Value v) {
		StaticTypeCheck.check(!v.isUndef(), "reference to undef value");
		if (op.val.equals(Operator.NOT))
			return new BoolValue(!v.boolValue());
		else if (op.val.equals(Operator.INT_NEG))
			return new IntValue(-v.intValue());
		else if (op.val.equals(Operator.FLOAT_NEG))
			return new FloatValue(-v.floatValue());
		else if (op.val.equals(Operator.I2F))
			return new FloatValue((float) (v.intValue()));
		else if (op.val.equals(Operator.F2I))
			return new IntValue((int) (v.floatValue()));
		else if (op.val.equals(Operator.C2I))
			return new IntValue((int) (v.charValue()));
		else if (op.val.equals(Operator.I2C))
			return new CharValue((char) (v.intValue()));
		throw new IllegalArgumentException("should never reach here: "
				+ op.toString());
	}

	Value M(Expression e, Functions f, State state) {
		if (e instanceof Value)
			return (Value) e;
		if (e instanceof Variable)
			// return (Value)(state.get(e));
			// return (Value)(state.get(e).get(0));
			if (e instanceof Binary) {
				Binary b = (Binary) e;
				return applyBinary(b.op, M(b.term1, f, state),
						M(b.term2, f, state));
			}
		if (e instanceof Unary) {
			Unary u = (Unary) e;
			return applyUnary(u.op, M(u.term, f, state));
		}
		throw new IllegalArgumentException("should never reach here");
	}
}