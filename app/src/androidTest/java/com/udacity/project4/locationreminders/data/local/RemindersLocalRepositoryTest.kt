package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    //  TODO: Add testing implementation to the RemindersLocalRepository.kt

    // Executes each task synchronously using Architecture Components.

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersLocalRepository: RemindersLocalRepository

    private lateinit var database: RemindersDatabase

    //setup database
    @Before
    fun setup() {
        // Using an in memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        remindersLocalRepository = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Main
        )
    }

    //clear database
    @After
    fun cleanUp() {
        database.close()
    }


    @Test
    fun saveReminder_retrieveReminderById() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val reminder = ReminderDTO("My home", "Get my home", "abu sliem", 30.7858127, 31.7473426)
        remindersLocalRepository.saveReminder(reminder)

        // WHEN  - reminder retrieved by ID.
        val result = remindersLocalRepository.getReminder(reminder.id) as? Result.Success

        // THEN - Same reminder is returned.
        assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        assertThat(result.data.title, `is`(reminder.title))
        assertThat(result.data.description, `is`(reminder.description))
        assertThat(result.data.latitude, `is`(reminder.latitude))
        assertThat(result.data.longitude, `is`(reminder.longitude))
        assertThat(result.data.location, `is`(reminder.location))
    }


    @Test
    fun deleteReminders_EmptyList()= runBlocking {
        // GIVEN - A new items(reminders) saved in the database.
        val reminder = ReminderDTO("My home", "Get my home", "abu sliem", 30.7858127, 31.7473426)
        remindersLocalRepository.saveReminder(reminder)
        // WHEN - Delete all items(Reminders) and try to retrieve all items(reminders)
        remindersLocalRepository.deleteAllReminders()
        val result = remindersLocalRepository.getReminders()
        // THEN - expect we retrieve no items cause we deleted all previously.
        assertThat(result is Result.Success, `is`(true))
        result as Result.Success

        assertThat(result.data, `is` (emptyList()))
    }

    @Test
    fun retrieveReminderById_ReturnError() = runBlocking {
        val reminder = ReminderDTO("My home", "Get my home", "abu sliem", 30.7858127, 31.7473426)
        remindersLocalRepository.saveReminder(reminder)
        //GIVEN - Empty Database
        remindersLocalRepository.deleteAllReminders()
        //WHEN - try to retrieve item(reminder) by id which does not exist
        val result = remindersLocalRepository.getReminder(reminder.id)
        //THEN - We get an Result.Error message
        assertThat(result is Result.Error, `is`(true))
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }

}