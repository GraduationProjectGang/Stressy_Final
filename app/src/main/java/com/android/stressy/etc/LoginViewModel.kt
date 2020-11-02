package com.android.stressy.etc

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import org.json.JSONObject

class LoginViewModel(
//    val validIdRepo: ValidIdRepo
):ViewModel() {
    fun checkValidId(inputEmail: String) {
//        Log.d("viewmodel","checkValidId")
//        withContext(Dispatchers.IO){
//            val response = validIdRepo.checkValidId(inputEmail)
//            return response
//        }
    }
}