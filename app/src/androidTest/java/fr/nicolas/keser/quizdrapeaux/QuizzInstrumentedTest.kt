package fr.nicolas.keser.quizdrapeaux

import android.content.Intent
import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.pressBack
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.view.View
import fr.nicolas.keser.quizdrapeaux.controler.QuizzActivity
import fr.nicolas.keser.quizdrapeaux.controler.QuizzActivity.Companion.currentQuestion
import fr.nicolas.keser.quizdrapeaux.model.*
import kotlinx.android.synthetic.main.activity_quizz.*
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.lang.Exception

@RunWith(AndroidJUnit4::class)
@LargeTest
class QuizzInstrumentedTest {
    @get: Rule
    var mActivityTestRule = ActivityTestRule<QuizzActivity>(QuizzActivity::class.java,false,false)

    lateinit var pack : Data.QuestionsPack
    lateinit var user : Data.User
    lateinit var intent : Intent

    @Before
    fun setUp(){
        infosUserAdded = true
        pack = Data.QuestionsPack().getQuestionsPacks()[0]
        user = Data.User("USER",25)
        user.score = 0
        user.idsQPUsed.clear()
        user.tempQuestionsChecked.clear()
        intent = Intent()
        intent.putExtra(KEY_EXTRAS_PACK,pack)
        intent.putExtra(KEY_EXTRAS_USER,user)
    }

    @After
    fun tearDown(){
        infosUserAdded = false
    }

    @Test
    fun playGoodAnswers() {

        /*  user.idsQPUsed.add("2|0")
          pack = Data.QuestionsPack().getQuestionsPacks()[1]
          intent.putExtra(KEY_EXTRAS_PACK,pack)
          intent.putExtra(KEY_EXTRAS_USER,user) */

        mActivityTestRule.launchActivity(intent)

        fun clickOnAnswer(){
            while (mActivityTestRule.activity.layoutLoadQuizz.visibility == View.VISIBLE){
                Thread.sleep(   100)
            }

            Thread.sleep(1000)

            onView(withText(QuizzActivity.currentQuestion.answers[QuizzActivity.currentQuestion.indexGoodAnswer])).perform(click())
            onView(withId(R.id.tvQuizz)).check(matches(withText(containsString(mActivityTestRule.activity.getString(R.string.good_answer)))))
        }

        for(i in 1..MAX_QUESTIONS_IN_ONE_PACK){
            clickOnAnswer()
            Thread.sleep(DURATION_GOOD_ANSWER_DISPLAYING*2)

            if(i< MAX_QUESTIONS_IN_ONE_PACK) {
                onView(withId(R.id.imageFlag)).check(matches(isDisplayed()))
                onView(withId(R.id.layoutLoadQuizz)).check(matches(not(isDisplayed())))
                onView(withId(R.id.layoutQuizzButtons)).check(matches(isDisplayed()))
                onView(withId(R.id.tvScoreQuizz)).check(matches(withText(("" + mActivityTestRule.activity.currentUser.score + " pts"))))
                onView(withText(mActivityTestRule.activity.buttonAnswer1.text.toString()))
                    .check(matches(not(anyOf(
                        withText(mActivityTestRule.activity.buttonAnswer2.text.toString()),
                        withText(mActivityTestRule.activity.buttonAnswer3.text.toString()),
                        withText(mActivityTestRule.activity.buttonAnswer4.text.toString())
                    )
                    )
                    )
                    )
                checkButtonContainsAnswer()
            }
        }

        /*
        Test continue
         */
        try{
            onView(withText(containsString(mActivityTestRule.activity.getString(R.string.continue_)))).check(matches(isDisplayed()))
            onView(withText(containsString(mActivityTestRule.activity.getString(R.string.continue_)))).perform(click())
        }catch (e : Exception){}

    }

