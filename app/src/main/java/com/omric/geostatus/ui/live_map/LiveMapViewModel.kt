package com.omric.geostatus.ui.live_map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LiveMapViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is live map Fragment"
    }
    val text: LiveData<String> = _text
}