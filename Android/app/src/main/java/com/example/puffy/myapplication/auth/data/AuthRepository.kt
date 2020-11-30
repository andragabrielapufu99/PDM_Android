package com.example.puffy.myapplication.auth.data

import com.example.puffy.myapplication.auth.data.remote.AuthApi
import com.example.puffy.myapplication.common.Api
import com.example.puffy.myapplication.common.MyResult
import com.example.puffy.myapplication.todo.data.local.ItemDao
import retrofit2.HttpException
import java.lang.Exception

object AuthRepository {
    var token : String? = null

    val isAuthenticated: Boolean
        get() = this.token != null

    init {
        this.token = Api.tokenInterceptor.token
    }

    suspend fun login(username: String, password: String): MyResult<TokenHolder> {
        val user = User(username, password)
        try {
            val result = AuthApi.login(user)
            this.token = result.token
            Api.tokenInterceptor.token = result.token
            return MyResult.Success(result)
        } catch (ex: Exception) {
            if (ex is HttpException) {
                val message: String? = ex.response()?.errorBody()?.string()
                val e = Exception(message)
                return MyResult.Error(e)
            }
            return MyResult.Error(ex)
        }
    }
}