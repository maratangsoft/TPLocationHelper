package com.maratangsoft.tplocationhelper

import com.maratangsoft.tplocationhelper.model.UserAccount

class G {
    companion object{ //동반객체: 클래스에 붙어 있는 객체
        //회원정보 저장
        var userAccount: UserAccount = UserAccount("","")
    }
}