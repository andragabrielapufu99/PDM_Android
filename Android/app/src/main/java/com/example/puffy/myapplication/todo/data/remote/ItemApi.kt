package com.example.puffy.myapplication.todo.data.remote

import androidx.lifecycle.LiveData
import com.example.puffy.myapplication.common.Api
import com.example.puffy.myapplication.todo.data.Item
import retrofit2.http.*

object ItemApi {
    interface Service{
        @GET("/api/items")
        @Headers("Accept: application/json")
        suspend fun getAll() : List<Item>

        @GET("/api/items/{id}")
        @Headers("Accept: application/json")
        suspend fun getOne(@Path("id") itemId : Int) : Item

        @Headers("Content-Type: application/json", "Accept: application/json")
        @POST("/api/items")
        suspend fun addItem(@Body item : Item) : Item

        @Headers("Content-Type: application/json", "Accept: application/json")
        @PUT("/api/items/{id}")
        suspend fun updateItem(@Path("id") itemId : Int, @Body item : Item) : Item
    }

    val service : Service = Api.retrofit.create(Service::class.java);
}