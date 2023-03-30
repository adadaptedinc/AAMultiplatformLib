package com.adadapted.library.payload

import com.adadapted.library.device.DeviceInfo
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

object PayloadRequestBuilder {
    fun buildRequest(deviceInfo: DeviceInfo): PayloadRequest {
        deviceInfo.run {
            return PayloadRequest(
                appId,
                udid,
                bundleId,
                bundleVersion,
                deviceName,
                os,
                osv,
                sdkVersion,
                Clock.System.now().epochSeconds
            )
        }
    }

    fun buildEventRequest(deviceInfo: DeviceInfo, event: PayloadEvent): PayloadEventRequest {
        val tracking = JsonArray(
            listOf(
                JsonObject(
                    mapOf(
                        "payload_id" to JsonPrimitive(event.payloadId),
                        "status" to JsonPrimitive(event.status),
                        "event_timestamp" to JsonPrimitive(event.timestamp)
                    )
                )
            )
        )
        deviceInfo.run {
            return PayloadEventRequest(
                appId,
                udid,
                bundleId,
                bundleVersion,
                deviceName,
                os,
                osv,
                sdkVersion,
                tracking
            )
        }
    }
}