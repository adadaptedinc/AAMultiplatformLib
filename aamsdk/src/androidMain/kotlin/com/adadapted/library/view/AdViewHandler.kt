package com.adadapted.library.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.adadapted.library.ad.Ad
import com.adadapted.library.constants.Config

actual class AdViewHandler(private val context: Context) {
    actual fun handleLink(ad: Ad) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.data = Uri.parse(ad.actionPath)
        context.startActivity(intent)
    }

    actual fun handlePopup(ad: Ad) {
        val intent = AndroidWebViewPopupActivity().createActivity(context, ad)
        context.startActivity(intent)
    }

    actual fun handleReportAd(adId: String, udid: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.data = buildReportAdUri(adId, udid)
        context.startActivity(intent)
    }

    private fun buildReportAdUri(adId: String, udid: String): Uri {
        return Uri.parse(Config.getAdReportingHost())
            .buildUpon()
            .appendQueryParameter(Config.AD_ID_PARAM, adId)
            .appendQueryParameter(Config.UDID_PARAM, udid)
            .build()
    }
}