package com.byte_stefan.collect_util.lifecycle

import com.byte_stefan.collect_util.ui.CollectUtilToolWindow
import com.byte_stefan.collect_util.Constants.TOOL_WINDOW_ID
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.RegisterToolWindowTask
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.indexing.UnindexedFilesUpdaterListener
import javax.swing.SwingUtilities

object UtilClassFileIndexedFilesListener : UnindexedFilesUpdaterListener {

    override fun updateStarted(project: Project) {

    }

    override fun updateFinished(project: Project) {
        if (ToolWindowManager.getInstance(project).toolWindowIds.contains(TOOL_WINDOW_ID))
            return

        SwingUtilities.invokeLater {
            ToolWindowManager.getInstance(project).registerToolWindow(
                RegisterToolWindowTask(
                    id = TOOL_WINDOW_ID,
                    canCloseContent = false,
                    contentFactory = CollectUtilToolWindow(),
                    icon = AllIcons.General.ExternalTools,
                    canWorkInDumbMode = false,
                    anchor = ToolWindowAnchor.BOTTOM
                )
            )
        }
    }
}