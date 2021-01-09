package com.example.puffy.myapplication.todo.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item (
    @PrimaryKey @ColumnInfo(name = "id")  var id : Int,
    @ColumnInfo(name = "title") var title : String,
    @ColumnInfo(name = "artist") var artist : String,
    @ColumnInfo(name = "year") var year : Int,
    @ColumnInfo(name = "genre") var genre : String,
    @ColumnInfo(name = "userId") var userId : String?,
    @ColumnInfo(name = "pathImage") var pathImage : String?,
    @ColumnInfo(name = "latitude") var latitude : Double?,
    @ColumnInfo(name = "longitude") var longitude : Double?
){
    override fun toString(): String =
        "Title : $title\nArtist : $artist\nYear : $year\nGenre : $genre"
}