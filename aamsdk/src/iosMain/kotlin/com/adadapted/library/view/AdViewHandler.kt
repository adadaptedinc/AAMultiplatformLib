package com.adadapted.library.view

import com.adadapted.library.ad.Ad
import com.adadapted.library.constants.Config
import io.ktor.client.request.*
import io.ktor.http.*

actual class AdViewHandler {
    actual fun handleLink(ad: Ad) {
        Popup(ad = ad, linkType = LinkType.EXTERNAL)
    }

    actual fun handlePopup(ad: Ad) {
        Popup(ad = ad, linkType = LinkType.POP_UP)
    }

    actual fun handleReportAd(adId: String, udid: String) {
        Popup(adId = adId, udid = udid)
    }
}