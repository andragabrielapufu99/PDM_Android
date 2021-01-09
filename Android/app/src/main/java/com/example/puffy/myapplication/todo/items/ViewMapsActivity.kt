package com.example.puffy.myapplication.todo.items

import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import com.example.puffy.myapplication.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class ViewMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var location : LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val intent = intent
        if (intent.hasExtra("location")) {
            location = intent.getParcelableExtra<Parcelable>("location") as LatLng
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if(location != null){
            val position = LatLng(location!!.latitude, location!!.longitude)
            mMap.addMarker(MarkerOptions().position(position).title("Current address delivery"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(position))
        }
    }
}