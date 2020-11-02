package com.example.puffy.myapplication.todo.item

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
import com.example.puffy.myapplication.todo.data.Item
import kotlinx.android.synthetic.main.fragment_item_edit.*
import kotlinx.android.synthetic.main.fragment_item_list.*

class ItemEditFragment  : Fragment(){

    companion object{
        const val ITEM_ID = "ITEM_ID"
    }

    private lateinit var viewModel: ItemEditViewModel
    private var itemId : Int? = -1
    private var item : Item? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v("ItemEditFragment", "onCreate")
        arguments?.let {
            if (it.containsKey(ITEM_ID)) {
                itemId = it.getInt(ITEM_ID)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v("ItemEditFragment","onCreateView")
        return inflater.inflate(R.layout.fragment_item_edit,container,false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v("ItemEditFragment","onActivityCreated")
        setupViewModel()
        saveBtn.setOnClickListener{
            Log.v("ItemEditFragment","update item")
            val i = item
            if(i!=null){
                println(i.id)
                println(i.toString())
                i.title = itemTitle.text.toString()
                i.artist = itemArtist.text.toString()
                i.year = itemYear.text.toString().toInt()
                i.genre = itemGenre.text.toString()
                viewModel.saveOrUpdateItem(i)
            }
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(ItemEditViewModel::class.java)

        viewModel.fetching.observe(viewLifecycleOwner) { fetching ->
            Log.v("ItemEditFragment", "update fetching")
            progress.visibility = if (fetching) View.VISIBLE else View.GONE
        }

        viewModel.exception.observe(viewLifecycleOwner) { exception ->
            if (exception != null) {
                Log.v("ItemEditFragment", "update fetching error")
                val message = "Fetching exception ${exception.message}"
                val parentActivity = activity?.parent
                if (parentActivity != null) {
                    Toast.makeText(parentActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.completed.observe(viewLifecycleOwner) { completed ->
            if (completed) {
                Log.v("ItemEditFragment", "completed, navigate back")
                findNavController().popBackStack()
            }
        }

        val id = itemId
        if (id == -1) {
            item = Item(-1, "","",-1,"")
        } else {
            if (id != null) {
                viewModel.getById(id).observe(viewLifecycleOwner) {
                    Log.v("ItemEditFragment", "update items")
                    if (it != null) {
                        item = it
                        itemTitle.setText(it.title.toString())
                        itemArtist.setText(it.artist.toString())
                        itemYear.setText(it.year.toString())
                        itemGenre.setText(it.genre.toString())
                    }
                }
            }
        }
    }

}