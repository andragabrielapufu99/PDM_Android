package com.example.puffy.myapplication.todo.items

import android.app.Application
import android.util.Log
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
import retrofit2.HttpException

class ItemListViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableItems = MutableLiveData<List<Item>>().apply { value = null }
    private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val items: LiveData<List<Item>> = mutableItems
    val loading : LiveData<Boolean> = mutableLoading
    val loadingError : LiveData<Exception> = mutableException

    init {
        Log.v("ItemListViewModel","init")
        mutableItems.value = emptyList()
    }

    suspend fun getAll() : MyResult<List<Item>>{
        try{
            val result : List<Item> = ItemApi.service.getAll()
            mutableItems.value = result
            return MyResult.Success(result)
        }catch (e : HttpException){
            val message : String? = e.response()?.errorBody()?.string()
            val m = JsonParser().parse(message)
            val ex = Exception(m.asJsonObject["message"].asString)
            return MyResult.Error(ex)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            Log.v("ItemListView", "refresh...");
            mutableLoading.value = true
            mutableException.value = null
            val result : MyResult<List<Item>> = getAll()
            when (result) {
                is MyResult.Success -> {
                    Log.d("ItemListViewModel", "refresh succeeded");
                }
                is MyResult.Error -> {
                    Log.w("ItemListViewModel", "refresh failed", result.exception);
                    mutableException.value = result.exception
                }
            }
            mutableLoading.value = false
        }
    }
}