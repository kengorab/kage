package co.kenrg.kagelang

import co.kenrg.kagelang.asm.JvmCompiler
import co.kenrg.kagelang.ast.validate
import co.kenrg.kagelang.typechecker.ParseTreeTypeChecker
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

fun main(args: Array<String>) {
    val code: InputStream? = when (args.size) {
        0 -> System.`in`
        1 -> FileInputStream(File(args[0]))
        else -> null
    }

    if (code == null) {
        System.err.println("You need to pass either 0 or 1 arguments")
        System.exit(1)
        return
    }

    val parsingResult = co.kenrg.kagelang.parser.KageParserFacade.parse(code)
    if (!parsingResult.isCorrect()) {
        println("Errors:")
        parsingResult.errors.forEach { println(" * (${it.position!!.line}, ${it.position.column}): ${it.error}") }
        return
    }

    val root = parsingResult.root!!
    val errors = root.validate()
    if (!errors.isEmpty()) {
        println("Errors:")
        errors.forEach { println(" * (${it.position!!.line}, ${it.position.column}): ${it.error}") }
        return
    }

    val (vars, typeErrors) = ParseTreeTypeChecker.typeCheck(root)
    if (typeErrors != null) {
        println("Type Errors:")
        typeErrors.forEach { println(" * (${it.position!!.line}, ${it.position.column}): ${it.error}") }
        return
    }

    val bytes = JvmCompiler.compileClass(root, "MyClass")
    val fos = FileOutputStream("MyClass.class")
    fos.write(bytes)
    fos.close()
}
