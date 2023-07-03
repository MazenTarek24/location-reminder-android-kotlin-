package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi

import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

//  testing SaveReminderViewModel with fakeDataSource
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    private lateinit var remindersRepository: FakeDataSource

    //Subject under test
    private lateinit var viewModel: SaveReminderViewModel

    @Before
    fun setupViewModel() {
        remindersRepository = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), remindersRepository)
    }

    @After
    fun tearDown() {
        stopKoin()
    }
    // test validateData by passing null title and we expect
    // showSnackBarInt to indicate to err_enter_title and validate return false
    @Test
    fun validateEnteredData_EmptyTitleAndUpdateSnackBar() {
        val reminder = ReminderDataItem("", "Description", "My School", 7.32323, 6.54343)
        // Calling validateEnteredData and passing no title
        assertThat(viewModel.validateEnteredData(reminder)).isFalse()
        // expect a SnackBar to be shown displaying err_enter_title string and validate return false
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_enter_title)
    }
    //  test validateData by passing null location and we expect
    // showSnackBarInt to indicate to err_select_location and validate return false
    @Test
    fun validateEnteredData_EmptyLocationAndUpdateSnackBar() {
        val reminder = ReminderDataItem("Title", "Description", "", 7.32323, 6.54343)
        // Calling validateEnteredData and passing no location
        assertThat(viewModel.validateEnteredData(reminder)).isFalse()
        // expect a SnackBar to be shown displaying err_select_location string and validate return false
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_select_location)
    }


    @Test
    fun saveReminder_showLoading(){
     val reminder = ReminderDataItem("Title", "Description", "Airport", 7.32323, 6.54343)
        // Pause dispatcher so you can verify initial values.
     mainCoroutineRule.pauseDispatcher()
        // WHEN save reminder
     viewModel.saveReminder(reminder)
        // THEN: the progress is shown.
        assertThat(viewModel.showLoading.getOrAwaitValue()).isTrue()
        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()
        // THEN: the indicator is hidden.
        assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse()
    }


}