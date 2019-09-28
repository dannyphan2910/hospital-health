package hu.ait.hospitalhealth

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import hu.ait.hospitalhealth.data.DataHospital
import hu.ait.hospitalhealth.location.MyLocationManager
import android.view.WindowManager
import android.os.Build
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import hu.ait.hospitalhealth.data.HospitalDataPoint
import hu.ait.hospitalhealth.data.Location

class NearbyPlacesActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var myLocationManager: MyLocationManager
    private lateinit var currentLocation: android.location.Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_places)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        myLocationManager = MyLocationManager(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.setAllGesturesEnabled(true)

        requestNeededPermission()

        addMarkers()
    }
//
//    fun getCurrentLocation(location: android.location.Location) {
//        this.currentLocation = location
//
//        positionCamera()
//    }
//
//    fun positionCamera() {
//        val cameraPosition = CameraPosition.Builder()
//            .target(LatLng(currentLocation.latitude, currentLocation.longitude))
//            .zoom(15f)
//            .tilt(30f)
//            .bearing(45f)
//            .build()
//
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
//    }

    fun addMarkers() {
        var allHospitalData = DataHospital(resources.openRawResource(R.raw.datajsonbudapest)).dataHospitals
        allHospitalData.addAll(DataHospital(resources.openRawResource(R.raw.datajsonhanoi)).dataHospitals)
        allHospitalData.addAll(DataHospital(resources.openRawResource(R.raw.datajsonbrandeis)).dataHospitals)

        addMarkerToMap(allHospitalData)

        setMarkerListener()
    }

    private fun addMarkerToMap(allHospitalData: MutableList<HospitalDataPoint>) {
        for (result in allHospitalData) {
            mMap.addMarker(
                MarkerOptions()
                    .position(result.location)
                    .title(result.name)
                    .snippet(
                        getString(
                            R.string.map_marker_rating,
                            result.ratingInfo.rating,
                            result.ratingInfo.user_rating
                        )
                    )
            )
        }
    }

    private fun setMarkerListener() {
        mMap!!.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(marker: Marker): Boolean {
                var textToSave = "${marker.title},${marker.position.latitude},${marker.position.longitude}"

                val clipboard = applicationContext!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val myClip = ClipData.newPlainText(getString(R.string.label_coordinate), textToSave)
                clipboard.primaryClip = myClip

                Toast.makeText(this@NearbyPlacesActivity, getString(R.string.toast_location_saved), Toast.LENGTH_LONG)
                    .show()
                return false
            }
        })
    }

    override fun onStop() {
        super.onStop()
        myLocationManager.stopLocationMonitoring()
    }

    private fun requestNeededPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                Toast.makeText(
                    this,
                    getString(R.string.location_permission_needed), Toast.LENGTH_SHORT
                ).show()
            }

            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
        } else {
            // we already have permission
            mMap.isMyLocationEnabled = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            101 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.location_perm_granted), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.location_perm_not_granted), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
