package com.maratangsoft.tplocationhelper.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import com.maratangsoft.tplocationhelper.databinding.ActivityPlaceUrlBinding

class PlaceUrlActivity : AppCompatActivity() {
    private val binding by lazy { ActivityPlaceUrlBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.wv.webViewClient = WebViewClient() //기본브라우저 켜지 말고 웹뷰 안에 웹문서가 열리도록 하기
        binding.wv.webChromeClient = WebChromeClient() // 웹페이지의 다이얼로그 같은 것들이 발동하도록 하기
        binding.wv.settings.javaScriptEnabled = true // 보안상 취약점 때문에 기본적으로 JS를 막아놨는데 그거 풀기

        val placeUrl = intent.getStringExtra("place_url") ?: ""
        binding.wv.loadUrl(placeUrl)
    }

    override fun onBackPressed() {
        if (binding.wv.canGoBack()) binding.wv.goBack()
        super.onBackPressed()
    }
}