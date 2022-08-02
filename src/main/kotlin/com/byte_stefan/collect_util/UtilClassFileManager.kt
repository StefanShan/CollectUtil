package com.byte_stefan.collect_util

import com.google.gson.annotations.SerializedName
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import org.jetbrains.annotations.NonNls
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElement
import java.util.concurrent.ConcurrentHashMap


object UtilClassFileManager {

    private var isScanning: Boolean = false
    private val markUtilClassMap = ConcurrentHashMap<String, List<UtilInfo>>()
    val supportLanguages = arrayListOf("java", "kt")

    fun setCacheMap(cache: Map<String, List<UtilInfo>>) {
        markUtilClassMap.putAll(cache)
    }

    fun getMarkUtilClassMap(): Map<String, List<UtilInfo>> = markUtilClassMap.toMap()

    fun getMarkUtilClassList(): List<UtilInfo> = markUtilClassMap.values.flatten()

    fun isScanning(): Boolean = isScanning

    fun scanUtilClass(project: Project) {
        if (isScanning) return
        val task = object : Task.Backgroundable(project, "scanUtilClass") {
            override fun run(indicator: ProgressIndicator) {
                isScanning = true
                ModuleManager.getInstance(project).modules.forEach {
                    ApplicationManager.getApplication().runReadAction {
                        supportLanguages.forEach { suffixStr ->
                            FilenameIndex.getAllFilesByExt(project, suffixStr, GlobalSearchScope.moduleScope(it))
                                .forEach { virtualFile ->
                                    scanVirtualFile(project, virtualFile)
                                }
                        }
                    }
                }
            }

            override fun onFinished() {
                super.onFinished()
                isScanning = false
            }

        }
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, BackgroundableProcessIndicator(task))
    }

    fun scanVirtualFile(project: Project, virtualFile: VirtualFile) {
        val moduleName = ModuleUtil.findModuleForFile(virtualFile, project)?.name
        val filePath = virtualFile.path
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: return
        PsiTreeUtil.collectElements(psiFile) { element ->
            element is PsiComment
        }.filter { psiComment ->
            psiComment.text.contains("@utilDesc")
        }.let {
            if (it.isEmpty()) {
                markUtilClassMap.remove(psiFile.name)
                return
            }
            val utilInfoList = mutableListOf<UtilInfo>()
            it.forEach { psiComment ->
                val descContent = Regex("(@utilDesc)(.*)").find(psiComment.text)?.groupValues?.lastOrNull()
                val parentUElement = psiComment.parent.toUElement()
                if (parentUElement is UClass) {
                    collectClassUtil((parentUElement.javaPsi), descContent, utilInfoList, moduleName, filePath)
                } else if (parentUElement is UMethod) {
                    collectMethodUtil(parentUElement, psiComment, descContent, utilInfoList, moduleName, filePath)
                }
            }
            markUtilClassMap[psiFile.name] = utilInfoList
        }
    }

    private fun collectMethodUtil(
        parentUElement: UMethod,
        psiComment: PsiElement,
        descContent: String?,
        utilInfoList: MutableList<UtilInfo>,
        moduleName: @NlsSafe String?,
        filePath: @NonNls String
    ) {
        val methodName = parentUElement.name
        val belong2ClassName = psiComment.parentOfType<PsiClass>()?.name
        if (descContent != null) {
            utilInfoList.add(
                UtilInfo(
                    belong2ClassName,
                    methodName,
                    moduleName,
                    descContent,
                    filePath,
                    psiComment.textOffset
                )
            )
        }
    }

    private fun collectClassUtil(
        parentUElement: PsiClass,
        descContent: String?,
        utilInfoList: MutableList<UtilInfo>,
        moduleName: @NlsSafe String?,
        filePath: @NonNls String
    ) {
        val className = parentUElement.name
        if (descContent != null) {
            utilInfoList.add(
                UtilInfo(
                    className = className,
                    moduleName = moduleName,
                    desc = descContent,
                    path = filePath,
                    offset = parentUElement.textOffset
                )
            )
        }
    }
}

data class UtilInfo(
    @SerializedName("className") val className: String? = null,
    @SerializedName("methodName") val methodName: String? = null,
    @SerializedName("moduleName") val moduleName: String? = null,
    @SerializedName("desc") val desc: String? = null,
    @SerializedName("path") val path: String? = null,
    @SerializedName("offset") val offset: Int? = 0
) : java.io.Serializable