    @Test
    fun playWrongAnswers() {

        user.idsQPUsed.add("2|0")
        pack = Data.QuestionsPack().getQuestionsPacks()[2]
        intent.putExtra(KEY_EXTRAS_PACK,pack)
        intent.putExtra(KEY_EXTRAS_USER,user)

        mActivityTestRule.launchActivity(intent)

        fun clickOnAnswer(){
            while (mActivityTestRule.activity.layoutLoadQuizz.visibility == View.VISIBLE){
                Thread.sleep(   100)
            }
            Thread.sleep(1000)
            val index = if(currentQuestion.indexGoodAnswer == 3) 2 else  currentQuestion.indexGoodAnswer+1
            onView(withText(QuizzActivity.currentQuestion.answers[index])).perform(click())
            onView(withId(R.id.tvQuizz)).check(matches(withText(containsString(mActivityTestRule.activity.getString(R.string.good_answer_was)))))
        }

        for(i in 1..MAX_QUESTIONS_IN_ONE_PACK){
            clickOnAnswer()
            Thread.sleep(DURATION_WRONG_ANSWER_DISPLAYING*2)

            if(i< MAX_QUESTIONS_IN_ONE_PACK) {
                onView(withId(R.id.imageFlag)).check(matches(isDisplayed()))
                onView(withId(R.id.layoutLoadQuizz)).check(matches(not(isDisplayed())))
                onView(withId(R.id.layoutQuizzButtons)).check(matches(isDisplayed()))
                onView(withId(R.id.tvScoreQuizz)).check(matches(withText(("0 pts"))))
                onView(withText(mActivityTestRule.activity.buttonAnswer1.text.toString()))
                    .check(matches(not(anyOf(
                        withText(mActivityTestRule.activity.buttonAnswer2.text.toString()),
                        withText(mActivityTestRule.activity.buttonAnswer3.text.toString()),
                        withText(mActivityTestRule.activity.buttonAnswer4.text.toString())))))
                checkButtonContainsAnswer()
            }
        }

        /*
        Test restart
         */
        try{
            onView(withText(containsString(mActivityTestRule.activity.getString(R.string.restart_quizz)))).check(matches(isDisplayed()))
            onView(withText(containsString(mActivityTestRule.activity.getString(R.string.restart_quizz)))).perform(click())
        }catch (e : Exception){}
    }

    @Test
    fun giveNoResponse() {
        user.score = 2
        user.idsQPUsed.clear()
        user.tempQuestionsChecked.clear()
        user.tempQuestionsChecked.add("01")
        user.tempQuestionsChecked.add("12")
        user.tempQuestionsChecked.add("03")
        user.tempQuestionsChecked.add("14")
        intent.putExtra(KEY_EXTRAS_USER,user)

        mActivityTestRule.launchActivity(intent)

        while (mActivityTestRule.activity.imageWait.visibility == View.VISIBLE || mActivityTestRule.activity.layoutLoadQuizz.visibility == View.VISIBLE){
            Thread.sleep(   100)
        }

        Thread.sleep(500)

        onView(withId(R.id.imageFlag)).check(matches(isDisplayed()))
        onView(withId(R.id.layoutLoadQuizz)).check(matches(not(isDisplayed())))
        onView(withId(R.id.layoutQuizzButtons)).check(matches(isDisplayed()))
        onView(withId(R.id.tvScoreQuizz)).check(matches(withText(containsString("2"))))
        onView(withText(mActivityTestRule.activity.buttonAnswer1.text.toString()))
            .check(matches(not(anyOf(withText(mActivityTestRule.activity.buttonAnswer2.text.toString()), withText(mActivityTestRule.activity.buttonAnswer3.text.toString()),
                withText(mActivityTestRule.activity.buttonAnswer4.text.toString())))))
        checkButtonContainsAnswer()

        while (!mActivityTestRule.activity.tvQuizz.text.toString().equals(mActivityTestRule.activity.getString(R.string.quizz_finish))){
            Thread.sleep(   100)
        }
        Thread.sleep(500)

        /*
        Test quit
         */
        try{
            onView(withText(containsString(mActivityTestRule.activity.getString(R.string.quit)))).check(matches(isDisplayed()))
            onView(withText(containsString(mActivityTestRule.activity.getString(R.string.quit)))).perform(click())
        }catch (e : Exception){}

    }

    @Test
    fun backToHome() {
        mActivityTestRule.launchActivity(intent)
        pressBack()
        try{
            onView(withText(containsString(mActivityTestRule.activity.getString(R.string.quit)))).check(matches(isDisplayed()))
            onView(withText(containsString(mActivityTestRule.activity.getString(R.string.quit)))).perform(click())
        }catch (e : Exception){}
    }

    fun checkButtonContainsAnswer(){
        val indice = QuizzActivity.currentQuestion.indexGoodAnswer
        if(indice == 0){
            onView(withText(mActivityTestRule.activity.buttonAnswer1.text.toString()))
                .check(matches(withText(QuizzActivity.currentQuestion.answers[QuizzActivity.currentQuestion.indexGoodAnswer])))
        }else if(indice == 1){
            onView(withText(mActivityTestRule.activity.buttonAnswer2.text.toString()))
                .check(matches(withText(QuizzActivity.currentQuestion.answers[QuizzActivity.currentQuestion.indexGoodAnswer])))
        }else if(indice == 2){
            onView(withText(mActivityTestRule.activity.buttonAnswer3.text.toString()))
                .check(matches(withText(QuizzActivity.currentQuestion.answers[QuizzActivity.currentQuestion.indexGoodAnswer])))
        }else if(indice == 3){
            onView(withText(mActivityTestRule.activity.buttonAnswer4.text.toString()))
                .check(matches(withText(QuizzActivity.currentQuestion.answers[QuizzActivity.currentQuestion.indexGoodAnswer])))
        }
    }

}
