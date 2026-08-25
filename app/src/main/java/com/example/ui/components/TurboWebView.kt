package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.util.AdBlockEngine
import com.example.util.SitePermissionManager

val DESKTOP_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TurboWebView(
    url: String,
    modifier: Modifier = Modifier,
    isDesktopMode: Boolean = false,
    isAdBlockEnabled: Boolean = true,
    isHardwareBoostEnabled: Boolean = true,
    isOledBlackMode: Boolean = false,
    customCss: String = "",
    customJs: String = "",
    onProgressChange: (Float) -> Unit = {},
    onTitleChange: (String) -> Unit = {},
    onFaviconChange: (Bitmap?) -> Unit = {},
    onBlockedAdCountChange: (Int) -> Unit = {},
    onWebViewCreated: (WebView) -> Unit = {},
    onCustomViewShow: ((View, WebChromeClient.CustomViewCallback) -> Unit)? = null,
    onCustomViewHide: (() -> Unit)? = null,
    onFileChooser: ((ValueCallback<Array<Uri>>?, WebChromeClient.FileChooserParams?) -> Boolean)? = null,
    onPermissionPromptRequested: ((PermissionRequest) -> Unit)? = null
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var blockedAdsCounter by remember { mutableStateOf(0) }

    // Back handling inside the webview
    val canGoBack = webViewInstance?.canGoBack() == true
    BackHandler(enabled = canGoBack) {
        webViewInstance?.goBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding() // Automatically resize container to sit above the keyboard
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // Hardware Acceleration & Rendering Boost
                    if (isHardwareBoostEnabled) {
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    }

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        mediaPlaybackRequiresUserGesture = false
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        setGeolocationEnabled(true)
                        allowFileAccessFromFileURLs = true
                        allowUniversalAccessFromFileURLs = true

                        userAgentString = if (isDesktopMode) {
                            DESKTOP_USER_AGENT
                        } else {
                            userAgentString.replace("; wv", "") // Act like full standalone mobile chrome
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val requestUrl = request?.url?.toString() ?: return null
                            val siteDomain = SitePermissionManager.extractDomain(url)
                            val siteRule = SitePermissionManager.getSiteRule(ctx, siteDomain)

                            if (isAdBlockEnabled && siteRule.adBlockEnabled && AdBlockEngine.isAdOrTracker(requestUrl)) {
                                blockedAdsCounter++
                                onBlockedAdCountChange(blockedAdsCounter)
                                return AdBlockEngine.createEmptyResponse()
                            }
                            return super.shouldInterceptRequest(view, request)
                        }

                        override fun onPageStarted(view: WebView?, pageUrl: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, pageUrl, favicon)
                            onFaviconChange(favicon)
                        }

                        override fun onPageFinished(view: WebView?, pageUrl: String?) {
                            super.onPageFinished(view, pageUrl)

                            // Inject Keyboard & Viewport Responsiveness Assistant
                            val keyboardAssistantJs = """
                                (function() {
                                    if (window.__webnative_keyboard_helper_installed) return;
                                    window.__webnative_keyboard_helper_installed = true;

                                    function scrollElementToVisible(el) {
                                        if (!el) return;
                                        try {
                                            setTimeout(function() {
                                                el.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'nearest' });
                                            }, 250);
                                        } catch (e) {}
                                    }

                                    document.addEventListener('focusin', function(e) {
                                        var target = e.target;
                                        if (target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable || target.id === 'prompt-input' || target.classList.contains('input'))) {
                                            scrollElementToVisible(target);
                                        }
                                    }, { passive: true });

                                    // Dynamic visual viewport adjustment for mobile layouts
                                    if (window.visualViewport) {
                                        window.visualViewport.addEventListener('resize', function() {
                                            var active = document.activeElement;
                                            if (active && (active.tagName === 'INPUT' || active.tagName === 'TEXTAREA' || active.isContentEditable)) {
                                                scrollElementToVisible(active);
                                            }
                                        });
                                    }
                                })();
                            """.trimIndent()
                            view?.evaluateJavascript(keyboardAssistantJs, null)

                            // Inject Ad hiding CSS
                            if (isAdBlockEnabled) {
                                view?.evaluateJavascript(AdBlockEngine.getAdHidingCss(), null)
                            }
                            // Inject OLED true dark mode
                            if (isOledBlackMode) {
                                view?.evaluateJavascript(AdBlockEngine.getOledBlackCss(), null)
                            }
                            // Inject user custom CSS
                            if (customCss.isNotBlank()) {
                                val cleanCss = customCss.replace("\n", " ").replace("'", "\\'")
                                val js = """
                                    (function() {
                                        var style = document.createElement('style');
                                        style.innerHTML = '$cleanCss';
                                        document.head.appendChild(style);
                                    })();
                                """.trimIndent()
                                view?.evaluateJavascript(js, null)
                            }
                            // Inject user custom JS
                            if (customJs.isNotBlank()) {
                                view?.evaluateJavascript(customJs, null)
                            }
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            onProgressChange(newProgress / 100f)
                        }

                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            title?.let { onTitleChange(it) }
                        }

                        override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                            onFaviconChange(icon)
                        }

                        // WebRTC Microphone / Camera / Audio Permissions
                        override fun onPermissionRequest(request: PermissionRequest?) {
                            if (request == null) return
                            val originUrl = request.origin.toString()
                            val isMicAllowed = SitePermissionManager.isMicAllowedForOrigin(ctx, originUrl)
                            val isCamAllowed = SitePermissionManager.isCameraAllowedForOrigin(ctx, originUrl)

                            val grantedResources = mutableListOf<String>()
                            for (res in request.resources) {
                                when (res) {
                                    PermissionRequest.RESOURCE_AUDIO_CAPTURE -> {
                                        if (isMicAllowed) grantedResources.add(res)
                                    }
                                    PermissionRequest.RESOURCE_VIDEO_CAPTURE -> {
                                        if (isCamAllowed) grantedResources.add(res)
                                    }
                                    PermissionRequest.RESOURCE_MIDI_SYSEX,
                                    PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID -> {
                                        grantedResources.add(res)
                                    }
                                }
                            }

                            if (grantedResources.isNotEmpty()) {
                                request.grant(grantedResources.toTypedArray())
                            } else {
                                if (onPermissionPromptRequested != null) {
                                    onPermissionPromptRequested.invoke(request)
                                } else {
                                    // Default grant if system permissions are active
                                    request.grant(request.resources)
                                }
                            }
                        }

                        // Geolocation / GPS permission
                        override fun onGeolocationPermissionsShowPrompt(
                            origin: String?,
                            callback: GeolocationPermissions.Callback?
                        ) {
                            if (origin == null || callback == null) return
                            val isLocAllowed = SitePermissionManager.isLocationAllowedForOrigin(ctx, origin)
                            callback.invoke(origin, isLocAllowed, false)
                        }

                        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                            if (view != null && callback != null && onCustomViewShow != null) {
                                onCustomViewShow(view, callback)
                            } else {
                                super.onShowCustomView(view, callback)
                            }
                        }

                        override fun onHideCustomView() {
                            if (onCustomViewHide != null) {
                                onCustomViewHide()
                            } else {
                                super.onHideCustomView()
                            }
                        }

                        // File attachment / Media Picker chooser
                        override fun onShowFileChooser(
                            webView: WebView?,
                            filePathCallback: ValueCallback<Array<Uri>>?,
                            fileChooserParams: FileChooserParams?
                        ): Boolean {
                            val currentOrigin = webView?.url ?: ""
                            val isFilesAllowed = SitePermissionManager.isFilesAllowedForOrigin(ctx, currentOrigin)
                            if (!isFilesAllowed) {
                                filePathCallback?.onReceiveValue(null)
                                return false
                            }

                            return onFileChooser?.invoke(filePathCallback, fileChooserParams)
                                ?: run {
                                    filePathCallback?.onReceiveValue(null)
                                    false
                                }
                        }
                    }

                    loadUrl(url)
                    webViewInstance = this
                    onWebViewCreated(this)
                }
            },
            update = { webView ->
                webViewInstance = webView
                webView.settings.userAgentString = if (isDesktopMode) {
                    DESKTOP_USER_AGENT
                } else {
                    webView.settings.userAgentString.replace("; wv", "")
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.destroy()
        }
    }
}
