package hu.ait.hospitalhealth

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import hu.ait.hospitalhealth.adapter.ReminderAdapter
import hu.ait.hospitalhealth.data.AppDatabase
import hu.ait.hospitalhealth.data.Location
import hu.ait.hospitalhealth.data.Reminder
import hu.ait.hospitalhealth.data.Time
import kotlinx.android.synthetic.main.activity_scrolling.*

class ScrollingActivity : AppCompatActivity(), DetailsDialog.DetailsHandler {

    companion object {
        val KEY_DETAILS = "KEY_DETAILS"
        val NONE = "NONE"
    }

    lateinit var reminderAdapter: ReminderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar)

        supportActionBar?.title = ""

        setTransparentStatusBar()

        initFab()

        initRecyclerView()
    }

    private fun initFab() {
        fab.setOnClickListener {
            var intentAddReminder = Intent(
                this@ScrollingActivity,
                AddReminderActivity::class.java
            )
            startActivityForResult(intentAddReminder, 1001)
        }
    }

    private fun setTransparentStatusBar() {
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
        }
        if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            var bundleResult = data?.getBundleExtra(AddReminderActivity.RETURN_RESULT)

            var reminderName = bundleResult?.getString(AddReminderActivity.NAME_RESULT)

            var timeResult = getResultDateTime(bundleResult)

            var locationResult: Location? = getResultLocation(bundleResult)

            var detailsResult = bundleResult?.getString(AddReminderActivity.DETAIL_RESULT, "")
            reminderAdapter.addItem(
                Reminder(
                    null,
                    reminderName!!,
                    locationResult,
                    timeResult,
                    detailsResult!!,
                    done = false,
                    expanded = false
                )
            )
        }
    }

    private fun getResultLocation(bundleResult: Bundle?): Location? {
        var locationString = bundleResult?.getString(AddReminderActivity.LOCATION_RESULT, NONE)
        var locationResult: Location? = null

        if (locationString != NONE) {
            var dataStringArr = locationString!!.split(",")

            locationResult = Location(
                null,
                dataStringArr.get(0),
                dataStringArr.get(1).toDouble(),
                dataStringArr.get(2).toDouble()
            )
        }
        return locationResult
    }

    private fun getResultDateTime(bundleResult: Bundle?): Time {
        var arrDate = bundleResult?.getIntegerArrayList(AddReminderActivity.DATE_RESULT)
        var arrTime = bundleResult?.getIntegerArrayList(AddReminderActivity.TIME_RESULT)

        var timeResult = Time(
            null,
            arrDate!!.get(0),
            arrDate!!.get(1),
            arrDate!!.get(2),

            arrTime!!.get(0),
            arrTime!!.get(1)
        )
        return timeResult
    }

    private fun initRecyclerView() {
        reminderAdapter = ReminderAdapter(this)
        recyclerItem.adapter = reminderAdapter

        // adds lines btw list items
        var itemDivider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        recyclerItem.addItemDecoration(itemDivider)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_scrolling, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item)
    }

    var detailsIndex: Int = -1

    public fun showDetailsDialog(reminder: Reminder, index: Int) {
        detailsIndex = index

        val detailsDialog = DetailsDialog()

        val bundle = Bundle()
        bundle.putSerializable(KEY_DETAILS, reminder)
        detailsDialog.arguments = bundle

        detailsDialog.show(supportFragmentManager, getString(R.string.tag_details_dialog))
    }

    override fun reminderCreated(reminder: Reminder) {
        Thread {
            var newId = AppDatabase.getInstance(this@ScrollingActivity).reminderDAO().insertItem(reminder)
            reminder.id = newId

            runOnUiThread {
                reminderAdapter.addItem(reminder)
            }

        }.start()
    }
}
