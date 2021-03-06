package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.codegen.BaseTest.Companion.testLogs
import co.kenrg.kagelang.tree.KGFile
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.KGTree.KGLiteral
import co.kenrg.kagelang.tree.types.KGType
import co.kenrg.kagelang.typechecker.TCNamespace
import co.kenrg.kagelang.typechecker.TypeCheckerAttributorVisitor
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Assertions.fail
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

fun compileAndExecuteFileAnd(file: KGFile, fn: (output: String) -> Unit) {
    compileAndExecuteFileWithInputAnd(file, "", fn)
}

fun compileAndExecuteFileWithInputAnd(file: KGFile, cmdLineInput: String, fn: (String) -> Unit) {
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

    writeAndExecClassFileAndThen(codeGenVisitor.results(), cmdLineInput, fn)
}

private fun writeAndExecClassFileAndThen(results: List<Pair<String, ByteArray?>>, cmdLineArgs: String, fn: (String) -> Unit) {
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

    val stdlibJar = Paths.get("stdlib/build/libs/stdlib.jar")
    if (!stdlibJar.toFile().exists()) {
        System.err.println()
        System.err.println("Cannot find stdlib.jar at expected path (${stdlibJar.toFile().absolutePath}")
        System.err.println("Has the stdlib project been build? (run `./gradlew :stdlib:build` from project root)")
        System.exit(1)
    }

    val classpath = "${tempClassesPath.toFile().absolutePath}:${stdlibJar.toFile().absolutePath}"
    val process = Runtime.getRuntime().exec("java -cp $classpath ${results[0].first} $cmdLineArgs", null, tempClassesPath.toFile())

    val errOutput = BufferedReader(InputStreamReader(process.errorStream))
            .lines()
            .collect(joining("\n"))
    if (errOutput.isNotEmpty())
        println(errOutput)

    val execOutput = BufferedReader(InputStreamReader(process.inputStream))
            .lines()
            .collect(joining("\n"))
    fn(execOutput)
}
