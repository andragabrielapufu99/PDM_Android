package com.example.puffy.myapplication.todo.item

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.puffy.myapplication.auth.data.AuthRepository
import com.example.puffy.myapplication.common.Api
import com.example.puffy.myapplication.common.MyResult
import com.example.puffy.myapplication.common.RemoteDataSource
import com.example.puffy.myapplication.todo.data.Item
import com.example.puffy.myapplication.todo.data.ItemRepository
import com.example.puffy.myapplication.todo.data.local.TodoDatabase
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

    val completed : LiveData<Boolean> = mutableCompleted
    val fetching : LiveData<Boolean> = mutableFetching
    val exception : LiveData<Exception> = mutableException

    val itemRepository : ItemRepository

    val tokenDao = TodoDatabase.getDatabase(application,viewModelScope).tokenDao()

    init{
        val itemDao = TodoDatabase.getDatabase(application, viewModelScope).itemDao()
        itemRepository = ItemRepository(itemDao)
    }

    fun getItemById(id: Int) : LiveData<Item> {
        Log.v("ItemEditViewModel", "getItemById")
        return itemRepository.getOne(id)
    }

    fun saveOrUpdateItem(item: Item) {
        viewModelScope.launch {
            Log.v("ItemEditViewModel", "saveOrUpdateItem...")
            mutableFetching.value = true
            mutableException.value = null
            val result: MyResult<Item>
            if (item.id != -1) {
                result = itemRepository.updateItem(item.id, item)
            } else {
                result = itemRepository.addItem(item)
            }
            when (result) {
                is MyResult.Success -> {
                    Log.d("ItemEditViewModel", "saveOrUpdateItem succeeded")
                }
                is MyResult.Error -> {
                    Log.w("ItemEditViewModel", "saveOrUpdateItem failed", result.exception)
                    mutableException.value = result.exception
                }
            }
            mutableCompleted.value = true
            mutableFetching.value = false

        }
    }

    fun logout(){
        runBlocking {
            tokenDao.deleteAll()
            itemRepository.deleteAllItemsLocal()
            AuthRepository.token = null
            Api.tokenInterceptor.token = null
        }

    }
}
