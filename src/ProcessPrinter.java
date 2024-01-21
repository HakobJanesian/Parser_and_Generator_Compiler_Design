
public class ProcessPrinter {

	private int processDepth = 0;
	private boolean silent = false;

	public ProcessPrinter(boolean silent) {
		this.silent = silent;
	}

	private void increaseDepth() {
		processDepth++;
		if (!silent)
			System.out.print("(");
	}

	private void printSymbol(String s) {
		if (!silent)
			System.out.println(s);
	}

	private void addTabulation() {
		if (!silent)
			for (int i = 0; i < processDepth; i++)
				System.out.print("  ");
	}

	private void decreaseDepth() {
		printSymbol(")");
		processDepth--;
		addTabulation();
	}

	public void print(String s) {
		printSymbol(s);
		addTabulation();
	}

	public void startProduction(String name) {
		increaseDepth();
		printSymbol(name);
		addTabulation();
	}

	public void endProduction() {
		decreaseDepth();
	}

}