package com.maratangsoft.tplocationhelper

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        //카카오 SDK의 초기화
        KakaoSdk.init(this, "834ac98d19eacddcf9523e1394d96d98")
    }
}