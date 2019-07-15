package clite;

import java.util.ArrayList;
import java.util.HashMap;

// Represents the program state as it executes and simulates its runtime behavior
class State extends HashMap<Variable, Value> {

	HashMap<Variable, Value> static_area; // Static area of memory (for globals)
	// create your own activation records class
	// Stack <ActivationRecord> runtime_stack; // Stack area of memory (holds a
	// stack of activation records)
	ArrayList<Value> heap_area; // Heap area of memory (holds dynamically
								// allocated elements, e.g. arrays)

	final static int STACK_LIMIT = 512; // Sets the limits on how big the stack
										// and heap areas
	final static int HEAP_LIMIT = 512; // are allowed to grow; simulates the RAM
										// size of a machine

	int nextInStack = 0; // Next available memory address in stack area
	int nextInHeap = STACK_LIMIT; // Next available memory address in heap area

	HashMap<VariableRef, Value> visible_state; // Set of variables visible to
												// the active function (topmost
												// AR)

	public State() {
		super();
	}

	public State(Variable var, Value val) {
		super();
		this.put(var, val);
	}

	public State getState(Variable var, Value val) {
		this.put(var, val);
		return this;
	}

	public State getState(State state) {
		for (Variable var : state.static_area.keySet())
			put(var, state.get(state));
		return this;
	}

	public void display(Declarations d) {
		System.out.print("\nAllocated global variables to memory.\n");
		System.out.print("Current state:\n");
		System.out.print("  MemAddr Var/Ref Type  Value\n");
		System.out.print(" Static Area:\n");

		int addressIncr = 0;
		String value = "undef";
		for (int i = 0; i < d.size(); i++) {
			System.out.print("  " + Integer.toString(nextInStack + addressIncr)
					+ "  " + d.get(i).v.id + "  " + d.get(i).t.id + " " + value
					+ "\n"); // will address undef later
			addressIncr += 4;
		}

		System.out.print(" Runtime Stack:\n");
		System.out.print(" Heap Area:\n");
	}

	public void display(Functions f) { // prob gotta pass function index to
										// retrieve name from Function class
		// System.out.print("\nAllocated AR for function " + memory.\n");
		System.out.print("Current state:\n");
		System.out.print("  MemAddr Var/Ref Type  Value\n");
		System.out.print(" Runtime Stack:\n");

		int addressIncr = 0;
		String value = "undef";
		// for (int i = 0; i < d.size(); i++) {
		// System.out.print("  " + Integer.toString(nextInStack + addressIncr)
		// + "  " + d.get(i).v.id + "  " + d.get(i).t.id + " " + value
		// + "\n"); // will address undef later
		// addressIncr += 4;
		// }

		System.out.print(" Runtime Stack:\n");
		System.out.print(" Heap Area:\n");
	}

}

class ActivationRecord {
	Type t;
	Variable id;

	// Declarations params, locals;
	// Block body;

	// public ActivationRecord(Type t, Variable id, Declarations params,
	// Declarations locals, Block body) {
	public ActivationRecord(Type t, Variable id) {
		this.t = t;
		this.id = id;
		// this.params = params;
		// this.locals = locals;
		// this.body = body;
	}

	public void display(int level) {
		Indenter indent = new Indenter(level);
		indent.display("Function = " + id + "; Return type = " + t.id);
		// indent.display("  params = ");
		// params.display(level + 1);
		// indent.display("  locals = ");
		// locals.display(level + 1);
		// body.display(level + 1);
	}
}