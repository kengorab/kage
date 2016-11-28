package co.kenrg.sandylang

import co.kenrg.sandylang.asm.JvmCompiler
import co.kenrg.sandylang.ast.validate
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

fun main(args: Array<String>) {
//    println(File(args[0]).absolutePath)
//    return
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

    val parsingResult = co.kenrg.sandylang.parser.SandyParser.parse(code)
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

    val bytes = JvmCompiler.compileClass(root, "MyClass")
    val fos = FileOutputStream("MyClass.class")
    fos.write(bytes)
    fos.close()
}
