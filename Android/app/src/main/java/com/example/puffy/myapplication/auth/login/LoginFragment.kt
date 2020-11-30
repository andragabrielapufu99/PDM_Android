package com.example.puffy.myapplication.auth.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.example.puffy.myapplication.R
import com.example.puffy.myapplication.auth.data.AuthRepository
import com.example.puffy.myapplication.common.Api
import com.example.puffy.myapplication.common.MyResult
import com.example.puffy.myapplication.common.RemoteDataSource
import kotlinx.android.synthetic.main.fragment_login.*
import org.json.JSONObject

class LoginFragment : Fragment() {
    private lateinit var viewModel : LoginViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login,container,false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        setupViewModel()
    }

    private fun setupViewModel(){
        viewModel.checkToken()

        viewModel.loginResult.observe(viewLifecycleOwner) {
            loading.visibility = View.GONE
            findNavController().navigate(R.id.fragment_item_list)
        }

        viewModel.loginError.observe(viewLifecycleOwner) {
            if(it != null){
                loading.visibility = View.GONE
                Toast.makeText(activity,it.message, Toast.LENGTH_LONG).show()
            }else{
                viewModel.login(username.text.toString(),password.text.toString())
            }
        }

        loginBtn.setOnClickListener{
            loading.visibility = View.VISIBLE
            viewModel.loginDataChanged(username.text.toString(),password.text.toString())
        }
    }
}