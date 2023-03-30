package com.adadapted.library

import com.adadapted.library.event.AdEvent
import com.adadapted.library.event.EventAdapter
import com.adadapted.library.event.SdkError
import com.adadapted.library.event.SdkEvent
import com.adadapted.library.session.Session
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object MockEventAdapter: EventAdapter {
    var publishedAdEvents = mutableListOf<AdEvent>()
    var publishedSdkEvents = mutableListOf<SdkEvent>()
    var publishedSdkErrors = mutableListOf<SdkError>()

    override suspend fun publishAdEvents(session: Session, adEvents: Set<AdEvent>) {
        publishedAdEvents.addAll(adEvents)
    }

    override suspend fun publishSdkEvents(session: Session, events: Set<SdkEvent>) {
        publishedSdkEvents.addAll(events)
    }

    override suspend fun publishSdkErrors(session: Session, errors: Set<SdkError>) {
        publishedSdkErrors.addAll(errors)
    }

    fun cleanupEvents() {
        publishedAdEvents.clear()
        publishedSdkEvents.clear()
        publishedSdkErrors.clear()
    }
}