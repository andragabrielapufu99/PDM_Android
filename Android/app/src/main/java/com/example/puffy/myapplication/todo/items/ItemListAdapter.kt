package com.example.puffy.myapplication.todo.items

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.puffy.myapplication.R
import com.example.puffy.myapplication.todo.data.Item

import com.example.puffy.myapplication.todo.item.ItemEditFragment
import kotlinx.android.synthetic.main.view_item_list.view.*


class ItemListAdapter(
    private val fragment: Fragment
) : RecyclerView.Adapter<ItemListAdapter.ViewHolder>(){
    var items = emptyList<Item>()
            set(value){
                field = value
                notifyDataSetChanged()
            }
    private lateinit var onItemClick : View.OnClickListener

    init {
        //click on an item
        onItemClick = View.OnClickListener { view ->
            val item = view.tag as Item
            //navigate to its fragment
            fragment.findNavController().navigate(R.id.fragment_item_edit, Bundle().apply {
                putInt(ItemEditFragment.ITEM_ID, item.id)
            })
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.text
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_item_list, parent, false)
        Log.v("ItemListAdapter", "onCreateViewHolder")
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.v("ItemListAdapter", "onBindViewHolder $position")
        val item = items[position]
        holder.itemView.tag = item
        holder.textView.text = item.toString()
        holder.itemView.setOnClickListener(onItemClick)
    }

    fun filterByText(text : String) : Boolean{
        var newItems : MutableList<Item> = ArrayList()
        items.forEach { item ->
            if(item.title.contains(text) || item.artist.contains(text) || item.genre.contains(text) || item.year.toString().contains(text)){
                newItems.add(item)
            }
        }
        if(newItems.size > 0){
            items = newItems
            return true
        }
        return false
    }
}