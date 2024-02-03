package com.omric.geostatus.room

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Status(
    @PrimaryKey val id: String,
    @ColumnInfo(name="name") val name: String,
    @ColumnInfo(name="date") val date: String,
    @ColumnInfo(name="imagePath") val imagePath: String,
    @ColumnInfo(name="creator") val creator: String,
    @ColumnInfo(name="latitude") val latitude: Double,
    @ColumnInfo(name="longitude") val longitude: Double,
    @ColumnInfo(name="imageBitmap") val imageBitmap: Bitmap?,
    @ColumnInfo(name="creatorName") val creatorName: String?
)