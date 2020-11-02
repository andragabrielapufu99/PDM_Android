package com.example.puffy.myapplication.todo.data

data class Item (
    val id : Int,
    var title : String,
    var artist : String,
    var year : Int,
    var genre : String
){
    override fun toString(): String = "Title : "+title+"\nArtist : "+artist+"\nYear : "+year.toString()+"\nGenre : "+genre
}