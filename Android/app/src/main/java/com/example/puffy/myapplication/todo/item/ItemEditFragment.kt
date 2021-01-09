package com.example.puffy.myapplication.todo.item

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.example.puffy.myapplication.R
import com.example.puffy.myapplication.todo.data.Item
import com.example.puffy.myapplication.todo.data.ItemRepository
import com.example.puffy.myapplication.todo.items.ViewMapsActivity
import com.google.android.gms.maps.model.LatLng
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_item_edit.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class ItemEditFragment  : Fragment(){

    companion object{
        const val ITEM_ID = "ITEM_ID"
    }

    private lateinit var viewModel: ItemEditViewModel
    private var itemId : Int? = -1
    private var item : Item? = null
    private val tagName: String = "ItemEditFragment"

    //photo stuff
    private val REQUEST_CODE_PERMISSION = 10
    private val REQUEST_CAPTURE_IMAGE = 1
    private val REQUEST_PICK_IMAGE = 2
    private val REQUEST_LOCATION = 3

    lateinit var currentPhotoPath : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(tagName, "onCreate")
        arguments?.let {
            if (it.containsKey(ITEM_ID)) {
                itemId = it.getInt(ITEM_ID)
            }
        }
    } //end onCreate

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    private fun checkPermissions(){
        if(activity?.let { ContextCompat.checkSelfPermission(
                it.applicationContext,
                Manifest.permission.CAMERA
            ) } != PackageManager.PERMISSION_GRANTED){
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_PERMISSION
                )
            }
        }
    }

    private fun openGallery(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_PICK_IMAGE)
    }

    private fun openCamera(){
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            activity?.let { intent.resolveActivity(it.packageManager).also {
                    val photoFile: File? = try{
                        createTemporaryFile()
                    }catch (ex: IOException){
                        null
                    }
                    Log.d(tagName, "photofile $photoFile")
                    photoFile.also {
                        val photoURI = it?.let { it1 ->
                            context?.let { it2 ->
                                FileProvider.getUriForFile(
                                    it2,
                                    "com.example.puffy.myapplication.fileprovider",
                                    it1
                                )
                            }
                        } //end photoURI
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(intent, REQUEST_CAPTURE_IMAGE)
                    }
                }
            } //end activity
        } //end Intent
    }

    private fun createTemporaryFile() : File{
        val timestamp: String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val storageDir = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PHOTO_${timestamp}", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_CAPTURE_IMAGE){
                val uri = Uri.parse(currentPhotoPath)
                item?.pathImage = currentPhotoPath
                imageView.setImageURI(uri)

            }else if(requestCode == REQUEST_PICK_IMAGE){
                val uri = data?.data
                val file = uri?.let { createFileFromUri(it) }
                if(file != null){
                    item?.pathImage = file.path
                }
                imageView.setImageURI(uri)
            }else if(requestCode == REQUEST_LOCATION){
                val obj = data?.extras?.get("location")
                if(obj != null){
                    val location = obj as LatLng
                    item?.latitude = location.latitude
                    item?.longitude = location.longitude
                }

            }
        }
    }

    private fun createFileFromUri(uri: Uri) : File? {
        val image : Bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, uri)
        val bytes : ByteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 40, bytes)
        val filetemp : File = createTemporaryFile()
        if(filetemp.exists()){
            val fos : FileOutputStream = FileOutputStream(filetemp)
            fos.write(bytes.toByteArray())
            fos.close()
            return filetemp
        }
        return null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(tagName, "onCreateView")
        return inflater.inflate(R.layout.fragment_item_edit, container, false)
    }

    private fun validateItem(item: Item){
        var errors: String = ""
        var blacklist: String = "[,:?/.]"
        if(item.title.equals("") || item.title.contains(blacklist.toRegex()) || item.title.contains(
                '\\'
            )){
            errors += "Title field cannot be empty or cannot contains the next caracters : , : ? / \\ . !"
        }
        if(item.artist.equals("") || item.artist.contains(blacklist.toRegex()) || item.artist.contains(
                '\\'
            )){
            errors += "Artist field cannot be empty or cannot contains the next caracters : , : ? / \\ . !"
        }
        if(!(item.year >= 1000 && item.year.toInt() <= 9999)){
            errors += "Year field must be a positive number of 4 digits!"
        }
        if(item.genre.equals("") || item.genre.contains(blacklist.toRegex()) || item.genre.contains(
                '\\'
            )){
            errors += "Genre field cannot be empty or cannot contains the next caracters : , : ? / \\ . !"
        }
        if(!errors.equals("")){
            throw Exception(errors)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(tagName, "onActivityCreated")
        setupViewModel()
        saveBtn.setOnClickListener{
            Log.v(tagName, "update item")
            val i = item
            if(i!=null){
                i.title = itemTitle.text.toString()
                i.artist = itemArtist.text.toString()
                try {
                    i.year = itemYear.text.toString().toInt()
                }catch (e: NumberFormatException){
                    i.year = -1;
                }

                i.genre = itemGenre.text.toString()
                try {
                    validateItem(i)
                    viewModel.saveOrUpdateItem(i)
                }catch (e: Exception){
                    Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
                }

            }
        }
        logoutBtn.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(R.id.fragment_login)
        }
        capturePhoto.setOnClickListener{
            openCamera()
        }
        pickPhoto.setOnClickListener{
            openGallery()
        }
        mapBtn.setOnClickListener{
            val intent : Intent = Intent(context, EditMapsActivity::class.java)
            val location = item?.latitude?.let { it1 -> item!!.longitude?.let { it2 ->
                LatLng(it1, it2) } }
            intent.putExtra("location", location)
            startActivityForResult(intent, REQUEST_LOCATION)
            //startActivityForResult(Intent(context, EditMapsActivity::class.java), REQUEST_LOCATION)
        }

    } //end onActivityCreated

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(ItemEditViewModel::class.java)

        viewModel.fetching.observe(viewLifecycleOwner) {
            Log.v(tagName, "update fetching")
            progress.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.exception.observe(viewLifecycleOwner) {
            if (it != null) {
                Log.v(tagName, "update fetching error")
                Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.completed.observe(viewLifecycleOwner) {
            if (it) {
                Log.v(tagName, "completed, navigate back")
                if(viewModel.exception.value == null){
                    if(!ItemRepository.getNetworkStatus()){
                        Toast.makeText(activity, "Saved locally", Toast.LENGTH_SHORT).show()
                    }
                    findNavController().popBackStack()
                }
            }
        }

        val id = itemId
        if (id == -1) {
            item = Item(-1, "", "", -1, "", "", "", null, null)
        } else {
            if (id != null) {
                viewModel.getItemById(id).observe(viewLifecycleOwner){
                    if(it != null){
                        item = it
                        itemTitle.setText(it.title)
                        itemArtist.setText(it.artist)
                        itemYear.setText(it.year.toString())
                        itemGenre.setText(it.genre)
                        if(it.pathImage?.isNotEmpty() == true){
                            val file = File(it.pathImage)
                            var uri : Uri?
                            file.also {
                                uri = it?.let { it1 ->
                                    context?.let { it2 ->
                                        FileProvider.getUriForFile(
                                            it2,
                                            "com.example.puffy.myapplication.fileprovider",
                                            it1
                                        )
                                    }
                                }
                            }
                            Picasso
                                .with(context)
                                .load(uri)
                              //  .rotate(90F)
                                .into(imageView)
                        }
                    }
                }
            }
        }
    } //end setupViewModel


}