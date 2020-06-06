package hu.ait.hospitalhealth.data

import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import hu.ait.hospitalhealth.R
import hu.ait.hospitalhealth.network.MapsAPI
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.DataInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.lang.Exception
import java.util.*

data class HospitalDataPoint(
    var name: String,
    var location: LatLng,
    var info: LocationInfo
)

data class LocationInfo (
    var address: String,
    var distance: Int
)

class DataHospital {
    var dataHospitals = mutableListOf<HospitalDataPoint>()

    constructor(venues: List<Venues>) {
        addHospitalData(venues)
    }

    private fun addHospitalData(venues: List<Venues>) : Boolean {
        venues.forEach {
            dataHospitals.add(
                HospitalDataPoint(
                    it.name,
                    LatLng(
                        it.location.lat,
                        it.location.lng
                    ),
                    LocationInfo(
                        it.location.formattedAddress.joinToString(", "),
                        it.location.distance
                    )
                )
            )
        }

        return dataHospitals.size > 0
    }
}