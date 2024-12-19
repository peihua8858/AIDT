package guohui.me.aidt.presentation

import guohui.me.aidt.Message
import guohui.me.aidt.composite.PixDiffComposite
import guohui.me.aidt.services.Device
import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.CollapsiblePanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.RadioButton
import com.intellij.ui.components.panels.HorizontalLayout
import com.intellij.ui.components.panels.VerticalLayout
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import kotlinx.coroutines.CoroutineScope
import java.awt.AlphaComposite
import java.awt.BorderLayout
import java.awt.Composite
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultComboBoxModel
import javax.swing.JButton
import javax.swing.JSlider
import javax.swing.JToggleButton

class IDiffToolWindowUI(
    scope: CoroutineScope
) : JBPanel<JBPanel<*>>() {
    val devicesModel = DefaultComboBoxModel(arrayOf(Device.noDevice))
    val devicesView = ComboBox(devicesModel)

    val refreshIcon = JBLabel(AllIcons.General.InlineRefreshHover)
    val refreshingAnimIcon = JBLabel(AllIcons.General.InlineRefresh)

    val refreshContainer = JBPanel<JBPanel<*>>().apply {
        add(refreshIcon)
        add(refreshingAnimIcon)
        refreshingAnimIcon.isVisible = false
    }

    private val devicePanel = JBPanel<JBPanel<*>>(HorizontalLayout(1)).apply {
        add(devicesView)
        add(refreshContainer)
    }

    val images = ImgCanvas(scope)

    val takeScreenshot = JButton(Message.string("create_screenshot"), AllIcons.Actions.MenuCut)
    val saveResult = JButton(Message.string("save_result"), AllIcons.Actions.Copy)
    val saveViewport = JButton(Message.string("save_viewport"), AllIcons.Actions.MenuSaveall)

    private val compositeAlphaSlider = JSlider(0, 20)
    lateinit var compositeProvider: (Int, Int) -> Composite
    val makeUpdateCallback: (() -> Unit) -> (Any) -> Unit = { apply ->
        { _ ->
            val x = compositeAlphaSlider.value
            val max = compositeAlphaSlider.maximum
            apply()
            images.setComposite(compositeProvider(x, max))
        }
    }

    val imageSizeLabel = JBLabel()
    private var aoOrSdpLabel  = JBLabel(Message.getMessage("ao_or_sdp",0,0))

    private val alphaOverAndPixDiffContainer = JBPanel<JBPanel<*>>(HorizontalLayout(10)).apply {
        run{
            val aoProvider = { x: Int, max: Int ->
                val alpha = (x.toFloat() / max.toFloat()).coerceIn(0f, 1f)
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
            }
            val sdpProvider = { x: Int, max: Int ->
                val diffThreshold = x * 3 * 255 / max
                PixDiffComposite(diffThreshold)
            }
            compositeProvider = aoProvider

            add(JBPanel<JBPanel<*>>(HorizontalLayout(10)).apply {
                val ao = RadioButton(Message.string("composite_ao")).apply {
                    isSelected = true
                    aoOrSdpLabel.text = Message.string("composite_ao")
                }
                val sdp = RadioButton(Message.string("composite_sdp"))
                add(ao)
                add(sdp)
                ao.addActionListener(
                    makeUpdateCallback {
                        ao.isSelected = true
                        sdp.isSelected = false
                        compositeProvider = aoProvider
                        aoOrSdpLabel.text = Message.string("composite_ao")
                    }
                )
                sdp.addActionListener(
                    makeUpdateCallback {
                        ao.isSelected = false
                        sdp.isSelected = true
                        compositeProvider = sdpProvider
                        aoOrSdpLabel.text = Message.string("composite_sdp")
                    }
                )
                ao.doClick()
            })
        }
    }

    private val imageControlContainer = JBPanel<JBPanel<*>>(HorizontalLayout(10)).apply {
        val locateIcon = JBLabel(AllIcons.General.Locate)
        locateIcon.toolTipText = "Reposition Image"
        locateIcon.addMouseListener(object:MouseAdapter(){
            override fun mouseClicked(e: MouseEvent?) {
                images.repositionImage()
            }
        })
        add(locateIcon)

        val zoomIn = JBLabel(AllIcons.General.ZoomIn)
        zoomIn.toolTipText = "Image Zoom In"
        zoomIn.addMouseListener(object:MouseAdapter(){
            override fun mouseClicked(e: MouseEvent?) {
                images.zoomInImage()
            }
        })
        add(zoomIn)

        val zoomOut = JBLabel(AllIcons.General.ZoomOut)
        zoomOut.toolTipText = "Image Zoom Out"
        zoomOut.addMouseListener(object:MouseAdapter(){
            override fun mouseClicked(e: MouseEvent?) {
                images.zoomOutImage()
            }
        })
        add(zoomOut)

        val zoomActual = JBLabel(AllIcons.General.ActualZoom)
        zoomActual.toolTipText = "Actual Zoom Image"
        zoomActual.addMouseListener(object:MouseAdapter(){
            override fun mouseClicked(e: MouseEvent?) {
                images.zoomActualImage()
            }
        })
        add(zoomActual)
        add(JBLabel(AllIcons.General.Divider))
        add(imageSizeLabel)

    }

    private val options = JBPanel<JBPanel<*>>().apply {
        layout = VerticalLayout(10)

        val extraContent = JBPanel<JBPanel<*>>().apply {
            layout = VerticalLayout(10)
            // Composite Selector
            run {
                compositeAlphaSlider.addChangeListener(makeUpdateCallback {})
                //add(compositeAlphaSlider)
                add(JBPanel<JBPanel<*>>(BorderLayout()).apply {
                    add(aoOrSdpLabel,BorderLayout.WEST)
                    add(compositeAlphaSlider,BorderLayout.CENTER)
                })
            }

            // Offset X
            add(
                JBPanel<JBPanel<*>>(BorderLayout()).apply {
                    val label = JBLabel(Message.string("xoffset_text", 0, 0))
                    val dimension = Dimension(220,label.preferredSize.height)
                    label.preferredSize = dimension
                    label.maximumSize = dimension
                    label.minimumSize = dimension
                    val offsetSlider = JSlider(-100, 100)
                    offsetSlider.addChangeListener {
                        val v = offsetSlider.value
                        label.text = Message.string("xoffset_text", v, v / images.pxScale)
                        images.referenceXOffset = v
                    }

                    add(label, BorderLayout.WEST)
                    add(offsetSlider, BorderLayout.CENTER)
                }
            )

            // Offset Y
            add(
                JBPanel<JBPanel<*>>(BorderLayout()).apply {
                    val softLabel = JBLabel(Message.string("soft_offset_text", 0, 0))
                    val hardOffset = JBTextField("0")
                    val hardLabel = JBLabel(Message.string("hard_offset_text", 0, 0))
                    val offsetSlider = JSlider(-100, 100)
                    val totalLabel = JBLabel(Message.string("total_offset_text", 0, 0))

                    var hard = 0

                    val update = {
                        val soft = offsetSlider.value
                        val tot = soft + hard
                        softLabel.text =
                            Message.string("soft_offset_text", soft, soft / images.pxScale)
                        hardLabel.text = Message.string("hard_offset_text", hard / images.pxScale)
                        totalLabel.text =
                            Message.string("total_offset_text", tot, tot / images.pxScale)
                        images.referenceYOffset = tot
                    }

                    hardOffset.addActionListener {
                        hardOffset.text.toIntOrNull()?.let { v ->
                            hard = v
                            update()
                        }
                    }
                    offsetSlider.addChangeListener { update() }

                    add(
                        JBPanel<JBPanel<*>>(HorizontalLayout(10)).apply {
                            add(softLabel)
                            add(hardOffset)
                            add(hardLabel)
                            add(totalLabel)
                        },
                        BorderLayout.WEST
                    )
                    add(offsetSlider, BorderLayout.CENTER)
                }
            )
        }
        val extrasPanel = CollapsiblePanel(extraContent, true)

        // Device selecter, refresh devices,takeScreenshot,Save......
        add(JBPanel<JBPanel<*>>(HorizontalLayout(5)).apply {
                add(devicePanel)
                add(takeScreenshot)
                add(saveResult)
                add(saveViewport)
                add(
                    JToggleButton(
                        Message.string("extras_panel_label"),
                        AllIcons.General.CollapseComponent,
                    ).apply {
                        addActionListener {
                            if (extrasPanel.isCollapsed) extrasPanel.expand()
                            else extrasPanel.collapse()
                        }
                    }
                )
                add(JBLabel(AllIcons.General.Divider))
                add(alphaOverAndPixDiffContainer)
                add(JBLabel(AllIcons.General.Divider))
                add(imageControlContainer)
            }
        )

        add(extrasPanel)
    }

    init {
        layout = GridLayoutManager(2, 1)

        val c = GridConstraints().apply {
            row = 0
            column = 0
            colSpan = 1
            fill = GridConstraints.FILL_HORIZONTAL
        }

        c.row = 0
        add(options, c)

        c.row = 1
        c.fill = GridConstraints.FILL_BOTH
        c.vSizePolicy = GridConstraints.SIZEPOLICY_WANT_GROW
        add(images, c)
    }
}
