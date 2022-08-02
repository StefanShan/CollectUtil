package com.byte_stefan.collect_util.ui

import com.byte_stefan.collect_util.UtilClassFileManager
import com.byte_stefan.collect_util.UtilInfo
import com.byte_stefan.collect_util.showWarning
import com.byte_stefan.collect_util.util.warn
import com.intellij.notification.NotificationGroupManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.SelectionAwareListCellRenderer
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.layout.panel
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.nio.file.Path
import javax.swing.DefaultListModel

class CollectUtilToolWindow : ToolWindowFactory {

    private lateinit var jbTextField: JBTextField
    private val listModel = DefaultListModel<UtilInfo>()
    private lateinit var jbList: JBList<UtilInfo>

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.contentManager.addContent(
            ContentFactory.SERVICE.getInstance().createContent(
                panel {
                    row {
                        cell {
                            label("按desc关键词搜索: ")
                            jbTextField = textField({ " " }, {}, 30).component
                            button("确认") {
                                if (UtilClassFileManager.isScanning()) {
                                    showScanningNotification(project)
                                    return@button
                                }
                                listModel.clear()
                                listModel.addAll(UtilClassFileManager.getMarkUtilClassList().filter {
                                    it.desc?.contains(jbTextField.text.trim(), true) == true
                                })
                                jbList.model = listModel
                                jbList.repaint()
                            }
                        }
                    }.right {
                        cell {
                            button("刷新") {
                                UtilClassFileManager.scanUtilClass(project)
                            }
                        }
                    }
                    row {
                        jbList = JBList()
                        scrollPane(jbList.apply {
                            cellRenderer = SelectionAwareListCellRenderer { utilInfo ->
                                return@SelectionAwareListCellRenderer panel {
                                    titledRow("className = ${utilInfo.className ?: "null"}") {
                                        row {
                                            cell {
                                                label("methodName = ")
                                                label(utilInfo.methodName ?: "null")
                                            }
                                        }.visible = utilInfo.methodName.isNullOrEmpty().not()
                                        row {
                                            cell {
                                                label("moduleName = ")
                                                label(utilInfo.moduleName ?: "null")
                                            }
                                        }
                                        row {
                                            cell {
                                                label("desc = ")
                                                label(utilInfo.desc ?: "null")
                                            }
                                        }
                                        row {
                                            cell {
                                                label("path = ")
                                                label(
                                                    utilInfo.path?.removePrefix(project.basePath ?: "")
                                                        ?: "null"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            addMouseListener(object : MouseListener {
                                override fun mouseClicked(e: MouseEvent) {
                                    toolWindow.hide(null)
                                    val selectUtilInfo = (e.source as JBList<*>).selectedValue as? UtilInfo
                                    if (selectUtilInfo?.path == null) return
                                    ApplicationManager.getApplication().invokeLater({
                                        val virtualFile = LocalFileSystem.getInstance()
                                            .findFileByNioFile(Path.of(selectUtilInfo.path))
                                        if (virtualFile != null) {
                                            OpenFileDescriptor(
                                                project,
                                                virtualFile,
                                                selectUtilInfo.offset ?: 0
                                            ).navigate(true)
                                        } else {
                                            warn(
                                                "ClickOpenUtilFileRenderer",
                                                "跳转失败 ---> virtualFile == null"
                                            )
                                        }
                                    }, ModalityState.defaultModalityState())
                                }

                                override fun mousePressed(e: MouseEvent?) {
                                }

                                override fun mouseReleased(e: MouseEvent?) {
                                }

                                override fun mouseEntered(e: MouseEvent?) {
                                }

                                override fun mouseExited(e: MouseEvent?) {
                                }
                            })
                        })
                    }
                }, "", false
            )
        )
    }

    private fun showScanningNotification(project: Project) {
        NotificationGroupManager.getInstance().showWarning(project, "正在扫描所有工具类，搜索功能暂不能用！！！")
    }
}