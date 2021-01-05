package com.example.puffy.myapplication.common

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.puffy.myapplication.todo.data.Item
import com.example.puffy.myapplication.todo.data.ItemRepository
import com.example.puffy.myapplication.todo.items.ItemListFragment
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import retrofit2.HttpException

class MyWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams){
    override fun doWork(): Result {
        val jsonObj = JSONObject(inputData.getString("data"))
        val eventType = jsonObj.getString("eventType")
        val itemObj = jsonObj.getJSONObject("item")
        var item = Item(
            itemObj.getInt("id"),
            itemObj.getString("title"),
            itemObj.getString("artist"),
            itemObj.getInt("year"),
            itemObj.getString("genre"),
            itemObj.getString("userId"),
            itemObj.getString("pathImage"))
        println("Woker : eventType $eventType")
        if(eventType == "created"){
            runBlocking {
                val result: MyResult<Item>
                result = ItemRepository.addItem(item)
                when (result) {
                    is MyResult.Success -> {
                        Log.d("MyWorker", "saveOrUpdateItem succeeded")
                    }
                    is MyResult.Error -> {
                        return@runBlocking Result.failure()
                    }
                    else -> {}
                }
            }
        }else if(eventType == "updated"){
            runBlocking {
                val result: MyResult<Item>
                result = ItemRepository.updateItem(item.id, item)
                when (result) {
                    is MyResult.Success -> {
                        Log.d("MyWorker", "saveOrUpdateItem succeeded")
                    }
                    is MyResult.Error -> {
                        return@runBlocking Result.failure()
                    }
                    else -> {}
                }
            }
        }
        return Result.success()
    }
}