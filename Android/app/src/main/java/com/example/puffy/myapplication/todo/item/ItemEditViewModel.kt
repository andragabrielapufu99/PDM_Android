package com.example.puffy.myapplication.todo.item

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.puffy.myapplication.common.MyResult
import com.example.puffy.myapplication.common.RemoteDataSource
import com.example.puffy.myapplication.todo.data.Item
import com.example.puffy.myapplication.todo.data.remote.ItemApi
import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import retrofit2.HttpException


class ItemEditViewModel(application: Application) : AndroidViewModel(application) {

    private val mutableCompleted = MutableLiveData<Boolean>().apply { value = false }
    private val mutableFetching = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }
    private val mutableItem = MutableLiveData<Item>().apply { value = null }

    val itemLive : LiveData<Item> = mutableItem
    val completed : LiveData<Boolean> = mutableCompleted
    val fetching : LiveData<Boolean> = mutableFetching
    val exception : LiveData<Exception> = mutableException

    init {
    }

    suspend fun getItemById(id: Int) : LiveData<Item>{
        Log.v("ItemEditViewModel", "getItemById")
        val item : Item = ItemApi.service.getOne(id)
        mutableItem.value = item
        return itemLive
    }

    suspend fun addItem(item: Item) : MyResult<Item>{
        Log.v("ItemEditViewModel", "addItem")
        try{
            val r : Item = ItemApi.service.addItem(item)
            return MyResult.Success(r)
        }catch (e: HttpException){
            val message : String? = e.response()?.errorBody()?.string()
            val m = JsonParser().parse(message)
            val ex = Exception(m.asJsonObject["message"].asString)
            return MyResult.Error(ex)
        }
    }

    suspend fun updateItem(id: Int, item: Item) : MyResult<Item> {
        Log.v("ItemEditViewModel", "updateItem")
        try{
            val r : Item = ItemApi.service.updateItem(id, item)
            return MyResult.Success(r)
        }catch (e: HttpException){
            val message : String? = e.response()?.errorBody()?.string()
            val m = JsonParser().parse(message)
            val ex = Exception(m.asJsonObject["message"].asString)
            return MyResult.Error(ex)
        }
    }

    fun getById(id: Int) = runBlocking<LiveData<Item>> {
        getItemById(id)
    }

    fun saveOrUpdateItem(item: Item) {
        viewModelScope.launch {
            Log.v("ItemEditViewModel", "saveOrUpdateItem...")
            mutableFetching.value = true
            mutableException.value = null
            val result: MyResult<Item>
            if (item.id != -1) {
                result = updateItem(item.id, item)
            } else {
                result = addItem(item)
            }
            when (result) {
                is MyResult.Success -> {
                    Log.d("ItemEditViewModel", "saveOrUpdateItem succeeded");
                }
                is MyResult.Error -> {
                    Log.w("ItemEditViewModel", "saveOrUpdateItem failed", result.exception);
                    mutableException.value = result.exception
                }
            }
            mutableCompleted.value = true
            mutableFetching.value = false

        }
    }
}
