package com.adadapted.library.view

import com.adadapted.library.ad.Ad
import com.adadapted.library.ad.AdActionType
import com.adadapted.library.session.Session
import com.adadapted.library.session.SessionListener
import com.adadapted.library.ad.AdContentPublisher
import com.adadapted.library.concurrency.Timer
import com.adadapted.library.constants.EventStrings
import com.adadapted.library.event.EventClient
import com.adadapted.library.log.AALogger
import com.adadapted.library.session.SessionClient
import kotlinx.datetime.Clock

interface AdZonePresenterListener {
    fun onZoneAvailable(zone: Zone)
    fun onAdsRefreshed(zone: Zone)
    fun onAdAvailable(ad: Ad)
    fun onNoAdAvailable()
}

class AdZonePresenter(private val adViewHandler: AdViewHandler, private val sessionClient: SessionClient?) : SessionListener {
    private var currentAd: Ad = Ad()
    private var zoneId: String = ""
    var adZonePresenterListener: AdZonePresenterListener? = null
    private var attached: Boolean
    private var sessionId: String? = null
    private var zoneLoaded: Boolean
    private var currentZone: Zone
    private var randomAdStartPosition: Int
    private var adStarted = false
    private var adCompleted = false
    private var timerRunning = false
    private lateinit var timer: Timer
    private val eventClient: EventClient = EventClient

    fun init(zoneId: String) {
        if (this.zoneId.isEmpty()) {
            this.zoneId = zoneId
        }
    }

    fun onAttach(adZonePresenterListener: AdZonePresenterListener?) {
        if (adZonePresenterListener == null) {
            AALogger.logError("NULL Listener provided")
            return
        }
        if (!attached) {
            attached = true
            this.adZonePresenterListener = adZonePresenterListener
            sessionClient?.addPresenter(this)
            setNextAd()
        }
    }

    fun onDetach() {
        if (attached) {
            attached = false
            adZonePresenterListener = null
            completeCurrentAd()
            sessionClient?.removePresenter(this)
        }
    }

    private fun setNextAd() {
        if (!zoneLoaded || sessionClient?.hasStaleAds() == true) {
            return
        }
        completeCurrentAd()

        currentAd = if (adZonePresenterListener != null && currentZone.hasAds()) {
            val adPosition = randomAdStartPosition % currentZone.ads.size
            randomAdStartPosition++
            currentZone.ads[adPosition]
        } else {
            Ad()
        }

        adStarted = false
        adCompleted = false
        displayAd()
    }

    private fun displayAd() {
        if (currentAd.isEmpty) {
            notifyNoAdAvailable()
        } else {
            notifyAdAvailable(currentAd)
        }
    }

    private fun completeCurrentAd() {
        if (!currentAd.isEmpty && adStarted && !adCompleted) {
            if (!currentAd.impressionWasTracked()) {
                eventClient.trackInvisibleImpression(currentAd)
            }
            currentAd.resetImpressionTracking() //this is critical to make sure rotating ads can get more than one impression (total)
            adCompleted = true
        }
    }

    fun onAdDisplayed(ad: Ad, isAdVisible: Boolean) {
        startZoneTimer()
        adStarted = true
        trackAdImpression(ad, isAdVisible)
    }

    fun onAdVisibilityChanged(isAdVisible: Boolean) {
        trackAdImpression(currentAd, isAdVisible)
    }

    fun onAdDisplayFailed() {
        startZoneTimer()
        adStarted = true
        currentAd = Ad()
    }

    fun onBlankDisplayed() {
        startZoneTimer()
        adStarted = true
        currentAd = Ad()
    }

    fun onAdClicked(ad: Ad) {
        val actionType = ad.actionType
        val params: MutableMap<String, String> = HashMap()
        params["ad_id"] = ad.id

        when (actionType) {
            AdActionType.CONTENT -> {
                eventClient.trackSdkEvent(EventStrings.ATL_AD_CLICKED, params)
                handleContentAction(ad)
            }
            AdActionType.LINK, AdActionType.EXTERNAL_LINK -> {
                eventClient.trackInteraction(ad)
                handleLinkAction(ad)
            }
            AdActionType.POPUP -> {
                eventClient.trackInteraction(ad)
                handlePopupAction(ad)
            }
            AdActionType.CONTENT_POPUP -> {
                eventClient.trackSdkEvent(EventStrings.POPUP_AD_CLICKED, params)
                handlePopupAction(ad)
            }
            else -> AALogger.logError("AdZonePresenter Cannot handle Action type: $actionType")
        }

        cycleToNextAdIfPossible()
    }

    private fun trackAdImpression(ad: Ad, isAdVisible: Boolean) {
        if (!isAdVisible || ad.impressionWasTracked() || ad.isEmpty) return
        ad.setImpressionTracked()
        eventClient.trackImpression(ad)
    }

    private fun startZoneTimer() {
        if (!zoneLoaded || timerRunning) {
            return
        }
        val timerDelay = currentAd.refreshTime * 1000
        timerRunning = true
        timer = Timer({
            setNextAd()
        }, timerDelay, timerDelay)
    }

    private fun cycleToNextAdIfPossible() {
        if (currentZone.ads.count() > 1) {
            restartTimer()
            setNextAd()
        }
    }

    private fun restartTimer() {
        if (::timer.isInitialized) {
            timer.cancelTimer()
            timerRunning = false
            startZoneTimer()
        }
    }

    private fun handleContentAction(ad: Ad) {
        val zoneId = ad.zoneId
        AdContentPublisher.publishContent(zoneId, ad.getContent())
    }

    private fun handleLinkAction(ad: Ad) {
        adViewHandler.handleLink(ad)
    }

    private fun handlePopupAction(ad: Ad) {
        adViewHandler.handlePopup(ad)
    }

    private fun notifyZoneAvailable() {
        adZonePresenterListener?.onZoneAvailable(currentZone)
    }

    private fun notifyAdsRefreshed() {
        adZonePresenterListener?.onAdsRefreshed(currentZone)
    }

    private fun notifyAdAvailable(ad: Ad) {
        adZonePresenterListener?.onAdAvailable(ad)
    }

    private fun notifyNoAdAvailable() {
        AALogger.logInfo("No ad available")
        adZonePresenterListener?.onNoAdAvailable()
    }

    private fun updateSessionId(sessionId: String): Boolean {
        if (this.sessionId == null || this.sessionId != sessionId) {
            this.sessionId = sessionId
            return true
        }
        return false
    }

    private fun updateCurrentZone(zone: Zone) {
        zoneLoaded = true
        currentZone = zone
        restartTimer()

        if (currentAd.isEmpty) {
            setNextAd()
        }
    }

    override fun onSessionAvailable(session: Session) {
        if (zoneId.isEmpty()) {
            AALogger.logError("AdZoneId is empty. Was onStop() called outside the host view's overriding function?")
        }
        updateCurrentZone(session.getZone(zoneId))
        if (updateSessionId(session.id)) {
            notifyZoneAvailable()
        }
    }

    override fun onAdsAvailable(session: Session) {
        updateCurrentZone(session.getZone(zoneId))
        notifyAdsRefreshed()
    }

    override fun onSessionInitFailed() {
        updateCurrentZone(Zone())
    }

    init {
        attached = false
        zoneLoaded = false
        currentZone = Zone()
        randomAdStartPosition = (Clock.System.now().epochSeconds.toInt() % 10)
    }
}