package fr.nicolas.keser.quizdrapeaux

import android.content.Intent
import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.v7.widget.RecyclerView
import fr.nicolas.keser.quizdrapeaux.controler.MainActivity
import fr.nicolas.keser.quizdrapeaux.controler.QuizzActivity.Companion.levels
import fr.nicolas.keser.quizdrapeaux.model.*
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainInstrumentedTest {

    @get: Rule
    var mActivityTestRule = ActivityTestRule<MainActivity>(MainActivity::class.java)

    lateinit var user : Data.User
    lateinit var intent : Intent

    @Before
    fun setUp(){
        user = Data.User("USER",25)
        infosUserAdded = true
        intent = Intent()
        intent.putExtra(KEY_EXTRAS_USER,user)
    }

    @Test
    fun testMain_Params() {

        fun prepare(){
            mActivityTestRule.finishActivity()
            Thread.sleep(1000)
            user = Data.User("USER", 25)
            infosUserAdded = true
            intent.putExtra(KEY_EXTRAS_USER, user)
            mActivityTestRule.launchActivity(intent)
        }

        fun register(){
            infosUserAdded = false
            user = Data.User("USER",25)
            intent.putExtra(KEY_EXTRAS_USER,user)
            mActivityTestRule.launchActivity(intent)

            Thread.sleep(1000)

            onView(withId(R.id.edTextNameForm)).perform(typeText("USER"), closeSoftKeyboard())
            onView(withId(R.id.edTextAgeForm)).perform(typeText("25"), closeSoftKeyboard())
            onView(withId(R.id.imageValidForm)).perform(click())

            onView(withId(R.id.button3)).perform(click())
            onView(withId(R.id.tvParamsInfos)).check(matches(withText(containsString("USER"))))
            onView(withId(R.id.tvParamsInfos)).check(matches(withText(containsString("25"))))
        }


        fun setParams() {
            prepare()
            onView(withId(R.id.button3)).perform(click())
            Thread.sleep(1000)
            onView(withId(R.id.recycler)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>
                    (0, click())
            )
            onView(withId(R.id.edTextParams)).perform(typeText("NEW USER"), closeSoftKeyboard())
            Thread.sleep(1000)
            onView(withId(R.id.imageValidParams)).perform(click())
            onView(withId(R.id.recycler)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>
                    (1, click())
            )
            onView(withId(R.id.edTextParams)).perform(typeText("32"), closeSoftKeyboard())
            Thread.sleep(1000)
            onView(withId(R.id.imageValidParams)).perform(click())

            onView(withId(R.id.tvParamsInfos)).check(matches(withText(containsString("NEW USER"))))
            onView(withId(R.id.tvParamsInfos)).check(matches(withText(containsString("32"))))

            onView(withId(R.id.recycler)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>
                    (8, click())
            )

            onView(withText(containsString("accueil"))).check(matches(isDisplayed()))
            onView(withText(containsString("OK"))).perform(click())
        }

        fun showScore() {
            prepare()
            mActivityTestRule.activity.currentUser.score = 10
            mActivityTestRule.activity.currentUser.idsQPUsed.add("2|0")
            onView(withId(R.id.button2)).perform(click())
            Thread.sleep(1000)
            onView(withText(containsString(mActivityTestRule.activity.getString(R.string.total_pts))))
                .check(matches(isDisplayed()))
            onView(withText(containsString(mActivityTestRule.activity.getString(R.string.return_))))
                .perform(click())

            mActivityTestRule.activity.currentUser.score = 0
            mActivityTestRule.activity.currentUser.idsQPUsed.clear()
            onView(withId(R.id.button2)).perform(click())
            Thread.sleep(1000)
            onView(withText(containsString(mActivityTestRule.activity.getString(R.string.no_score_available))))
                .check(matches(isDisplayed()))
            onView(withText(containsString(mActivityTestRule.activity.getString(R.string.return_))))
                .perform(click())

        }

        fun quit() {
            prepare()
            onView(withId(R.id.button3)).perform(click())
            onView(withId(R.id.recycler)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>
                (4, click()))
            Thread.sleep(1000)
            onView(withText("QUITTER"))
                .check(matches(isDisplayed()))
            onView(withText("QUITTER"))
                .perform(click())
        }

        fun reset() {
            prepare()
            onView(withId(R.id.button3)).perform(click())
            onView(withId(R.id.recycler)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>
                (5, click()))
            Thread.sleep(1000)
            onView(withText("CONTINUER"))
                .check(matches(isDisplayed()))
            onView(withText("CONTINUER"))
                .perform(click())
            Thread.sleep(2000)

            onView(withText(containsString("Bienvenue")))
                .check(matches(isDisplayed()))
        }

        fun goToQuizz(){
            prepare()
            mActivityTestRule.activity.currentUser.idsQPUsed.clear()
            for(i in 1..levels) {
                mActivityTestRule.activity.currentUser.idsQPUsed.add("2|${i-1}")
            }
            onView(withId(R.id.button)).perform(click())

            Thread.sleep(1000)
            onView(withText(containsString("OK")))
                .check(matches(isDisplayed()))
            onView(withText(containsString("OK")))
                .perform(click())
        }

        register()
        setParams()
        showScore()
        goToQuizz()
        quit()
        reset()
    }

}