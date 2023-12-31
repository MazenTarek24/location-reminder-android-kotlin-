package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.annotation.ExperimentalTestApi
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get





@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest : AutoCloseKoinTest() {
    // Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    val activityRule = ActivityTestRule(RemindersActivity::class.java)
    // Get activity context
    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


    @Test
    fun saveReminderScreen_showSnackBarTitleError() {
        // GIVEN - Launch Reminders Activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        // Verify error message is shown
        val snackBarMessage = appContext.getString(R.string.err_enter_title)
        onView(withText(snackBarMessage)).check(matches(isDisplayed()))
        //  activity is closed before resetting the db:
        activityScenario.close()
    }

    @Test
    fun saveReminderScreen_showSnackBarLocationError() {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // Click add new reminder
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Typing title
        onView(withId(R.id.reminderTitle)).perform(typeText("Title"))
        closeSoftKeyboard()
        // Save reminder
        onView(withId(R.id.saveReminder)).perform(click())
        // Verify error message is shown
        val snackBarMessage = appContext.getString(R.string.err_select_location)
        onView(withText(snackBarMessage)).check(matches(isDisplayed()))
        //  activity is closed before resetting the db:

        activityScenario.close()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun saveReminderScreen_showToastMessage() = runBlocking {
        // GIVEN - Launch Reminder activity
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)
        //  We begin entering information for the reminder.
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("Title"))
        closeSoftKeyboard()
        onView(withId(R.id.reminderDescription)).perform(typeText("Description"))
        closeSoftKeyboard()

        // Select location
        onView(withId(R.id.selectLocation)).perform(click())

        // Click any position in the map
        onView(withId(R.id.mapFragment)).perform(longClick())
        // Save location
        onView(withId(R.id.save_button)).perform(click())
        // Save
        onView(withId(R.id.saveReminder)).perform(click())

        // THEN - expect to have a Toast displaying reminder_saved String.
        onView(withText(R.string.reminder_saved)).
        inRoot(withDecorView(not(`is`(getActivity(scenario)?.window?.decorView))))
            .check(matches(isDisplayed()))

        //  activity is closed before resetting the db:
        scenario.close()
    }



}
