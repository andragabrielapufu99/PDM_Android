package com.example.puffy.myapplication.todo.data

import android.app.LauncherActivity
import androidx.lifecycle.LiveData
import com.example.puffy.myapplication.auth.data.TokenHolder
import com.example.puffy.myapplication.common.MyResult
import com.example.puffy.myapplication.todo.data.local.ItemDao
import com.example.puffy.myapplication.todo.data.local.TokenDao
import com.example.puffy.myapplication.todo.data.remote.ItemApi
import retrofit2.HttpException
import java.lang.Exception

class ItemRepository(private val itemDao: ItemDao) {
    val items : LiveData<List<Item>> = itemDao.getAll() //listener la db
    suspend fun refresh() : MyResult<Boolean> {
        try{
            val items = ItemApi.service.getAll() //date de pe server
            for (item in items) {
                itemDao.insert(item)
            }
            return MyResult.Success(true)
        }catch (ex : Exception){
            if(ex is HttpException){
                val message : String? = ex.response()?.errorBody()?.string()
                val e = Exception(message)
                return MyResult.Error(e)
            }
            return MyResult.Error(ex)
        }
    }

    fun getOne(itemId : Int) : LiveData<Item> {
        return itemDao.getById(itemId)
    }

    suspend fun addItem(item : Item) : MyResult<Item>{
        try{
            val result = ItemApi.service.addItem(item)
            return MyResult.Success(result)
        }catch(ex : Exception){
            if(ex is HttpException){
                val message : String? = ex.response()?.errorBody()?.string()
                val e = Exception(message)
                return MyResult.Error(e)
            }
            return MyResult.Error(ex)
        }
    }

    suspend fun addItemLocal(item : Item) {
        itemDao.insert(item)
    }

    suspend fun updateItemLocal(item : Item){
        itemDao.update(item)
    }
    suspend fun updateItem(itemId : Int, item : Item) : MyResult<Item>{
        try{
            val result = ItemApi.service.updateItem(itemId,item)
            return MyResult.Success(result)
        }catch(ex : Exception){
            if(ex is HttpException){
                val message : String? = ex.response()?.errorBody()?.string()
                val e = Exception(message)
                return MyResult.Error(e)
            }
            return MyResult.Error(ex)
        }
    }

    suspend fun deleteAllItemsLocal(){
        itemDao.deleteAll()
    }

    fun refreshLocal() : LiveData<List<Item>>{
        return items
    }
}