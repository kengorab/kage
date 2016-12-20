package co.kenrg.kagelang

import co.kenrg.kagelang.codegen.CodeGenVisitor
import co.kenrg.kagelang.parser.KageParserFacade
import co.kenrg.kagelang.typechecker.TypeCheckerAttributorVisitor
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

    val parsingResult = KageParserFacade.parse(code)
    if (!parsingResult.isCorrect()) {
        println("Errors:")
        parsingResult.errors.forEach {
            println(" * (${it.position!!.line}, ${it.position.column}): ${it.error}")
        }
        return
    }

    val typeCheckAttribVisitor = TypeCheckerAttributorVisitor()
    parsingResult.root.accept(typeCheckAttribVisitor, mapOf())
    if (!typeCheckAttribVisitor.isValid()) {
        println("Errors:")
        typeCheckAttribVisitor.typeErrors.forEach {
            println(" * (${it.position!!.line}, ${it.position.column}): ${it.error}")
        }
        return
    }

    val codeGenVisitor = CodeGenVisitor(className = "MyClass")
    parsingResult.root.accept(codeGenVisitor, mapOf())

    val bytes = codeGenVisitor.resultBytes()
    val fos = FileOutputStream("MyClass.class")
    fos.write(bytes)
    fos.close()
}
