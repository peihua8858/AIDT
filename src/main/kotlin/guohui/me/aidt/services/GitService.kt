package guohui.me.aidt.services

import guohui.me.aidt.Cli
import guohui.me.aidt.Cli.attempt
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class GitService(private val project: Project) {
    fun branchName(): String = attempt {
        Cli.execForStdout("git symbolic-ref --short HEAD", workingDir = project.basePath)
            .trim()
    } ?: "UNKNOWN"
}
