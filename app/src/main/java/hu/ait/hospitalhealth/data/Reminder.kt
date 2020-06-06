package hu.ait.hospitalhealth.data

import androidx.room.*
import java.io.Serializable

@Entity(tableName = "reminderlisttable")
data class Reminder (
    @PrimaryKey(autoGenerate = true) var id: Long? = 0,
    @ColumnInfo(name = "name") var name : String = "",
    @Embedded var location : AppointmentLocation? = null,
    @Embedded var time : AppointmentTime,
    @ColumnInfo(name = "details") var details : String = "",
    @ColumnInfo(name = "done") var done : Boolean = false,
    @ColumnInfo(name = "expanded") var expanded: Boolean = false
) : Serializable

data class AppointmentLocation (
    var locationName : String = "",
    var latitude: Double = 0.0,
    var longitude : Double = 0.0
)

data class AppointmentTime (
    var day : Int = 0,
    var month : Int = 0,
    var year : Int = 0,

    var hour : Int = 0,
    var minute : Int = 0
)