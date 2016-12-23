package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.tree.KGFile
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.KGTree.KGLiteral
import co.kenrg.kagelang.tree.types.KGTypeTag.*
import co.kenrg.kagelang.typechecker.TypeCheckerAttributorVisitor
import org.apache.commons.collections4.map.LinkedMap
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors.joining

val tempClassesPathName = "build/test-temp-classes"
val tempClassesPath: Path = Paths.get(tempClassesPathName)

data class Case(val stringRepr: String, val tree: KGTree.KGExpression, val expected: String)

val trueLiteral = KGLiteral(BOOL, true)
val falseLiteral = KGLiteral(BOOL, false)
fun intLiteral(int: Int): KGLiteral = KGLiteral(INT, int)
fun decLiteral(dec: Double): KGLiteral = KGLiteral(DEC, dec)

fun generateTestsToCompileAndExecuteCases(testCases: List<Case>): List<DynamicTest> {
    return testCases.map { testCase ->
        val (stringRepr, tree, expected) = testCase
        dynamicTest("Printing $stringRepr should output $expected") {
            val randomClassName = RandomStringUtils.randomAlphabetic(16)
            val typeCheckAttribVisitor = TypeCheckerAttributorVisitor()
            val codeGenVisitor = CodeGenVisitor(className = randomClassName)

            val treeWrappedInPrint = KGTree.KGPrint(expr = tree)

            treeWrappedInPrint.accept(typeCheckAttribVisitor, HashMap())
            treeWrappedInPrint.accept(codeGenVisitor, LinkedMap())

            writeAndExecClassFileAndThen(randomClassName, codeGenVisitor.resultBytes()) { output ->
                assertEquals(expected, output)
            }
        }
    }
}

fun compileAndExecuteFileAnd(file: KGFile, fn: (output: String) -> Unit) {
    val randomClassName = RandomStringUtils.randomAlphabetic(16)
    val typeCheckAttribVisitor = TypeCheckerAttributorVisitor()
    val codeGenVisitor = CodeGenVisitor(className = randomClassName)

    file.accept(typeCheckAttribVisitor, HashMap())
    file.accept(codeGenVisitor, LinkedMap())

    writeAndExecClassFileAndThen(randomClassName, codeGenVisitor.resultBytes(), fn)
}

fun writeAndExecClassFileAndThen(className: String, bytes: ByteArray?, fn: (String) -> Unit) {
    if (bytes == null) {
        fail("Cannot write a null bytes-array to class file")
    }

    val randomClassPath = "$tempClassesPathName/$className.class"
    println("Generating class file at $randomClassPath")
    val fos = FileOutputStream(randomClassPath)
    fos.write(bytes)
    fos.close()

    val process = Runtime.getRuntime().exec("java $className", null, tempClassesPath.toFile())
    val execOutput = BufferedReader(InputStreamReader(process.inputStream))
            .lines()
            .collect(joining("\n"))
    fn(execOutput)
}
