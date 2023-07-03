package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi

import kotlinx.coroutines.test.runBlockingTest
import org.junit.runner.RunWith
import org.hamcrest.core.IsNot
import org.junit.*
import org.koin.core.context.stopKoin
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Rule
import org.junit.Test

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Inject a fake data source into the viewModel.
    private lateinit var remindersRepository: FakeDataSource

    //Subject under test
    private lateinit var viewModel: RemindersListViewModel

    @Before
    fun setupViewModel() {
        remindersRepository = FakeDataSource()
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), remindersRepository)
    }
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminders_showLoading() {
        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()
        // WHEN load reminders
        viewModel.loadReminders()
        // THEN: the progress indicator is shown.
        assertThat(viewModel.showLoading.getOrAwaitValue()).isTrue()
        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()
        // THEN: the progress indicator is hidden.
        assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse()

    }

    @Test
    fun loadReminders_remainderListNotEmpty() = mainCoroutineRule.runBlockingTest  {
        // GIVEN items
        val reminder = ReminderDTO("My Store", "Pick Stuff", "Abuja", 6.454202, 7.599545)
        //save
        remindersRepository.saveReminder(reminder)
        // load reminders
        viewModel.loadReminders()
        //data is not empty
        assertThat(viewModel.remindersList.getOrAwaitValue()).isNotEmpty()
    }

    @Test
    fun loadReminders_updateSnackBarValue() {
        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()
        //set reminder
        remindersRepository.setReturnError(true)
        // load reminders
        viewModel.loadReminders()
        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()
        //show error
        assertThat(viewModel.showSnackBar.getOrAwaitValue()).isEqualTo("Error getting reminders")
    }
}