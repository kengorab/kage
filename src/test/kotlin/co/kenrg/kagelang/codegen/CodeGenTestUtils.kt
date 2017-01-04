package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.codegen.BaseTest.Companion.testLogs
import co.kenrg.kagelang.tree.KGFile
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.KGTree.KGLiteral
import co.kenrg.kagelang.tree.types.KGTypeTag.*
import co.kenrg.kagelang.typechecker.TypeCheckerAttributorVisitor
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

fun intLiteral(int: Int) = KGLiteral(INT, int)
fun decLiteral(dec: Double) = KGLiteral(DEC, dec)
fun trueLiteral() = KGLiteral(BOOL, true)
fun falseLiteral() = KGLiteral(BOOL, false)
fun stringLiteral(s: String) = KGLiteral(STRING, s)

fun generateTestsToCompileAndExecuteCases(testCases: List<Case>): List<DynamicTest> {
    return testCases.map { testCase ->
        val (stringRepr, tree, expected) = testCase
        dynamicTest("Printing $stringRepr should output $expected") {
            val randomClassName = RandomStringUtils.randomAlphabetic(16)
            val typeCheckAttribVisitor = TypeCheckerAttributorVisitor()
            val codeGenVisitor = CodeGenVisitor(className = randomClassName)

            val treeWrappedInPrintAndMainMethod = KGTree.KGFnDeclaration("main", KGTree.KGPrint(expr = tree))

            treeWrappedInPrintAndMainMethod.accept(typeCheckAttribVisitor, HashMap())
            treeWrappedInPrintAndMainMethod.accept(codeGenVisitor, Namespace(randomClassName, LinkedHashMap(), LinkedHashMap()))

            writeAndExecClassFileAndThen(randomClassName, codeGenVisitor.resultBytes()) { output ->
                assertEquals(expected, output)
            }
        }
    }
}

fun wrapInMainMethod(statementOrExpression: KGTree) =
        KGTree.KGFnDeclaration("main", statementOrExpression)

fun compileAndExecuteFileAnd(file: KGFile, fn: (output: String) -> Unit) {
    val randomClassName = RandomStringUtils.randomAlphabetic(16)
    val typeCheckAttribVisitor = TypeCheckerAttributorVisitor()
    val codeGenVisitor = CodeGenVisitor(className = randomClassName)

    file.accept(typeCheckAttribVisitor, HashMap())
    file.accept(codeGenVisitor, Namespace(randomClassName, LinkedHashMap(), LinkedHashMap()))

    writeAndExecClassFileAndThen(randomClassName, codeGenVisitor.resultBytes(), fn)
}

fun writeAndExecClassFileAndThen(className: String, bytes: ByteArray?, fn: (String) -> Unit) {
    if (bytes == null) {
        fail("Cannot write a null bytes-array to class file")
    }

    val randomClassPath = "$tempClassesPathName/$className.class"
    if (testLogs) println("Generating class file at $randomClassPath")
    val fos = FileOutputStream(randomClassPath)
    fos.write(bytes)
    fos.close()

    val process = Runtime.getRuntime().exec("java $className", null, tempClassesPath.toFile())
    val execOutput = BufferedReader(InputStreamReader(process.inputStream))
            .lines()
            .collect(joining("\n"))
    fn(execOutput)
}
