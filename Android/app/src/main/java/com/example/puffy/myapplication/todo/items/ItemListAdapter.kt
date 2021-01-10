package com.example.puffy.myapplication.todo.items

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationSet
import android.widget.*
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.puffy.myapplication.R
import com.example.puffy.myapplication.todo.data.Item
import com.example.puffy.myapplication.todo.item.ItemEditFragment
import com.google.android.gms.maps.model.LatLng
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.view_item_list.view.*
import java.io.File

class ItemListAdapter(private val fragment: Fragment) : RecyclerView.Adapter<ItemListAdapter.ViewHolder>(){

    var items = emptyList<Item>()
            set(value){
                field = value
                notifyDataSetChanged()
            }

    private lateinit var onItemClick : View.OnClickListener
    private val tagName: String = "ItemListAdapter"
    private var searchBar: SearchView?
    private var progressBar: ProgressBar?

    init {
        //click on an item
        onItemClick = View.OnClickListener { view ->
            val item = view.tag as Item

            //navigate to item edit fragment
            fragment.findNavController().navigate(R.id.action_ItemListFragment_to_ItemEditFragment, Bundle().apply {
                putInt(ItemEditFragment.ITEM_ID, item.id)
            })

        }
        searchBar = fragment.activity?.findViewById(R.id.searchBar)
        progressBar = fragment.activity?.findViewById(R.id.fetchProgress)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.imageView
        val textView: TextView = view.text
        val viewLocationBtn: Button = view.viewLocation
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_item_list, parent, false)
        Log.v(tagName, "onCreateViewHolder")
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.v(tagName, "onBindViewHolder $position")
        val item = items[position]
        holder.itemView.tag = item
        holder.imageView.setImageURI(null)
        holder.imageView.visibility = View.GONE
        if(item.pathImage?.isNotEmpty() == true){
            val file = File(item.pathImage)
            if(file.exists()){
                var uri : Uri?
                file.also {
                    uri = it?.let { it1 ->
                        fragment.context?.let { it2 ->
                            FileProvider.getUriForFile(
                                it2,
                                "com.example.puffy.myapplication.fileprovider",
                                it1
                            )
                        }
                    }
                }
                Picasso
                    .with(fragment.context)
                    .load(uri)
                    //.rotate(90F)
                    .into(holder.imageView)
                holder.imageView.visibility = View.VISIBLE
            }
        }
        holder.textView.text = item.toString()
        holder.itemView.setOnClickListener(onItemClick)
        if(!(item.latitude != null && item.longitude != null)){
            holder.viewLocationBtn.visibility = View.GONE
        }else{
            holder.viewLocationBtn.visibility = View.VISIBLE
        }
        holder.viewLocationBtn.setOnClickListener{
            item.latitude?.let { it1 -> item.longitude?.let { it2 -> seeLocation(it1, it2) } }
        }

        searchBar?.bringToFront()
        progressBar?.bringToFront()
    }

    private fun seeLocation(latitude : Double, longitude : Double){
        val intent : Intent = Intent(fragment.activity, ViewMapsActivity::class.java)
        val location = LatLng(latitude, longitude)
        intent.putExtra("location", location)
        fragment.context?.startActivity(intent)
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