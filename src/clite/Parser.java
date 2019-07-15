/*
 * Parser.java is a recursive descent parser that inputs a CLite program
 * and generates its abstract syntax.  Each method corresponds to a
 * concrete syntax grammar rule, which appears as a comment at the
 * beginning of the method.
 */

package clite;

import java.util.ArrayList;

public class Parser {

	Token token; // current token from the input stream
	Lexer lexer;
	String funcId = "main";
	String functionName;
	Boolean toTypeCheck = false;
	//ArrayList<Statement> membersPerm;

	// Open the Clite source program as a token stream,
	// and retrieve its first Token
	public Parser(Lexer ts) {
		lexer = ts;
		token = lexer.next();
	}

	private String match(TokenType t) {
		String value = token.value();
		if (token.type().equals(t))
			token = lexer.next();
		else
			error(t);
		return value;
	}

	private void error(TokenType tok) {
		System.err.println("Syntax error: expecting: " + tok + "; saw: "
				+ token);
		System.exit(1);
	}

	private void error(String tok) {
		System.err.println("Syntax error: expecting: " + tok + "; saw: "
				+ token);
		System.exit(1);
	}

	// ROOT (globals or body?)
	public Program program() {
		Declarations globals = new Declarations();
		Functions functions = new Functions();

		Declarations isGlobal = checkFunction(functions);
		globals.addAll(isGlobal);

		return new Program(globals, functions);
	}

	// CREATE LIST OF FUNCTIONS
	private Declarations checkFunction(Functions functions) {
		// list of declarations
		Declarations ds = new Declarations();

		while (isType())
			declaration(ds, functions);

		return ds;
	}

	// CHECK IF FUNCTION OR GLOBAL VARIABLE
	private void declaration(Declarations d, Functions f) {
		Type t = type();

		// check for main and break if found
		while (!token.type().equals(TokenType.Eof)) {
			
			// Check if function, if not it must be global variable
			Variable v = new Variable(token.value());
			match(TokenType.Identifier);

			// function
			if (token.type().equals(TokenType.LeftParen)) {
				function(f, t, v);

				if (isType())
					t = type();
				else
					break;
			}

			// variable
			else {
				d.add(new VariableDecl(v, t));

				if (token.type().equals(TokenType.Comma))
					match(TokenType.Comma);
				else if (token.type().equals(TokenType.Semicolon)) {
					match(TokenType.Semicolon);

					if (isType())
						t = type();
					else
						break;
				}
			}
		} // end while
	}

	private void function(Functions f, Type t, Variable v) {
		// System.err.print("Function name(v.id): " + v.id + "\n");
		functionName = v.id;

		// System.err.print("Function name: " + functionName + "\n");

		match(TokenType.LeftParen);
		Declarations params = parameters();
		match(TokenType.RightParen);

		match(TokenType.LeftBrace);
		Declarations locals = checkFunction(f);
		Block body = statements();
		match(TokenType.RightBrace);

		Function function = new Function(t, v, params, locals, body);

		f.add(function);
	}

	private Declarations parameters() {
		Declarations params = new Declarations();

		// Make new variables for the parameters
		while (!token.type().equals(TokenType.RightParen)) {
			Type t = type();
			Variable v = new Variable(token.value());
			match(TokenType.Identifier);
			params.add(new VariableDecl(v, t));

			if (token.type().equals(TokenType.Comma)) {
				match(TokenType.Comma);
			}
		}

		return params;
	}

	private Type type() {
		// Type --> int | bool | float | char | void
		Type t = null;
		if (token.type().equals(TokenType.Int))
			t = Type.INT;
		else if (token.type().equals(TokenType.Bool))
			t = Type.BOOL;
		else if (token.type().equals(TokenType.Float))
			t = Type.FLOAT;
		else if (token.type().equals(TokenType.Char))
			t = Type.CHAR;
		// added void
		else if (token.type().equals(TokenType.Void))
			t = Type.VOID;
		else
			error("int | bool | float | char | void");
		token = lexer.next(); // pass over the type

		return t;
	}

