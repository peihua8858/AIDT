package guohui.me.aidt

import com.intellij.openapi.diagnostic.thisLogger
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

object Cli {
    inline fun <reified R : Any, T> R.attempt(finally: () -> Unit = {}, action: () -> T): T? {
        return try {
            action()
        } catch (e: Exception) {
            val caller = Error().stackTrace[0].methodName
            //thisLogger().error("Method \"$caller\" failed with exception", e)
            println("Method \"$caller\" failed with exception")
            println("exception ${e.localizedMessage}")
            null
        } finally {
            finally()
        }
    }

    @Throws(IOException::class)
    fun execForStdout(
        command: String,
        workingDir: String? = null,
        wait: Boolean = true,
    ): String = execForStream(command, workingDir, wait).readToEnd()

    @Throws(IOException::class)
    fun execForStream(
        command: String,
        workingDir: String? = null,
        wait: Boolean = true,
    ): InputStream = exec(command, workingDir, wait).inputStream

    @Throws(IOException::class)
    fun exec(
        command: String,
        workingDir: String? = null,
        wait: Boolean = true,
    ): Process {
        val processBuilder = ProcessBuilder(command.split(" "))
        processBuilder.redirectErrorStream(true)
        val proc = if (workingDir != null) {
            //Runtime.getRuntime().exec(command, null, File(workingDir))
            processBuilder.directory(File(workingDir))
            processBuilder.start()
        } else {
            //Runtime.getRuntime().exec(command)
            processBuilder.start()
        }
        if (wait) {
            val result = proc.waitFor()
            if (result != 0) {
                throw IOException("Process completed with result $result, command: \"$command\", stderr: ${proc.errorStream.readToEnd()}")
            }
        }
        return proc
    }

    private fun InputStream.readToEnd(): String = InputStreamReader(this).readText()
}
