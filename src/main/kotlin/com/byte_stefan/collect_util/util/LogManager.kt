package com.byte_stefan.collect_util.util

import java.util.logging.Level
import java.util.logging.Logger

object LogManager {
    var DEFAULT_TAG = "LogManager"
    private var CATEGORY = "com.stefan.plugin"

    private val logger: Logger = Logger.getLogger(CATEGORY)

    fun getInstance(): Logger {
        return logger
    }
}

fun debug(tag: String? = LogManager.DEFAULT_TAG, message: String) {
    LogManager.getInstance().log(Level.INFO, "$tag --> $message")
}

fun warn(tag: String? = LogManager.DEFAULT_TAG, message: String) {
    LogManager.getInstance().log(Level.WARNING, "$tag --> $message")
}

fun error(tag: String? = LogManager.DEFAULT_TAG, message: String) {
    LogManager.getInstance().log(Level.SEVERE, "$tag --> $message")
}

