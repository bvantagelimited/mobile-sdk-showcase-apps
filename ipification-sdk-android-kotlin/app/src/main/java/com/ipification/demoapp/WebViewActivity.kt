package com.ipification.demoapp

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.webkit.WebView

class WebViewActivity : AppCompatActivity() {

    private lateinit var webview: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Privacy Policy"
        //set back button

        actionbar.setDisplayHomeAsUpEnabled(true)

        webview = findViewById(R.id.webview)
        webview.settings.javaScriptEnabled = true
        webview.settings.javaScriptCanOpenWindowsAutomatically = true
//        webview.setBackgroundColor(Color.parseColor("#f2f2f2"))
        webview.loadUrl("http://web-st.ipification.com/legal?hide-header=true")
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            android.R.id.home -> {
                // API 5+ solution
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}