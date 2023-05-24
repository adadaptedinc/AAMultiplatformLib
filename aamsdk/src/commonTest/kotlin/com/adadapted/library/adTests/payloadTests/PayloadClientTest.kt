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
        EventClient.onSessionAvailable(mockSession)
    }

    @Test
    fun markContentAcknowledged() {
        val content = getTestAdditPayloadContent()
        PayloadClient.markContentAcknowledged(content)
        EventClient.onPublishEvents()

        assertTrue {
            MockEventAdapter.publishedSdkEvents.any {event -> event.name == EventStrings.ADDIT_ADDED_TO_LIST }
            MockEventAdapter.publishedSdkEvents.any {event -> event.params.getValue("payload_id") == "testPayloadId"}
            MockEventAdapter.publishedSdkEvents.any {event -> event.params.getValue("source") == AddItContent.AddItSources.PAYLOAD}
        }
    }

    @Test
    fun markNonPayloadContentAcknowledged() {
        val content = getTestAdditPayloadContent(isPayloadSource = false)
        PayloadClient.markContentAcknowledged(content)
        EventClient.onPublishEvents()
        assertTrue {
            MockEventAdapter.publishedSdkEvents.any {event -> event.name == EventStrings.ADDIT_ADDED_TO_LIST }
            MockEventAdapter.publishedSdkEvents.any {event -> event.params.getValue("payload_id") == "testPayloadId"}
            MockEventAdapter.publishedSdkEvents.any {event -> event.params.getValue("source") == ""}
        }
    }

    @Test
    fun markContentItemAcknowledged() {
        val content = getTestAdditPayloadContent()
        PayloadClient.markContentItemAcknowledged(content, getTestAddToListItem())
        EventClient.onPublishEvents()

        assertTrue {
            MockEventAdapter.publishedSdkEvents.any {event -> event.name == EventStrings.ADDIT_ITEM_ADDED_TO_LIST }
            MockEventAdapter.publishedSdkEvents.any {event -> event.params.getValue("payload_id") == "testPayloadId"}
            MockEventAdapter.publishedSdkEvents.any {event -> event.params.getValue("item_name") == "testTitle"}
        }
    }

    @Test
    fun markContentDuplicate() {
        val content = getTestAdditPayloadContent()
        PayloadClient.markContentDuplicate(content)
        EventClient.onPublishEvents()

        assertTrue {
            MockEventAdapter.publishedSdkEvents.any {event -> event.name == EventStrings.ADDIT_DUPLICATE_PAYLOAD }
            MockEventAdapter.publishedSdkEvents.any {event -> event.params.getValue("payload_id") == "testPayloadId"}
        }
    }

    @Test
    fun markNonPayloadContentDuplicate() {
        val content = getTestAdditPayloadContent(isPayloadSource = false)
        PayloadClient.markContentDuplicate(content)
        EventClient.onPublishEvents()
        assertTrue {
            MockEventAdapter.publishedSdkEvents.any {event -> event.name == EventStrings.ADDIT_DUPLICATE_PAYLOAD }
            MockEventAdapter.publishedSdkEvents.any {event -> event.params.getValue("payload_id") == "testPayloadId"}
        }
    }

    @Test
    fun markContentFailed() {
        val content = getTestAdditPayloadContent()
        PayloadClient.markContentFailed(content, "testFail")
        EventClient.onPublishEvents()

        assertTrue {
            MockEventAdapter.publishedSdkErrors.any {event -> event.code == EventStrings.ADDIT_CONTENT_FAILED }
            MockEventAdapter.publishedSdkErrors.any {event -> event.message == "testFail" }
        }
    }

    @Test
    fun markNonPayloadContentFailed() {
        val content = getTestAdditPayloadContent(isPayloadSource = false)
        PayloadClient.markContentFailed(content, "testFail")
        EventClient.onPublishEvents()

        assertTrue {
            MockEventAdapter.publishedSdkErrors.any {event -> event.code == EventStrings.ADDIT_CONTENT_FAILED }
            MockEventAdapter.publishedSdkErrors.any {event -> event.message == "testFail" }
        }
    }

    @Test
    fun markContentItemFailed() {
        val content = getTestAdditPayloadContent()
        PayloadClient.markContentItemFailed(content, getTestAddToListItem(), "testItemFail")
        EventClient.onPublishEvents()

        assertTrue {
            MockEventAdapter.publishedSdkErrors.any {event -> event.code == EventStrings.ADDIT_CONTENT_ITEM_FAILED }
            MockEventAdapter.publishedSdkErrors.any {event -> event.message == "testItemFail" }
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
