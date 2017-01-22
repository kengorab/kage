package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.codegen.BaseTest.Companion.testLogs
import co.kenrg.kagelang.tree.KGFile
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.KGTree.KGLiteral
import co.kenrg.kagelang.tree.types.KGType
import co.kenrg.kagelang.typechecker.TCNamespace
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
import java.util.stream.Collectors.joining

val tempClassesPathName = "build/test-temp-classes"
val tempClassesPath: Path = Paths.get(tempClassesPathName)

data class Case(val stringRepr: String, val tree: KGTree.KGExpression, val expected: String)

fun intLiteral(int: Int) = KGLiteral(KGType.INT, int)
fun decLiteral(dec: Double) = KGLiteral(KGType.DEC, dec)
fun trueLiteral() = KGLiteral(KGType.BOOL, true)
fun falseLiteral() = KGLiteral(KGType.BOOL, false)
fun stringLiteral(s: String) = KGLiteral(KGType.STRING, s)

fun generateTestsToCompileAndExecuteCases(testCases: List<Case>): List<DynamicTest> {
    return testCases.map { testCase ->
        val (stringRepr, tree, expected) = testCase
        dynamicTest("Printing $stringRepr should output $expected") {
            val randomClassName = RandomStringUtils.randomAlphabetic(16)
            val typeCheckAttribVisitor = TypeCheckerAttributorVisitor(randomClassName)
            val codeGenVisitor = CodeGenVisitor(className = randomClassName)

            val treeWrappedInPrintAndMainMethod = KGTree.KGFnDeclaration("main", KGTree.KGPrint(expr = tree), listOf())

            val tcNamespace = TCNamespace.empty(randomClassName)
            treeWrappedInPrintAndMainMethod.accept(typeCheckAttribVisitor, tcNamespace.rootScope)
            val ns = CGNamespace(randomClassName, CGScope())
            treeWrappedInPrintAndMainMethod.accept(codeGenVisitor, ns.rootScope)

            writeAndExecClassFileAndThen(codeGenVisitor.results()) { output ->
                assertEquals(expected, output)
            }
        }
    }
}

fun wrapInMainMethod(statementOrExpression: KGTree) =
        // There's a hard-coded special case for main methods at this point, so any params passed here will be ignored.
        KGTree.KGFnDeclaration("main", statementOrExpression, listOf())

fun compileAndExecuteFileAnd(file: KGFile, fn: (output: String) -> Unit) {
    val randomClassName = RandomStringUtils.randomAlphabetic(16)
    val typeCheckAttribVisitor = TypeCheckerAttributorVisitor(randomClassName)
    val codeGenVisitor = CodeGenVisitor(className = randomClassName)

    val tcNamespace = TCNamespace.empty(randomClassName)
    file.accept(typeCheckAttribVisitor, tcNamespace.rootScope)

    if (!typeCheckAttribVisitor.isValid()) {
        fail("File failed typechecking with the following errors: ${typeCheckAttribVisitor.typeErrors}")
    }

    val ns = CGNamespace(randomClassName, CGScope())
    file.accept(codeGenVisitor, ns.rootScope)

    writeAndExecClassFileAndThen(codeGenVisitor.results(), fn)
}

fun writeAndExecClassFileAndThen(results: List<Pair<String, ByteArray?>>, fn: (String) -> Unit) {
    if (results.isEmpty())
        fail("No results returned from codegen, cannot proceed")

    results.forEach {
        val (name, bytes) = it
        if (bytes == null) {
            fail("Cannot write a null bytes-array to class file")
        }

        val randomClassPath = "$tempClassesPathName/$name.class"
        if (testLogs) println("Generating class file at $randomClassPath")
        val fos = FileOutputStream(randomClassPath)
        fos.write(bytes)
        fos.close()
    }

    val process = Runtime.getRuntime().exec("java ${results[0].first}", null, tempClassesPath.toFile())
    val execOutput = BufferedReader(InputStreamReader(process.inputStream))
            .lines()
            .collect(joining("\n"))
    fn(execOutput)
}
