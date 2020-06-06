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
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import hu.ait.hospitalhealth.data.Base
import hu.ait.hospitalhealth.data.HospitalDataPoint
import hu.ait.hospitalhealth.network.MapsAPI
import kotlinx.android.synthetic.main.activity_nearby_places.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NearbyPlacesActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var myLocationManager: MyLocationManager
    private var currentLocation: android.location.Location? = null
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mapsAPI: MapsAPI

    val BASE_URL = "https://api.foursquare.com/v2/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_places)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        myLocationManager = MyLocationManager(this)

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (intent.hasExtra(AddReminderActivity.GET_LOCATION)) {
            btnCancel.visibility = View.VISIBLE
            tvPickerMode.visibility = View.VISIBLE
        } else {
            btnCancel.visibility = View.INVISIBLE
            tvPickerMode.visibility = View.INVISIBLE
        }

        btnCancel.setOnClickListener {
            finish()
        }

        // retrofit for MapsAPI
        var retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        mapsAPI = retrofit.create(MapsAPI::class.java)
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

        getCurrentLocation()
    }

    fun getCurrentLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                var locationResult = mFusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener {
                    if (it.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        currentLocation = it.result
                        positionCamera()
                    }

                }
            }
        } catch(e: SecurityException)  {
            Log.e("Exception: %s", e.message)
        }
    }

    private fun positionCamera() {
        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(currentLocation!!.latitude, currentLocation!!.longitude))
            .zoom(10f)
            .tilt(30f)
            .bearing(45f)
            .build()

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        addMarkers()
    }

    fun addMarkers() {
        var latlng = currentLocation!!.latitude.toString() + "," + currentLocation!!.longitude.toString()
        val call = mapsAPI.getHospitalDetails(latlng)

        call.enqueue(object : Callback<Base> {
            override fun onResponse(call: Call<Base>, response: Response<Base>) {
                var mapsResponse = response.body()?.response!!.venues
                var allHospitalData = DataHospital(mapsResponse).dataHospitals

                addMarkerToMap(allHospitalData)

                setMarkerListener()
            }
            override fun onFailure(call: Call<Base>, t: Throwable) {
                Toast.makeText(this@NearbyPlacesActivity, t.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun addMarkerToMap(allHospitalData: MutableList<HospitalDataPoint>) {
        for (result in allHospitalData) {
            mMap.addMarker(
                MarkerOptions()
                    .position(result.location)
                    .title(result.name)
                    .snippet(
                        getString(
                            R.string.map_marker_info,
                            result.info.address,
                            result.info.distance
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
