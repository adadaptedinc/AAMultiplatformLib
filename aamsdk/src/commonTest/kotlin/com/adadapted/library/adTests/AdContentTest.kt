package com.adadapted.library.adTests

import com.adadapted.library.MockEventAdapter
import com.adadapted.library.TestTransporter
import com.adadapted.library.ad.Ad
import com.adadapted.library.ad.AdContent
import com.adadapted.library.atl.AddToListItem
import com.adadapted.library.concurrency.TransporterCoroutineScope
import com.adadapted.library.constants.EventStrings
import com.adadapted.library.event.AdEventTypes
import com.adadapted.library.event.EventClient
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

@OptIn(ExperimentalCoroutinesApi::class)
class AdContentTest {
    var testTransporter = UnconfinedTestDispatcher()
    val testTransporterScope: TransporterCoroutineScope = TestTransporter(testTransporter)
    var mockSession = Session("testId", true, true, 30, 1907245044, mutableMapOf())
    var testAd = Ad("adId", "zoneId", "impId")

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testTransporter)
        AALogger.disableAllLogging()
        EventClient.createInstance(MockEventAdapter, testTransporterScope)
        EventClient.onSessionAvailable(mockSession)
    }

    @Test
    fun acknowledgesAndFailuresAreProperlyTracked() {
        val testAdContent = AdContent.createAddToListContent(testAd)
        val testAtl = AddToListItem("id","testAtl")
        testAdContent.acknowledge()
        testAdContent.itemAcknowledge(testAtl)
        testAdContent.failed("test content failed")
        testAdContent.itemFailed(testAtl, "test item failed")
        EventClient.onPublishEvents()
        assertTrue {
            MockEventAdapter.publishedAdEvents.any { event -> event.eventType == AdEventTypes.INTERACTION }
            MockEventAdapter.publishedSdkEvents.any { event -> event.name == EventStrings.ATL_ITEM_ADDED_TO_LIST }
            MockEventAdapter.publishedSdkErrors.any { error -> error.code == EventStrings.ATL_ADDED_TO_LIST_FAILED }
            MockEventAdapter.publishedSdkErrors.any { error -> error.code == EventStrings.ATL_ADDED_TO_LIST_ITEM_FAILED }
        }

        val count = MockEventAdapter.publishedAdEvents.count { event -> event.eventType == AdEventTypes.INTERACTION }
        assertTrue { count == 1 }
    }

    @AfterTest
    fun cleanup() {
        MockEventAdapter.cleanupEvents()
    }
}