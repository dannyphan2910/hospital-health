package hu.ait.hospitalhealth.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hu.ait.hospitalhealth.R
import hu.ait.hospitalhealth.ScrollingActivity
import hu.ait.hospitalhealth.data.*
import kotlinx.android.synthetic.main.reminder_row.view.*

class ReminderAdapter : RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    var reminderList = mutableListOf<Reminder>()

    val context : Context
    constructor(context: Context) : super() {
        this.context = context
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView = itemView.card_view
        val tvName = itemView.tvNameReminder
        val tvLocation = itemView.tvLocation
        val tvTime = itemView.tvTime
        val cbDone = itemView.cbDone
        val btnDelete = itemView.btnDelete
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.reminder_row,
                parent,
                false
            )
        )

    override fun getItemCount(): Int = reminderList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var reminder = reminderList.get(holder.adapterPosition)

        setItemData(reminder, holder)

        setClickListener(holder, reminder)
    }

    private fun setItemData(
        reminder: Reminder,
        holder: ViewHolder
    ) {
        var expanded = reminder.expanded
        holder.btnDelete.visibility = if (expanded) View.VISIBLE else View.GONE

        holder.tvName.text = reminder.name
        if (reminder.location != null) holder.tvLocation.text = reminder.location!!.locationName
        else holder.tvLocation.visibility = View.GONE
        holder.tvTime.text =
            "${reminder.time!!.hour}:${reminder.time!!.minute}     ${reminder.time!!.day}/${reminder.time!!.month}/${reminder.time!!.year}"
        holder.cbDone.isChecked = reminder.done
    }

    private fun setClickListener(
        holder: ViewHolder,
        reminder: Reminder
    ) {
        holder.cardView.setOnClickListener {
            (context as ScrollingActivity).showDetailsDialog(
                reminder, holder.adapterPosition
            )
        }

        holder.cardView.setOnLongClickListener {
            var expanded = reminder.expanded
            reminder.expanded = !expanded
            notifyItemChanged(holder.adapterPosition)

            return@setOnLongClickListener true
        }

        holder.btnDelete.setOnClickListener {
            removeItem(holder.adapterPosition)
        }
    }

    fun addItem(reminder: Reminder) {
        reminderList.add(reminder)

        Thread {
            AppDatabase.getInstance(context).reminderDAO().insertItem(reminder)
        }.start()

        notifyItemInserted(reminderList.lastIndex)
    }

    fun updateItem(reminder: Reminder, index: Int) {
        reminderList.set(index, reminder)
        notifyItemChanged(index)
    }

    fun removeItem(index: Int) {
        Thread {

            AppDatabase.getInstance(context).reminderDAO().deleteItem(reminderList.get(index))

            (context as ScrollingActivity).runOnUiThread {
                reminderList.removeAt(index)
                notifyItemRemoved(index)
            }

        }.start()
    }

    fun deleteAll() {
        Thread {
            AppDatabase.getInstance(context).reminderDAO().deleteAll()

            (context as ScrollingActivity).runOnUiThread {
                reminderList.clear()
                notifyDataSetChanged()
            }
        }.start()
    }
}