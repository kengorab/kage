package co.kenrg.kagelang

import co.kenrg.kagelang.codegen.CGNamespace
import co.kenrg.kagelang.codegen.CGScope
import co.kenrg.kagelang.codegen.CodeGenVisitor
import co.kenrg.kagelang.model.Error
import co.kenrg.kagelang.parser.KageParserFacade
import co.kenrg.kagelang.typechecker.TCNamespace
import co.kenrg.kagelang.typechecker.TypeCheckerAttributorVisitor
import java.io.*

private fun printErrors(lines: List<String>, errs: List<Error>) {
    System.err.println("Found ${errs.size} errors:\n")

    errs.forEachIndexed { index, err ->
        val (error, position) = err

        System.err.println("(${position.line}, ${position.column}): $error\n")
        System.err.println("    ${lines[position.line - 1].trim()}")
        System.err.println("    ${" ".repeat(position.column)}^")

        if (index < errs.size - 1) {
            System.err.println("\n------------\n")
        }
    }
}

fun main(args: Array<String>) {
    val code: InputStream? = when (args.size) {
        0 -> {
            System.err.println("No file provided!")
            return System.exit(1)
        }
        1 -> {
            val file = File(args[0])
            if (file.extension != "kg") {
                System.err.println("Error: File provided (${file.absolutePath}) ends in unsupported extension .${file.extension} but accepted extensions are .kg")
                return System.exit(1)
            } else {
                FileInputStream(file)
            }
        }
        else -> null
    }

    if (code == null) {
        System.err.println("You need to pass either 0 or 1 arguments")
        System.exit(1)
    }

    val lines = BufferedReader(InputStreamReader(code)).readLines()
    val codeAsString = lines.joinToString("\n")

    val parsingResult = KageParserFacade.parse(codeAsString)
    if (!parsingResult.isCorrect()) {
        printErrors(lines, parsingResult.errors)
        System.exit(1)
    }

    val typeCheckAttribVisitor = TypeCheckerAttributorVisitor("MyClass")
    val tcNamespace = TCNamespace.empty("MyClass")
    try {
        parsingResult.root.accept(typeCheckAttribVisitor, tcNamespace.rootScope)
    } catch (e: IllegalStateException) {
        System.err.println("A fatal error occurred when processing. See errors for more information")
    }
    if (!typeCheckAttribVisitor.isValid()) {
        printErrors(lines, typeCheckAttribVisitor.typeErrors)
        System.exit(1)
    }

    val codeGenVisitor = CodeGenVisitor(className = "MyClass")
    val cgNamespace = CGNamespace("MyClass", CGScope())
    parsingResult.root.accept(codeGenVisitor, cgNamespace.rootScope)

    codeGenVisitor.results().forEach {
        val (name, bytes) = it
        val fos = FileOutputStream("$name.class")
        fos.write(bytes)
        fos.close()
    }
}
