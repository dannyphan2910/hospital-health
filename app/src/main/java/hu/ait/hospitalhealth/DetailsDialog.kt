package hu.ait.hospitalhealth

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import hu.ait.hospitalhealth.data.AppointmentLocation
import hu.ait.hospitalhealth.data.Reminder
import kotlinx.android.synthetic.main.activity_nearby_places.*
import kotlinx.android.synthetic.main.details_dialog_layout.view.*
import kotlinx.android.synthetic.main.reminder_row.view.tvNameReminder
import java.lang.RuntimeException

class DetailsDialog : DialogFragment(), OnMapReadyCallback {


    interface DetailsHandler {
        fun reminderCreated(reminder: Reminder)
    }

    lateinit var detailsHandler: DetailsHandler
    private lateinit var mMapDetails: GoogleMap
    private var location: AppointmentLocation? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is DetailsHandler) {
            detailsHandler = context
        } else {
            throw Throwable(getString(R.string.throwable_interface_not_implemented))
        }
    }

    lateinit var tvNameReminder: TextView
    lateinit var tvDateTime: TextView
    lateinit var tvDetails: TextView
    lateinit var tvLocation: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(requireContext())

        dialogBuilder.setTitle(getString(R.string.details_dialog_title))
        val dialogView = requireActivity().layoutInflater.inflate(
            R.layout.details_dialog_layout, null
        )

        initItemsDialog(dialogView)

        dialogBuilder.setView(dialogView)

        invokeDetailsDialog()

        dialogBuilder.setPositiveButton(getString(R.string.btn_ok)) { dialog, which ->
            dialog.dismiss()
        }

        return dialogBuilder.create()
    }

    private fun initItemsDialog(dialogView: View) {
        tvNameReminder = dialogView.tvNameReminder
        tvDateTime = dialogView.tvDateTimeDialog
        tvDetails = dialogView.tvDetailsDialog
        tvLocation = dialogView.tvLocationDialog

        val mapFragment = activity?.supportFragmentManager!!
            .findFragmentById(R.id.mapViewDetails) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun invokeDetailsDialog() {
        if (arguments != null && arguments!!.containsKey(ScrollingActivity.KEY_DETAILS)) {
            val reminder = arguments!!.getSerializable(ScrollingActivity.KEY_DETAILS) as Reminder
            location = reminder.location
            tvNameReminder.text = reminder.name
            tvDateTime.text =
                getString(R.string.dialog_datetime) + "${reminder.time!!.hour}:${reminder.time!!.minute} ${reminder.time!!.day}/${reminder.time!!.month}/${reminder.time!!.year}"
            tvDetails.text = getString(R.string.dialog_details, reminder.details)
            tvLocation.text =
                if (reminder.location != null) getString(R.string.dialog_location, reminder.location!!.locationName)
                else getString(R.string.location_not_specified)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMapDetails = googleMap
        mMapDetails.uiSettings.setAllGesturesEnabled(true)
        mMapDetails.uiSettings.isZoomControlsEnabled = true

        if (location != null) {
            mMapDetails.addMarker(
                MarkerOptions()
                    .position(LatLng(location!!.latitude, location!!.longitude))
                    .title(location!!.locationName)
            )

            positionCamera()
        }
    }

    fun positionCamera() {
        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(location!!.latitude, location!!.longitude))
            .zoom(15f)
            .tilt(30f)
            .bearing(45f)
            .build()

        mMapDetails.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val mapFragment = fragmentManager!!.findFragmentById(R.id.mapViewDetails)
        if (mapFragment != null) {
            fragmentManager!!.beginTransaction().remove(mapFragment).commit()
        }
    }

}