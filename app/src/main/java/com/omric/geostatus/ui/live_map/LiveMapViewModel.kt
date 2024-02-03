package com.omric.geostatus.ui.live_map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.omric.geostatus.classes.Status

class LiveMapViewModel : ViewModel() {

    val statuses: MutableLiveData<MutableList<Status>> = MutableLiveData<MutableList<Status>>().apply {
        value = null
    }
}