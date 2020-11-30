package com.example.puffy.myapplication.todo.data.remote

import com.example.puffy.myapplication.common.Api
import com.example.puffy.myapplication.todo.data.Item
import retrofit2.http.*

object ItemApi {
    interface Service{

        @GET("/api/items")
        @Headers("Accept: application/json")
        suspend fun getAll() : List<Item>

        @POST("/api/items")
        @Headers("Content-Type: application/json", "Accept: application/json")
        suspend fun addItem(@Body item : Item) : Item

        @PUT("/api/items/{id}")
        @Headers("Content-Type: application/json", "Accept: application/json")
        suspend fun updateItem(@Path("id") itemId : Int, @Body item : Item) : Item
    }

    val service : Service = Api.retrofit.create(Service::class.java)
}