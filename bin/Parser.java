package bin;

import java.util.*;


import java.util.List;

public class Parser {
	public static final int _EOF = 0;
	public static final int _identifier = 1;
	public static final int _integer = 2;
	public static final int maxT = 15;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	//only for the editor
	private boolean isContentAssistant = false;
	private boolean errorsDetected = false;
	private boolean updateProposals = false;

	private int stopPosition = 0;
	private int proposalToken = _EOF;
	private List<String> ccSymbols = null;
	private List<String> proposals = null;
	private Token dummy = new Token();


	public SymbolTable st;
public CodeGenerator gen;

private int MAIN_LABEL = -1;

private static final int // types
        UNDEFINED=0, INTEGER=1, BOOLEAN=2, INTEGER_ARRAY=3;

public static final Map<Integer, String> TYPES_INVERSE;
static {
	  Map<Integer, String> m = new HashMap<Integer, String>();
	  m.put(0, "undefined");
	  m.put(1, "int");
	  m.put(2, "boolean");
	  m.put(3, "int[]");
	  TYPES_INVERSE = Collections.unmodifiableMap(m);
}

private void checkBoolean(int type, String operation) {
	if (type != BOOLEAN) 
  		SemErr("type error: " + operation + " expects boolean type, got " + TYPES_INVERSE.get(type));
}

private void checkInt(int type, String operation) {
	if (type != INTEGER) 
  		SemErr("type error: " + operation + " expects integer type, got " + TYPES_INVERSE.get(type));
}

private void checkArrayType(int type, String operation) {
	if (type != INTEGER_ARRAY) 
		SemErr("type error: " +operation + "expects integer array type, got " + TYPES_INVERSE.get(type));
}

private void checkAssignmentType(int expected, int type) {
	if (expected == type) return;
	
	SemErr("incompatible types: trying to assign " + TYPES_INVERSE.get(type) + " value to " + TYPES_INVERSE.get(expected) + " variable");
}

private int safeParseInt(String val) {
  try {
    return Integer.parseInt(val);
  } catch (NumberFormatException e) {
    SemErr("too large integer");
    return Integer.MAX_VALUE;
  }
}

private SymbolTable.Function newFunction(String name, int type) {
  SymbolTable.Function fn = st.addFunction(name, type, gen.newLabel());
  gen.emit("LAB", fn.label);//gen.emit("LAB, fn.label);
  st.openScope(fn.type);
  return fn;
}

private void functionReturn() {
  if (st.top_scope.level == 1 && st.top_scope.return_type == UNDEFINED) return;
  gen.emit("RET");
}

private class IdAccessData {
	public boolean local; // 'local' or 'dynamic'
	public SymbolTable.Variable var; // The variable in question
}



	public Parser(Scanner scanner) {
		this.scanner = scanner;
		errors = new Errors();
	}

	void SynErr (int n) {
		if (errDist >= minErrDist) errors.SynErr(la.line, la.col, n);
		errDist = 0;
		
		//for the editor
		errorsDetected = true;
	}

	public void SemErr (String msg) {
		if (errDist >= minErrDist) errors.SemErr(t.line, t.col, msg);
		errDist = 0;

		//for the editor
		errorsDetected = true;
	}
	 
	void Get () {
		if(isContentAssistant && updateProposals){
			la = la.next;
			if(!errorsDetected){
				proposals = ccSymbols;
			
				errorsDetected = true;
			}
		}
		
		else{
			for (;;) {
				t = la;
				la = scanner.Scan();
				if (la.kind <= maxT) {
					++errDist;
					break;
				} 

				la = t;

			}
		}
				

		

		//auch aktuellen token mitgeben,
		//if la.charPos >= current Token && la.charPos < stopPosition + la.val.length()
		//  Token temp = la.clone();
		//	la.kind = proposalToken;


		//only for the Editor
		if(isContentAssistant && !errorsDetected && la.charPos >= stopPosition + la.val.length()){
			dummy = createDummy();
			dummy.next = la;
			la = dummy;
			updateProposals = true;
			
		}
		ccSymbols = null;
	}
	
	void Expect (int n) {
		if (la.kind==n) Get(); else { SynErr(n); }
	}
	
	boolean StartOf (int s) {
		return set[s][la.kind];
	}
	
	void ExpectWeak (int n, int follow) {
		if (la.kind == n) Get();
		else {
			SynErr(n);
			while (!StartOf(follow)) Get();
		}
	}
	
	boolean WeakSeparator (int n, int syFol, int repFol) {
		int kind = la.kind;
		if (kind == n) { Get(); return true; }
		else if (StartOf(repFol)) return false;
		else {
			SynErr(n);
			while (!(set[syFol][kind] || set[repFol][kind] || set[0][kind])) {
				Get();
				kind = la.kind;
			}
			return StartOf(syFol);
		}
	}
	
