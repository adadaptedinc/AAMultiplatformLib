package com.adadapted.library.sessionTests

import com.adadapted.library.TestTransporter
import com.adadapted.library.concurrency.TransporterCoroutineScope
import com.adadapted.library.device.DeviceInfo
import com.adadapted.library.interfaces.AdGetListener
import com.adadapted.library.interfaces.SessionAdapter
import com.adadapted.library.interfaces.SessionInitListener
import com.adadapted.library.log.AALogger
import com.adadapted.library.session.Session
import com.adadapted.library.session.SessionClient
import com.adadapted.library.session.SessionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@ExperimentalCoroutinesApi
class SessionClientTest {
    var testTransporter = UnconfinedTestDispatcher()
    val testTransporterScope: TransporterCoroutineScope = TestTransporter(testTransporter)
    private var testSessionClient = SessionClient
    private var mockSessionAdapter = TestSessionAdapter()
    private lateinit var testListener: SessionListener
    private var onSessionAvailHit = false

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testTransporter)
        AALogger.disableAllLogging()
        testSessionClient.createInstance(mockSessionAdapter, testTransporterScope)
        testSessionClient.onSessionInitialized(SessionTest().buildTestSession())
        testListener = object : SessionListener {
            override fun onSessionAvailable(session: Session) {
                onSessionAvailHit = true
            }

            override fun onAdsAvailable(session: Session) {
            }

            override fun onSessionInitFailed() {
            }
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createInstance() {
        assertNotNull(testSessionClient)
    }

    @Test
    fun start() {
        val testListener = TestSessionClientListener()
        testSessionClient.start(testListener)
        assertTrue(testListener.getTrackedSession()?.id == "SessionAvailable")
    }

    @Test
    fun addListener() {
        testSessionClient.addListener(testListener)
        assertTrue(onSessionAvailHit)
    }

    @Test
    fun removeListener() {
        val testListener = TestSessionClientListener()
        testSessionClient.addListener(testListener)
        assertNotNull(testListener.getTrackedSession())

        testSessionClient.removeListener(testListener)
        testSessionClient.onNewAdsLoaded(Session())
        assertTrue(testListener.getTrackedSession()?.id != "AdsAvailable")
    }

    @Test
    fun addRemovePresenter() {
        val testListener = TestSessionClientListener()
        testSessionClient.addPresenter(testListener)
        assertNotNull(testListener.getTrackedSession())

        testSessionClient.removePresenter(testListener)
        testSessionClient.onNewAdsLoaded(Session())
        assertTrue(testListener.getTrackedSession()?.id != "AdsAvailable")
    }

    @Test
    fun onSessionInitializeFailed() {
        val testListener = TestSessionClientListener()
        testSessionClient.start(testListener)
        testSessionClient.onSessionInitializeFailed()
        assertTrue(testListener.getTrackedSession()?.id == "SessionFailed")
    }

    @Test
    fun onNewAdsLoaded() {
        val testListener = TestSessionClientListener()
        testSessionClient.addListener(testListener)
        testSessionClient.onNewAdsLoaded(Session())
        assertTrue(testListener.getTrackedSession()?.id == "AdsAvailable")
    }

    @Test
    fun onNewAdsLoadFailed() {
        val testListener = TestSessionClientListener()
        testSessionClient.start(testListener)
        testSessionClient.onNewAdsLoadFailed()
        assertTrue(testListener.getTrackedSession()?.id == "AdsAvailable")
    }
}

class TestSessionClientListener: SessionListener {
    private var trackedSession: Session? = null

    fun getTrackedSession(): Session? {
        return trackedSession
    }

    override fun onPublishEvents() {
        trackedSession = Session("EventsPublished")
    }

    override fun onSessionAvailable(session: Session) {
        trackedSession = Session("SessionAvailable")
    }

    override fun onAdsAvailable(session: Session) {
        trackedSession = Session("AdsAvailable")
    }

    override fun onSessionInitFailed() {
        trackedSession = Session("SessionFailed")
    }
}

class TestSessionAdapter: SessionAdapter {
    var initSent = false
    var adsRefreshed = false

    override suspend fun sendInit(deviceInfo: DeviceInfo, listener: SessionInitListener) {
        initSent = true
    }

    override suspend fun sendRefreshAds(session: Session, listener: AdGetListener) {
       adsRefreshed = true
    }

    fun reset() {
        initSent = false
        adsRefreshed = false
    }
}
