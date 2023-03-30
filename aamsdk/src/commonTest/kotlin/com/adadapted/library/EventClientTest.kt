package com.adadapted.library

import com.adadapted.library.ad.Ad
import com.adadapted.library.concurrency.TransporterCoroutineScope
import com.adadapted.library.constants.EventStrings
import com.adadapted.library.device.DeviceInfo
import com.adadapted.library.event.*
import com.adadapted.library.log.AALogger
import com.adadapted.library.session.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class EventClientTest {
    var testTransporter = UnconfinedTestDispatcher()
    val testTransporterScope: TransporterCoroutineScope = TestTransporter(testTransporter)
    var mockSession = Session("testId", true, true, 30, 1907245044, mutableMapOf())
    var testAd = Ad("adId", "zoneId", "impId")

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testTransporter)
        AALogger.disableAllLogging()
        EventClient.createInstance(MockEventAdapter, testTransporterScope)
    }

    @Test
    fun assertEventClientEventsAreLoggedCorrectly() {
        mockSession.deviceInfo = DeviceInfo(isAllowRetargetingEnabled = false)
        EventClient.onSessionAvailable(mockSession)
        EventClient.onSessionExpired()
        EventClient.trackImpression(testAd)
        EventClient.trackInteraction(testAd)
        EventClient.trackInvisibleImpression(testAd)
        EventClient.trackPopupBegin(testAd)
        EventClient.onPublishEvents()
        assertTrue {
            MockEventAdapter.publishedSdkErrors.any { error -> error.code == EventStrings.GAID_UNAVAILABLE }
            MockEventAdapter.publishedSdkErrors.any { error -> error.code == EventStrings.GAID_UNAVAILABLE }
            MockEventAdapter.publishedAdEvents.any { event -> event.eventType == AdEventTypes.IMPRESSION }
            MockEventAdapter.publishedAdEvents.any { event -> event.eventType == AdEventTypes.INTERACTION }
            MockEventAdapter.publishedAdEvents.any { event -> event.eventType == AdEventTypes.INVISIBLE_IMPRESSION }
        }
    }

    @AfterTest
    fun cleanup() {
        MockEventAdapter.cleanupEvents()
    }
}
