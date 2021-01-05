package com.example.puffy.myapplication.todo.data

import androidx.lifecycle.LiveData
import com.example.puffy.myapplication.common.MyResult
import com.example.puffy.myapplication.todo.data.local.ItemDao
import com.example.puffy.myapplication.todo.data.remote.ItemApi
import retrofit2.HttpException
import java.lang.Exception

object ItemRepository {
    private lateinit var itemDao: ItemDao
    lateinit var items : LiveData<List<Item>>
    var itemsAddLocal : MutableList<Item> = ArrayList()
    var itemsUpdatedLocal : MutableList<Item> = ArrayList()
    private var networkStatus : Boolean = false

    fun setItemDao(itemDao : ItemDao){
        this.itemDao = itemDao
        items = this.itemDao.getAll() //listener la db
    }

    fun setNetworkStatus(status : Boolean){
        networkStatus = status
    }

    fun getNetworkStatus() : Boolean{
        return networkStatus
    }

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
            if(networkStatus){
                val result = ItemApi.service.addItem(item)
                return MyResult.Success(result)
            }
            addItemLocal(item)
            itemsAddLocal.add(item)
            return MyResult.Success(item)
        }catch(ex : Exception){
            if(ex is HttpException){
                var errCode: Int? = ex.response()?.code()
                if(errCode == 409){
                    return MyResult.Error(ex)
                }
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
            if(networkStatus){
                val result = ItemApi.service.updateItem(itemId,item)
                return MyResult.Success(result)
            }
            updateItemLocal(item)
            itemsUpdatedLocal.add(item)
            return MyResult.Success(item)
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