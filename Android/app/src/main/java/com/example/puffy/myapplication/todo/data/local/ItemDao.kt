package com.example.puffy.myapplication.todo.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.puffy.myapplication.auth.data.TokenHolder
import com.example.puffy.myapplication.todo.data.Item

@Dao
interface ItemDao {
    @Query("SELECT * from items")
    fun getAll(): LiveData<List<Item>>

    @Query("SELECT * FROM items WHERE id=:id ")
    fun getById(id: Int): LiveData<Item>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(item: Item)

    @Query("DELETE FROM items")
    suspend fun deleteAll()
}