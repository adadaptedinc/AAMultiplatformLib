package com.adadapted.library.adTests.payloadTests

import com.adadapted.library.MockEventAdapter
import com.adadapted.library.MockPayloadAdapter
import com.adadapted.library.TestTransporter
import com.adadapted.library.atl.AddItContent
import com.adadapted.library.atl.AddToListItem
import com.adadapted.library.concurrency.TransporterCoroutineScope
import com.adadapted.library.constants.EventStrings
import com.adadapted.library.event.EventClient
import com.adadapted.library.log.AALogger
import com.adadapted.library.payload.PayloadClient
import com.adadapted.library.session.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@ExperimentalCoroutinesApi
class PayloadClientTest {
    var testTransporter = UnconfinedTestDispatcher()
    val testTransporterScope: TransporterCoroutineScope = TestTransporter(testTransporter)
    var mockSession = Session("testId", true, true, 30, 1907245044, mutableMapOf())

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testTransporter)
        AALogger.disableAllLogging()
        EventClient.createInstance(MockEventAdapter, testTransporterScope)
        PayloadClient.createInstance(MockPayloadAdapter, EventClient, testTransporterScope)
    }

    @Test
    fun markContentAcknowledged() {
        val content = getTestAdditPayloadContent()
        EventClient.onSessionAvailable(mockSession)
        PayloadClient.markContentAcknowledged(content)
        EventClient.onPublishEvents()

        assertTrue {
            MockEventAdapter.publishedSdkEvents.any {event -> event.name == EventStrings.ADDIT_ADDED_TO_LIST }
            MockEventAdapter.publishedSdkEvents.any {event -> event.params.getValue("payload_id") == "testPayloadId"}
            MockEventAdapter.publishedSdkEvents.any {event -> event.params.getValue("source") == AddItContent.AddItSources.PAYLOAD}
        }
    }

    @AfterTest
    fun cleanup() {
        MockEventAdapter.cleanupEvents()
        MockPayloadAdapter.cleanupEvents()
    }

    companion object {
        fun getTestAdditPayloadContent(isPayloadSource: Boolean = true): AddItContent {
            return AddItContent("testPayloadId", "testMessage", "image", 0, "source", if (isPayloadSource) { AddItContent.AddItSources.PAYLOAD } else "", mutableListOf(getTestAddToListItem()))
        }

        fun getTestAddToListItem(): AddToListItem {
            return AddToListItem(
                "testTrackId",
                "testTitle",
                "testBrand",
                "testCategory",
                "testUPC",
                "testSKU",
                "testDiscount",
                "testImage")
        }
    }
}
