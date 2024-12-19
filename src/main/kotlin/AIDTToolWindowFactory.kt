import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import guohui.me.aidt.ToolStartUI

class AIDTToolWindowFactory : ToolWindowFactory,DumbAware{
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        val toolStartUI = ToolStartUI(toolWindow)
        val content = contentFactory.createContent(toolStartUI,"Start",false)
        content.isCloseable = false
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.addContentManagerListener(toolStartUI.CustomContentManagerListener())
    }

}