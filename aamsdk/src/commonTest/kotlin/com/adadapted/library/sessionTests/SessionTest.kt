package com.adadapted.library.sessionTests

import com.adadapted.library.ad.Ad
import com.adadapted.library.device.DeviceInfo
import com.adadapted.library.payload.Payload
import com.adadapted.library.session.Session
import com.adadapted.library.view.Zone
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SessionTest {
    @Test
    fun emptySessionCreated() {
        DeviceInfo.empty()
        val session = Session()
        assertEquals("", session.id)
    }

    @Test
    fun constructSession() {
        DeviceInfo.empty()
        val session = Session()
        val constructedSession = Session(session.id)

        assertEquals(constructedSession.id, session.id)
    }

    @Test
    fun sessionHasCampaigns() {
        assertTrue(buildTestSession().hasActiveCampaigns())
    }

    @Test
    fun sessionDoesNotHaveZoneAds() {
        assertTrue(buildTestSession().getZonesWithAds().isEmpty())
    }

    @Test
    fun sessionHasZoneAds() {
        val session = buildTestSession()
        val zones = mapOf<String, Zone>().plus(Pair("testZone", Zone("zoneId", listOf(Ad("testAdId", "impId", "url", "action", "actionPath", Payload())))))
        session.updateZones(zones)
        assertTrue(session.getZonesWithAds().isNotEmpty())
    }

    @Test
    fun sessionIsExpired() {
        val oldSession = Session("testId", willServeAds = true, hasAds = true, refreshTime = 1L, expiration = Clock.System.now().epochSeconds - 1)
        assertTrue(oldSession.hasExpired())
    }

    @Test
    fun sessionSetsAndRetrievesZones() {
        val session = buildTestSession()
        assertTrue(session.getZone("testZone").id == "")

        val zones = mapOf<String, Zone>().plus(Pair("testZone", Zone("zoneId", listOf())))
        session.updateZones(zones)

        assertTrue(session.getZone("testZone").id == "zoneId")
    }

    @Test
    fun sessionWillNotServeAds() {
        assertFalse(buildTestSession().willNotServeAds())
    }

    fun buildTestSession(): Session {
        return Session("testId", willServeAds = true, hasAds = true, refreshTime = 1L, expiration = Clock.System.now().epochSeconds)
    }
}