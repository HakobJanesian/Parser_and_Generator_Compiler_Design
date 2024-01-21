package SimpleCompilerPackage;

public enum CommandWord {

	LAB(1), JZE(1), JMP(1), ALC(0), SFR(1), SBR(2), RET(0), ENT(1), STL(0), STM(0), LDL(0), LDM(0), ADD(0), SUB(0),
	MUL(0), DIV(0), UMN(0), WRI(0), REA(0), REQ(0), RNE(0), RLT(0), RGT(0), RLE(0), RGE(0), NOT(0), HLT(0);

	private final int numberOfParameters;

	private CommandWord(int numberOfParameters) {
		this.numberOfParameters = numberOfParameters;
	}

	public int getNumberOfParameters() {
		return this.numberOfParameters;
	}

}
