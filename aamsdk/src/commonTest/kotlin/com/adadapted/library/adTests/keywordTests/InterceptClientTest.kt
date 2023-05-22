package com.adadapted.library.adTests.keywordTests

import com.adadapted.library.sessionTests.TestSessionAdapter
import com.adadapted.library.TestTransporter
import com.adadapted.library.concurrency.TransporterCoroutineScope
import com.adadapted.library.interfaces.InterceptListener
import com.adadapted.library.keyword.Intercept
import com.adadapted.library.keyword.InterceptAdapter
import com.adadapted.library.keyword.InterceptClient
import com.adadapted.library.keyword.InterceptEvent
import com.adadapted.library.session.Session
import com.adadapted.library.session.SessionClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@ExperimentalCoroutinesApi
class InterceptClientTest {

    var testTransporter = UnconfinedTestDispatcher()
    val testTransporterScope: TransporterCoroutineScope = TestTransporter(testTransporter)
    private var testInterceptClient = InterceptClient
    private var testInterceptAdapter = TestInterceptAdapter()
    private var mockSessionAdapter = TestSessionAdapter()
    private val testEvent = InterceptEvent("testId", "testTermId", "testTerm", "testInput")
    private var mockSession = Session("testId", willServeAds = true, hasAds = true, refreshTime = 30, expiration = 10000000, zones = mutableMapOf())

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testTransporter)
        SessionClient.createInstance(mockSessionAdapter, testTransporterScope)
        testInterceptClient.createInstance(testInterceptAdapter, testTransporterScope)
        testInterceptClient.getInstance().onSessionAvailable(mockSession)
    }

    @Test
    fun createInstance() {
        assertNotNull(testInterceptClient.getInstance())
    }

    @Test
    fun initialize() {
        var keywordInit = false
        val mockListener = object:InterceptListener {
            override fun onKeywordInterceptInitialized(intercept: Intercept) {
                keywordInit = true
            }

        }
        testInterceptClient.getInstance().initialize(mockSession, mockListener)
        assertTrue(keywordInit)
    }

    @Test
    fun trackMatched() {
        testInterceptClient.getInstance().trackMatched(testEvent.searchId, testEvent.termId, testEvent.term, testEvent.userInput)
        testInterceptClient.getInstance().onPublishEvents()

        assertEquals(InterceptEvent.MATCHED, testInterceptAdapter.testEvents.first().event)
    }

    @Test
    fun trackPresented() {
        testInterceptClient.getInstance().trackPresented(testEvent.searchId, testEvent.termId, testEvent.term, testEvent.userInput)
        testInterceptClient.getInstance().onPublishEvents()

        assertEquals(InterceptEvent.PRESENTED, testInterceptAdapter.testEvents.first().event)
    }

    @Test
    fun trackSelected() {
        testInterceptClient.getInstance().trackSelected(testEvent.searchId, testEvent.termId, testEvent.term, testEvent.userInput)
        testInterceptClient.getInstance().onPublishEvents()

        assertEquals(InterceptEvent.SELECTED, testInterceptAdapter.testEvents.first().event)
    }

    @Test
    fun trackNotMatched() {
        testInterceptClient.getInstance().trackNotMatched(testEvent.searchId, testEvent.userInput)
        testInterceptClient.getInstance().onPublishEvents()

        assertEquals(InterceptEvent.NOT_MATCHED, testInterceptAdapter.testEvents.first().event)
    }
}

class TestInterceptAdapter: InterceptAdapter {
    var testEvents = mutableSetOf<InterceptEvent>()
    var testIntercept = Intercept()
    override suspend fun retrieve(session: Session, listener: InterceptAdapter.Listener) {
        listener.onSuccess(testIntercept)
    }

    override suspend fun sendEvents(session: Session, events: MutableSet<InterceptEvent>) {
        testEvents = events
    }
}
