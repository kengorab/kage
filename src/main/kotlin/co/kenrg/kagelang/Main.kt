package co.kenrg.kagelang

import co.kenrg.kagelang.codegen.CodeGenVisitor
import co.kenrg.kagelang.model.Error
import co.kenrg.kagelang.parser.KageParserFacade
import co.kenrg.kagelang.typechecker.TypeCheckerAttributorVisitor
import org.apache.commons.collections4.map.LinkedMap
import java.io.*
import java.util.*

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
        0 -> System.`in`
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

    val typeCheckAttribVisitor = TypeCheckerAttributorVisitor()
    parsingResult.root.accept(typeCheckAttribVisitor, HashMap())
    if (!typeCheckAttribVisitor.isValid()) {
        printErrors(lines, typeCheckAttribVisitor.typeErrors)
        System.exit(1)
    }

    val codeGenVisitor = CodeGenVisitor(className = "MyClass")
    parsingResult.root.accept(codeGenVisitor, LinkedMap())

    val bytes = codeGenVisitor.resultBytes()
    val fos = FileOutputStream("MyClass.class")
    fos.write(bytes)
    fos.close()
}
