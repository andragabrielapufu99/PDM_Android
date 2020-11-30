package com.example.puffy.myapplication.todo.items

import android.app.Application
import android.util.Log
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class ItemListViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    var items: LiveData<List<Item>>
    val loading : LiveData<Boolean> = mutableLoading
    val loadingError : LiveData<Exception> = mutableException

    val itemRepository : ItemRepository
    val tokenDao = TodoDatabase.getDatabase(application,viewModelScope).tokenDao()
    init {
        Log.v("ItemListViewModel","init")
        val itemDao = TodoDatabase.getDatabase(application, viewModelScope).itemDao()
        itemRepository = ItemRepository(itemDao)
        items = itemRepository.items
        CoroutineScope(Dispatchers.Main).launch { ws() }
    }

    fun refresh() {
        viewModelScope.launch {
            Log.v("ItemListView", "refresh...");
            mutableLoading.value = true
            mutableException.value = null
            when (val result = itemRepository.refresh()) {
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

    private suspend fun ws(){
        while (true){
            val event = RemoteDataSource.eventChannel.receive()
            val jsonObject = JSONObject(event)
            val eventType = jsonObject.get("event")
            val payload = jsonObject.get("payload")
            val itemObj = JSONObject(payload.toString())
            var item = Item(
                itemObj.getInt("id"),
                itemObj.getString("title"),
                itemObj.getString("artist"),
                itemObj.getInt("year"),
                itemObj.getString("genre"),
                itemObj.getString("userId"))
            if(eventType == "created"){
                itemRepository.addItemLocal(item)
            }
            else if(eventType == "updated"){
                itemRepository.updateItemLocal(item)
            }
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