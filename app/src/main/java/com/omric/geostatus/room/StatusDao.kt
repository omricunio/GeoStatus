package com.omric.geostatus.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.DeleteTable
import androidx.room.Insert
import androidx.room.Query
import com.omric.geostatus.room.Status

@Dao
interface StatusDao {
    @Query("SELECT * FROM status")
    fun getAll(): List<Status>

    @Query("SELECT * FROM status WHERE id IN (:statusIds)")
    fun loadAllByIds(statusIds: IntArray): List<Status>

    @Insert
    fun insertAll(vararg statuses: Status)

    @Delete
    fun delete(user: Status)

    @Query("DELETE FROM status")
    fun deleteAll()
}