package SimpleCompilerPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompilerProgram {

    private final List<Instruction> program;
    private final Map<Integer, Integer> labelMap;
    
    public CompilerProgram() {
        super();
        this.program = new ArrayList<Instruction>();
        this.labelMap = new HashMap<Integer, Integer>();
    }
    
    public void emit(final CommandWord command) {
        if (command.getNumberOfParameters() != 0) {
            throw new IllegalArgumentException
                ("Wrong number of parameters for " + command
                 + ", number of required parameters: " + 
                 command.getNumberOfParameters());
        }
        this.program.add(new Instruction(command, new Integer[] {}));
    }
    
    @SuppressWarnings("deprecation")
	public void emit(final CommandWord command, final int param1) {
        if (command.getNumberOfParameters() != 1) {
            throw new IllegalArgumentException
                ("Wrong number of parameters for " + command
                 + ", number of required parameters: " + 
                 command.getNumberOfParameters());
        }
        this.program.add(new Instruction(command, new Integer[] { param1 }));

        if (command.equals(CommandWord.LAB)) {
            this.labelMap.put(param1, new Integer(this.program.size() - 1));
        }
        
    }

    public void emit(final CommandWord command, final int param1, 
                     final int param2) {
        if (command.getNumberOfParameters() != 2) {
            throw new IllegalArgumentException
                ("Wrong number of parameters for " + command
                 + ", number of required parameters: " + 
                 command.getNumberOfParameters());
        }
        
        this.program.add(new Instruction(command, 
                                         new Integer[] { param1, param2 }));
    }

    public String toString() {
        String str = this.program.toString();
        str = str.substring(1, str.length() - 1);
        str = str.replaceAll(", ", "\n");
        return str;
    }
	
    public List<Instruction> getProgram() {
        return this.program;
    }

    public Map<Integer, Integer> getLabelMap() {
        return labelMap;
    }
}
