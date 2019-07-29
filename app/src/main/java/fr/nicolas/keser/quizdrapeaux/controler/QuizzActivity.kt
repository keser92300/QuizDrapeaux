package fr.nicolas.keser.quizdrapeaux.controler

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.android.volley.RequestQueue
import fr.nicolas.keser.quizdrapeaux.model.*
import kotlinx.android.synthetic.main.activity_quizz.*
import fr.nicolas.keser.quizdrapeaux.model.AsyncResponse
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import fr.nicolas.keser.quizdrapeaux.R
import java.lang.Exception

class QuizzActivity : AppCompatActivity(), View.OnClickListener {

    val TAG = this::class.java.simpleName
    var currentUser = Data.User()
    var currentPackQuestion = Data.QuestionsPack()
    lateinit var queue : RequestQueue
    lateinit var  animationTimer : CountDownTimer
    lateinit var  chrono : CountDownTimer
    var quizFinished = false
    var quizPaused = false
    var points = 0

    companion object { var levels = 4
        var currentQuestion = Data.Question()}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(fr.nicolas.keser.quizdrapeaux.R.layout.activity_quizz)

        supportActionBar?.hide()
        val intent = intent
        currentPackQuestion = getAnyFromExtras(currentPackQuestion,intent) as Data.QuestionsPack
        currentUser = getAnyFromExtras(currentUser,intent) as Data.User
        queue = Volley.newRequestQueue(this)
        initialize()
    }


    override fun onClick(v: View?) {
        when(v?.id){
            R.id.buttonAnswer1 -> checkAnswer(0)
            R.id.buttonAnswer2 -> checkAnswer(1)
            R.id.buttonAnswer3 -> checkAnswer(2)
            R.id.buttonAnswer4 -> checkAnswer(3)
            R.id.imageVolumeQuizz -> setVolume(!volumeOff)
        }
    }

    fun initialize(){
        setTypeFaces(Typeface.createFromAsset(assets, MAIN_TYPEFONT),tvNiveauQuizz)
        initButtons(listOf(buttonAnswer1,buttonAnswer2,buttonAnswer3,buttonAnswer4,imageVolumeQuizz))
        displayImageFromPath(imageUserQuizz,currentUser.pathImage)
        displayUserScore()
        tvNiveauQuizz.append(" ${currentPackQuestion.id}")
        Log.d(TAG,"Level : ${currentPackQuestion.id}")
        loadQuizz()

    }

    fun initButtons(listViews : List<View>){for (v in listViews) v.setOnClickListener(this)}
    fun displayUserScore() = tvScoreQuizz.setText("${currentUser.score} pts")
    fun setVolume(pVolumeOff: Boolean){setVolumeOff(pVolumeOff);setImageVolume(imageVolumeQuizz)}
    fun setVolumeOff(pVolumeOff: Boolean){volumeOff = pVolumeOff}
    fun setImageVolume(imv : ImageView) = if(volumeOff) imv.setImageResource(R.drawable.util_volume) else imv.setImageResource(R.drawable.util_volume_mute)

    /**
     * Display loading message and
     * launch an async task to retrieve requiered flags identifiers to start the quizz
     */

    fun loadQuizz(){
        startTimerAnimation()
        AsyncQuizzHandler(object : AsyncResponse {
            override fun processFinish(output: Data.QuestionsPack) {
                setViewsVisibility(View.VISIBLE,layoutScoreQuizz,imageVolumeQuizz)
                startQuizz(output)
                saveUserValues(this@QuizzActivity,currentUser)
                animationTimer.cancel()
                Log.d(TAG,"AsyncResponse id pack return: ${output.id}")
            }
        },tvPourcentLoadQuizz,progress_quizz_hor,layoutLoadQuizz,layoutQuizzButtons,
            queue,this).execute(currentPackQuestion)
    }

    /**
     * Shuffle the questions in the question pack before launching the quiz
     */

    fun startQuizz(questionsPack: Data.QuestionsPack){
        if(questionsPack.id == -1){
            makeToast(this@QuizzActivity,getString(R.string.error))
            quit()
            return
        }

        layoutQuizzButtons.visibility = View.VISIBLE
        questionsPack.questions.shuffle()
        displayQuestion(questionsPack.getQuestionNotPlayedInPack(currentUser))
    }

    /**
     * If all questions have been answered, go to finish Quiz method, otherwise display the new question
     */

    fun displayQuestion(question : Data.Question){
        if(question.answers.size<1){
            finishQuizz()
            return
        }

        imageWait.visibility = View.VISIBLE
        imageFlag.visibility = View.INVISIBLE
        layoutQuizzButtons.visibility = View.GONE
        tvQuizz.text = getString(R.string.img_load)
        currentQuestion = question
        val url = FIRST_PART_URL_FLAGS + question.countryCode + SECOND_PART_URL_FLAGS
        val listButtons = listOf<Button>(buttonAnswer1,buttonAnswer2,buttonAnswer3,buttonAnswer4)
        //Write all possible answers at the buttons
        for((indice,button) in listButtons.withIndex()){
            button.text = question.answers[indice]
        }
        Picasso.get()
            .load(url)
            .into(imageFlag, object : com.squareup.picasso.Callback {
                override fun onSuccess() {
                    setViewsVisibility(View.GONE,imageWait)
                    setViewsVisibility(View.VISIBLE,imageFlag,layoutQuizzButtons,progress_chrono_quizz)
                    colorGoodButton(true)
                    tvQuizz.text = getString(R.string.question)
                    startChrono()
                }
                override fun onError(e: Exception?) {
                    quit()
                    makeToast(this@QuizzActivity,getString(R.string.error))
                }

            })
    }

    fun checkAnswer(numAnswer : Int){
        try {chrono.cancel()}catch (e : Exception){e.printStackTrace()}
        progress_chrono_quizz.setProgress(0)
        progress_chrono_quizz.visibility = View.INVISIBLE
        var count : Long = DURATION_GOOD_ANSWER_DISPLAYING
        if(numAnswer == currentQuestion.indexGoodAnswer){
            tvQuizz.text = getString(R.string.good_answer)
            currentUser.score++
            currentUser.tempQuestionsChecked.add("1${currentQuestion.id}")
            displayUserScore()
            playSound(this,R.raw.good_answer)
        }else{
            if(numAnswer == -1){tvQuizz.text = getString(R.string.time_elapsed)}
            else{tvQuizz.text = getString(R.string.wrong)}
            tvQuizz.append(" ${getString(R.string.good_answer_was)} ${currentQuestion.answers[currentQuestion.indexGoodAnswer]}")
            currentUser.tempQuestionsChecked.add("0${currentQuestion.id}")
            count = DURATION_WRONG_ANSWER_DISPLAYING
            playSound(this,R.raw.wrong_answer)
        }

        Log.d(TAG,"numAnswer : $numAnswer / numGoodAnswer : ${currentQuestion.indexGoodAnswer} / score : ${currentUser.score}")
        saveUserValues(this@QuizzActivity,currentUser)
        colorGoodButton(false)
        // Freeze the screen to display the appropriate message after the user's choice
        val timer2 = object: CountDownTimer(count,500) { override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {startQuizz(currentPackQuestion)} }
        timer2.start()
    }

    fun finishQuizz(){
        quizFinished = true
        tvQuizz.text = getString(R.string.quizz_finish)
        imageFlag.setImageResource(R.drawable.eart)
        layoutQuizzButtons.visibility = View.GONE
        for(string in currentUser.tempQuestionsChecked){
            points += string.substring(0,1).toInt()
        }
        currentUser.idsQPUsed.add("$points|${currentPackQuestion.id}")
        currentUser.tempQuestionsChecked.clear()
        saveUserValues(this@QuizzActivity,currentUser)
        displayFinishMessage(points)
        Log.d(TAG,"Pts : $points")

        if(!volumeOff) {
            val resSound = if (points == MAX_QUESTIONS_IN_ONE_PACK) R.raw.win
            else if (points < (MAX_QUESTIONS_IN_ONE_PACK / 2)) R.raw.fail else R.raw.nice
            playSound(this,resSound)
        }
    }

    fun displayFinishMessage(points : Int){
        val msg = "${getString(R.string.game_finish)}\n${getString(R.string.score)} : $points / $MAX_QUESTIONS_IN_ONE_PACK"
        val txtB2 = getString(R.string.restart_quizz)
        val runB2 = Runnable { restartQuizz() }
        val res = if(points == MAX_QUESTIONS_IN_ONE_PACK) R.drawable.smiley_winner
        else if (points<(MAX_QUESTIONS_IN_ONE_PACK/2)) R.drawable.smiley_fail else R.drawable.smiley_nice
        val txtB3 : String? = if(currentUser.isAllPlayed()) null else getString(R.string.continue_)
        val runB3 : Runnable? = if(txtB3 == null)null else Runnable { nextQuizz() }

        displayAlertDialog(this@QuizzActivity,getString(R.string.finish_) + " !",msg,getString(R.string.quit),
            res,txtB2,runPositive = runB2, txtNeutral = txtB3, runNeutral = runB3,
            runNegative = Runnable { quit() })
    }

    fun nextQuizz(){
        currentPackQuestion = Data.QuestionsPack().getQuestionsPacks()[currentUser.idsQPUsed.size]
        startActivity(getPreparedIntent(this,QuizzActivity(), currentPackQuestion, currentUser))
        finish()
    }

    fun restartQuizz(){
        val pos = currentPackQuestion.getPositionPackInArrayIds(currentUser.idsQPUsed)
        if (pos !=-1){
            currentUser.idsQPUsed.removeAt(pos)
        }
        currentUser.score-=points
        saveUserValues(this@QuizzActivity,currentUser)
        startActivity(getPreparedIntent(this,QuizzActivity(), currentUser, currentPackQuestion))
        finish()
    }

    fun quit(){
        saveUserValues(this@QuizzActivity,currentUser)
        setViewsVisibility(View.VISIBLE,imageWait,progressQuizz)
        setViewsVisibility(View.GONE,imageVolumeQuizz,imageFlag,layoutQuizzButtons,progress_chrono_quizz,
            tvNiveauQuizz,tvQuizz,imageUserQuizz,tvScoreQuizz)
        startActivity(getPreparedIntent(this,MainActivity(), currentUser))
        finish()
    }

    /**
     * Assign a special color to the button of the correct answer if param enabled is true,
     * otherwise, assign the default color to all buttons
     */

    fun colorGoodButton(enabled : Boolean){
        val list = listOf<Button>(buttonAnswer1,buttonAnswer2,buttonAnswer3,buttonAnswer4)
        for(b in list){
            b.isEnabled = enabled
            b.setBackgroundResource(R.drawable.rounded_button)
        }

        if(!enabled)
            list[currentQuestion.indexGoodAnswer].setBackgroundResource(R.drawable.rounded_button_good_answer)
    }

    /**
     * Rotate loading image
     */

    var i = 0F
    fun startTimerAnimation(){
        animationTimer = object: CountDownTimer(100,50) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                i+=15F
                if(i == 360F){
                    i = 0F
                }
                earthQuizzLoad.rotation = i
                startTimerAnimation()
            }
        }
        animationTimer.start()
    }

    /**
     * Launch a timer for every question
     */

    fun startChrono(){
        chrono = object: CountDownTimer(DURATION_CHRONO, DURATION_CHRONO/100) {
            override fun onTick(millisUntilFinished: Long) {
                progress_chrono_quizz.setProgress(progress_chrono_quizz.progress+1)
            }
            override fun onFinish() {
                checkAnswer(-1)
                progress_chrono_quizz.setProgress(0)
            }
        }
        chrono.start()
    }

    override fun onBackPressed() {
        if (quizFinished){
            val pos = currentPackQuestion.getPositionPackInArrayIds(currentUser.idsQPUsed)
            displayFinishMessage(currentUser.idsQPUsed[pos].split("|")[0].toInt())
        }else{
            displayAlertDialog(this@QuizzActivity,"","${getString(R.string.quit)} ?",getString(R.string.cancel),
                null,getString(R.string.quit),runPositive = Runnable {quit()})
        }
    }

    override fun onPause() {
        super.onPause()
        quizPaused = true
        try {chrono.cancel()}catch (e : Exception){e.printStackTrace()}
    }

    override fun onResume() {
        super.onResume()
        if(quizPaused && !quizFinished){
            progress_chrono_quizz.setProgress(0)
            layoutQuizzButtons.visibility = View.GONE
            restartQuizz()
        }else{
            quizPaused = false
        }
    }
}
