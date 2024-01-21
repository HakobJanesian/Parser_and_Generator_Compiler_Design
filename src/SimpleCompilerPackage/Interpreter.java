package SimpleCompilerPackage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class Interpreter {
	private static final Logger log = Logger.getLogger(Interpreter.class.getName());

	private final static int MEM_SIZE = 5000;
	private final static int MAX_PROGRAM_SIZE = 1000000;

	private final static int FRAME_SIZE = 2;

	private final List<Instruction> program;
	private final Map<Integer, Integer> labelMap;
	private final List<Integer> result;

	private final Integer[] memory;
	private final Stack<Integer> stack;
	private int programCounter = 0;
	private int framePointer = 0;
	private int heapPointer = MEM_SIZE;
	private boolean programExecuted = false;

	@SuppressWarnings("deprecation")
	public Interpreter(final String fileName) {
		super();
		if (fileName == null) {
			throw new IllegalArgumentException("Filename may not be null!");
		}
		this.program = new ArrayList<Instruction>();
		this.labelMap = new HashMap<Integer, Integer>();
		this.stack = new Stack<Integer>();
		this.result = new ArrayList<Integer>();
		this.memory = new Integer[MEM_SIZE];
		for (int i = 0; i < this.memory.length; i++) {
			this.memory[i] = new Integer(0);
		}
		this.readFile(fileName);
	}

	@SuppressWarnings("deprecation")
	public Interpreter(final CompilerProgram slxProgram, final String loggingLevel) {
		super();
		if (slxProgram == null) {
			throw new IllegalArgumentException("Parameter may not be null!");
		}

		Interpreter.log.setLevel(Level.SEVERE);
		Interpreter.parseLoggingLevel(loggingLevel);

		this.stack = new Stack<Integer>();
		this.result = new ArrayList<Integer>();
		this.memory = new Integer[MEM_SIZE];
		for (int i = 0; i < this.memory.length; i++) {
			this.memory[i] = new Integer(0);
		}
		this.program = slxProgram.getProgram();
		this.labelMap = slxProgram.getLabelMap();
	}

	@SuppressWarnings("deprecation")
	private void readFile(final String fileName) {
		BufferedReader reader = null;
		log.info("Start reading file " + fileName);
		try {
			try {
				reader = new BufferedReader(new FileReader(fileName));

				int instructionNumber = 0;
				int sourceLineNumber = 0;
				while (reader.ready()) {
					String line = reader.readLine().trim();
					sourceLineNumber++;
					if (line.length() > 0 && !line.startsWith(";")) {
						try {
							Instruction c = new Instruction(line, sourceLineNumber);
							if (c.getCommandWord().equals(CommandWord.LAB)) {
								this.labelMap.put(c.getCommandParameter(0), new Integer(instructionNumber));
							}
							this.program.add(c);
							instructionNumber++;
						} catch (IllegalInstructionException e) {
							log.severe(e.getMessage());
							this.program.clear();
							return;
						}
					}
				}
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
		} catch (FileNotFoundException e) {
			log.severe("File '" + fileName + "' not found!");
		} catch (IOException e) {
			log.severe("Unknown IO exception");
			e.printStackTrace();
		}
		log.info("Reading done. Success? " + !this.program.isEmpty());
	}

	public boolean isReady() {
		return !this.program.isEmpty();
	}

	public void execute(final boolean printToStdout, int[] input) {
		if (this.isReady()) {
			try {
				this.executePrivate(printToStdout, input);
				this.programExecuted = true;
			} catch (ExecutionException ee) {
				log.severe("Execution halted due program error.");
			} catch (RuntimeException re) {
				log.severe("Execution halted due unexpected error.");
				re.printStackTrace();
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void executePrivate(final boolean printToStdout, int[] input) {

		boolean halt = false;
		Integer x1 = null;
		Integer x2 = null;
		int step = 0;
		int inputPointer = 0;
		boolean readDone;

		BufferedReader inputReader = null;

		if (input == null) {
			inputReader = new BufferedReader(new InputStreamReader(System.in));
		}

		log.info("Start executing...");

		while (!halt && step < MAX_PROGRAM_SIZE) {
			Instruction instr = this.program.get(programCounter);
			programCounter++;
			switch (instr.getCommandWord()) {
			case ADD:
				this.check(instr, stack.size() >= 2, "Stack underflow");
				x1 = stack.pop();
				x2 = stack.pop();
				this.checkForNull(x1, x2, instr);
				log.info("Stack <- " + x1 + " + " + x2);
				stack.push(new Integer(x1.intValue() + x2.intValue()));
				break;
			case ALC:
				this.check(instr, stack.size() > 0, "Stack underflow");
				x1 = stack.pop();
				x2 = x1.intValue() + 1;
				heapPointer = heapPointer - x2 - 1;
				memory[heapPointer] = x1;
				stack.push(new Integer(heapPointer));
				log.info("Allocate memory: " + x1 + " slots, HP=" + heapPointer);
				break;
			case DIV:
				// x = pop(); y = pop(); push(y / x);
				this.check(instr, stack.size() >= 2, "Stack underflow");
				x1 = stack.pop();
				x2 = stack.pop();
				this.checkForNull(x1, x2, instr);
				this.check(instr, x1 != 0, "Division by zero");
				stack.push(new Integer(x2.intValue() / x1.intValue()));
				break;
			case ENT:
				stack.push(instr.getCommandParameter(0));
				break;
			case HLT:
				halt = true;
				log.info("Halt!");
				break;
			case JMP:
				this.check(instr, this.labelMap.containsKey(instr.getCommandParameter(0)),
						"Unknown label: " + instr.getCommandParameter(0));
				programCounter = this.labelMap.get(instr.getCommandParameter(0));
				log.info("Jump to " + programCounter);
				break;
			case JZE:
				this.check(instr, stack.size() > 0, "Stack underflow");
				x1 = stack.pop();
				log.info("Compare " + x1 + " to zero...");
				if (x1.intValue() == 0) {
					this.check(instr, this.labelMap.containsKey(instr.getCommandParameter(0)),
							"Unknown label: " + instr.getCommandParameter(0));
					programCounter = this.labelMap.get(instr.getCommandParameter(0));
					log.info("... true => jump to " + programCounter);
				} else {
					log.info("... false => continue");
				}
				break;
			case LAB:
				// Just skip the label
				break;
			case LDL:
				this.check(instr, stack.size() > 0, "Stack underflow");
				x1 = stack.pop();
				log.info("Stack <- mem[" + (x1 + framePointer) + "]=" + memory[framePointer + x1.intValue()]);
				stack.push(memory[framePointer + x1.intValue()]);
				break;
			case LDM:
				this.check(instr, stack.size() > 0, "Stack underflow");
				x1 = stack.pop();
				this.check(instr, x1 >= 0 && x1 < memory.length, "Memory access out of bounds");
				log.info("Stack <- mem[" + x1 + "]=" + memory[x1.intValue()]);
				stack.push(memory[x1.intValue()]);
				break;
			case MUL:
				this.check(instr, stack.size() >= 2, "Stack underflow");
				x1 = stack.pop();
				x2 = stack.pop();
				this.checkForNull(x1, x2, instr);
				log.info("Stack <- " + x1 + " * " + x2);
				stack.push(new Integer(x1.intValue() * x2.intValue()));
				break;
			case NOT:
				this.check(instr, stack.size() > 0, "Stack underflow");
				x1 = stack.pop();
				log.info("Stack <- !" + x1);
				stack.push(new Integer(x1.intValue() == 1 ? 0 : 1));
				break;
			case REQ:
				this.check(instr, stack.size() >= 2, "Stack underflow");
				x1 = stack.pop();
				x2 = stack.pop();
				this.checkForNull(x1, x2, instr);
				log.info("Stack <-" + x2 + " == " + x1);
				stack.push(new Integer(x1.intValue() == x2.intValue() ? 1 : 0));
				break;
			case RET:
				programCounter = memory[framePointer];
				heapPointer = memory[framePointer - FRAME_SIZE + 1];
				framePointer = memory[framePointer - FRAME_SIZE];
				log.info("Return from subroutine to " + programCounter + ", FP=" + framePointer);
				break;
			case RGE:
				this.check(instr, stack.size() >= 2, "Stack underflow");
				x1 = stack.pop();
				x2 = stack.pop();
				this.checkForNull(x1, x2, instr);
				log.info("Stack <- " + x2 + " >= " + x1);
				stack.push(new Integer(x2.intValue() >= x1.intValue() ? 1 : 0));
				break;
			case RGT:
				this.check(instr, stack.size() >= 2, "Stack underflow");
				x1 = stack.pop();
				x2 = stack.pop();
				this.checkForNull(x1, x2, instr);
				log.info("Stack <- " + x2 + " > " + x1);
				stack.push(new Integer(x2.intValue() > x1.intValue() ? 1 : 0));
				break;
			case RLE:
				this.check(instr, stack.size() >= 2, "Stack underflow");
				x1 = stack.pop();
				x2 = stack.pop();
				this.checkForNull(x1, x2, instr);
				log.info("Stack <- " + x2 + " <= " + x1);
				stack.push(new Integer(x2.intValue() <= x1.intValue() ? 1 : 0));
				break;
			case RLT:
				this.check(instr, stack.size() >= 2, "Stack underflow");
				x1 = stack.pop();
				x2 = stack.pop();
				this.checkForNull(x1, x2, instr);
				log.info("Stack <- " + x2 + " < " + x1);
				stack.push(new Integer(x2.intValue() < x1.intValue() ? 1 : 0));
				break;
			case RNE:
				this.check(instr, stack.size() >= 2, "Stack underflow");
				x1 = stack.pop();
				x2 = stack.pop();
				this.checkForNull(x1, x2, instr);
				log.info("Stack <- " + x2 + " != " + x1);
				stack.push(new Integer(x1.intValue() != x2.intValue() ? 1 : 0));
				break;
			case SBR:
				memory[framePointer] = programCounter;
				this.check(instr, this.labelMap.containsKey(instr.getCommandParameter(0)),
						"Unknown label: " + instr.getCommandParameter(0));
				programCounter = this.labelMap.get(instr.getCommandParameter(0));
				log.info("Call subroutine, jump to " + programCounter + ", old PC stored to " + framePointer);
				this.check(instr, stack.size() >= instr.getCommandParameter(1), "Stack underflow");
				this.check(instr, instr.getCommandParameter(1) >= 0, "Negative amount of parameters");
				this.check(instr, framePointer + instr.getCommandParameter(1) < memory.length,
						"Memory access out of bounds");
				for (int i = instr.getCommandParameter(1); i > 0; i--) {
					x1 = stack.pop();
					memory[framePointer + i] = x1;
					log.info("Store parameter " + (i - 1) + "(value=" + x1 + ") at FP=" + (framePointer + i));
				}
				break;
			case SFR:
				int frameOffset = instr.getCommandParameter(0);
				this.check(instr, framePointer + frameOffset + FRAME_SIZE + 1 < memory.length,
						"Frame pointer out of memory bounds");
				log.info("Store FP=" + framePointer + " to memory location " + (framePointer + frameOffset + 1)
						+ "(Offset=" + frameOffset + ")");
				memory[framePointer + frameOffset + FRAME_SIZE - 1] = framePointer;
				memory[framePointer + frameOffset + FRAME_SIZE] = heapPointer;
				framePointer = framePointer + frameOffset + FRAME_SIZE + 1;
				log.info("New FP=" + framePointer);
				break;
			case STL:
				this.check(instr, stack.size() >= 2, "Stack underflow");
				x1 = stack.pop();
				x2 = stack.pop();
				this.checkForNull(x1, x2, instr);
				this.check(instr, (framePointer + x2.intValue()) >= 0 && (framePointer + x2.intValue()) < memory.length,
						"Memory access out of bounds");
				log.info("Stack (value=" + x1 + ") -> mem[" + (x2.intValue() + framePointer) + "]");
				memory[framePointer + x2.intValue()] = x1;
				break;
			case STM:
				this.check(instr, stack.size() >= 2, "Stack underflow");
				x1 = stack.pop();
				x2 = stack.pop();
				this.checkForNull(x1, x2, instr);
				this.check(instr, x2 >= 0 && x2 < memory.length, "Memory access out of bounds");
				log.info("Stack (value=" + x1 + ") -> mem[" + x2.intValue() + "]");
				memory[x2.intValue()] = x1;
				break;
			case SUB:
				this.check(instr, stack.size() >= 2, "Stack underflow");
				x1 = stack.pop();
				x2 = stack.pop();
				this.checkForNull(x1, x2, instr);
				log.info("Stack <- " + x2 + " - " + x1);
				stack.push(new Integer(x2.intValue() - x1.intValue()));
				break;
			case UMN:
				this.check(instr, stack.size() > 0, "Stack underflow");
				x1 = stack.pop();
				log.info("Stack <- -" + x1);
				stack.push(new Integer(-x1.intValue()));
				break;
			case WRI:
				this.check(instr, stack.size() > 0, "Stack underflow");
				x1 = stack.pop();
				if (printToStdout) {
					System.out.println(x1);
				}
				this.result.add(x1);
				break;
			case REA:
				if (input == null) {
					readDone = false;
					while (!readDone) {
						try {
							x1 = new Integer(Integer.parseInt(inputReader.readLine()));
							readDone = true;
						} catch (IOException ioe) {
						} catch (NumberFormatException nfe) {
						}
					}
					stack.push(x1);
				} else {
					if (inputPointer < input.length) {
						stack.push(new Integer(input[inputPointer]));
						inputPointer++;
					} else {
						log.severe("Could not read input at line " + instr.getSourceLine());
						halt = true;
					}
				}
				break;
			default:
				log.severe("Unknown instruction: " + instr + " at line " + instr.getSourceLine());
				halt = true;
				break;
			}
			step++;
		}

		if (step >= MAX_PROGRAM_SIZE) {
			log.severe("Program execution halted due too many steps");
			throw new ExecutionException();
		}

	}

	private void check(final Instruction instr, final boolean checkExpression, final String message) {
		if (!checkExpression) {
			log.severe(message);
			this.logCurrentState(instr);
			throw new ExecutionException();
		}
	}

	private void logCurrentState(final Instruction instr) {
		if (log.isLoggable(Level.INFO)) {
			log.info("Current instruction: " + instr + " (line " + instr.getSourceLine() + ")");
			log.info("PC: " + programCounter + "\tFP: " + framePointer + "\tHP:" + heapPointer);
			log.info("Stack contents:");
			if (this.stack.size() > 0) {
				int limit = Math.max(stack.size() - 10, -1);
				for (int i = stack.size() - 1; i > limit; i--) {
					log.info(i + ": " + stack.get(i));
				}
			} else {
				log.info("Stack is empty!");
			}

			log.info("Memory dump:");
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("0x00000001\t");
			int i = 0;
			while (i < MEM_SIZE) {
				stringBuilder.append("0x");
				stringBuilder.append(String.format("%08X", this.memory[i]));
				stringBuilder.append(" ");
				i++;
				if (i < MEM_SIZE && (i % 8) == 0) {
					log.info(stringBuilder.toString());
					stringBuilder = new StringBuilder();
					stringBuilder.append("0x");
					stringBuilder.append(String.format("%08X", i));
					stringBuilder.append("\t");
				}
			}
			log.info(stringBuilder.toString());
		}
	}

	private void checkForNull(final Integer x1, final Integer x2, final Instruction instr) {
		if (x1 == null) {
			throw new IllegalStateException(
					"Parameter 1 is null for instruction " + instr + " at line " + instr.getSourceLine());
		}
		if (x2 == null) {
			throw new IllegalStateException(
					"Parameter 2 is null for instruction " + instr + " at line " + instr.getSourceLine());
		}
	}

	public List<Integer> getResult() {
		return this.result;
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			log.getParent().getHandlers()[0].setFormatter(new Formatter() {
				@Override
				public String format(LogRecord record) {
					return record.getMillis() + ": " + record.getMessage() + "\n";
				}
			});

			log.setLevel(Level.WARNING);
			if ((args.length > 2) && "-d".equals(args[0].trim().toLowerCase())) {
				parseLoggingLevel(args[1]);
			}

			Interpreter inter = new Interpreter(args[args.length - 1]);
			inter.execute(true, null);
		} else {
			System.out.println("Usage: java -jar SlxInterpreter.jar [-d logging-level] <filename>");
			System.out.println("Valid logging levels are one of the following:");
			System.out.println("\tSevere, Warning [default], Info");
		}
	}

	private static void parseLoggingLevel(final String loggingLevelString) {
		try {
			log.setLevel(Level.parse(loggingLevelString.toUpperCase()));
		} catch (IllegalArgumentException e) {
			log.warning("Invalid logging level: " + loggingLevelString);
		}
		log.info("Logging level set to " + loggingLevelString.toUpperCase());
	}

	public boolean isProgramExecuted() {
		return this.programExecuted;
	}

}