	private Statement statement() {
		// Statement --> ; | Block | Assignment | IfStatement | WhileStatement

		// Skip
		Statement s = new Skip();
		if (token.type().equals(TokenType.Semicolon))
			match(TokenType.Semicolon);
		// Block
		else if (token.type().equals(TokenType.LeftBrace)) {
			token = lexer.next();
			s = statements();
			match(TokenType.RightBrace);
		}
		// IfStatement
		else if (token.type().equals(TokenType.If))
			s = ifStatement();
		// WhileStatement
		else if (token.type().equals(TokenType.While))
			s = whileStatement();
		// Assignment
		else if (token.type().equals(TokenType.Identifier)) {
			Variable v = new Variable(token.value());
			match(TokenType.Identifier);

			if (token.type().equals(TokenType.Assign))
				s = assignment(v);
			// call
			else if (token.type().equals(TokenType.LeftParen)) {
				s = callStatement(v);
				match(TokenType.Semicolon);
			}
		}
		// Return
		else if (token.type().equals(TokenType.Return)) {
			// System.err.print("Function name(return var): " + functionName +
			// "\n");
			// System.err.print("token.type() = return\n");
			Variable v = new Variable(token.value());
			// set v.id to function name to keep it from
			// displaying "return" as the variable
			v.id = functionName;
			//s = returnStatement(v);
			return returnStatement(v);
		} else
			error("Illegal statement");

		return s;
	}

	private Block statements() {
		// Block --> '{' Statements '}'
		Block b = new Block();

		while (!token.type().equals(TokenType.RightBrace)) {
			b.members.add(statement());
			//System.err.print(b.members);
		}
//		System.err.print("\nFrom Parser.Block.statements()");
//		System.err.print("\nFunction: " + functionName);
//		System.err.print("\nmembers.size() = " + b.members.size() + "\n");
		
		return b;
	}

	private Assignment assignment(Variable v) {
		// Assignment --> Identifier [ [ Expression ] ] = Expression ;
		match(TokenType.Assign);
		Expression source = expression();
		match(TokenType.Semicolon);

		return new Assignment(v, source);
	}

	private Conditional ifStatement() {
		// IfStatement --> if ( Expression ) Statement [ else Statement ]
		match(TokenType.If);
		match(TokenType.LeftParen);
		Expression test = expression();
		match(TokenType.RightParen);

		Statement thenbranch = statement();
		Statement elsebranch = new Skip();

		if (token.type().equals(TokenType.Else)) {
			match(TokenType.Else);
			elsebranch = statement();
		}

		return new Conditional(test, thenbranch, elsebranch);
	}

	private Loop whileStatement() {
		// WhileStatement --> while ( Expression ) Statement
		match(TokenType.While);
		match(TokenType.LeftParen);
		Expression test = expression();
		match(TokenType.RightParen);

		Statement body = statement();
		return new Loop(test, body);
	}

	private Call callStatement(Variable v) {
		match(TokenType.LeftParen);
		ArrayList<Expression> params = new ArrayList<Expression>();

		while (!(token.type().equals(TokenType.RightParen))) {
			params.add(expression());

			if (token.type().equals(TokenType.Comma))
				match(TokenType.Comma);
		}

		match(TokenType.RightParen);

		return new Call(v, params);
	}

	private Return returnStatement(Variable v) {
		match(TokenType.Return);
		Expression finalReturn = expression();
		match(TokenType.Semicolon);

		return new Return(v, finalReturn);
	}

	private Expression expression() {
		// Expression --> Conjunction { || Conjunction }
		Expression e = conjunction();

		while (token.type().equals(TokenType.Or)) {
			Operator op = new Operator(match(TokenType.Or));
			Expression term2 = conjunction();
			e = new Binary(op, e, term2);
		}

		return e;
	}

	private Expression conjunction() {
		// Conjunction --> Equality { && Equality }
		Expression e = equality();

		while (token.type().equals(TokenType.And)) {
			Operator op = new Operator(match(TokenType.And));
			Expression term2 = equality();
			e = new Binary(op, e, term2);
		}

		return e;
	}

	private Expression equality() {
		// Equality --> Relation [ EquOp Relation ]
		Expression e = relation();

		while (isEqualityOp()) {
			Operator op = new Operator(match(token.type()));
			Expression term2 = relation();
			e = new Binary(op, e, term2);
		}

		return e;
	}

