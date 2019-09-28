package hu.ait.hospitalhealth.data

import com.google.android.gms.maps.model.LatLng
import hu.ait.hospitalhealth.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.DataInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.lang.Exception
import java.util.*

data class HospitalDataPoint(
    var name: String,
    var location: LatLng,
    var ratingInfo: Rating
)

data class Rating (
    var rating : Double,
    var user_rating : Int
)

class DataHospital {
    var dataHospitals = mutableListOf<HospitalDataPoint>()

    constructor(inputStream: InputStream) {

        var rawString = ""

        try {
            var fileScanner = Scanner(inputStream)
            while (fileScanner.hasNextLine()) rawString += fileScanner.nextLine()

            fileScanner.close()

            var dataLoaded = JSONObject(rawString).getJSONArray("results")

            addHospitalData(dataLoaded)
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    private fun addHospitalData(dataLoaded: JSONArray) {
        for (i in 0..dataLoaded.length() - 1) {
            var thisResult = dataLoaded.getJSONObject(i)

            dataHospitals.add(
                HospitalDataPoint(
                    thisResult.getString("name"),
                    LatLng(
                        thisResult.getJSONObject("geometry").getJSONObject("location").getDouble("lat"),
                        thisResult.getJSONObject("geometry").getJSONObject("location").getDouble("lng")
                    ),
                    Rating(
                        thisResult.getDouble("rating"),
                        thisResult.getInt("user_ratings_total")
                    )
                )
            )
        }
    }
}