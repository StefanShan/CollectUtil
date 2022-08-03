package com.byte_stefan.collect_util.util

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

fun NotificationGroupManager.showWarning(project: Project, content: String, groupId: String? = null) {
    NotificationGroupManager.getInstance().show(project, content, NotificationType.WARNING, groupId)
}

fun NotificationGroupManager.showInformation(project: Project, content: String, groupId: String? = null) {
    NotificationGroupManager.getInstance().show(project, content, NotificationType.INFORMATION, groupId)
}

fun NotificationGroupManager.showError(project: Project, content: String, groupId: String? = null) {
    NotificationGroupManager.getInstance().show(project, content, NotificationType.ERROR, groupId)
}

private fun NotificationGroupManager.show(
    project: Project,
    content: String,
    type: NotificationType,
    groupId: String? = null
) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup(groupId ?: "BALLOON Notification Group")
        .createNotification(
            content,
            type
        )
        .notify(project)
}