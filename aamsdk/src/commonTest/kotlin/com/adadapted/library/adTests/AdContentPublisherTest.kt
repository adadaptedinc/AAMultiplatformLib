package com.adadapted.library.adTests

import com.adadapted.library.ad.Ad
import com.adadapted.library.ad.AdContent
import com.adadapted.library.ad.AdContentListener
import com.adadapted.library.ad.AdContentPublisher
import com.adadapted.library.atl.AddToListContent
import com.adadapted.library.atl.AddToListItem
import com.adadapted.library.payload.Payload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class AdContentPublisherTest {
    private var testTransporter = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testTransporter)
    }

    @Test
    fun listenerIsAddedAndPublished() {
        val testListener = TestAdContentListener()
        AdContentPublisher.addListener(testListener)
        AdContentPublisher.publishContent(
            "testZoneId",
            AdContent.createAddToListContent(
                Ad("adId", "adZoneId",
                    payload = Payload(
                        detailedListItems = listOf(
                            AddToListItem("track", "title", "brand", "cat", "upc", "sku", "discount", "image"
                            )
                        )
                    )
                )
            )
        )
        assertEquals("testZoneId", testListener.resultZoneId)
        assertEquals("adZoneId", (testListener.resultContent as AdContent).zoneId)
    }

    @Test
    fun listenerIsRemovedAndPublished() {
        val testListener = TestAdContentListener()
        AdContentPublisher.addListener(testListener)
        AdContentPublisher.removeListener(testListener)
        AdContentPublisher.publishContent(
            "testZoneId",
            AdContent.createAddToListContent(
                Ad("adId", "adZoneId",
                    payload = Payload(
                        detailedListItems = listOf(
                            AddToListItem("track", "title", "brand", "cat", "upc", "sku", "discount", "image"
                            )
                        )
                    )
                )
            )
        )
        assertEquals("", testListener.resultZoneId)
        assertNull(testListener.resultContent)
    }
}

class TestAdContentListener : AdContentListener {
    var resultZoneId = ""
    var resultContent: AddToListContent? = null

    override fun onContentAvailable(zoneId: String, content: AddToListContent) {
        resultZoneId = zoneId
        resultContent = content
    }
}