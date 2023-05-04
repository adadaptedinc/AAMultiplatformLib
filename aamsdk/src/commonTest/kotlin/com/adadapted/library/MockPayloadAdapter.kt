package com.adadapted.library

import com.adadapted.library.atl.AddItContent
import com.adadapted.library.device.DeviceInfo
import com.adadapted.library.payload.PayloadAdapter
import com.adadapted.library.payload.PayloadEvent
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object MockPayloadAdapter: PayloadAdapter {
    var publishedPayloadEvents = mutableListOf<PayloadEvent>()

    override suspend fun pickup(deviceInfo: DeviceInfo, callback: (content: List<AddItContent>) -> Unit) {
        callback.invoke(listOf())
    }

    override suspend fun publishEvent(deviceInfo: DeviceInfo, event: PayloadEvent) {
        publishedPayloadEvents.add(event)
    }

    fun cleanupEvents() {
        publishedPayloadEvents.clear()
    }
}
