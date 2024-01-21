package SimpleCompilerPackage;

public interface SimpleCompiler {

    boolean isErrors();
    
    CompilerProgram compile(String sourceFilename);
}
