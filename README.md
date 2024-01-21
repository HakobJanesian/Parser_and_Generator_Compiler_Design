language syntax is described in the following EBNF:

```
prg              = prgHeader varDefs "begin" statementSeq "end" "."
prgHeader        = "program" identifier ";"
varDefs          = [ "var" varSeq { varSeq } ]
varSeq           = identifier { "," identifier } : type ";"
type             = "integer" | "string"
statementSeq     = { ( simpleAssignment | complexAssignment) ";" }
simpleAssignment = identifier ":=" operand ";"
complexAssignment= identifier ":=" operand mathOP operand ";"
operand          = identifier | number
mathOp           = "+" | "-"

```

identifiers, numbers you already identify in the scanner.

your parser also needs to conduct semantic analisys, you will need to create a symbol table, which will be used in the following checks:

variable name cannot be the same as program name.

the variable can be declared only once.

the result of the work will be a minimal compiler.

generated code can be for any platform: x86, x86_64, aarch64.

we recommend using x86 or x86_64 under linux. otherwise we'll waste office hours time on solving unnecessary problems, instead of concentrating on compiler programming.

the result of the last calculation should be available as an exit code of the program.

if you can instead link to libc or use a kernel call write(), and introduce a function in the language or in your library with a function that is able to print, you'll get extra points.

therefore, if you are using windows, we recommend installing linux subsystem, if you are using macos, we recommend using virtual machine like virtualbox or qemu with some linux os installed.

after the assembly code is generated, your compiler should call assembler and linker to produce the resulting binary.

we should be able to compile your compiler source by just typing 'make'.

we won't consider checking your work otherwise.

your compiler should accept a commandline argument - source code to compile.

we will use this by calling your compiler and giving it different source files to compile.

we expect, that if we give your compiler file 'source.pas' then it'll produce the file 'source.s', which will be later assembled to 'source.o', and linked to the program named 'source'.
