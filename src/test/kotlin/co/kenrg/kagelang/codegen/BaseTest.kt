package co.kenrg.kagelang.codegen

import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import java.nio.file.Files

open class BaseTest {
    companion object {
        val deleteTempClassesWhenDone = true
        val testLogs = false

        @BeforeAll
        @JvmStatic
        fun setup() {
            // Make tmp dir for classes written during tests
            if (Files.exists(tempClassesPath)) {
                FileUtils.cleanDirectory(tempClassesPath.toFile())
            } else {
                Files.createDirectory(tempClassesPath)
            }
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            // Remove tempclasses dir
            if (deleteTempClassesWhenDone && Files.exists(tempClassesPath)) {
                FileUtils.cleanDirectory(tempClassesPath.toFile())
                if (testLogs) println("Cleaning directory: $tempClassesPathName")
            }
        }
    }
}