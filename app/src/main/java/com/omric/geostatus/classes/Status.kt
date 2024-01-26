package com.omric.geostatus.classes

import java.io.Serializable

class Location(val latitude: Double, val longitude: Double)
class Status(val name: String, val date: String, val imagePath: String, val creator: String, val location: Location): Serializable