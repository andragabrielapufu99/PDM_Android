package com.example.puffy.myapplication.auth.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tokens")
data class TokenHolder (
   @PrimaryKey @ColumnInfo(name = "token") val token : String
)