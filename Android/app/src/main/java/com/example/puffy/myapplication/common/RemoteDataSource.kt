package com.example.puffy.myapplication.common

import android.util.Log
import com.example.puffy.myapplication.todo.data.Item
import com.example.puffy.myapplication.todo.items.ItemListAdapter
import kotlinx.coroutines.channels.Channel
import okhttp3.*
import okio.ByteString
import org.json.JSONObject

object RemoteDataSource {
    val eventChannel = Channel<String>()
    lateinit var adapter: ItemListAdapter

    init {
        //val request = Request.Builder().url("ws://192.168.1.101:3000").build() //Cluj
        val request = Request.Builder().url("ws://192.168.1.102:3000").build() //Mioveni
        val webSocket = OkHttpClient().newWebSocket(request, MyWebSocketListener())
    }

    fun setItems(adapter: ItemListAdapter){
        this.adapter = adapter
    }

    private class MyWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("WebSocket","onOpen")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("WebSocket","onMessage $text")
            var jsonObj : JSONObject = JSONObject(text)
            var message : String = jsonObj["message"] as String
            adapter.setMessage(message)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d("WebSocket","onMessage bytes")
            println("Receive $bytes bytes")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("WebSocket","onFailure",t)
            t.printStackTrace()
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        }
    }
}