package hu.ait.hospitalhealth

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_add_reminder.*
import android.widget.TimePicker
import android.app.TimePickerDialog
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.content.getSystemService
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import java.util.*
import kotlin.collections.ArrayList


class AddReminderActivity : AppCompatActivity() {

    companion object {
        val RETURN_RESULT = "RETURN_RESULT"

        val NAME_RESULT = "NAME_RESULT"
        val DATE_RESULT = "DATE_RESULT"
        val TIME_RESULT = "TIME_RESULT"
        val DETAIL_RESULT = "DETAILS_RESULT"
        val LOCATION_RESULT = "LOCATION_RESULT"

        val GET_LOCATION = "GET_LOCATION"
    }

    lateinit var resultBundle: Bundle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reminder)

        initView()

        getLocation()
    }

    private fun initButton() {
        btnOk.setOnClickListener {
            if (hasValidInput()) {
                sendResult()
            }
        }

        btnCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    fun initView() {
        resultBundle = Bundle()

        initDatePicker()

        initTimePicker()

        initButton()
    }

    private fun initTimePicker() {
        timePicker.setIs24HourView(false)

        val thisHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val thisMinute = Calendar.getInstance().get(Calendar.MINUTE)

        timePicker.hour = thisHour
        timePicker.minute = thisMinute

        resultBundle.putIntegerArrayList(TIME_RESULT, arrayListOf(thisHour, thisMinute))

        timePicker.setOnTimeChangedListener { view, hourOfDay, minute ->
            var result = arrayListOf(hourOfDay, minute)
            resultBundle.putIntegerArrayList(TIME_RESULT, result)
        }
    }

    private fun initDatePicker() {
        var calendar = Calendar.getInstance()
        val thisYear = calendar.get(Calendar.YEAR)
        val thisMonth = calendar.get(Calendar.MONTH)
        val thisDay = calendar.get(Calendar.DAY_OF_MONTH)

        btnOk.visibility = View.INVISIBLE
        resultBundle.putIntegerArrayList(DATE_RESULT, arrayListOf(thisDay, thisMonth, thisYear))

        datePicker.init(
            thisYear,
            thisMonth,
            thisDay,
            DatePicker.OnDateChangedListener { view, year, monthOfYear, dayOfMonth ->
                btnOk.visibility = View.INVISIBLE
                if (year > thisYear
                    || (year == thisYear && monthOfYear > thisMonth)
                    || (year == thisYear && monthOfYear == thisMonth && dayOfMonth > thisDay)
                ) {
                    btnOk.visibility = View.VISIBLE
                    var result = arrayListOf(dayOfMonth, monthOfYear, year)
                    resultBundle.putIntegerArrayList(DATE_RESULT, result)
                } else {
                    Toast.makeText(
                        this@AddReminderActivity,
                        getString(R.string.toast_invalid_datetime),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    fun getLocation() {
        btnSetLocation.setOnClickListener {
            val clipboard = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            if (clipboard.primaryClip == null || clipboard.primaryClip.getItemAt(0).text == null || clipboard.primaryClip.getItemAt(0).text.isEmpty() ||
                clipboard.primaryClip.getItemAt(0).text.toString().split(",").size < 3) {
                setLocationNotSuccessful()
            } else {
                setLocationSuccessful(clipboard)
            }
        }

        btnSetLocation.setOnLongClickListener {
            setLocationNotSuccessful()
        }
    }

    private fun setLocationSuccessful(clipboard: ClipboardManager) {
        val dataString = clipboard.primaryClip.getItemAt(0).text.toString()

        Toast.makeText(this@AddReminderActivity, getString(R.string.toast_location_set, dataString), Toast.LENGTH_LONG)
            .show()

        resultBundle.putString(LOCATION_RESULT, dataString)
    }

    private fun setLocationNotSuccessful() : Boolean {
        Toast.makeText(
            this@AddReminderActivity,
            getString(R.string.toast_set_location_not_successful),
            Toast.LENGTH_LONG
        ).show()

        var mapIntent = Intent(
            this@AddReminderActivity,
            NearbyPlacesActivity::class.java
        )
        mapIntent.putExtra(GET_LOCATION, "true")
        startActivity(mapIntent)
        return true
    }

    fun sendResult() {
        if (hasValidInput()) {
            resultBundle.putString(NAME_RESULT, etName.text.toString())
        }

        resultBundle.putString(DETAIL_RESULT, etDetails.text.toString())

        var intentToReturn = Intent()
        intentToReturn.putExtra(RETURN_RESULT, resultBundle)
        setResult(Activity.RESULT_OK, intentToReturn)
        finish()
    }

    fun hasValidInput(): Boolean {
        if (etName.text.isNotEmpty()) {
            return true
        } else {
            etName.error = getString(R.string.error_required_input)
            return false
        }
    }
}
