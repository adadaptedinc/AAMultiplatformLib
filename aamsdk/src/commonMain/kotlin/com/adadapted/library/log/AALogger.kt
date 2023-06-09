package com.adadapted.library.log

import com.adadapted.library.constants.Config
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object AALogger {
    private var isDebugLoggingEnabled = false
    private var disableLogging = false

    fun logDebug(message: String) {
        if (isDebugLoggingEnabled && !disableLogging) {
            Napier.d(message = message, tag = Config.LOG_TAG)
        }
    }

    fun logInfo(message: String) {
        if (!disableLogging)Napier.i(message = message, tag = Config.LOG_TAG)
    }

    fun logError(message: String) {
        if (!disableLogging)Napier.e(message = message, tag = Config.LOG_TAG)
    }

    fun enableDebugLogging() {
        isDebugLoggingEnabled = true
    }

    fun disableAllLogging() {
        disableLogging = true
    }

    init {
        Napier.base(DebugAntilog())
    }
}