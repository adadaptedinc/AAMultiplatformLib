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
        println(buildReportAdUrl(adId, udid))
    }

    private fun buildReportAdUrl(adId: String, udid: String): Url {
        val builder = HttpRequestBuilder()
        builder.url {
            path(Config.getAdReportingHost())
            parameters.append(Config.AD_ID_PARAM, adId)
            parameters.append(Config.UDID_PARAM, udid)
        }
        return builder.build().url
    }
}