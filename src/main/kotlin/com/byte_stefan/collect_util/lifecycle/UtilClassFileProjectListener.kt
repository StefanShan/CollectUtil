package com.byte_stefan.collect_util.lifecycle

import com.byte_stefan.collect_util.Constants.CACHE_KEY
import com.byte_stefan.collect_util.UtilClassFileManager
import com.byte_stefan.collect_util.UtilInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.vfs.VirtualFileEvent
import com.intellij.openapi.vfs.VirtualFileListener

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

        //解除哑巴模式
        DumbService.getInstance(project).runWhenSmart {
            //开启扫描
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