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
        setupItemList()
        fab.setOnClickListener {
            Log.v("ItemListFragment", "add new item")
            findNavController().navigate(R.id.fragment_item_edit)
        }
    }

    private fun setupItemList(){
        itemListAdapter = ItemListAdapter(this)
        item_list.adapter = itemListAdapter
        itemsModel = ViewModelProvider(this).get(ItemListViewModel::class.java)
        RemoteDataSource.setItems(itemListAdapter)
        itemListAdapter.onValueChanged = { oldValue, newValue ->
            Thread(Runnable {
                if(!newValue.equals("")){
                    getActivity()?.runOnUiThread(java.lang.Runnable {
                        Toast.makeText(activity,newValue,Toast.LENGTH_LONG).show()
                        itemsModel.refresh()
                    })

                }
            }).start()

        }
        itemsModel.items.observe(viewLifecycleOwner) { items ->
            Log.v("ItemListFragment", "update items")
            itemListAdapter.items = items
        }

        itemsModel.loading.observe(viewLifecycleOwner) { loading ->
            Log.i("ItemListFragment", "update loading")
            fetchProgress.visibility = if (loading) View.VISIBLE else View.GONE
        }
        itemsModel.loadingError.observe(viewLifecycleOwner) { exception ->
            if (exception != null) {
                Log.i("ItemListFragment", "update loading error")
                val message = "Loading exception ${exception.message}"
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

