import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import guohui.me.aidt.Message
import io.ktor.utils.io.*
import java.awt.Desktop

class AIDTToolMenuPlugin : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        // TODO: insert action logic here
    }

    class AIDTToolMenuPluginDocument : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            val url = Message.getMessage("document_url")
            openURL(url)
        }
    }

    class AIDTToolMenuPluginSourceCode : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            val url = Message.getMessage("source_code_url")
            openURL(url)
        }
    }

    companion object{
        fun openURL(url:String){
            try {
                val uri = java.net.URI(url)
                if(Desktop.isDesktopSupported()){
                    val desktop = Desktop.getDesktop()
                    if(desktop.isSupported(Desktop.Action.BROWSE)){
                        desktop.browse(uri)
                    }
                }
            }catch (e:Exception){
                e.printStack()
            }
        }
    }
}
