package com.example.puffy.myapplication.todo.items

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.work.*
import com.example.puffy.myapplication.R
import com.example.puffy.myapplication.auth.data.AuthRepository
import com.example.puffy.myapplication.common.ConnectivityLiveData
import com.example.puffy.myapplication.common.MyWorker
import com.example.puffy.myapplication.todo.data.Item
import com.example.puffy.myapplication.todo.data.ItemRepository
import kotlinx.android.synthetic.main.fragment_item_list.*
import kotlinx.android.synthetic.main.main_activity.*
import org.json.JSONObject




class ItemListFragment : Fragment() {

    private lateinit var itemListAdapter : ItemListAdapter
    private lateinit var itemsModel : ItemListViewModel
    private lateinit var connectivityManager : ConnectivityManager
    private lateinit var connectivityLiveData: ConnectivityLiveData
    private lateinit var statusOnlineTextView: TextView
    private lateinit var statusOfflineTextView: TextView
    private lateinit var statusOnlineImageView: ImageView
    private lateinit var statusOfflineImageView: ImageView
    private val tagName: String = "ItemListFragment"

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(tagName, "onCreate")

        //from MainActivity
        statusOnlineTextView = requireActivity().statusOnline
        statusOfflineTextView = requireActivity().statusOffline
        statusOnlineImageView = requireActivity().statusImageOnline
        statusOfflineImageView = requireActivity().statusImageOffline

        connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityLiveData = ConnectivityLiveData(connectivityManager)
        connectivityLiveData.observe(this) {
            Log.d(tagName, "Network status : $it")
            itemsModel.setNetworkStatus(it)
            if(it){
                setOnline()
            }else{
                setOffline()
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_item_list, container, false)
    }

    val networkCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP) object: ConnectivityManager.NetworkCallback(){
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d(tagName, "Available network")
            setOnline()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.d(tagName, "Lost network")
            setOffline()
        }
    }

    private fun setOnline(){
        val handler = Handler(Looper.getMainLooper())
        handler.post(Runnable {
            // UI code goes here
            statusOfflineTextView.visibility = View.INVISIBLE
            statusOfflineImageView.visibility = View.INVISIBLE
            statusOnlineTextView.visibility = View.VISIBLE
            statusOnlineImageView.visibility = View.VISIBLE
        })
        itemsModel.setNetworkStatus(true)
    }

    private fun setOffline(){
        val handler = Handler(Looper.getMainLooper())
        handler.post(Runnable {
            // UI code goes here
            statusOnlineTextView.visibility = View.INVISIBLE
            statusOnlineImageView.visibility = View.INVISIBLE
            statusOfflineTextView.visibility = View.VISIBLE
            statusOfflineImageView.visibility = View.VISIBLE
        })
        itemsModel.setNetworkStatus(false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStart() {
        super.onStart()
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStop() {
        super.onStop()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(tagName, "onActivityCreated")
        if(!AuthRepository.isAuthenticated){
            findNavController().navigate(R.id.fragment_login)
            return
        }
        setupItemList()

        //add
        addBtn.setOnClickListener {
            Log.v(tagName, "Click on add button")
            findNavController().navigate(R.id.fragment_item_edit)
        }

        //logout
        logoutBtn.setOnClickListener {
            itemsModel.logout()
            findNavController().navigate(R.id.fragment_login)
        }

        //filter
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                itemListAdapter.items = itemsModel.refreshLocal()!!
                if (newText != null) {
                    if (!itemListAdapter.filterByText(newText)) {
                        Toast.makeText(
                            activity,
                            "Nothing found... Back to default data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                return false
            }

        })
    }

    private fun setupItemList(){
        itemListAdapter = ItemListAdapter(this)
        item_list.adapter = itemListAdapter
        itemsModel = ViewModelProvider(this).get(ItemListViewModel::class.java)

        itemsModel.items.observe(viewLifecycleOwner) {
            Log.v(tagName, "update items")
            itemListAdapter.items = it
        }

        itemsModel.loading.observe(viewLifecycleOwner) {
            Log.i(tagName, "update loading")
            fetchProgress.visibility = if (it) View.VISIBLE else View.GONE
        }

        itemsModel.loadingError.observe(viewLifecycleOwner) {
            if (it != null) {
                Log.i(tagName, "update loading error")
                val message = "Loading exception ${it.message}"
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                itemsModel.refreshLocal()
            }
        }

        itemsModel.networkStatus.observe(viewLifecycleOwner) {
            if(it == true){
                startWokers()
                itemsModel.refresh()
            }else if(it == false){
                itemsModel.refreshLocal()
                Toast.makeText(
                    activity,
                    "You're not connected to server. All operations will be done on local data and we will send as soon as we can to server.",
                    Toast.LENGTH_LONG
                ).show()
            }else{
                itemsModel.refreshLocal()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(tagName, "onDestroy")
    }

    fun startWokers(){
        ItemRepository.itemsAddLocal.forEach{ item -> startOneWorker(item, "created")}
        ItemRepository.itemsUpdatedLocal.forEach{ item -> startOneWorker(item, "updated")}
        ItemRepository.itemsAddLocal = ArrayList()
        ItemRepository.itemsUpdatedLocal = ArrayList()
    }

    @SuppressLint("RestrictedApi")
    fun startOneWorker(item: Item, eventType: String){
        val jsonObj = JSONObject()
        jsonObj.put("eventType", eventType)

        val itemObj = JSONObject()
        itemObj.put("id", item.id)
        itemObj.put("title", item.title)
        itemObj.put("artist", item.artist)
        itemObj.put("year", item.year)
        itemObj.put("genre", item.genre)
        itemObj.put("userId", item.userId)
        itemObj.put("pathImage", item.pathImage)

        jsonObj.put("item", itemObj)

        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build() //connectat
        val inputData = Data.Builder().put("data", jsonObj.toString()).build()
        val myWork = OneTimeWorkRequest.Builder(MyWorker::class.java)
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()
        val workId = myWork.id
        activity?.let {
            WorkManager.getInstance(it.applicationContext).apply {
                enqueue(myWork)
                getWorkInfoByIdLiveData(workId).observe(viewLifecycleOwner) { status ->
                    val isFinished = status?.state?.isFinished
                    println("Status : $status")
                }
            }
        }

    }

}

