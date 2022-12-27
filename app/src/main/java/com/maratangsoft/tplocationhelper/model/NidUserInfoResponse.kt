package com.maratangsoft.tplocationhelper.model

data class NidUserInfoResponse(
    val resultcode:String,
    val message:String,
    val response:NidUser
)
data class NidUser(
    val id:String,
    val email:String
)