	void Compiler() {
		Expect(3);
		while (la.kind == 5) {
			prgHeader();
			varDefs();
			statementSeq();
		}
		Expect(4);
	}

	void prgHeader() {
		Expect(5);
		Expect(1);
		Expect(6);
	}

	void varDefs() {
		if (la.kind == 7) {
			Get();
			varSeq();
			while (la.kind == 1 || la.kind == 9 || la.kind == 10) {
				varSeq();
			}
		}
	}

	void statementSeq() {
		while (la.kind == 1) {
			if (la.kind == 1) {
				simpleAssignment();
			} else {
				complexAssignment();
			}
			Expect(6);
		}
	}

	void varSeq() {
		if (la.kind == 1) {
			Get();
			while (la.kind == 8) {
				Get();
				Expect(1);
			}
		} else if (la.kind == 9 || la.kind == 10) {
			type();
			Expect(6);
		} else SynErr(16);
	}

	void type() {
		if (la.kind == 9) {
			Get();
		} else if (la.kind == 10) {
			Get();
		} else SynErr(17);
	}

	void simpleAssignment() {
		Expect(1);
		Expect(11);
		operand();
		Expect(6);
	}

	void complexAssignment() {
		Expect(1);
		Expect(11);
		operand();
		Expect(12);
		mathOp();
		Expect(12);
		operand();
		Expect(6);
	}

	void operand() {
		if (la.kind == 1) {
			Get();
		} else if (la.kind == 2) {
			Get();
		} else SynErr(18);
	}

	void mathOp() {
		if (la.kind == 13) {
			Get();
		} else if (la.kind == 14) {
			Get();
		} else SynErr(19);
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		Compiler();
		Expect(0);

	}

	private static final boolean[][] set = {
		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x}

	};
 
	
	//only for the editor
	public Parser(Scanner scanner, int proposalToken, int stopPosition){
		this(scanner);
		isContentAssistant = true;
		this.proposalToken = proposalToken;
		this.stopPosition = stopPosition;
	}

	public String ParseErrors(){
		java.io.PrintStream oldStream = System.out;
		
		java.io.OutputStream out = new java.io.ByteArrayOutputStream();
		java.io.PrintStream newStream = new java.io.PrintStream(out);
		
		errors.errorStream = newStream;
				
		Parse();

		String errorStream = out.toString();
		errors.errorStream = oldStream;

		return errorStream;

	}

	public List<String> getCodeCompletionProposals(){
		return proposals;
	}

	private Token createDummy(){
		Token token = new Token();
		
		token.pos = la.pos;
		token.charPos = la.charPos;
		token.line = la.line;
		token.col = la.col;
		
		token.kind = proposalToken;
		token.val = "";
		
		
		return token;
	}
} // end Parser


class Errors {
	public int count = 0;                                    // number of errors detected
	public java.io.PrintStream errorStream = System.out;     // error messages go to this stream
	public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text
	
	protected void printMsg(int line, int column, String msg) {
		StringBuffer b = new StringBuffer(errMsgFormat);
		int pos = b.indexOf("{0}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
		pos = b.indexOf("{1}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
		pos = b.indexOf("{2}");
		if (pos >= 0) b.replace(pos, pos+3, msg);
		errorStream.println(b.toString());
	}
	
	public void SynErr (int line, int col, int n) {
		String s;
		switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "identifier expected"; break;
			case 2: s = "integer expected"; break;
			case 3: s = "\"begin\" expected"; break;
			case 4: s = "\"end\" expected"; break;
			case 5: s = "\"program\" expected"; break;
			case 6: s = "\";\" expected"; break;
			case 7: s = "\"var\" expected"; break;
			case 8: s = "\",\" expected"; break;
			case 9: s = "\"integer\" expected"; break;
			case 10: s = "\"string\" expected"; break;
			case 11: s = "\":=\" expected"; break;
			case 12: s = "\".\" expected"; break;
			case 13: s = "\"+\" expected"; break;
			case 14: s = "\"-\" expected"; break;
			case 15: s = "??? expected"; break;
			case 16: s = "invalid varSeq"; break;
			case 17: s = "invalid type"; break;
			case 18: s = "invalid operand"; break;
			case 19: s = "invalid mathOp"; break;
			default: s = "error " + n; break;
		}
		printMsg(line, col, s);
		count++;
	}

	public void SemErr (int line, int col, String s) {	
		printMsg(line, col, s);
		count++;
	}
	
	public void SemErr (String s) {
		errorStream.println(s);
		count++;
	}
	
	public void Warning (int line, int col, String s) {	
		printMsg(line, col, s);
	}
	
	public void Warning (String s) {
		errorStream.println(s);
	}
} // Errors


class FatalError extends RuntimeException {
	public static final long serialVersionUID = 1L;
	public FatalError(String s) { super(s); }
}
