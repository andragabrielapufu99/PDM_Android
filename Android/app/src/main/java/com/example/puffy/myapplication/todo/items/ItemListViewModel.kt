package com.example.puffy.myapplication.todo.items

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
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
    private val mutableNetworkStatus = MutableLiveData<Boolean>()
    private val tagName: String = "ItemListViewModel"
    private val tokenDao = TodoDatabase.getDatabase(application,viewModelScope).tokenDao()

    //public
    var items: LiveData<List<Item>>
    val loading : LiveData<Boolean> = mutableLoading
    val loadingError : LiveData<Exception> = mutableException
    val networkStatus : LiveData<Boolean> = mutableNetworkStatus

    init {
        Log.v(tagName,"init")
        val itemDao = TodoDatabase.getDatabase(application, viewModelScope).itemDao()
        ItemRepository.setItemDao(itemDao)
        items = ItemRepository.items
        if (networkStatus.value == true){
            CoroutineScope(Dispatchers.Main).launch { ws() }
            ItemRepository.setNetworkStatus(true)
        }else{
            ItemRepository.setNetworkStatus(false)
        }
    }

    fun setNetworkStatus(status : Boolean){
        mutableNetworkStatus.postValue(status)
        ItemRepository.setNetworkStatus(status)
        if (networkStatus.value == true){
            CoroutineScope(Dispatchers.Main).launch { ws() }
        }
    }

    fun refreshLocal() : List<Item>? {
        return ItemRepository.items.value
    }

    fun refresh() {
        viewModelScope.launch {
            Log.v(tagName, "Refresh");
            mutableLoading.value = true
            mutableException.value = null
            when (val result = ItemRepository.refresh()) {
                is MyResult.Success -> {
                    Log.d(tagName, "Refresh succeeded");
                }
                is MyResult.Error -> {
                    Log.w(tagName, "Refresh failed", result.exception);
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
                itemObj.getString("userId"),
                itemObj.getString("pathImage"))
            if(eventType == "created"){
                ItemRepository.addItemLocal(item)
            }
            else if(eventType == "updated"){
                ItemRepository.updateItemLocal(item)
            }
        }
    }

    fun logout(){
        runBlocking {
            tokenDao.deleteAll()
            ItemRepository.deleteAllItemsLocal()
            AuthRepository.token = null
            Api.tokenInterceptor.token = null
        }

    }
}