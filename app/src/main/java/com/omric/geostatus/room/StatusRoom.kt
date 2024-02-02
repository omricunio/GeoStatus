package com.omric.geostatus.room

import android.content.Context
import androidx.room.Room
import com.omric.geostatus.classes.Location

enum class StatusDBs{
    ProfileStatuses,
}

class StatusRoom(context: Context, dbName: StatusDBs) {
    val db: AppDatabase

    init {
        this.db = Room.databaseBuilder(context, AppDatabase::class.java, dbName.name).build()
    }

    fun getStatuses(): List<com.omric.geostatus.classes.Status> {
        val statuses = db.statusDao().getAll().map { statusDao ->
            com.omric.geostatus.classes.Status(
                statusDao.name,
                statusDao.date,
                statusDao.imagePath,
                statusDao.creator,
                Location(statusDao.latitude, statusDao.longitude),
                statusDao.id,
                statusDao.imageBitmap,
                statusDao.creatorName
            )
        }
        return statuses
    }

    fun insertStatuses(statuses: Array<com.omric.geostatus.classes.Status>) {
        db.statusDao().deleteAll()
        val statuses = statuses.map { status ->
            Status(status.id!!, status.name, status.date, status.imagePath, status.creator, status.location.longitude, status.location.latitude, status.imageBitmap, status.creatorName)
        }
        db.statusDao().insertAll(*statuses.toTypedArray())
    }
}