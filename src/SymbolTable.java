import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

	private class Symbol {
		String name;
		int type;

		public Symbol(String name, int type) {
			this.name = name;
			this.type = type;
		}
	}

	public class Variable extends Symbol {
		public int adr;
		public int level;

		public Variable(String name, int type) {
			super(name, type);
		}
	}

	public class Function extends Symbol {
		public int label;
		public ArrayList<Variable> parameters = new ArrayList<Variable>();

		public Function(String name, int type, int label) {
			super(name, type);
			this.label = label;
		}

		public void addParameter(Variable var) {
			this.parameters.add(var);
		}
	}

	public class Scope {
		public Scope next;
		public int return_type, frame_size, level;

		private HashMap<String, Symbol> locals = new HashMap<String, Symbol>();

		public Scope(int type, int level) {
			this.return_type = type;
			this.level = level;
			this.frame_size = 0;
		}

		public void printLocals() {
			for (Map.Entry<String, Symbol> symbol_entry : this.locals.entrySet()) {

				String name = symbol_entry.getKey();
				Variable var = (Variable) symbol_entry.getValue();

				for (int i = 0; i < var.level; i++)
					System.out.print("  ");

				System.out.println(name + ": " + Integer.toString(var.adr));
			}
		}

		public void checkUniqueSymbolName(String name) {
			for (String local_name : this.locals.keySet())
				if (local_name.equals(name))
					parser.SemErr("name '" + name + "' declared twice");
		}

		public void addSymbol(Function fn) {
			this.checkUniqueSymbolName(fn.name);
			this.locals.put(fn.name, fn);
		}

		public void addSymbol(Variable var) {
			this.checkUniqueSymbolName(var.name);
			this.locals.put(var.name, var);
			var.adr = ++this.frame_size;
		}

		public Symbol findSymbol(String name) {
			for (Map.Entry<String, Symbol> symbol_entry : this.locals.entrySet()) {
				String symbol_name = symbol_entry.getKey();
				Symbol symbol_value = symbol_entry.getValue();
				if (name.equals(symbol_name))
					return symbol_value;
			}
			return null;
		}
	}

	private int cur_level;
	private Variable undef_var;
	Scope top_scope;

	private Parser parser;

	public SymbolTable(Parser parser) {
		this.parser = parser;
		this.top_scope = null;
		this.cur_level = 0;
		undef_var = new Variable("undef", 0);
		undef_var.adr = 0;
		undef_var.level = 0;
	}

	public void printTable() {
		System.out.println("Symbol table contents: ");
		Scope scope = this.top_scope;

		while (scope != null) {
			System.out.println(scope);
			scope.printLocals();
			scope = scope.next;
		}
		System.out.println();
	}

	public void openScope(int type) {
		Scope new_scope = new Scope(type, this.cur_level++);
		new_scope.next = top_scope;
		this.top_scope = new_scope;
	}

	public void closeScope() {
		this.top_scope = top_scope.next;
		this.cur_level--;
	}

	public Function addFunction(String name, int type, int label) {
		Function new_function = new Function(name, type, label);
		this.top_scope.addSymbol(new_function);
		return new_function;
	}

	public Variable addVariable(String name, int type) {
		Variable new_variable = new Variable(name, type);
		this.top_scope.addSymbol(new_variable);
		return new_variable;
	}

	public Symbol findSymbol(String name) {

		Scope scope = this.top_scope;
		Symbol symbol;

		while (scope != null) {
			symbol = scope.findSymbol(name);
			if (symbol != null)
				return symbol;
			scope = scope.next;
		}

		parser.SemErr("'" + name + "' is undeclared");
		return undef_var;
	}

}
