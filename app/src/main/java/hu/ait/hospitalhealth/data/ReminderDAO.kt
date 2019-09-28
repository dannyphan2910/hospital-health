package hu.ait.hospitalhealth.data

import androidx.room.*

@Dao
interface ReminderDAO {
    @Query("SELECT * FROM reminderlisttable")
    fun getAllItems(): List<Reminder>

    @Insert
    fun insertItem(reminder: Reminder): Long

    @Delete
    fun deleteItem(reminder: Reminder)

    @Update
    fun updateItem(reminder: Reminder)

    @Query("DELETE FROM reminderlisttable")
    fun deleteAll()
}