	private Expression relation() {
		// Relation --> Addition [RelOp Addition]
		Expression e = addition();

		while (isRelationalOp()) {
			Operator op = new Operator(match(token.type()));
			Expression term2 = addition();
			e = new Binary(op, e, term2);
		}

		return e;
	}

	private Expression addition() {
		// Addition --> Term { AddOp Term }
		Expression e = term();

		while (isAddOp()) {
			Operator op = new Operator(match(token.type()));
			Expression term2 = term();
			e = new Binary(op, e, term2);
		}

		return e;
	}

	private Expression term() {
		// Term --> Factor { MultiplyOp Factor }
		Expression e = factor();

		while (isMultiplyOp()) {
			Operator op = new Operator(match(token.type()));
			Expression term2 = factor();
			e = new Binary(op, e, term2);
		}

		return e;
	}

	private Expression factor() {
		// Factor --> [ UnaryOp ] Primary
		if (isUnaryOp()) {
			Operator op = new Operator(match(token.type()));
			Expression term = primary();
			return new Unary(op, term);
		} else
			return primary();
	}

	private Expression primary() {
		// Primary --> Identifier [ [ Expression ] ] | Literal | ( Expression )
		// | Type ( Expression )
		Expression e = null;
		if (token.type().equals(TokenType.Identifier)) {
			Variable v = new Variable(token.value());
			match(TokenType.Identifier);

			if (token.type().equals(TokenType.LeftParen))
				e = callStatement(v);
			else
				e = v;

		} else if (isLiteral())
			e = literal();
		else if (token.type().equals(TokenType.LeftParen)) {
			match(TokenType.LeftParen);
			e = expression();
			match(TokenType.RightParen);
		} else if (isType()) {
			Operator op = new Operator(match(token.type()));
			match(TokenType.LeftParen);
			Expression term = expression();
			match(TokenType.RightParen);
			e = new Unary(op, term);
		} else
			error("Identifier | Literal | ( | Type");

		return e;
	}

	private Value literal() {
		String s = null;
		switch (token.type()) {
		case IntLiteral:
			s = match(TokenType.IntLiteral);
			return new IntValue(Integer.parseInt(s));
		case CharLiteral:
			s = match(TokenType.CharLiteral);
			return new CharValue(s.charAt(0));
		case True:
			s = match(TokenType.True);
			return new BoolValue(true);
		case False:
			s = match(TokenType.False);
			return new BoolValue(false);
		case FloatLiteral:
			s = match(TokenType.FloatLiteral);
			return new FloatValue(Float.parseFloat(s));
		}
		throw new IllegalArgumentException("should not reach here");
	}

	private boolean isAddOp() {
		return token.type().equals(TokenType.Plus)
				|| token.type().equals(TokenType.Minus);
	}

	private boolean isMultiplyOp() {
		return token.type().equals(TokenType.Multiply)
				|| token.type().equals(TokenType.Divide);
	}

	private boolean isUnaryOp() {
		return token.type().equals(TokenType.Not)
				|| token.type().equals(TokenType.Minus);
	}

	private boolean isEqualityOp() {
		return token.type().equals(TokenType.Equals)
				|| token.type().equals(TokenType.NotEqual);
	}

	private boolean isRelationalOp() {
		return token.type().equals(TokenType.Less)
				|| token.type().equals(TokenType.LessEqual)
				|| token.type().equals(TokenType.Greater)
				|| token.type().equals(TokenType.GreaterEqual);
	}

	private boolean isType() {
		return token.type().equals(TokenType.Int)
				|| token.type().equals(TokenType.Bool)
				|| token.type().equals(TokenType.Float)
				|| token.type().equals(TokenType.Char)
				// added void
				|| token.type().equals(TokenType.Void);
	}

	private boolean isLiteral() {
		return token.type().equals(TokenType.IntLiteral) || isBooleanLiteral()
				|| token.type().equals(TokenType.FloatLiteral)
				|| token.type().equals(TokenType.CharLiteral);
	}

	private boolean isBooleanLiteral() {
		return token.type().equals(TokenType.True)
				|| token.type().equals(TokenType.False);
	}

} // Parser