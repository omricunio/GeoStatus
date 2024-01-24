package com.omric.geostatus.classes

class Location(val latitude: Double, val longitude: Double)
class Status(val name: String, val date: String, val imagePath: String, val creator: String, val location: Location)