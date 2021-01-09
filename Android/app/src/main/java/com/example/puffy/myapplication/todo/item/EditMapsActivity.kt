package com.example.puffy.myapplication.todo.item

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Parcelable
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.puffy.myapplication.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*

class EditMapsActivity : AppCompatActivity(),
    GoogleMap.OnMapClickListener,
    GoogleMap.OnMapLongClickListener,
    GoogleMap.OnCameraIdleListener,
    OnMapReadyCallback,
    GoogleMap.OnMyLocationClickListener,
    GoogleMap.OnMyLocationButtonClickListener,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var map : GoogleMap
    private var marker : Marker? = null
    private var location : LatLng? = null
    private val REQUEST_CODE = 11
    private var isDenial : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        val intent = intent
        if (intent.hasExtra("location")) {
            val l = intent.getParcelableExtra<Parcelable>("location")
            if(l != null){
                location = l as LatLng
            }

        }
        done.setOnClickListener{
            prepareResponse()
        }
    }

    private fun prepareResponse(){
        val intent : Intent = Intent()
        intent.putExtra("location", location)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onMapClick(point: LatLng?) {
        if(point != null){
            marker?.remove()
            val position = LatLng(point.latitude, point.longitude)
            location = position
            marker = map.addMarker(MarkerOptions().position(position).title("Chosen delivery address"))
            map.moveCamera(CameraUpdateFactory.newLatLng(position))
        }
    }

    override fun onMapLongClick(point: LatLng?) {
        if(point != null){
            marker?.remove()
            val position = LatLng(point.latitude, point.longitude)
            location = position
            marker = map.addMarker(MarkerOptions().position(position).title("Chosen delivery address"))
            map.moveCamera(CameraUpdateFactory.newLatLng(position))
        }
    }

    override fun onCameraIdle() {
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap ?: return
        if(!checkPermissions()){
            requestPermissions()
        }else{
            enableMyLocation()
            map.setOnMyLocationClickListener(this)
            map.setOnMyLocationButtonClickListener(this)
            map.setOnMapClickListener(this)
            map.setOnMapLongClickListener(this)
            map.setOnCameraIdleListener(this)
            if(location != null){
                marker = map.addMarker(MarkerOptions().position(location!!).title("Chosen delivery address"))
                map.moveCamera(CameraUpdateFactory.newLatLng(location))
            }

        }
    }

    override fun onMyLocationClick(myLocation: Location) {
        if(myLocation != null){
            val position = LatLng(myLocation.latitude, myLocation.longitude)
            location = position
            marker = map.addMarker(MarkerOptions().position(position).title("Chosen delivery address"))
            map.moveCamera(CameraUpdateFactory.newLatLng(position))
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        marker?.remove()
        return false
    }

    private fun checkPermissions() : Boolean{
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions(){
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var found : Boolean = false
        if(requestCode == REQUEST_CODE){
            for(i in permissions.indices){
                if(permissions[i] == Manifest.permission.ACCESS_FINE_LOCATION){
                    isDenial = grantResults[i] == PackageManager.PERMISSION_DENIED
                    found = true
                    break
                }
            }
            if(!found){
                isDenial = true
            }
        }
    }

    private fun enableMyLocation(){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        }
    }
}