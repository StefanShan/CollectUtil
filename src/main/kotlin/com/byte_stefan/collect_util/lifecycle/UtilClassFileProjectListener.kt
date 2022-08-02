package com.byte_stefan.collect_util.lifecycle

import com.byte_stefan.collect_util.Constants.CACHE_KEY
import com.byte_stefan.collect_util.Constants.TOOL_WINDOW_ID
import com.byte_stefan.collect_util.UtilClassFileManager
import com.byte_stefan.collect_util.UtilInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.ide.SaveAndSyncHandler
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.vfs.VirtualFileEvent
import com.intellij.openapi.vfs.VirtualFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ex.ToolWindowManagerListener

object UtilClassFileProjectListener : ProjectManagerListener {
    private lateinit var mProject: Project

    private val mFileChangeListener: VirtualFileListener = object : VirtualFileListener {
        override fun contentsChanged(event: VirtualFileEvent) {
            UtilClassFileManager.supportLanguages.forEach {
                if (event.fileName.contains(".$it")) {
                    ReadAction.run<Throwable> {
                        UtilClassFileManager.scanVirtualFile(mProject, event.file)
                    }
                }
            }
        }
    }

    override fun projectOpened(project: Project) {
        mProject = project
        //读取缓存
        val cacheJson = PropertiesComponent.getInstance(project).getValue(CACHE_KEY)
        if (cacheJson.isNullOrEmpty().not()) {
            try {
                val cacheMap = Gson().fromJson<HashMap<String, List<UtilInfo>>>(
                    cacheJson,
                    object : TypeToken<HashMap<String, List<UtilInfo>>>() {}.type
                )
                UtilClassFileManager.setCacheMap(cacheMap)
            } catch (e: Exception) {
                com.byte_stefan.collect_util.util.error("UtilClassFileProjectListener", "jsonFormatError = $e")
            }
        }

        //注册监听
        project.projectFile?.fileSystem?.addVirtualFileListener(mFileChangeListener)

        //窗口显示时，将内存数据存至文本
        project.messageBus.connect().subscribe(ToolWindowManagerListener.TOPIC, object : ToolWindowManagerListener {
            override fun toolWindowShown(toolWindow: ToolWindow) {
                super.toolWindowShown(toolWindow)
                if (toolWindow.id == TOOL_WINDOW_ID) {
                    //refreshFile
                    FileDocumentManager.getInstance().saveAllDocuments()
                    SaveAndSyncHandler.getInstance().refreshOpenFiles()
                    VirtualFileManager.getInstance().refreshWithoutFileWatcher(true)
                }
            }
        })

        //接触哑巴模式后开启扫描
        DumbService.getInstance(project).runWhenSmart {
            if (UtilClassFileManager.getMarkUtilClassMap().isEmpty()) {
                UtilClassFileManager.scanUtilClass(project)
            }
        }
    }

    override fun projectClosed(project: Project) {
        //存储缓存
        PropertiesComponent.getInstance(project)
            .setValue(CACHE_KEY, Gson().toJson(UtilClassFileManager.getMarkUtilClassMap()))
        project.projectFile?.fileSystem?.removeVirtualFileListener(mFileChangeListener)
    }
}