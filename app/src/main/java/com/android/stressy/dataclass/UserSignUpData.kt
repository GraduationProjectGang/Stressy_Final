package com.android.stressy.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class UserSignUpData(
    val name:String,
    val gender:Boolean,
    val age:Int,
    val email:String = ""
) {
}