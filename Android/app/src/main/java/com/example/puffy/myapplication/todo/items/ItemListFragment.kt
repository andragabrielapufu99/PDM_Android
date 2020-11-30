package com.example.puffy.myapplication.todo.items
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.example.puffy.myapplication.R
import com.example.puffy.myapplication.auth.data.AuthRepository
import com.example.puffy.myapplication.common.RemoteDataSource
import kotlinx.android.synthetic.main.fragment_item_list.*


class ItemListFragment : Fragment() {

    private lateinit var itemListAdapter : ItemListAdapter
    private lateinit var itemsModel : ItemListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("ItemListFragment", "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_item_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v("ItemListFragment", "onActivityCreated")
        if(!AuthRepository.isAuthenticated){
            findNavController().navigate(R.id.fragment_login)
            return
        }
        setupItemList()
        fab.setOnClickListener {
            Log.v("ItemListFragment", "add new item")
            findNavController().navigate(R.id.fragment_item_edit)
        }
        logoutBtn.setOnClickListener {
            itemsModel.logout()
            findNavController().navigate(R.id.fragment_login)
        }
    }

    private fun setupItemList(){
        itemListAdapter = ItemListAdapter(this)
        item_list.adapter = itemListAdapter
        itemsModel = ViewModelProvider(this).get(ItemListViewModel::class.java)

        itemsModel.items.observe(viewLifecycleOwner) {
            Log.v("ItemListFragment", "update items")
            itemListAdapter.items = it
        }

        itemsModel.loading.observe(viewLifecycleOwner) {
            Log.i("ItemListFragment", "update loading")
            fetchProgress.visibility = if (it) View.VISIBLE else View.GONE
        }
        itemsModel.loadingError.observe(viewLifecycleOwner) {
            if (it != null) {
                Log.i("ItemListFragment", "update loading error")
                val message = "Loading exception ${it.message}"
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
            }
        }
        itemsModel.refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v("ItemListFragment", "onDestroy")
    }
}

