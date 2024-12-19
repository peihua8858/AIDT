package guohui.me.aidt

import com.intellij.icons.AllIcons
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import guohui.me.aidt.presentation.IDiffToolWindow
import io.ktor.utils.io.*
import java.awt.Desktop
import java.awt.Dimension
import java.awt.Font
import javax.swing.*

class ToolStartUI(toolWindow:ToolWindow) :JBPanel<JBPanel<*>>(){

    private val mToolWindow = toolWindow
    init {
        layout =  BoxLayout(this,BoxLayout.Y_AXIS)

        add(Box.createVerticalGlue())
        val verticalBox = Box.createVerticalBox()
        verticalBox.add(Box.createVerticalGlue())

        verticalBox.add(createTitleBox())
        verticalBox.add(Box.createVerticalGlue())

        val leftFunctionDesList = getLeftFunctionDes()
        val rightFunctionDesList = getRightFunctionDes()
        for(index in leftFunctionDesList.indices){
            val leftFunctionDes = leftFunctionDesList[index]
            val rightFunctionDes = rightFunctionDesList[index]
            verticalBox.add(createHorizontalBox(leftFunctionDes,rightFunctionDes))
        }
        add(verticalBox)

        add(createButtonBox())
        add(Box.createVerticalGlue())
    }

    private fun createTitleBox():Box{
        val horizontalBox = Box.createHorizontalBox()
        horizontalBox.add(Box.createHorizontalGlue())
        val primaryLabel = JLabel(Message.string("name")+"(${Message.string("full_name")})")
        primaryLabel.font = Font("Serif",Font.BOLD,28)
        horizontalBox.add(primaryLabel)
        val authorLabel = JLabel("  ——guohui.me")
        authorLabel.font = Font("Serif",Font.BOLD,16)
        horizontalBox.add(authorLabel)
        horizontalBox.add(Box.createHorizontalGlue())
        return horizontalBox
    }

    private fun createHorizontalBox(leftFunctionDes:FunctionDes,rightFunctionDes:FunctionDes):Box{
        val horizontalBox = Box.createHorizontalBox()
        horizontalBox.add(Box.createHorizontalGlue())

        horizontalBox.add(JBLabel(leftFunctionDes.image))
        val contentLabel = JLabel(leftFunctionDes.content)
        contentLabel.preferredSize = Dimension(420,contentLabel.preferredSize.height)
        contentLabel.minimumSize = Dimension(420,contentLabel.preferredSize.height)
        horizontalBox.add(Box.createHorizontalStrut(5))
        horizontalBox.add(contentLabel)

        horizontalBox.add(Box.createHorizontalStrut(10))
        horizontalBox.add(JBLabel(rightFunctionDes.image))
        val contentLabel2 = JLabel(rightFunctionDes.content)
        contentLabel2.preferredSize = Dimension(420,contentLabel2.preferredSize.height)
        contentLabel2.minimumSize = Dimension(420,contentLabel2.preferredSize.height)
        horizontalBox.add(Box.createHorizontalStrut(5))
        horizontalBox.add(contentLabel2)

        horizontalBox.add(Box.createHorizontalGlue())

        return horizontalBox
    }

    private fun createButtonBox():Box{
        val horizontalBox = Box.createHorizontalBox()
        horizontalBox.add(Box.createHorizontalGlue())
        val buttonGitHub = JButton("GitHub")
        buttonGitHub.preferredSize = Dimension(180,50)
        buttonGitHub.addActionListener{
            openGitHubURL()
        }
        horizontalBox.add(buttonGitHub)

        val buttonStart = JButton("Start", AllIcons.General.Add)
        buttonStart.preferredSize = Dimension(280,50)
        buttonStart.addActionListener{
            createNewDiffContent()
        }
        horizontalBox.add(buttonStart)
        horizontalBox.add(Box.createHorizontalGlue())
        return horizontalBox
    }

    private fun openGitHubURL(){
        try {
            val uri = java.net.URI(Message.getMessage("source_code_url"))
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

    private fun createNewDiffContent(){
        mToolWindow.let {
            val wnd = IDiffToolWindow(it)
            val contentNumber = getContentNextNumber()
            val content = ContentFactory.getInstance().createContent(wnd.ui,"Diff $contentNumber",true)
            it.contentManager.addContent(content)
            it.contentManager.setSelectedContent(content)
            usedNumbers.add(contentNumber)
        }
    }

    private val usedNumbers = mutableSetOf<Int>()
    private var nextNumber = 1
    private fun getContentNextNumber():Int{
        while (usedNumbers.contains(nextNumber)){
            nextNumber++
        }
        return nextNumber
    }

    inner class CustomContentManagerListener:ContentManagerListener{
        override fun contentRemoved(event: ContentManagerEvent) {
            val title = event.content.displayName
            val number = title.substringAfterLast(' ').toIntOrNull()
            if(number != null){
                usedNumbers.remove(number)
                if(number<nextNumber){
                    nextNumber = number
                }
            }
        }
    }

    inner class FunctionDes(image: Icon,content:String){
        var image: Icon = image
        var content:String = content
    }

    private fun getLeftFunctionDes():List<FunctionDes>{
        val leftFunctionDesList = mutableListOf<FunctionDes>()
        leftFunctionDesList.add(FunctionDes(AllIcons.General.InlineRefresh,"Refresh: Android devices list"))
        leftFunctionDesList.add(FunctionDes(AllIcons.Actions.MenuCut,"Screenshot: Make android device screenshot"))
        leftFunctionDesList.add(FunctionDes(AllIcons.Actions.Copy,"SaveResult: Save the superimposed contrast images"))
        leftFunctionDesList.add(FunctionDes(AllIcons.Actions.MenuSaveall,"SaveViewport: Save the window viewport image"))
        leftFunctionDesList.add(FunctionDes(AllIcons.General.CollapseComponentHover,"Extra: The params control area is hidden or expanded"))
        leftFunctionDesList.add(FunctionDes(AllIcons.General.Note,"AlphaOver&PixDiff: Choose one of two contrast modes"))
        leftFunctionDesList.add(FunctionDes(AllIcons.General.Note,"Reference X or Y offset: Pixel-level movement"))
        return leftFunctionDesList
    }

    private fun getRightFunctionDes():List<FunctionDes>{
        val rightFunctionDesList = mutableListOf<FunctionDes>()
        rightFunctionDesList.add(FunctionDes(AllIcons.General.Locate,"Reposition: Move the image to the top left corner"))
        rightFunctionDesList.add(FunctionDes(AllIcons.General.ZoomIn,"ZoomIn: You can also use the MouseWheel with ALT_DOWN"))
        rightFunctionDesList.add(FunctionDes(AllIcons.General.ZoomOut,"ZoomOut: You can also use the MouseWheel with ALT_DOWN"))
        rightFunctionDesList.add(FunctionDes(AllIcons.General.ActualZoom,"ZoomActual: Scale to the actual size of the image"))
        rightFunctionDesList.add(FunctionDes(AllIcons.General.Mouse,"MouseWheelMoved with SHIFT_DOWN: Move along the axis"))
        rightFunctionDesList.add(FunctionDes(AllIcons.General.Mouse,"MouseWheelMoved with ALT_DOWN: Follow the mouse to zoom"))
        //rightFunctionDesList.add(FunctionDes(AllIcons.Webreferences.MessageQueue,Message.getMessage("email")))
        rightFunctionDesList.add(FunctionDes(AllIcons.General.FitContent,"Measurement Box: Click and drag with the mouse"))
        return rightFunctionDesList
    }
}