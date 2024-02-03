package com.omric.geostatus.utils

import android.content.Context
import android.widget.Toast

class Toaster {
    fun show(context: Context, text: String) {
        Toast.makeText(
            context,
            text,
            Toast.LENGTH_SHORT,
        ).show()
    }
}