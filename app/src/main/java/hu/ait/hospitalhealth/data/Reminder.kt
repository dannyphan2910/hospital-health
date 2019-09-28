package hu.ait.hospitalhealth.data

import androidx.room.*
import java.io.Serializable

@Entity(tableName = "reminderlisttable"
//    ,
//    foreignKeys = [
//        ForeignKey(
//            entity = Location::class,
//            parentColumns = ["id"],
//            childColumns = ["locId"]
//        ),
//        ForeignKey(
//            entity = Time::class,
//            parentColumns = ["id"],
//            childColumns = ["timeId"]
//        )]
)
data class Reminder (
    @PrimaryKey(autoGenerate = true) var id: Long? = 0,
    @ColumnInfo(name = "name") var name : String = "",
    @Ignore var location : Location? = null,
//    @ColumnInfo(name = "locId")  var locId: Long? = 0,
    @Ignore var time : Time? = null,
//    @ColumnInfo(name = "timeId") var timeId: Long? = 0,
    @ColumnInfo(name = "details") var details : String = "",
    @ColumnInfo(name = "done") var done : Boolean = false,
    @ColumnInfo(name = "expanded") var expanded: Boolean = false
) : Serializable

@Entity(tableName = "locationtable"
    //,
//    foreignKeys = [
//        ForeignKey(
//            entity = Reminder::class,
//            parentColumns = ["id"],
//            childColumns = ["locId"]
//        )]
)
data class Location (
    @PrimaryKey(autoGenerate = true) var locId: Long? = 0,
    @ColumnInfo(name = "locationName") var locationName : String = "",
    @ColumnInfo(name = "latitude") var latitude: Double = 0.0,
    @ColumnInfo(name = "longitude") var longitude : Double = 0.0
) : Serializable

@Entity(tableName = "timetable"
//    ,
//    foreignKeys = [
//        ForeignKey(
//            entity = Reminder::class,
//            parentColumns = ["id"],
//            childColumns = ["timeId"]
//        )]
)
data class Time (
    @PrimaryKey(autoGenerate = true) var timeId: Long? = 0,
    @ColumnInfo(name = "day") var day : Int = 0,
    @ColumnInfo(name = "month") var month : Int = 0,
    @ColumnInfo(name = "year") var year : Int = 0,

    @ColumnInfo(name = "hour") var hour : Int = 0,
    @ColumnInfo(name = "minute") var minute : Int = 0
) :  Serializable