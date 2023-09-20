package com.adadapted.library.view

import com.adadapted.library.ad.Ad
import com.adadapted.library.ad.AdContentListener
import com.adadapted.library.ad.AdContentPublisher
import com.adadapted.library.concurrency.Transporter
import com.adadapted.library.constraintsToFillSuperview
import com.adadapted.library.interfaces.WebViewListener
import com.adadapted.library.interfaces.ZoneViewListener
import com.adadapted.library.log.AALogger
import com.adadapted.library.session.SessionClient
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.cValue
import platform.CoreGraphics.CGPoint
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectZero
import platform.Foundation.*
import platform.QuartzCore.CAShapeLayer
import platform.UIKit.*
import platform.objc.sel_registerName

class IosZoneView : UIView(frame = cValue { CGRectZero }) {

    var presenter: AdZonePresenter = AdZonePresenter(AdViewHandler(), SessionClient)
    var zoneViewListener: ZoneViewListener? = null
    private var webView: IosWebView = IosWebView()
    private var reportAdView: UIButton = UIButton.buttonWithType(UIButtonTypeCustom)
    private var isVisible = true
    private var isAdVisible = true
    private var currentAd: Ad? = null

    init {
        this.webView.addWebViewListener(setWebViewListener())
        reportAdView.addTarget(this, sel_registerName("reportAdTapped"), UIControlEventTouchUpInside)
//        reportAdView.layer.backgroundColor = UIColor.whiteColor().CGColor()
//        reportAdView.layer.borderColor = UIColor.cyanColor().CGColor()
//        reportAdView.layer.borderWidth = 1.5
        reportAdView.setTitleColor(UIColor.cyanColor, UIControlStateNormal)
        reportAdView.setTitle("¡", UIControlStateNormal)

        var trianglePath = UIBezierPath()
        var triangleLayer = CAShapeLayer()
        trianglePath.moveToPoint(CGPointMake(0.0, 20.0))
        trianglePath.addLineToPoint(CGPointMake(10.0, 0.0))
        trianglePath.addLineToPoint(CGPointMake(20.0, 20.0))
        trianglePath.addLineToPoint(CGPointMake(0.0, 20.0))
        trianglePath.closePath()

        triangleLayer.path = trianglePath.CGPath()
        triangleLayer.borderColor = UIColor.cyanColor().CGColor()
        triangleLayer.borderWidth = 1.5
        triangleLayer.layoutIfNeeded()
        triangleLayer.strokeColor = UIColor.cyanColor().CGColor()
        triangleLayer.lineWidth = 1.5

        reportAdView.layer.addSublayer(triangleLayer)


        //reportAdView.setImage(UIImage(named: "reportAdIcon", in: Bundle(for: AAZoneView.self), compatibleWith: nil), for: .normal)
//        webView.layer.borderColor = UIColor.blueColor().CGColor()
//        webView.layer.borderWidth = 1.5
        addSubview(webView)
        addSubview(reportAdView)
        setupConstraints()
    }

    fun initZone(zoneId: String) {
        this.presenter.init(zoneId)
    }

    fun setZoneViewListener(listener: ZoneViewListener? = null) {
        presenter.onAttach(setAdZonePresenterListener())
        this.zoneViewListener = listener
    }

    fun setAdContentListener(listener: AdContentListener? = null) {
        if (listener != null) {
            AdContentPublisher.addListener(listener)
        }
    }

    fun shutdown() {
        this.onStop()
    }

    fun setAdZoneVisibility(isViewable: Boolean) {
        isAdVisible = isViewable
        presenter.onAdVisibilityChanged(isAdVisible)
    }

    private fun onStop(contentListener: AdContentListener? = null) {
        this.zoneViewListener = null
        presenter.onDetach()
        if (contentListener != null) {
            AdContentPublisher.removeListener(contentListener)
        }
    }

