import SimpleCompilerPackage.CommandWord;
import SimpleCompilerPackage.CompilerProgram;

public class CodeGenerator {

	private int label_counter;
	private CompilerProgram slx;

	public CodeGenerator() {
		this.label_counter = 0;
		this.slx = new CompilerProgram();
	}

	public void emit(String word) {
		this.slx.emit(CommandWord.valueOf(word));
	}

	public void emit(String word, int arg1) {
		this.slx.emit(CommandWord.valueOf(word), arg1);
	}

	public void emit(String word, int arg1, int arg2) {
		this.slx.emit(CommandWord.valueOf(word), arg1, arg2);
	}

	public int newLabel() {
		return this.label_counter++;
	}

	public CompilerProgram getProgram() {
		return this.slx;
	}
}
