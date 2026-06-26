package com.example.circalux.ui.screens

import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ManifestoScreen() {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                webViewClient = WebViewClient()
                
                // Optimización de rendimiento y carga
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true // Necesario para muchos blogs modernos
                    databaseEnabled = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    
                    // Mejora de renderizado
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                }

                // Mejora de scroll y fluidez
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
                scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
                isVerticalScrollBarEnabled = true

                loadUrl("https://www.jota-manifesto.blog/blog-jota/")
            }
        }
    )
}