    private fun notifyClientZoneHasAds(hasAds: Boolean) {
        Transporter().dispatchToThread {
            zoneViewListener?.onZoneHasAds(hasAds)
        }
    }

    private fun notifyClientAdLoaded() {
        Transporter().dispatchToThread {
            zoneViewListener?.onAdLoaded()
        }
    }

    private fun notifyClientAdLoadFailed() {
        Transporter().dispatchToThread {
            zoneViewListener?.onAdLoadFailed()
        }
    }

    private fun setAdZonePresenterListener() = object : AdZonePresenterListener {
        override fun onZoneAvailable(zone: Zone) {
            notifyClientZoneHasAds(zone.hasAds())
        }

        override fun onAdsRefreshed(zone: Zone) {
            notifyClientZoneHasAds(zone.hasAds())
        }

        override fun onAdAvailable(ad: Ad) {
            if (isVisible) {
                Transporter().dispatchToThread {
                    webView.loadAd(ad)
                    setNeedsDisplay()
                }
            }
        }

        override fun onNoAdAvailable() {
            Transporter().dispatchToThread {
                webView.loadBlank()
                setNeedsDisplay()
            }
        }
    }

    private fun setWebViewListener() = object : WebViewListener {
        override fun onAdLoadedInWebView(ad: Ad) {
            AALogger.logInfo("Ad ${ad.id} loaded")
            currentAd = ad
            presenter.onAdDisplayed(ad, isAdVisible)
            notifyClientAdLoaded()
        }

        override fun onAdLoadInWebViewFailed() {
            AALogger.logInfo("Ad load failed")
            presenter.onAdDisplayFailed()
            notifyClientAdLoadFailed()
        }

        override fun onAdInWebViewClicked(ad: Ad) {
            AALogger.logInfo("ad ${ad.id} interaction")
            presenter.onAdClicked(ad)
        }

        override fun onBlankAdInWebViewLoaded() {
            AALogger.logInfo("Blank ad loaded")
            presenter.onBlankDisplayed()
        }
    }

    @ObjCAction
    private fun reportAdTapped() {
        println("report ad tapped")
        val AA_UUID_KEY = "aamsdk_uuid"
        val preferences = NSUserDefaults.standardUserDefaults()
        currentAd?.id?.let { AdViewHandler().handleReportAd(it, preferences.valueForKey(AA_UUID_KEY).toString()) }
    }

    private fun setVisible() {
        isVisible = true
        presenter.onAttach(setAdZonePresenterListener())
    }

    private fun setInvisible() {
        isVisible = false
        presenter.onDetach()
    }

    private fun setupConstraints() {
        val constraints = mutableListOf<NSLayoutConstraint>()
        constraints += this.constraintsToFillSuperview()
        constraints.add(webView.leadingAnchor.constraintEqualToAnchor(leadingAnchor))
        constraints.add(webView.trailingAnchor.constraintEqualToAnchor(trailingAnchor))
        constraints.add(webView.heightAnchor.constraintEqualToAnchor(heightAnchor))
        constraints.add(webView.widthAnchor.constraintEqualToAnchor(widthAnchor))
        constraints.add(webView.centerXAnchor.constraintEqualToAnchor(centerXAnchor))
        constraints.add(reportAdView.topAnchor.constraintEqualToAnchor(webView.topAnchor, 10.0))
        constraints.add(reportAdView.trailingAnchor.constraintEqualToAnchor(webView.trailingAnchor, -10.0))
        constraints.add(reportAdView.widthAnchor.constraintEqualToConstant(40.0))
        constraints.add(reportAdView.heightAnchor.constraintEqualToConstant(40.0))
        reportAdView.translatesAutoresizingMaskIntoConstraints = false
        reportAdView.clipsToBounds = true
        reportAdView.contentMode = UIViewContentMode.UIViewContentModeScaleAspectFill
        reportAdView.setNeedsLayout()
        reportAdView.layoutIfNeeded()
        NSLayoutConstraint.activateConstraints(constraints)
    }
}
