# Todo
Listed here are things that _need_ to or that I'd like to accomplish in this project

## Requirements
- ~~Fully convert to visitor pattern for typechecking/codegen~~
  - ~~Delete old implementation, from following Antlr/Kotlin blog post series~~
- ~~Fully rename project from sandy -> kage~~
- ~~Add line of code to error message~~
- ~~Enforce .kg file extension~~
- ~~Top-level val declaration~~
  - ~~AST validations: Verify no duplicate top-level bindings~~ (Happens during Typechecking)
- Add tests for TreeMaker (with and without positioning)
  - With positioning
  - ~~Without positioning~~
- String Literals
  - ~~Declaration and referencing~~
  - ~~Concatenation of Strings (++)~~
  - ~~Concatenation of Strings and non-Strings~~
- ~~Type annotations~~
- ~~Top-level function declaration~~
- ~~Function invocation (top-level functions)~~
- Boolean comparisons (>, >=, <, <=, ==, !=)
- If-expression
- Use Kotlin to make an extension-based wrapper around ASM, easier to use?
- Warnings/validations
  - No top-level expressions
- Arrays
  - Int arrays
  - Bool arrays
  - Dec arrays 
- Make error messages for binary expressions more explicit
    (Currently it does not identify which branch has error)
- When attributing types, if node has error give it a type of ERROR and don't continue checking
    (This avoids an error being pointed out at each parent expression)
- Custom types (other than primitives Int, Dec, Bool, and String)
- Compiler command-line arguments
  - input file
  - output file


## Nice-to-haves
- Syntax highlighting plugin for IntelliJ? (Good opportunity to learn about IntelliJ plugins?)