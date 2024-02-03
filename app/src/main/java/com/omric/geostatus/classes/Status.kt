package com.omric.geostatus.classes

import android.graphics.Bitmap
import java.io.Serializable

class Location(val latitude: Double, val longitude: Double)
data class Status(val name: String, val date: String, val imagePath: String, val creator: String, val location: Location, val id: String?, val imageBitmap: Bitmap? = null, val creatorName: String? = null): Serializable