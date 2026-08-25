package com.example.util

import android.net.Uri
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream

object AdBlockEngine {

    private val BLOCKED_DOMAINS = hashSetOf(
        "doubleclick.net",
        "googleadservices.com",
        "googlesyndication.com",
        "adservice.google.com",
        "pagead2.googlesyndication.com",
        "adsystem.com",
        "adroll.com",
        "adnxs.com",
        "criteo.com",
        "criteo.net",
        "taboola.com",
        "outbrain.com",
        "popads.net",
        "popcash.net",
        "adcolony.com",
        "unityads.unity3d.com",
        "vungle.com",
        "scorecardresearch.com",
        "quantserve.com",
        "advertising.com",
        "admob.com",
        "adsafeprotected.com",
        "moatads.com",
        "serving-sys.com",
        "flashtalking.com",
        "zemanta.com",
        "revcontent.com",
        "mgid.com",
        "bidswitch.net",
        "rubiconproject.com",
        "openx.net",
        "pubmatic.com",
        "casalemedia.com",
        "smartadserver.com",
        "yieldmo.com",
        "amazon-adsystem.com",
        "inmobi.com",
        "applovin.com",
        "chartbeat.net",
        "hotjar.com",
        "crazyegg.com"
    )

    private val BLOCKED_KEYWORDS = listOf(
        "/ads/", "/ad-server/", "/adserver/", "/banner-ads/",
        "ad_type=", "pagead", "googleads", "/popunder"
    )

    fun isAdOrTracker(url: String): Boolean {
        try {
            val uri = Uri.parse(url)
            val host = uri.host?.lowercase() ?: return false
            
            // Check direct or sub-domain matches
            for (blocked in BLOCKED_DOMAINS) {
                if (host == blocked || host.endsWith(".$blocked")) {
                    return true
                }
            }

            // Check keyword matches in path
            val path = uri.path?.lowercase() ?: ""
            val query = uri.query?.lowercase() ?: ""
            for (keyword in BLOCKED_KEYWORDS) {
                if (path.contains(keyword) || query.contains(keyword)) {
                    return true
                }
            }
        } catch (_: Exception) {
            // Ignore parse errors
        }
        return false
    }

    fun createEmptyResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "utf-8",
            ByteArrayInputStream("".toByteArray())
        )
    }

    // CSS script injected into websites to hide lingering banner elements and enable true OLED dark mode
    fun getOledBlackCss(): String {
        return """
            javascript:(function() {
                var style = document.getElementById('webnative_oled_dark');
                if (!style) {
                    style = document.createElement('style');
                    style.id = 'webnative_oled_dark';
                    style.innerHTML = 'html, body { background-color: #000000 !important; color: #E0E0E0 !important; } * { border-color: #222 !important; } img, video { filter: brightness(0.9) !important; }';
                    document.head.appendChild(style);
                }
            })();
        """.trimIndent()
    }

    fun getAdHidingCss(): String {
        return """
            javascript:(function() {
                var style = document.getElementById('webnative_ad_hide');
                if (!style) {
                    style = document.createElement('style');
                    style.id = 'webnative_ad_hide';
                    style.innerHTML = '.ad, .ads, .adsbygoogle, .banner-ad, [class*="advert"], [id*="google_ads"], iframe[id*="google_ads"] { display: none !important; height: 0 !important; }';
                    document.head.appendChild(style);
                }
            })();
        """.trimIndent()
    }
}
